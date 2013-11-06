package application.component.util;

/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.irm.util.HandleEntry;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.rib.impl.RIBImpl;
import application.component.impl.IPCResourceManagerImpl;



/**
 * Default application message Handler, just print whatever received as a txt message
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class RegularHandler extends Thread{


	private  Log log = LogFactory.getLog(this.getClass());

	private IPCResourceManagerImpl ipcManager = null;

	private ApplicationProcessNamingInfo apInfo = null;

	private int handleID = -1;

	private RIBImpl rib = null;

	private HandleEntry handleEntry = null;

	private boolean sendBack = false;

	public RegularHandler(int handleID, IPCResourceManagerImpl ipcManager,HandleEntry handleEntry , RIBImpl rib )
	{
		this.handleID = handleID;
		this.ipcManager = ipcManager;
		this.rib = rib;
		this.apInfo =  (ApplicationProcessNamingInfo)this.rib.getAttribute("apInfo");
		this.handleEntry = handleEntry;

		this.start();

	}

	//This constructor is used when it receives something, and does not want to send anything back
	public RegularHandler(int handleID, IPCResourceManagerImpl ipcManager)
	{
		this.handleID = handleID;
		this.ipcManager = ipcManager;
		this.sendBack = false;
		this.start();

	}



	public void run()
	{
		this.log.info("Regular handler started");

		while(true)
		{
			byte[] msg =  this.ipcManager.receive(this.handleID);

			String msgContent = new String (msg);

			this.log.debug("MMMMMMMMMMMMMMMMMMMMMMMMsg  content is " + msgContent);

			if(this.sendBack == true)
			{
				//NOTE: this is for tesing purpose only
				//to send message back to the sender , n times
			
				int n = 10;
				for(int i = 0; i< n; i++)
				{
					String newMsg = msgContent + ": new " + i;
					
					try {
						this.ipcManager.send(this.handleID, newMsg.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}


		}

	}

}
