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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.message.DTP;
import rina.tcp.TCPFlow;
import rina.tcp.util.WellKnownRINAAddr.DirectoryEntry;
import rina.util.MessageQueue;



public class IncomingComHandlerData extends Thread {

	private Log log = LogFactory.getLog(IncomingComHandlerData.class);

	private TCPFlow  tcpflow = null;

	private MessageQueue msgQueue = null;

	private boolean listening = true;

	private TCPFlowAllocated tcpFlowAllocated;


	public IncomingComHandlerData(TCPFlow tcpflow, TCPFlowAllocated tcpFlowAllocated)
	{
		this.tcpflow = tcpflow;	
		this.tcpFlowAllocated = tcpFlowAllocated;
	}


	/**
	 * listen for messages and add them to flow
	 */
	public void run()
	{

		this.log.info("new incomming TCP data connection started");

		byte[] msg = null;

		try {
			msg = this.tcpflow.receive();


		} catch (Exception e1) {

			this.log.error(e1.getMessage());
			return;
		}



		try {
			//by definition, the first message over a data flow is a DTP message containing a DTP first message flag 

			DTP firstDTPMsg = new DTP(msg); 

			if(( (byte) firstDTPMsg.getPdu_type()  & 0xFF )==  0xC1)
			{

				this.log.info("First Data  DTP received with pdu type 0xC1");

			}else
			{

				this.log.error("First Data  DTP received, but pdu type is not 0xC1. New incoming data flow failed");
				return;
			}


			int dstAddr = firstDTPMsg.getSrcAddr();



			//according to the address find who is the src app name, app AEname

			DirectoryEntry srcEntry = this.tcpFlowAllocated.getWellKnownRINAAddr().getDataEntry
					(this.tcpFlowAllocated.getApName(), this.tcpFlowAllocated.getApInstance());
			
			
			String srcName = srcEntry.getApName() + srcEntry.getApInstance() + srcEntry.getAeName() + srcEntry.getAeInstance();


			DirectoryEntry dstEntry = this.tcpFlowAllocated.getWellKnownRINAAddr().getDataEntry(dstAddr);
			


			String dstName = dstEntry.getApName() + dstEntry.getApInstance() + dstEntry.getAeName() + dstEntry.getAeInstance();

			this.tcpflow.setSrcName(srcName);

			this.tcpflow.setDstName(dstName);

			int flowID = this.tcpFlowAllocated.addTCPFlow(srcName + dstName, this.tcpflow);

			this.msgQueue = this.tcpflow.getMsgQueue();

			//	this.msgQueue.addReceive(msg);

			this.log.info("new incomming TCP data flow  successfully created  with flow ID " + flowID +
					", srcName: " + srcName + " and dstName: " + dstName);


		} catch (Exception e) {
			this.log.error(e.getMessage());
		}





		while(listening)
		{

			try {
				msg = this.tcpflow.receive();

				this.msgQueue.addReceive(msg);

			} catch (Exception e) {
				if(this.tcpflow!=null){
					this.tcpflow.close();
					listening = false;
					this.log.info("connection close");
				}
			} 


		}

	}
}