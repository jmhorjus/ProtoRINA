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

import rina.tcp.TCPFlow;
import rina.util.MessageQueue;

/**
 * OutgoingComHandler: this one is used for a 0 dif each time when this ipc creates a flow out
 */
public class OutgoingComHandler extends Thread {
	
	private Log log = LogFactory.getLog(OutgoingComHandler.class);
	/**
	 * flow0
	 */
	private TCPFlow tcpFlow = null;
	/**
	 * message queue
	 */
	private MessageQueue msgQueue = null;
	
	private int flowID = -1;

	private boolean listening = true;

	/**
	 * Constructor
	 * @param buffer
	 * @param flow
	 */
	public OutgoingComHandler( TCPFlow tcpFlow)
	{
		this.tcpFlow = tcpFlow;	
		this.msgQueue = this.tcpFlow.getMsgQueue();
		this.flowID = this.tcpFlow.getFlowID();
	}
	/**
	 * this object is attached each time a new flow is created
	 */
	public void run()
	{
		this.log.info("Connection started with flow id " + this.flowID);

		while(listening)
		{
			try {
				byte[] msg = this.tcpFlow.receive();
				this.log.debug("Message received at tcp flow with flow id " + this.flowID);
				this.msgQueue.addReceive(msg);

			} catch (Exception e) {
				//e.printStackTrace();
				if(this.tcpFlow!=null)
				{
					listening = false;
					this.tcpFlow.close();
					this.log.debug("Connection closed with flowID " + this.flowID);
				}

			}

		}
	}

}