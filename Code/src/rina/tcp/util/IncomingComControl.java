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


public class IncomingComControl extends Thread{

	private Log log = LogFactory.getLog(IncomingComControl.class);

	private TCPFlow tcpListeningFlow = null ;

	private int portID;


	private TCPFlowAllocated tcpFlowAllocated = null;

	private boolean listening = true;

	public  IncomingComControl( TCPFlow tcpListeningFlow,TCPFlowAllocated tcpFlowAllocated)
	{
		this.tcpListeningFlow = tcpListeningFlow;	
		this.portID = this.tcpListeningFlow.getLocalPort();
		this.tcpFlowAllocated = tcpFlowAllocated;
		this.start();
		
	}



	/**
	 * accepts incomingCom threads
	 */
	public void run()
	{

		this.log.info("IncomingCom started on port:" + this.portID);

		while(listening)
		{	
			try {

				TCPFlow clientTCPFlow = tcpListeningFlow.accept();
				new IncomingComHandlerControl(clientTCPFlow, this.tcpFlowAllocated).start();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}