/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *   
 */

package rina.tcp.util;

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import rina.irm.util.HandleEntry;
import rina.message.CDAP;
import rina.message.DTP;
import rina.message.CDAP.CDAPMessage;
import rina.object.gpb.ApplicationProcessNamingInfoMessage_t.applicationProcessNamingInfo_t;
import rina.object.gpb.Flow_t.connectionId_t;
import rina.object.gpb.Flow_t.flow_t;
import rina.rib.impl.RIBImpl;
import rina.tcp.TCPFlow;
import rina.util.MessageQueue;

/**
 * This only belongs to DIF 0 IPC Process's IRM as its underlying DIF is the wire(emulated by TCP connection).
 * 
 * This is a thread attached to each wire to demultiplex  messages received on the wire
 * Note: the M_REATE (flow) is put to irm msg queue by wirelistener
 * 
 *
 */
public class WireListenerTCP extends Thread {


	private Log log = LogFactory.getLog(this.getClass());
	
	private RIBImpl rib = null;

	private int wireID = -1;

	private TCPFlow tcpFlow = null;

	private boolean listen = true;

	private int flowIDRange = 10000;
	
	
	
	private LinkedHashMap<Integer, MessageQueue> dif0FlowQueues = null;

	private LinkedHashMap<Integer,HandleEntry > flowIDToHandleEntry = null;

	private TCPFlowAllocated tcpFlowAllocated;

	public WireListenerTCP(TCPFlow tcpFlow, TCPFlowAllocated tcpFlowAllocated)
	{
		this.tcpFlow = tcpFlow;
		this.dif0FlowQueues = new  LinkedHashMap<Integer, MessageQueue>();
		this.flowIDToHandleEntry = new LinkedHashMap<Integer,HandleEntry >();
		this.wireID = this.tcpFlow.getFlowID();
		this.tcpFlowAllocated = tcpFlowAllocated;
		this.rib = this.tcpFlowAllocated.getRib();
		this.start();
	}

	public void run()
	{
		this.log.info("WireListener started for TCP flow(wire): " +  wireID);

		byte[] msg = null;

		while(this.listen)
		{
			DTP dtpMsg = null;
			try {
				msg = this.tcpFlow.receive();

				dtpMsg = new DTP(msg);
				
//				dtpMsg.printDTPHeader();


			} catch (Exception e) {
				//e.printStackTrace();
				this.log.error(e.getMessage());
				this.log.error("Wire down, ID  " + this.wireID );
				this.tcpFlow.close();
				this.listen = false;
				continue;
				
			}
			
			
			
			if( ( (byte)dtpMsg.getPdu_type() & 0xFF )== 0xc0)
			{
				this.log.info("Management DTP received, it contains CDAP message");

				CDAP.CDAPMessage cdapMessage = null;
				try {
					cdapMessage = CDAP.CDAPMessage.parseFrom(dtpMsg.getPayload());
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}

				if(dtpMsg.getDestCEPid() == 0
						&& cdapMessage.getOpCode().toString().equals("M_CREATE") 
						&& cdapMessage.getObjClass().equals("flow") )
				{
					this.log.info("this is a M_CREATE msg to request a flow");

					this.handleM_CREATE_flow(cdapMessage);
				}else
				{
					this.demultiplex(dtpMsg);
				}

			}else if( ( (byte)dtpMsg.getPdu_type() & 0xFF ) == 0x81)
			{
				this.log.info("Data Transfer DTP received, it contains data payload");
				this.demultiplex(dtpMsg);
			}else
			{
				this.demultiplex(dtpMsg);
			}



		}
	}



	private void handleM_CREATE_flow(CDAPMessage cdapMessage) {
		
	    //give this message to IRM to handle
		( (MessageQueue)this.rib.getAttribute("irmMsgQueue") ).addReceive(cdapMessage.toByteArray());

	
		
	}

	//put it in the flow queue for that port ID on the wire
	private synchronized void demultiplex(DTP dtpMsg) {

//		this.log.debug("demultiplexing received dtpMsg");
		
		short dstCEPid = dtpMsg.getDestCEPid();
		
//		this.log.debug("dstCEPid is " + dstCEPid);
		
		if(dstCEPid == 0)
		{
			return;
		}
		
		//this.dif0FlowQueues.get(dstCEPid).addReceive(dtpMsg.getPayload());
		
		byte[] msg = dtpMsg.getPayload();
		
		this.dif0FlowQueues.get((int)dstCEPid).addReceive(msg);

	}

	public synchronized int  addDIF0Flow(HandleEntry he)
	{
		int portID = this.generatePortId();

		this.dif0FlowQueues.put(portID, new MessageQueue());

		this.flowIDToHandleEntry.put(portID, he);
		
		he.setSrcPortID(portID);

		return portID; 

	}

	public synchronized void removeDIF0Flow(int portID)
	{
		this.flowIDToHandleEntry.remove(portID);
		this.dif0FlowQueues.remove(portID);

	}

	public synchronized void close() {

		this.listen = false;
	}

	public synchronized int getWireID() {
		return wireID;
	}

	public synchronized void setWireID(int wireID) {
		this.wireID = wireID;
	}

	public byte[] receive(int portID)
	{
		return this.dif0FlowQueues.get(portID).getReceive();
	}

	private synchronized int generatePortId()
	{
		int portID = -1;

		portID = (int)( Math.random()* (double)flowIDRange); 

		while(this.dif0FlowQueues.containsKey(portID) )
		{
			portID = (int)( Math.random()* (double)flowIDRange); 
		}
		
		this.dif0FlowQueues.put(portID, null);

		return portID;
	}



}
