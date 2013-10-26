/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */

package rina.ipc.ae;

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import application.ae.ApplicationEntity;

import rina.irm.impl.IRMImpl;
import rina.irm.util.HandleEntry;
import rina.rib.impl.RIBImpl;
import rina.util.MessageQueue;

/**
 * Data Transfer AE of an IPC process
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class DataTransferAE extends ApplicationEntity{
	
	private  Log log = LogFactory.getLog(this.getClass());
	
	private boolean listen = true;
	
	private IRMImpl irm = null;
	
	//stores all connections to this AE
	private LinkedHashMap<Integer, HandleEntry> handleEntries = null;
	
	//this is all Flows allocated by the flow allocator
	private LinkedHashMap<Integer, MessageQueue> FlowQueues = null;
	
	private LinkedHashMap <Integer, Integer> forwardingTable = null;  

	
	
	public DataTransferAE(String ApName, String ApInstance, String AeInstance, RIBImpl rib,  IRMImpl irm)
	{
		super(ApName, ApInstance, "Data Transfer", AeInstance,rib);
		this.rib.addAttribute("dataTransferAeMsgQueue", this.msgQueue);
		this.irm = irm;
		this.irm.setDae(this);
		
		this.handleEntries = new LinkedHashMap<Integer, HandleEntry> ();
		
		this.FlowQueues = (LinkedHashMap<Integer, MessageQueue>)this.rib.getAttribute("flowQueues");
		
		this.start();
		
	}

	public void run()
	{
		this.log.info(this.apName + "/" +  this.apInstance + "/" +  this.aeName + "/" + this.aeInstance + " started");
		//this AE do nothing for now
	}
	

	
	public synchronized void addNewHandle(int handleID ,HandleEntry he)
	{
		this.handleEntries.put(handleID, he);
		
		//start a thread there to demultiplex and relay flow on  this handle
		new DataTransferAEHandler(handleID,he, this.rib,this.irm);
	}
	
	
	
	public synchronized void stopAE() {
		this.listen = false;
	}

	
}
