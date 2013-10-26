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

import rina.object.gpb.LinkStateRoutingEntrySet_t.routingEntry_t;

/**
 * This corrpsonds to RoutingEntry.java in package rina.object.gpb
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class LinkStateRoutingEntry {
	
	private Log log = LogFactory.getLog(this.getClass());

	private long srcAddr = -1;
	private long dstAddr = -1;
	private long timeStamp = 0;
	private double cost = 0.0;

	public LinkStateRoutingEntry( long  srcAddr, long dstAddr, double cost, long timeStamp)
	{
		
		this.srcAddr = srcAddr;
		this.dstAddr = dstAddr;
		this.cost = cost;
		this.timeStamp = timeStamp;
	}
	
	public LinkStateRoutingEntry(routingEntry_t  routingEntry)
	{
		this.srcAddr = routingEntry.getSrcAddr();
		this.dstAddr = routingEntry.getDstAddr();
		this.cost = routingEntry.getCost();
		this.timeStamp = routingEntry.getTimestamp();
	}
	
	public routingEntry_t convert()
	{
		routingEntry_t.Builder entry = routingEntry_t.newBuilder();
		entry.setSrcAddr(this.srcAddr);
		entry.setDstAddr(this.dstAddr);
		entry.setCost(this.cost);
		entry.setTimestamp(this.timeStamp);
		
		return entry.build();
	}

	public String getPrint()
	{
		String content = "LinkStateRoutingEntry(srcAddr/dstAddr/cost/timeStamp):" + this.srcAddr + "/" + this.dstAddr + "/"
				+ this.cost + "/" + this.timeStamp;
		
		return content;
	}
	
	public synchronized long getSrcAddr() {
		return srcAddr;
	}

	public synchronized void setSrcAddr(long srcAddr) {
		this.srcAddr = srcAddr;
	}

	public synchronized long getDstAddr() {
		return dstAddr;
	}

	public synchronized void setDstAddr(long dstAddr) {
		this.dstAddr = dstAddr;
	}

	public synchronized long getTimeStamp() {
		return timeStamp;
	}

	public synchronized void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public synchronized double getCost() {
		return cost;
	}

	public synchronized void setCost(double cost) {
		this.cost = cost;
	}
	
	
}
