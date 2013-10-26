/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.timer.test;

import rina.util.MessageQueue;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testMsgQTimeout implements Runnable {

	public static void main(String args[])
	{
		MessageQueue msgQ = new MessageQueue();
		
		testMsgQTimeout t = new testMsgQTimeout(msgQ);

		new Thread(t).start(); 


		byte[] msg = msgQ.getReceive(5000);

		if(msg == null)
		{
			System.out.println("Timeout, nothing received in the queue");
		}else
		{
			System.out.println( new String(msg));
		}

	}


	public MessageQueue msgQ = null;

	public testMsgQTimeout(MessageQueue msgQ)
	{
		this.msgQ = msgQ;
	}

	public void run()
	{
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		msgQ.addReceive(new String("hello").getBytes());
	}


}
