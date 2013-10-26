/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University 
 */

package application.ae;

import rina.rib.impl.RIBImpl;
import rina.util.MessageQueue;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class ApplicationEntity extends Thread {


	protected String apName = null;
	protected String apInstance = null;
	protected String aeName = null;
	protected String aeInstance = null;
	
	protected MessageQueue msgQueue = null;
	
	protected RIBImpl rib = null;


	public ApplicationEntity(String apName, String apInstance, String aeName, String aeInstance, RIBImpl rib)
	{
		this.apName = apName;
		this.apInstance = apInstance;
		this.aeName = aeName;
		this.aeInstance = aeInstance;
		this.msgQueue = new MessageQueue();
		this.rib = rib;
	
	}


	public synchronized MessageQueue getMsgQueue() {
		return msgQueue;
	}




}
