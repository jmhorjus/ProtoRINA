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

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.irm.util.HandleEntry;
import rina.rib.impl.RIBImpl;
import application.component.impl.IPCResourceManagerImpl;


/**
 * Management AE of DAP that handle management message by forking DAPManagementAEHandler
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class DAPManagementAE extends ApplicationEntity{ 
	
	private  Log log = LogFactory.getLog(this.getClass());
	
	private IPCResourceManagerImpl irm = null;
	
	//stores all connections to this AE
	private LinkedHashMap<Integer, HandleEntry> handleEntries = null;
	
	public DAPManagementAE(String ApName, String ApInstance, String AeInstance, RIBImpl rib,  IPCResourceManagerImpl irm)
	{
		super(ApName, ApInstance, "Management", AeInstance,rib);
		this.rib.addAttribute("managementAeMsgQueue", this.msgQueue);
		
		this.irm = irm;
		this.irm.setMae(this);
		
		this.handleEntries = new LinkedHashMap<Integer, HandleEntry> ();
		
		this.start();
		
	}
	
	public void run()
	{
		
	}
	
	public synchronized void addNewHandle(int handleID, HandleEntry he) {

		
		this.handleEntries.put(handleID, he);

		//start a thread there to handle new flow with the handle
		new DAPManagementAEHandler(handleID, he, this.rib, this.irm);
		
	}


}
