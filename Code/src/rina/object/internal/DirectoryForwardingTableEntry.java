/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */


package rina.object.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.object.gpb.DirectoryForwardingTableEntry_t.directoryForwardingTableEntry_t;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class DirectoryForwardingTableEntry {

	private Log log = LogFactory.getLog(this.getClass());

	private ApplicationProcessNamingInfo apInfo;
	private long addr;
	private long timestamp;

	public DirectoryForwardingTableEntry(ApplicationProcessNamingInfo apInfo, long addr, long timestamp)
	{
		this.apInfo = apInfo;
		this.addr = addr;
		this.timestamp = timestamp;

	}

	public directoryForwardingTableEntry_t convert()
	{
		directoryForwardingTableEntry_t.Builder directoryForwardingTableEntry = directoryForwardingTableEntry_t.newBuilder();

		directoryForwardingTableEntry.setApplicationName(this.apInfo.convert());

		directoryForwardingTableEntry.setTimestamp(this.timestamp);

		directoryForwardingTableEntry.setIpcProcessAddress(this.addr);

		return directoryForwardingTableEntry.buildPartial();
	}

	public boolean compare(DirectoryForwardingTableEntry directoryForwardingTableEntry)
	{
		ApplicationProcessNamingInfo apInfo = directoryForwardingTableEntry.getApInfo();
		long addr = directoryForwardingTableEntry.getAddr();
		long timestamp = directoryForwardingTableEntry.getTimestamp();

		if( !this.apInfo.getApName().equals(apInfo.getApName()) ||  !this.apInfo.getApInstance().equals(apInfo.getApInstance()) )
		{
			this.log.debug("Compare Entry: naming different, return false");
			return false;
		}else 
		{
			if(this.timestamp <= timestamp) 
			{
				if(this.addr != addr)
				{  this.log.debug("Application's IPC Address changed, return false");
					return false;
				}else 
				{
					this.log.debug("Entry compared is the same, return true.");
					return true;
				} 
			}else
			{
				this.log.debug("Entry compared is an expired entry, so nothing changed, return true");
				return true;
			}
		}

	}
	
	
	public void print()
	{
		this.log.debug("Print DirectoryForwardingTableEntry: " +  this.apInfo.getApName() + "/" + this.apInfo.getApInstance() +
				"/" + this.apInfo.getAeName() + "/" + this.apInfo.getAeInstance() + ", rina address is " + this.addr +  
				", timestamp is " + this.timestamp);
	}

	public synchronized ApplicationProcessNamingInfo getApInfo() {
		return apInfo;
	}

	public synchronized void setApInfo(ApplicationProcessNamingInfo apInfo) {
		this.apInfo = apInfo;
	}

	public synchronized long getAddr() {
		return addr;
	}

	public synchronized void setAddr(long addr) {
		this.addr = addr;
	}

	public synchronized long getTimestamp() {
		return timestamp;
	}

	public synchronized void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}



}
