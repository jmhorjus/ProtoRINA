/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.flowAllocator.impl;

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowAllocator.api.FlowAllocatorInstance;
import rina.irm.impl.IRMImpl;
import rina.message.CDAP;
import rina.message.CDAP.CDAPMessage;
import rina.object.gpb.ApplicationProcessNamingInfoMessage_t.applicationProcessNamingInfo_t;
import rina.object.gpb.Flow_t.connectionId_t;
import rina.object.gpb.Flow_t.flow_t;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.DirectoryForwardingTable;
import rina.object.internal.Flow;
import rina.object.internal.ForwardingTable;
import rina.object.internal.Neighbor;
import rina.object.internal.Neighbors;
import rina.rib.impl.RIBImpl;
import rina.util.MessageQueue;

import com.google.protobuf.ByteString;

/**
 * Flow Allocator Instance Impl
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class FlowAllocatorInstanceImpl extends Thread implements FlowAllocatorInstance {

	private Log log = LogFactory.getLog(this.getClass());

	private String IPCName = null;
	private String IPCInstance = null;

	private Flow flow = null;
	private RIBImpl rib = null;
	private IRMImpl irm = null;
	private boolean listen = true;

	private int portID  = -1;
	private MessageQueue msgQueue = null;

	private DirectoryForwardingTable directoryForwardingTable = null;

	private Neighbors neighbors = null;

	private ForwardingTable forwardingTable = null;  





	//this one is used to notify back to the call in Flow Allocator
	// about whether the flow allocation is successful not
	private MessageQueue notifyToRequst = null;


	public FlowAllocatorInstanceImpl(){}

	public FlowAllocatorInstanceImpl(RIBImpl rib, IRMImpl irm, MessageQueue notify)
	{
		this.rib = rib;
		this.irm = irm;

		this.notifyToRequst = notify;

		this.IPCName = (String) this.rib.getAttribute("ipcName");
		this.IPCInstance = (String)this.rib.getAttribute("ipcInstance");

		this.directoryForwardingTable = (DirectoryForwardingTable)this.rib.getAttribute("directoryForwardingTable");

		this.neighbors = (Neighbors)this.rib.getAttribute("neighbors");

		this.forwardingTable = (ForwardingTable)this.rib.getAttribute("forwardingTable");

	}


	public void run()
	{


		while(this.listen)
		{
			byte[] msg = this.msgQueue.getReceive();

			CDAP.CDAPMessage cdapMessage = null;

			try {
				cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
			} catch (Exception e) {
				this.log.error(e.getMessage());
			}

			this.processCDAPMessage(cdapMessage);
		}



	}

	private void processCDAPMessage(CDAPMessage cdapMessage) {
		// TODO Auto-generated method stub

	}


	///this is to handle the flow request, and M_CREATE_R is also handled here
	/**
	 * Flow allocator got the request from application
	 */
	public void submitAllocationRequest(Flow flowRequest) {

		this.flow = flowRequest;

		this.portID  = (int) this.flow.getSrcPortID();


		this.msgQueue =  ( ( LinkedHashMap<Integer, MessageQueue> ) this.rib.getAttribute("flowQueues") ).get(this.portID);


		ApplicationProcessNamingInfo apInfo = flow.getDstApInfo();

		String apName = apInfo.getApName();
		String apInstance = apInfo.getApInstance();

		String name = null;

		if(apInstance == null) //sometimes only apName is used, for example: applications users write
		{
			name = apName;
		}else //sometimes both apName and aeName are used, for example: IPC process
		{
			name = apName + apInstance;
		}

		//this address needs to be "translated" to the IPC name (IPC apName, IPC apInstance ) 
		long dstAddr = this.directoryForwardingTable.getAddress(name);


		if(dstAddr == -1)
		{

			this.log.info( "No IPC address found for " + name + ", flow allocation failed");

			this.notifyToRequst.addReceive("false".getBytes());

			this.stopFAI();

			return;

			//or it can query DIF manager 


		}else
		{

			this.flow.setDstAddr(dstAddr);

			//			System.out.println("ddddddddddddddstAddr is " + dstAddr);
			//			System.out.println("this.forwardingTable is " + this.forwardingTable);

			//find next hop of dst ipc 
			//note this dstAddr must be cast to int, otherwise there will be error since forwarding table is long type
			long nextHop = this.forwardingTable.getNextHop((int)dstAddr);

			//		this.log.debug("next hop of " +  dstAddr + " is " +  nextHop);


			Neighbor neighbor = this.neighbors.getBeighbor(nextHop);



			if(neighbor == null)
			{

				this.log.info("Next hop does not exist for dstAddr " + dstAddr );

				this.notifyToRequst.addReceive("false".getBytes());

				this.stopFAI();

				return;

			}


			String dstIPCName = neighbor.getApName();

			String dstIPCInstance = neighbor.getApInstance();

			this.log.info( " IPC address found for " + name + ":" +  dstAddr +". IPC info:" + dstIPCName + "/" + dstIPCInstance );

			//Talk to dst(or next hop's) IPC's Management AE
			int handleID = this.irm.allocateFlow(this.IPCName, this.IPCInstance, "Management", "1",
					dstIPCName, dstIPCInstance, "Management", "1");


			this.log.debug("hanlde for CREATE(flow) is " + handleID);

			ApplicationProcessNamingInfo srcInfo = flow.getSrcApInfo();
			ApplicationProcessNamingInfo dstInfo = flow.getDstApInfo();


			String srcApName = srcInfo.getApName();
			String srcApInstance = srcInfo.getApInstance();
			String srcAeName = srcInfo.getAeName();
			String srcAeInstance = srcInfo.getAeInstance();

			String dstApName = dstInfo.getApName();
			String dstApInstance = dstInfo.getApInstance();
			String dstAeName = dstInfo.getAeName();
			String dstAeInstance = dstInfo.getAeInstance();



			applicationProcessNamingInfo_t.Builder src = applicationProcessNamingInfo_t.newBuilder();
			src.setApplicationProcessName(srcApName);

			//The following three need to be checked,
			//If the application calling is IPC process, then all are used
			//But if it is just a regular application, then they might not be used, as only apName is required
			if(srcApInstance != null)
			{
				src.setApplicationProcessInstance(srcApInstance);
			}
			if(srcAeName != null)
			{
				src.setApplicationEntityName(srcAeName);
			}
			if(srcAeInstance != null)
			{
				src.setApplicationEntityInstance(srcAeInstance);
			}

			applicationProcessNamingInfo_t.Builder dst = applicationProcessNamingInfo_t.newBuilder();
			dst.setApplicationProcessName(dstApName);

			//The following three need to be checked,
			//If the application calling is IPC process, then all are used
			//But if it is just a regular application, then they might not be used, as only apName is required
			if(dstApInstance != null)
			{
				dst.setApplicationProcessInstance(dstApInstance);
			}
			if(dstAeName != null)
			{
				dst.setApplicationEntityName(dstAeName);
			}
			if(dstAeInstance !=null)
			{
				dst.setApplicationEntityInstance(dstAeInstance);
			}

			connectionId_t.Builder cid = connectionId_t.newBuilder();
			cid.setQosId(1); // ignore this for now
			cid.setSourceCEPId((int)flow.getSrcPortID()); // Note: src cid is the portID for simplicity 
			cid.setDestinationCEPId(-1); // For now the other side is unkonw so make it -1

			flow_t.Builder flow_obj = flow_t.newBuilder();
			flow_obj.setDestinationNamingInfo(dst.buildPartial());
			flow_obj.setSourceNamingInfo(src.buildPartial());

			flow_obj.setSourcePortId((int)flow.getSrcPortID());
			flow_obj.setDestinationPortId(-1); // first time all dst port is -1
			flow_obj.setSourceAddress(flow.getSrcAddr()); //note this is a required field
			flow_obj.setDestinationAddress(flow.getDstAddr());

			flow_obj.addConnectionIds(cid.buildPartial());

			CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();

			obj.setByteval(ByteString.copyFrom(flow_obj.buildPartial().toByteArray()));

			CDAP.CDAPMessage M_CREATE = rina.message.CDAPMessageGenerator.generateM_CREATE
					(      "flow",
							"/dif/resourceallocation/flowallocator/flow/",
							obj.buildPartial(),
							99
							);


			try {
				this.irm.send(handleID, M_CREATE.toByteArray());
				this.log.info( "  M_CREATE(flow) sent out over handleID " + handleID);

			} catch (Exception e) {


				this.notifyToRequst.addReceive("false".getBytes());

				this.stopFAI();

				this.log.error(e.getMessage());

				this.log.info( "M_CREATE(flow) sent error");

				return;

			}


			byte [] msg_reply = this.msgQueue.getReceive();

			CDAP.CDAPMessage cdapMessage = null;

			try {
				cdapMessage = CDAP.CDAPMessage.parseFrom(msg_reply);
			} catch (Exception e) {

				this.notifyToRequst.addReceive("false".getBytes());

				this.stopFAI();

				this.log.error(e.getMessage());

				this.log.error( "Error when receiving M_CREATE_R  ");

				return;

			}


			if(cdapMessage.getOpCode().toString().equals("M_CREATE_R") && cdapMessage.getResult() == 0)
			{
				this.log.debug("we got the M_CREATE_R(flow) success");
			}else
			{

				this.log.debug("we got the M_CREATE_R(flow) fail");

				this.notifyToRequst.addReceive("false".getBytes());

				this.stopFAI();

				return;

			}

			CDAP.objVal_t objValue = null;
			flow_t  flow_reply = null;
			try {

				objValue = cdapMessage.getObjValue();
				flow_reply  = flow_t.parseFrom(objValue.getByteval().toByteArray());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int dstPortID = (int)flow_reply.getDestinationPortId();

			this.log.debug("dstPortID in the M_CREATE_R received is " + dstPortID);

			int srcPortID = (int)flow_reply.getSourcePortId();

			this.flow.setDstPortID(srcPortID);


			this.notifyToRequst.addReceive("true".getBytes());





		}

		//flow allocation done, start maintaining this flow
		this.start();
	}


	/**
	 * Flow allocator received request from another flow allocator
	 */
	public void receiveAllocationRequest(Flow flowRequest) {

		this.flow = flowRequest;
		this.portID  = (int) this.flow.getSrcPortID();
		this.msgQueue =  ( ( LinkedHashMap<Integer, MessageQueue> ) this.rib.getAttribute("flowQueues") ).get(this.portID);



		ApplicationProcessNamingInfo srcApInfo = this.flow.getSrcApInfo();
		ApplicationProcessNamingInfo dstApInfo = this.flow.getDstApInfo();

		//send M_CREATE_R back



		applicationProcessNamingInfo_t.Builder src = applicationProcessNamingInfo_t.newBuilder();   
		applicationProcessNamingInfo_t.Builder dst = applicationProcessNamingInfo_t.newBuilder();


		src.setApplicationProcessName(srcApInfo.getApName());
		src.setApplicationProcessInstance(srcApInfo.getApInstance());
		src.setApplicationEntityName(srcApInfo.getAeName());
		src.setApplicationEntityInstance(srcApInfo.getAeInstance());

		dst.setApplicationProcessName(dstApInfo.getApName());
		dst.setApplicationProcessInstance(dstApInfo.getApInstance());
		dst.setApplicationEntityName(dstApInfo.getAeName());
		dst.setApplicationEntityInstance(dstApInfo.getAeInstance());



		flow_t.Builder flow_reply = flow_t.newBuilder();

		connectionId_t.Builder cid = connectionId_t.newBuilder();
		cid.setQosId(1); // ignore this for now
		cid.setSourceCEPId((int)this.flow.getSrcPortID()); //portID is CEPID for simplicity   
		cid.setDestinationCEPId((int)this.flow.getDstPortID());  


		flow_reply.setDestinationNamingInfo(dst.buildPartial());
		flow_reply.setSourceNamingInfo(src.buildPartial());

		flow_reply.setSourcePortId(this.flow.getSrcPortID());
		flow_reply.setDestinationPortId(this.flow.getDstPortID()); // this is the one the other side needs

		flow_reply.addConnectionIds(cid.buildPartial());

		flow_reply.setSourceAddress(this.flow.getSrcAddr()); 
		flow_reply.setDestinationAddress(this.flow.getDstAddr());

		CDAP.objVal_t.Builder  flowObj = CDAP.objVal_t.newBuilder();

		flowObj.setByteval(ByteString.copyFrom(flow_reply.buildPartial().toByteArray()));

		int result = 0; //true all the time


		CDAP.CDAPMessage M_CREATE_R = rina.message.CDAPMessageGenerator.generateM_CREATE_R
				(       result,
						"flow",
						"/dif/resourceallocation/flowallocator/flow/",
						flowObj.buildPartial(),
						99
						);

		//sent M_CREATE_R to the other side's mamagement AE which will forwards result to its flow allocator

		//find next hop of dst ipc 
		//note this dstAddr must be cast to int, otherwise there will be error since forwarding table is long type
		long nextHop = this.forwardingTable.getNextHop((int)this.flow.getDstAddr());

		this.log.debug("next hop of " +  this.flow.getDstAddr() + " is " +  nextHop);


		Neighbor neighbor = this.neighbors.getBeighbor(nextHop);



		if(neighbor == null)
		{

			this.log.info("Next hop does not exist for dstAddr " + this.flow.getDstAddr() );

			this.notifyToRequst.addReceive("false".getBytes());

			this.stopFAI();

			return;

		}


		String dstIPCName = neighbor.getApName();
		String dstIPCInstance = neighbor.getApInstance();

		this.log.info( " IPC address found for dest Addr  " +  this.flow.getDstAddr()  +". IPC info:" + dstIPCName + "/" + dstIPCInstance );

		//Talk to dst IPC's Management AE
		int handleID = this.irm.allocateFlow(this.IPCName, this.IPCInstance, "Management", "1",
				dstIPCName, dstIPCInstance, "Management", "1");

		try {
			this.irm.send(handleID, M_CREATE_R.toByteArray());
			this.log.info( "  M_CREATE_R (flow) sent out over handleID " + handleID);

		} catch (Exception e) {


			this.notifyToRequst.addReceive("false".getBytes());

			this.stopFAI();

			this.log.error(e.getMessage());

			this.log.info( "M_CREATE_R(flow) sent error");

			return;

		}

		//just agree the flow request every time
		this.notifyToRequst.addReceive("true".getBytes());



		//flow allocation done, start maintaining this flow
		this.start();
	}



	public void stopFAI()
	{
		this.listen = false;

	}




}
