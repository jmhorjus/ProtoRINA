/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */


package rina.object.internal;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.object.gpb.DirectoryForwardingTableEntry_t.directoryForwardingTableEntrySet_t;
import rina.object.gpb.DirectoryForwardingTableEntry_t.directoryForwardingTableEntry_t;

/**
 * This one is used by Flow Allocator to get the IPC address of an application
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class DirectoryForwardingTable {


	private Log log = LogFactory.getLog(this.getClass());

	private LinkedHashMap<Long, DirectoryForwardingTableEntry>  addrToEntry = null;
	
	//note: key name is the concatenation of apInfo and apInstance
	private LinkedHashMap<String, DirectoryForwardingTableEntry>  nameToEntry = null; 
	
	private LinkedList<DirectoryForwardingTableEntry> entryList = null;


	public DirectoryForwardingTable()
	{
		this.addrToEntry = new LinkedHashMap<Long, DirectoryForwardingTableEntry>();
		this.nameToEntry = new LinkedHashMap<String, DirectoryForwardingTableEntry>();	
		this.entryList = new LinkedList<DirectoryForwardingTableEntry> ();
	}

	public synchronized void addEntry(DirectoryForwardingTableEntry entry)
	{
		ApplicationProcessNamingInfo apInfo = entry.getApInfo();

		String apName = apInfo.getApName();
		
		String apInstance = apInfo.getApInstance();

		String name = apName + apInstance;
		
		if(this.nameToEntry.containsKey(name)) // update old entry
		{
			long oldAddr = this.nameToEntry.get(name).getAddr();
			
			this.entryList.remove(this.addrToEntry.get(oldAddr)); //remove the old entry
			
			this.addrToEntry.remove(oldAddr);//remove the old entry under the addrToEntry
			
		}
		
		this.addrToEntry.put(entry.getAddr(), entry);
		
		this.nameToEntry.put(name, entry);
		
		this.entryList.add(entry);
	}

	/**
	 * get dst app's underlying IPC's address
	 * @param name
	 * @return
	 */
	public synchronized long  getAddress(String name)
	{

		long addr = -1;

		if(this.nameToEntry.containsKey(name))
		{
			addr = this.nameToEntry.get(name).getAddr();
		}

		this.log.debug("Addr of " +  name + " is " + addr);

		return addr;
	}


	/**
	 * Check the reachablity based on apName (for IPC, this will be concatenation of ICPName + IPCInstance)
	 * But sometime, regular application might only has an apName without apInstance
	 * @param apName
	 * @return
	 */
	public synchronized boolean checkAppReachability(String apName)
	{
		return this.nameToEntry.containsKey(apName);
	}
	

	public synchronized boolean checkAppReachability(ApplicationProcessNamingInfo apInfo)
	{
		String name = apInfo.getApName() + apInfo.getApInstance();
		
		return this.nameToEntry.containsKey(name);
	}

	
	
	public synchronized boolean checkEntry(DirectoryForwardingTableEntry directoryForwardingTableEntry) {
		
	
		 ApplicationProcessNamingInfo apInfo = directoryForwardingTableEntry.getApInfo();
	
		 if(this.nameToEntry.containsKey(apInfo.getApName()+apInfo.getApInstance()))
		 {
			 this.log.debug("Entry of this application"  + apInfo.getApName() + "/" + apInfo.getApInstance() + " exists");
			 
			 DirectoryForwardingTableEntry existingDirectoryForwardingTableEntry = this.nameToEntry.get(apInfo.getApName()+apInfo.getApInstance());
			 
			 return directoryForwardingTableEntry.compare(existingDirectoryForwardingTableEntry);
	
			 
		 }else
		 {
			 this.log.debug("New Directory Forwarding Table Entry");
			 return false;
		 }
		
	}

	public synchronized directoryForwardingTableEntrySet_t convert()
	{
		directoryForwardingTableEntrySet_t.Builder sets = directoryForwardingTableEntrySet_t.newBuilder();
		
		for(int i =0;i< this.entryList.size();i++)
		{
			directoryForwardingTableEntry_t entry = this.entryList.get(i).convert();
			sets.addDirectoryForwardingTableEntry(entry);
		}
		
		return sets.buildPartial();
	}
	
	
	
	
	public synchronized int getSize()
	{
		return this.entryList.size();
	}


}
