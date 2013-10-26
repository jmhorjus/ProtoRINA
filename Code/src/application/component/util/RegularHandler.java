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

	public RegularHandler(int handleID, IPCResourceManagerImpl ipcManager,HandleEntry handleEntry , RIBImpl rib )
	{
		this.handleID = handleID;
		this.ipcManager = ipcManager;
		this.rib = rib;
		this.apInfo =  (ApplicationProcessNamingInfo)this.rib.getAttribute("apInfo");
		this.handleEntry = handleEntry;

		this.start();

	}


	public void run()
	{
		this.log.info("Regular handler started");

		while(true)
		{
			byte[] msg =  this.ipcManager.receive(this.handleID);


			this.log.debug("MMMMMMMMMMMMMMMMMMMMMMMMsg  content is " + new String(msg));

		}

	}

}
