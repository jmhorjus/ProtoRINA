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



public class IncomingComHandlerControl extends Thread {

	private Log log = LogFactory.getLog(IncomingComHandlerControl.class);

	private TCPFlow  tcpflow = null;

	private MessageQueue msgQueue = null;

	private boolean listening = true;

	private TCPFlowAllocated tcpFlowAllocated;


	public IncomingComHandlerControl(TCPFlow tcpflow, TCPFlowAllocated tcpFlowAllocated)
	{
		this.tcpflow = tcpflow;	
		this.tcpFlowAllocated = tcpFlowAllocated;
	}


	/**
	 * listen for messages and add them to flow
	 */
	public void run()
	{

		this.log.info("new incomming TCP control connection started");

		byte[] msg = null;

		try {
			msg = this.tcpflow.receive();

		} catch (Exception e1) {

			e1.printStackTrace();
		}


		try {
			//by definition, the first message over a control flow is a DTP message containing a CDAP message

			DTP firstDTPMsg = new DTP(msg); 

			byte[] firstCDAPMsg = firstDTPMsg.getPayload();

			CDAP.CDAPMessage cdapMessage = null;

			cdapMessage = CDAP.CDAPMessage.parseFrom(firstCDAPMsg);
			
			
			//System.out.println("cdapMessage.getOpCode() " +cdapMessage.getOpCode().toString());
			if(!cdapMessage.getOpCode().toString().equals("M_CONNECT"))
			{
				this.log.error("first CDAP message over a control flow is not M_CONNECT, error, quit");
				return;
			}

			String srcName = cdapMessage.getDestApName() +  cdapMessage.getDestApInst() 
					+ cdapMessage.getDestAEName() + cdapMessage.getDestAEInst();

			String dstName = cdapMessage.getSrcApName() +  cdapMessage.getSrcApInst() 
					+ cdapMessage.getSrcAEName() + cdapMessage.getSrcAEInst();

			this.tcpflow.setSrcName(srcName);
			this.tcpflow.setDstName(dstName);

			int flowID = this.tcpFlowAllocated.addTCPFlow(srcName + dstName, this.tcpflow);


			//send M_CONNECT_R back here
			//this.msgQueue = this.tcpflow.getMsgQueue();
			//this.msgQueue.addReceive(msg);


			CDAP.CDAPMessage M_CONNECT_R = rina.message.CDAPMessageGenerator.generateM_CONNECT_R
					(       
							cdapMessage.getAbsSyntax(),
							0,
							CDAP.authTypes_t.AUTH_NONE, 
							cdapMessage.getSrcAEInst(),
							cdapMessage.getSrcAEName(),
							cdapMessage.getSrcApInst(),
							cdapMessage.getSrcApName(),
							cdapMessage.getInvokeID(),
							cdapMessage.getDestAEInst(),
							cdapMessage.getDestAEName(),
							cdapMessage.getDestApInst(),
							cdapMessage.getDestApName(),
							cdapMessage.getVersion()
							);

			DTP DTP_M_CONNECT_R = new DTP(M_CONNECT_R);

			try {

				this.tcpflow.send(DTP_M_CONNECT_R.toBytes());


				this.log.info("M_CONNECT_R sent: " +  cdapMessage.getSrcApName() + "/" + cdapMessage.getSrcApInst() + 
						"/"+cdapMessage.getSrcAEName() + "/" + cdapMessage.getSrcAEInst());

			} catch (Exception e1) {

				this.log.error(e1.getMessage());
			}


			this.log.info("new incomming TCP control flow created  with flow ID " + flowID +
					", srcName: " + srcName + " and dstName: " + dstName);

		} catch (InvalidProtocolBufferException e) {
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