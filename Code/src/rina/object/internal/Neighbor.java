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
import rina.object.gpb.Neighbour_t.neighbor_t;

/**
 * This is used in Class Neighbors
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Neighbor
{
	
	private  Log log = LogFactory.getLog(this.getClass());
	private String apName = null;
	private String apInstance = null;
	private long addr = -1;
	
	
	public Neighbor(String apName, String apInstance, long addr)
	{
		this.apName  = apName;
		this.apInstance = apInstance;
		this.addr = addr;
	}

	
	
	
	public void print()
	{
		this.log.debug("Neighor(apName/apInstance/Addr): " +  this.apName + "/" +  this.apInstance + "/" + this.addr);
	}

	public  neighbor_t convert()
	{
		neighbor_t.Builder  neighbour =  neighbor_t.newBuilder();
		neighbour.setApplicationProcessName(this.apName);
		neighbour.setApplicationProcessInstance(this.apInstance);
		neighbour.setAddress(this.addr);
		
		return neighbour.buildPartial();
	}
	
	public synchronized String getApName() {
		return apName;
	}


	public synchronized void setApName(String apName) {
		this.apName = apName;
	}


	public synchronized String getApInstance() {
		return apInstance;
	}


	public synchronized void setApInstance(String apInstance) {
		this.apInstance = apInstance;
	}


	public synchronized long getAddr() {
		return addr;
	}


	public synchronized void setAddr(long addr) {
		this.addr = addr;
	}
	
}
