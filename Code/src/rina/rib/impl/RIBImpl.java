/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.rib.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.rib.api.RIBAPI;

import rina.object.internal.*;


/**
 * The OIB/RIB Daemon is a key element for the DIF or DAF. For DAFs
 * (Distributed Application Facilities), this is the Object Information Base (OIB) 
 * Daemon, for DIFs (Distributed IPC Facilities), this is the Resource Information
 * Base (RIB) Daemon. The members of a DIF/DAF need to share information
 * relevant to their collaboration. Different aspects of the DAF/DIF will want
 * different information that will need to be updated with different frequency or
 * upon the occurrence of some event. The OIB/RIB Daemon provides this service
 * and optimizes the operation by combining requests where possible.
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0
 *
 */
public class RIBImpl implements RIBAPI {

	private Log log = LogFactory.getLog(RIBImpl.class);

	private LinkedHashMap<String, Object> attributeList = null;

	/**
	 * the members of the DAF/DIF from/to which attributes are exchanged. 
	 * ApName + ApInstance is the key
	 */
	private LinkedList<String> memberList = null;
	private LinkedHashMap<Integer,Member> addrToMember = null;
	private LinkedList<Member> memberEntryList = null; //contains the details 


	private LinkedHashMap<Integer, String > addrToName = null;

	//ApName + ApInstance is the key
	private LinkedHashMap<String, Integer > nameToAddr = null;


	//the following is used to calculate the direct neighbor for new IPC member based on their underlying DIF
	private LinkedHashMap<String, LinkedList<Integer>> underlyingDIFsMembers = null;
	//an ipc may have multiple underlying DIF, so here the key is LinkedList<String> 
	private LinkedHashMap<Integer,LinkedList<String>> addrToUnderlyingDIFName = null;
	private LinkedHashMap<Integer, Neighbor> addrInfo = null; //

	private int addrRange = 10000;



	public RIBImpl()
	{
		this.attributeList = new LinkedHashMap<String, Object>();

		this.memberList =  new LinkedList<String>();
		this.memberEntryList = new  LinkedList<Member>();
		this.addrToMember = new LinkedHashMap<Integer,Member>();

		this.addrToName = new LinkedHashMap<Integer, String>();
		this.nameToAddr = new  LinkedHashMap<String,Integer>();

		this.underlyingDIFsMembers = new LinkedHashMap<String, LinkedList<Integer>>();
		this.addrToUnderlyingDIFName = new LinkedHashMap<Integer,LinkedList<String > >();
		this.addrInfo = new LinkedHashMap<Integer, Neighbor>();
	}


	public synchronized LinkedHashMap<String, Object> getAttributeList() {
		return attributeList;
	}


	public synchronized void setAttributeList(LinkedHashMap<String, Object> attributeList) {
		this.attributeList = attributeList;
	}


	public synchronized LinkedList<String> getMemberList() {
		return memberList;
	}


	public synchronized void setMemberList(LinkedList<String> memberList) {
		this.memberList = memberList;
	}



	public synchronized Object getAttribute(String attribute) {

		return this.attributeList.get(attribute);
	}


	public synchronized void removeAttribute(String attribute) {

		this.attributeList.remove(attribute);
	}


	public synchronized void addAttribute(String attributeName, Object attribute) {

		this.attributeList.put(attributeName, attribute);

	}




	public synchronized int addMember(String ipcName, String ipcInstance, LinkedList<String> underlyingDIFs) {

		int rinaAddr  = -1;

		rinaAddr = this.generateRINAAddr();

		this.addMember(rinaAddr, ipcName, ipcInstance, underlyingDIFs);

		return rinaAddr;

	}


	public synchronized void addMember(int rinaAddr, String ipcName, String ipcInstance, LinkedList<String> underlyingDIFs)
	{
		String name = ipcName + ipcInstance;

		if(this.nameToAddr.containsKey(name))
		{
			this.log.debug("The IPC process("  +  ipcName + "/" + ipcInstance + ")was a member before with address" +  rinaAddr);
			this.removeMember(name);// remove the old existing info, FIXME needs to update everyone

		}else
		{
			this.log.debug("The IPC process("  +  ipcName + "/" + ipcInstance + ")is  a new  member with address " +  rinaAddr);
		}


		this.addrToName.put(rinaAddr, name);

		this.nameToAddr.put(name, rinaAddr);

		this.addrInfo.put(rinaAddr, new Neighbor(ipcName, ipcInstance,rinaAddr));

		this.memberList.add(name);
		Member newMember = new Member(ipcName, ipcInstance,rinaAddr, underlyingDIFs);

		this.memberEntryList.add(newMember);
		this.addrToMember.put(rinaAddr, newMember);

		this.addUnderlyingDIFsInfo(rinaAddr, underlyingDIFs);


	}


	/**
	 * Check if DIF contains the member
	 * @param rinaAddr
	 * @param ipcName
	 * @param ipcInstance
	 */
	public synchronized boolean containMember(String ipcName, String ipcInstance)
	{
		String name = ipcName + ipcInstance;

		return this.memberList.contains(name);

	}

	public synchronized boolean containMember(String ipcName, String ipcInstance, int addr)
	{
		String name = ipcName + ipcInstance;

		if( this.memberList.contains(name) && this.nameToAddr.get(name) == addr ) 
		{
			return true;
		}else
		{
			return false;
		}



	}

	//	private  int generateRINAAddr()
	//	{
	//		int rinaAddr = -1;
	//
	//		rinaAddr = (int)( Math.random()* this.addrRange); 
	//
	//		while(this.addrToName.containsKey(rinaAddr))
	//		{
	//			rinaAddr = (int)( Math.random()* this.addrRange); 
	//		}
	//		
	//
	//		this.log.debug("RINA Address generated is " +  rinaAddr);
	//
	//		this.addrToName.put(rinaAddr, null);
	//		
	//		return rinaAddr;
	//	}



	//just for testinging purpose, delete later, and replace with the above one

	//	static int rinaAddr = 1;
	//	private synchronized int generateRINAAddr()
	//	{
	//		rinaAddr = rinaAddr + 1;
	//
	//		this.log.debug("RINA Address generated is " +  rinaAddr);
	//		
	//		this.addrToName.put(rinaAddr, null);
	//
	//		return rinaAddr;
	//	}

	//static int counter = 1;
	
	 int counter = 1;

	private synchronized int generateRINAAddr()
	{

		String addressPolicy = this.getAttribute("addressPolicy").toString();

		this.log.debug("addressPolicy is " + addressPolicy);

		int rinaAddr = -1;


		//POCICY HOLDER
		if(addressPolicy.equals("RANDOM"))
		{

			rinaAddr = (int)( Math.random()* this.addrRange); 

			while(this.addrToName.containsKey(rinaAddr))
			{
				rinaAddr = (int)( Math.random()* this.addrRange); 
			}

		}
		else if(addressPolicy.equals("DEFAULT"))
		{

			int localRINAAddr = (Integer)this.getAttribute("rinaAddr");

			rinaAddr = localRINAAddr * 10 + counter;

			counter++;

			this.log.debug("888888888888888888888888888888888888This IPC process's Addr is " +  localRINAAddr);

			
		}else if (addressPolicy.equals("myNewAddressPolicy"))
		{
			//Implement your own policy here
		}

		this.log.debug("88888888888888888888888888888888888RINA Address generated is " +  rinaAddr);

		
		this.addrToName.put(rinaAddr, null);

		return rinaAddr;
	}

	/**
	 * Here name = ipcName + ipcInstance
	 */
	public synchronized void removeMember(String name) {

		if(this.memberList.contains(name))
		{

			int rinaAddr = this.nameToAddr.get(name);

			this.memberList.remove(name);
			Member oldMemberEntry = this.addrToMember.get(rinaAddr);
			this.memberEntryList.remove(oldMemberEntry);

			this.addrToName.remove(rinaAddr);

			this.nameToAddr.remove(name);


			///////////////////////////////////////////////////////////////////////////////////////////////////
			//the following is to clean the part used for tell new member's direct neigbor
			// see the def above for details
			LinkedList<String> allUnderlyingDIFs = this.addrToUnderlyingDIFName.get(rinaAddr);

			this.addrToUnderlyingDIFName.remove(rinaAddr);

			for(int i = 0 ;i < allUnderlyingDIFs.size(); i++)
			{

				String underlyingDIFName = allUnderlyingDIFs.get(i);

				this.underlyingDIFsMembers.get(underlyingDIFName).remove((Integer)rinaAddr);

				//			System.out.println("tttttttttttttttttttthis.underlyingDIFsMembers.get(underlyingDIFName) is " + this.underlyingDIFsMembers.get(underlyingDIFName) );

			}
			//////////////////////////////////////////////////////////////////////////////////////////////

			this.addrInfo.remove(rinaAddr);


		}else
		{
			this.log.error(name + " does not exist in the DIF ");
		}

	}


	/**
	 * This is used to add the underlying DIF info for a IPC with address rinaAddr
	 * @param rinaAddr
	 * @param underlyingDIFName
	 */
	private synchronized void addUnderlyingDIFInfo(int rinaAddr, String underlyingDIFName) 
	{
		this.log.debug("addUnderlyingDIFInfo(rinaAddr/underlyingDIFName): " + rinaAddr + "/" + underlyingDIFName );


		if(! this.addrToUnderlyingDIFName.containsKey(rinaAddr))
		{
			this.addrToUnderlyingDIFName.put(rinaAddr, new LinkedList<String>());
		}

		this.addrToUnderlyingDIFName.get(rinaAddr).add(underlyingDIFName);


		if(!this.underlyingDIFsMembers.containsKey(underlyingDIFName))
		{
			this.underlyingDIFsMembers.put(underlyingDIFName, new LinkedList<Integer>());
		}

		this.underlyingDIFsMembers.get(underlyingDIFName).add(rinaAddr);

	}


	public synchronized LinkedList<Neighbor> getIPCListOfUnderlyingDIFName(String underlyingDIFName) 
	{
		LinkedList <Integer> addrList = this.underlyingDIFsMembers.get(underlyingDIFName);

		System.out.println("aaaaaaaaaaaaddrList is " + addrList);

		LinkedList<Neighbor> neighbors = new LinkedList<Neighbor>();

		for(int i= 0; i<addrList.size();i++)
		{
			neighbors.add(this.addrInfo.get( addrList.get(i) ) );
		}

		return neighbors;
	}


	public synchronized void addUnderlyingDIFsInfo(int rinaAddr,LinkedList<String> underlyingDIFs) {

		for(int i = 0;i < underlyingDIFs.size(); i++)
		{
			this.addUnderlyingDIFInfo(rinaAddr, underlyingDIFs.get(i));
		}

	}


	/**
	 * @return the memberEntryList
	 */
	public synchronized LinkedList<Member> getMemberEntryList() {
		return memberEntryList;
	}


	/**
	 * @param memberEntryList the memberEntryList to set
	 */
	public synchronized void setMemberEntryList(LinkedList<Member> memberEntryList) {
		this.memberEntryList = memberEntryList;
	}





}
