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

import com.google.protobuf.InvalidProtocolBufferException;

import rina.message.CDAP;
import rina.message.DTP;
import rina.tcp.TCPFlow;
import rina.util.MessageQueue;



public class IncomingComHandler extends Thread {

	private Log log = LogFactory.getLog(IncomingComHandler.class);

	private TCPFlow  tcpflow = null;

	private MessageQueue msgQueue = null;

	private boolean listening = true;

	private String dstName = null;

	private TCPFlowAllocated tcpFlowAllocated;


	public IncomingComHandler(TCPFlow tcpflow, TCPFlowAllocated tcpFlowAllocated)
	{
		this.tcpflow = tcpflow;	
		this.tcpFlowAllocated = tcpFlowAllocated;
	}


	/**
	 * listen for messages and add them to flow
	 */
	public void run()
	{

		this.log.info("new incomming TCP connection started");

		byte[] msg = null;

		try {
			msg = this.tcpflow.receive();

		} catch (Exception e1) {
			e1.printStackTrace();
		}


		//first message is a byte[] which is the ipc name of the other side of the wire


		this.dstName = new String(msg);

		this.tcpflow.setDstName(this.dstName);

		int flowID = this.tcpFlowAllocated.addTCPFlow(this.dstName, this.tcpflow);

		this.log.info("Wire to " + this.dstName + " allocated, flowID is " + flowID );

		this.msgQueue = this.tcpflow.getMsgQueue();

		//this.msgQueue.addReceive(msg);

		//attach a wireListener thread for this flow to receive msg and put it in the msgQueue
		// also pass the flowallocated to the wireListener
		this.tcpFlowAllocated.addWireListener(flowID, new WireListenerTCP(this.tcpflow, this.tcpFlowAllocated));

		// The following is commented, because wierelistener above is used
		// in dataIncoming and controlIncoming, which is in the talking to RINA community case, the following is used
		// go to these two to see details
		

		//		while(listening)
		//		{
		//
		//			try {
		//				msg = this.tcpflow.receive();
		//
		//				this.msgQueue.addReceive(msg);
		//
		//			} catch (Exception e) {
		//				if(this.tcpflow!=null){
		//					this.tcpflow.close();
		//					listening = false;
		//					this.log.info("connection close");
		//				}
		//			} 
		//
		//
		//		}

	}
}