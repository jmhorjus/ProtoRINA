/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */
package rina.ddf.relayApplication;


import rina.irm.util.HandleEntry;
import rina.object.internal.Flow;
import application.impl.Application;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class RelayApplicationProcess extends Application {


	private String relayedApName = null;
	private String relayedApInstance = null;
	private boolean difFormedFlag = false;

	public RelayApplicationProcess(String apName, String relayedApName) {

		super(apName,null);
		this.relayedApName = relayedApName;
		this.rib.addAttribute("difFormedFlag", this.difFormedFlag);
		this.rib.addAttribute("relayedApName", this.relayedApName);
		
	}
	
	public RelayApplicationProcess(String apName, String  apInstance, String relayedApName, String relayedApInstance) {

		super(apName,apInstance);
		this.relayedApName = relayedApName;
		this.relayedApInstance = relayedApInstance;
		this.rib.addAttribute("difFormedFlag", this.difFormedFlag);
		this.rib.addAttribute("relayedApName", this.relayedApName);
		this.rib.addAttribute("relayedApInstance", this.relayedApInstance);
		
	}
	
	public void registerRelayServiceToIDD()
	{
		this.log.debug("registerRelayServiceToIDD");
		this.registerServiceToIDD("relay:" + this.relayedApName + this.relayedApInstance);
	}
	

	public void attachHandler(int handleID, HandleEntry he) 
	{
		new RelayHandler(handleID, this.ipcManager, he, this.rib);
	}

}
