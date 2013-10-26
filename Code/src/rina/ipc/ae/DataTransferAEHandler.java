/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 * 
 **/

package rina.ipc.ae;

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.irm.impl.IRMImpl;
import rina.irm.util.HandleEntry;
import rina.message.DTP;
import rina.object.internal.ForwardingTable;
import rina.object.internal.Neighbor;
import rina.object.internal.Neighbors;
import rina.rib.impl.RIBImpl;
import rina.util.MessageQueue;

/**
 * Demultiplex flows on the handle
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class DataTransferAEHandler extends Thread{


	private  Log log = LogFactory.getLog(this.getClass());

	private int handleID = -1;

	private HandleEntry he = null;

	private String apName = null;
	private String apInstance = null;
	private String aeName = null;
	private String aeInstance = null;


	private RIBImpl rib = null;
	private IRMImpl irm = null;

	private boolean listen = true;

	private int rinaAddr = -1;

	private LinkedHashMap<Integer, MessageQueue> flowQueues = null;

	private ForwardingTable forwardingTable = null;  

	private Neighbors neighbors = null;




	public DataTransferAEHandler(int handleID, HandleEntry he, RIBImpl rib, IRMImpl irm)
	{
		this.handleID = handleID;

		this.he = he;

		this.apName = this.he.getSrcApName();
		this.apInstance = this.he.getSrcApInstance();
		this.aeName = this.he.getSrcAeName();
		this.aeInstance = this.he.getSrcAeInstance();


		this.rib = rib;
		this.irm = irm;
		this.rinaAddr = (Integer)this.rib.getAttribute("rinaAddr");

		this.flowQueues = (LinkedHashMap<Integer, MessageQueue>)this.rib.getAttribute("flowQueues");

		this.neighbors = (Neighbors)this.rib.getAttribute("neighbors");

		this.forwardingTable = (ForwardingTable)this.rib.getAttribute("forwardingTable");

		this.start();
	}

	public void run()
	{
		this.log.info("Data Transfer Handler started on handleID " + this.handleID + ". RINA Addr is " + this.rinaAddr);

		while(this.listen)
		{
			byte[] msg  = this.irm.receive(this.handleID);
			DTP dtpMsg = new DTP(msg);
			this.processDTPMessage(dtpMsg);
		}
	}

	private void processDTPMessage(DTP dtpMsg) {

		dtpMsg.printDTPHeader();

		if(dtpMsg.getDestAddr() == this.rinaAddr)
		{
			this.demultiplex(dtpMsg);
		}else
		{
			this.relay(dtpMsg);
		}

	}


	//int counter = 0;

	private void relay(DTP dtpMsg) {

		this.log.debug("Relay DTP msg");

//		counter++;
//
//		if(counter > 100)
//		{
//			if( Math.random() < 0.3 )
//			{
//				System.err.println("packet drop zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz " +  counter);
//				return;
//			}
//		}


		int dstAddr = dtpMsg.getDestAddr();

		long nextHop = this.forwardingTable.getNextHop(dstAddr);

		//	this.log.debug("next hop of " +  dstAddr + " is " + nextHop);


		Neighbor neighbor = this.neighbors.getBeighbor(nextHop);

		if( neighbor == null)
		{
			this.log.error("Next hop does not exsit");
		}

		
		try {
			
			int handleID = this.irm.allocateFlow(this.apName,this.apInstance, "Data Transfer", "1",
					neighbor.getApName(),neighbor.getApInstance(), "Data Transfer", "1");

			this.irm.send(handleID, dtpMsg.toBytes());
			this.log.info( " DTP Msg relayed, and sent over handleID " + handleID);

		} catch (Exception e) {

			this.log.info( "DTP Msg relayed, but sent error");

			return;

		}




	}

	private void demultiplex(DTP dtpMsg) {

		this.log.debug("Demultiplex DTP msg");

		//  this.log.debug("msg is " + new String(dtpMsg.getPayload()));


		int dstCEPID = dtpMsg.getDestCEPid();

		if(this.flowQueues.containsKey(dstCEPID))
		{
			this.flowQueues.get(dstCEPID).addReceive(dtpMsg.getPayload());
		}else
		{
			this.log.error("dstCEPID is not found, msg discarded");
		}


	}



}
