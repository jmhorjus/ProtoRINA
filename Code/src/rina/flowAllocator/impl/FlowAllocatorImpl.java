/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

/**
 * Flow Allocator Implementaion 
 * Each IPC has a Flow Allocator to serve the application on top of it
 * @author Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0
 *
 */

package rina.flowAllocator.impl;

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowAllocator.api.FlowAllocator;
import rina.irm.impl.IRMImpl;
import rina.message.DTP;
import rina.object.internal.DirectoryForwardingTable;
import rina.object.internal.Flow;
import rina.object.internal.ForwardingTable;
import rina.object.internal.Neighbor;
import rina.object.internal.Neighbors;
import rina.rib.impl.RIBImpl;
import rina.util.MessageQueue;

public class FlowAllocatorImpl implements FlowAllocator{

	private Log log = LogFactory.getLog(this.getClass());
	private RIBImpl rib = null;
	private IRMImpl irm = null;

	private int portIDRange = 10000;
	private LinkedHashMap<Integer,FlowAllocatorInstanceImpl> portIDToFAI = null;
	private LinkedHashMap<Integer, Flow>  portIDToFlow = null;
	private LinkedHashMap<Integer, MessageQueue> flowQueues = null;


	private String IPCName = null;
	private String IPCInstance = null;

	private int rinaAddr = -1;

	private Neighbors neighbors = null;

	private DirectoryForwardingTable directoryForwardingTable = null;

	private ForwardingTable forwardingTable = null;  


	//note: portID is used as a flowID for in the flow allocator



	public FlowAllocatorImpl(RIBImpl rib, IRMImpl irm)
	{
		this.rib = rib;
		this.irm = irm;
		this.portIDToFAI = new LinkedHashMap<Integer,FlowAllocatorInstanceImpl>();
		this.portIDToFlow = new LinkedHashMap<Integer, Flow>();
		this.flowQueues = new  LinkedHashMap<Integer, MessageQueue>();


		this.rib.addAttribute("portIDToFAI", this.portIDToFAI);
		this.rib.addAttribute("portIDToFlow", this.portIDToFlow);
		this.rib.addAttribute("flowQueues", this.flowQueues);

		this.IPCName = (String) this.rib.getAttribute("ipcName");
		this.IPCInstance = (String)this.rib.getAttribute("ipcInstance");
		this.neighbors = (Neighbors)this.rib.getAttribute("neighbors");

		this.directoryForwardingTable = (DirectoryForwardingTable) this.rib.getAttribute("directoryForwardingTable");

		this.forwardingTable = (ForwardingTable)this.rib.getAttribute("forwardingTable");



	}


	public int submitAllocationRequest(Flow flowRequest) {

		this.log.debug("submitAllocationRequest is called");

		this.rinaAddr = Integer.parseInt(this.rib.getAttribute("rinaAddr").toString());

		int portID = -1;

		if(this.checkFlowRequest(flowRequest) == false)
		{
			this.log.error("Flow request is not well formed");
			return -1;
		}

		portID = this.generatePortID();

		flowRequest.setSrcAddr(this.rinaAddr);
		flowRequest.setSrcPortID(portID);

		this.flowQueues.put(portID, new MessageQueue());

		// this one is used to get response from the FAI
		// Use the MeeeageQueue is just a temp way,//TODO

		MessageQueue notify = new MessageQueue();

		FlowAllocatorInstanceImpl fai = new FlowAllocatorInstanceImpl(this.rib, this.irm, notify);

		fai.submitAllocationRequest(flowRequest);

		boolean result = Boolean.parseBoolean( new String( notify.getReceive()) );

		if(result == false)
		{
			this.log.info("Flow allocation failed");
			fai.stopFAI();
			this.portIDToFAI.remove(portID);
			return -1;
		}


		this.portIDToFAI.put(portID, fai);
		this.portIDToFlow.put(portID, flowRequest);
		//update this flowQueue which is also accessible by the Data Transfer AE to do the RMT
		this.flowQueues.put(portID,new MessageQueue());



		this.log.info("Flow allocation successful");

		return portID;
	}




	public  int receiveAllocationRequest(Flow flowRequest) {

		this.log.debug("receiveAllocationRequest is called");

		this.rinaAddr = Integer.parseInt(this.rib.getAttribute("rinaAddr").toString());

		int portID = -1;


		if(this.checkFlowRequestReceived(flowRequest) == false)
		{
			this.log.error("Dest application cannot be reached through this ipc");
			return -1;
		}else
		{
			this.log.debug("Dest application found on this IPC");
		}

		portID = this.generatePortID();

		flowRequest.setSrcAddr(this.rinaAddr);
		flowRequest.setSrcPortID(portID);

		this.flowQueues.put(portID, new MessageQueue());


		// this one is used to get response from the FAI
		// Use the MessageQueue is just a temp way,//TODO

		MessageQueue notify = new MessageQueue();

		// needs to modify this 
		FlowAllocatorInstanceImpl fai = new FlowAllocatorInstanceImpl(this.rib, this.irm, notify);

		fai.receiveAllocationRequest(flowRequest);

		boolean result = Boolean.parseBoolean( new String( notify.getReceive()) );


		if(result == false)
		{
			this.log.info("Flow allocation failed");
			fai.stopFAI();
			this.portIDToFAI.remove(portID);
			return -1;
		}


		this.portIDToFAI.put(portID, fai);
		this.portIDToFlow.put(portID, flowRequest);
		//update this flowQueue which is also accessible by the Data Transfer AE to do the RMT
		this.flowQueues.put(portID,new MessageQueue());


		this.log.info("Flow allocation successful");

		return portID;

	}






	public void deallocateFlow(int portID) {

		this.flowQueues.remove(portID);

		this.portIDToFAI.get(portID).stopFAI();

		this.portIDToFAI.remove(portID);



	}

	/**
	 * Check if the IPC has the application requested on top of it
	 * Now always return true
	 * @param flowRequest
	 * @return
	 */
	private boolean checkFlowRequestReceived(Flow flowRequest) {

		//check if this IPC has the target application on top of it

		return this.directoryForwardingTable.checkAppReachability(flowRequest.getSrcApInfo());

	}

	/**
	 * Check if the flow request is well-formed 
	 * Now return True all the time
	 * @param flowRequest
	 * @return
	 */
	private boolean checkFlowRequest(Flow flowRequest)
	{
		boolean result = true;

		//		applicationProcessNamingInfo_t src = flowRequest.getSourceNamingInfo();
		//		applicationProcessNamingInfo_t dst = flowRequest.getDestinationNamingInfo();
		//			
		//		String dstApName = dst.getApplicationProcessName();
		//		String dstApInstance = dst.getApplicationProcessInstance();
		//		String dstAeName = dst.getApplicationEntityName();
		//		String dstAeInstance = dst.getApplicationEntityInstance();
		//
		//		String srcApName = src.getApplicationProcessName();
		//		String srcApInstance = src.getApplicationProcessInstance();
		//		String srcAeName = src.getApplicationEntityName();
		//		String srcAeInstance = src.getApplicationEntityInstance();
		//
		//	
		//		this.log.debug("DEST info: " + dstApName + "/" +  dstApInstance + "/" + dstAeName + "/" + dstAeInstance);
		//		this.log.debug("SRC info: " + srcApName + "/" +  srcApInstance + "/" + srcAeName + "/" + srcAeInstance);


		return result;

	}


	private synchronized int generatePortID()
	{
		int portID = -1;

		portID = (int)( Math.random()* this.portIDRange); 

		while(this.portIDToFAI.containsKey(portID))
		{
			portID = (int)( Math.random()* this.portIDRange); 
		}

		//this is to make it consistent
		this.portIDToFAI.put(portID, null);

		this.log.debug("portID generated is " +  portID);

		return portID;
	}


	public void send(int flowID, byte[] msg) throws Exception {

		Flow flow = this.portIDToFlow.get(flowID);


		int handleID = flow.getDataTransferHandleID();


		//if(handleID == -1)
		//{
		////////////////////////////////////////////////////
		//make sure there is a handle between Data Transfer AEs

		long nextHop = this.forwardingTable.getNextHop((int)flow.getDstAddr());

		this.log.debug("FA (send) nnnnnnnnnnnnnnnnnnnnnnext hop of " +  flow.getDstAddr() + " is " + nextHop);

		Neighbor neighbor = this.neighbors.getBeighbor(nextHop);


		handleID = this.irm.allocateFlow(this.IPCName, this.IPCInstance, "Data Transfer", "1",
				neighbor.getApName(), neighbor.getApInstance(), "Data Transfer", "1");

		this.log.debug(this.IPCName + "/" + this.IPCInstance + "/" + neighbor.getApName() + "/" + neighbor.getApInstance());

		//here both sender and receiver only use one handle to each other's Data Transfer AE
		flow.setDataTransferHandleID(handleID);

		this.log.debug(" this.portIDToFlow.get(flowID).getDataTransferHandleID()" + this.portIDToFlow.get(flowID).getDataTransferHandleID());

		this.log.debug("HandleID between Data Transfer AEs is " + handleID );

		//	}

		DTP dtp = new DTP((short)flow.getDstAddr(), (short)flow.getSrcAddr(),
				(short)flow.getDstPortID(),(short)flow.getSrcPortID(), msg);

		dtp.printDTPHeader();

		this.irm.send(handleID, dtp.toBytes());

	}


	public byte[] receive(int flowID) {

		return this.flowQueues.get(flowID).getReceive();
	}


	public synchronized IRMImpl getIrm() {
		return irm;
	}


	public synchronized Neighbors getNeighbors() {
		return neighbors;
	}













}
