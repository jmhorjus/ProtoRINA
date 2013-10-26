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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ForwardingTable to keep synchronization.
 * It can be used for different routing policies (Link State or Distance Vector), 
 * and this is the reason to have this Forwarding Table as a object class.
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class ForwardingTable {

	private Log log = LogFactory.getLog(this.getClass());

	private LinkedHashMap <Integer, Integer> forwardingTable = null; 

	private Neighbors neighbors = null;



	public ForwardingTable(Neighbors neighbors)
	{
		this.forwardingTable = new LinkedHashMap <Integer, Integer>();
		this.neighbors = neighbors;
	}

	public synchronized int getNextHop(int dstAddr)
	{
		int nextHop = -1;


		
		if(this.neighbors.containNeighbor(dstAddr) )
		{
			 //this.log.debug("forwarding Table is " +  this.forwardingTable);

			//	this.log.debug("Next hop not found in the forwarding table (due to routing update delay) is its director neighbor:" + dstAddr );

			nextHop =  dstAddr;
		}else
		{

			if(this.forwardingTable.containsKey(dstAddr))
			{

				nextHop = this.forwardingTable.get(dstAddr);

				//	this.log.debug("Next hop found in the forwarding table for " + dstAddr  + ", and it is " + nextHop);

			}else
			{

				this.log.error("Next hop cannot be  found for " + dstAddr + ", hence not reachable" );
			}
		}

		return nextHop;
	}


	//	public synchronized int getNextHop(int dstAddr)
	//	{
	//		int nextHop = -1;
	//		
	//		
	//
	//		if(this.forwardingTable.containsKey(dstAddr))
	//		{
	//
	//			nextHop = this.forwardingTable.get(dstAddr);
	//
	//		//	this.log.debug("Next hop found in the forwarding table for " + dstAddr  + ", and it is " + nextHop);
	//
	//		}else
	//		{
	//			// Note:
	//			// the routing update is a factor in a sense that in the current Link State (Dijsktra algorithm)
	//			// we first empty the forwarding table, then build a new one,
	//			// to guarantee the pointer to the forwarding table is not changed ( C/C++ concept), as object name is kind of a pointer in java
	//			// There will be sync problem for direct neighbor, whose link routing entry is not received
	//			// So we need to check the direct neighbor to solve this issue	
	//			
	//			if( this.neighbors.containNeighbor(dstAddr) )
	//			{
	//		//		this.log.debug("forwarding Table is " +  this.forwardingTable);
	//
	//		//		this.log.debug("Next hop not found in the forwarding table (due to routing update delay) is its director neighbor:" + dstAddr );
	//
	//				nextHop =  dstAddr;
	//			}else
	//			{
	//				this.log.error("Next hop cannot be  found for " + dstAddr + ", hence not reachable" );
	//			}
	//		}
	//
	//		return nextHop;
	//	}


	/**
	 * For now this will be called when the process is told someone is its direct neighbor
	 * @param dstAddr
	 * @param nextHop
	 */
	public synchronized void addNextHop(int dstAddr, int nextHop)
	{
		this.forwardingTable.put(dstAddr, nextHop);
	}

	public synchronized LinkedHashMap<Integer, Integer> getForwardingTable() {
		return forwardingTable;
	}



}
