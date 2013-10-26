/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 * 
 */

package rina.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MessageQueue {

	private Log log = LogFactory.getLog(MessageQueue.class);
	
	private BlockingQueue<byte[]> receiveQueue = null;
	private BlockingQueue<byte[]> sendQueue = null;
	
	//each flow is attached with a MessageQueue.
	private int flowID = -1;
	
	public MessageQueue()
	{
		   this.receiveQueue = new LinkedBlockingQueue<byte[]>();
		   this.sendQueue = new LinkedBlockingQueue<byte[]>();
	}
	
	public  void addReceive(byte[] data)
	{
		this.receiveQueue.offer(data);
	}
	
	public  byte[] getReceive() 
	{
		byte[] msg = null;
		try {
			msg =  this.receiveQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//log.error(e.getMessage());
		}
		return msg;
	}
	
	/**
	 * Timeout in milliseconds, and return null
	 * @param timeout
	 * @return
	 */
	public byte[] getReceive(long timeout)
	{
		byte[] msg = null;
		
		try {
			msg =  this.receiveQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//log.error(e.getMessage());
		}
		return msg;
	}

	public  void addSend(byte[] data)
	{
		this.sendQueue.offer(data);
	}
	
	public  byte[] getSend()
	{
		byte[] msg = null;
		try {
			msg =  this.sendQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//log.error(e.getMessage());
		}
		return msg;
	}

	public synchronized int getFlowID() {
		return flowID;
	}

	public synchronized void setFlowID(int flowID) {
		this.flowID = flowID;
	}

}
