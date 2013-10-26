
/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */

package application.component.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.object.internal.Flow;

import application.component.impl.IPCResourceManagerImpl;

/**
 * This one is used to print  msgs received on a handle 
 * Just for testing purpose
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class DummyHandler extends Thread {
	
	private  Log log = LogFactory.getLog(this.getClass());
	
	private IPCResourceManagerImpl ipcManager = null;
	
	private int handleID = -1;

	
	public DummyHandler(int handleID, IPCResourceManagerImpl ipcManager)
	{
		this.handleID = handleID;
		this.ipcManager = ipcManager;
		this.start();
	}

	
	public void run()
	{
		this.log.info("dummyThraed started");
		
		int count = 0;
		
		while(true)
		{
			byte[] msg =  this.ipcManager.receive(this.handleID);
			this.log.debug("#################### " + count + " content is " + new String(msg));
			count ++;
		}
	}
}
