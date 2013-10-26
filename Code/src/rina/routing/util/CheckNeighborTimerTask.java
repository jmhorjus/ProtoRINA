/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.routing.util;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.irm.impl.IRMImpl;
import rina.object.internal.Neighbor;
import rina.object.internal.Neighbors;
import rina.routing.RoutingDaemon;

/**
 * This is a timer task which checks if a neighbor still connected or not
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class CheckNeighborTimerTask extends TimerTask {

	private Log log = LogFactory.getLog(this.getClass());


	private boolean alive = true;

	private Neighbors neighbors = null;


	private LinkStateRoutingInfo linkStateRoutingInfo = null;
	
	private IRMImpl irm = null;

	private int neighborAddr ;
	
	private Neighbor neighbor = null;
	private String neighborApName = null;
	private String neighborApInstance = null;

	public CheckNeighborTimerTask(int neighborAddr,Neighbors neighbors , LinkStateRoutingInfo linkStateRoutingInfo,IRMImpl irm)
	{
		this.neighborAddr = neighborAddr;
		this.neighbors = neighbors;

		this.linkStateRoutingInfo = linkStateRoutingInfo;
		
		this.irm = irm;
		
		this.neighbor = this.neighbors.getBeighbor((long) this.neighborAddr);
		this.neighborApName = this.neighbor.getApName();
		this.neighborApInstance = this.neighbor.getApInstance();
		
		
		this.log.info("CheckNeighborTimerTask started for neighbor " + this.neighborAddr);

	}

	public void run() {

//		this.log.debug("alive is " + this.alive + " for " +  this.neighborAddr);

		if(alive == false)
		{
			
			// neighbor fails removes it. POLICY HOLDER
			// Right now if the sub event is not received once, then the neighbor is seen down.
			
			
			this.neighbors.removeNeighbor( (long) this.neighborAddr); 
			
		    //deallocate all previous allocated handles to this neighbor including ManagementAE handle and Data Transfer AE handle
			// Note: Between two AEs only one handle exists, and defined in the IRM allocated
			
			this.irm.deallocateAllHandle(this.neighborApName, this.neighborApInstance);
		

			if(this.linkStateRoutingInfo  != null) // If the ruoting is Link State
			{
				this.linkStateRoutingInfo.removeNeighbor(this.neighborAddr);
				
				
			}
			else // Here can be for Distance Vector Routing 
			{

			}
			
			this.log.debug("Tasktask(checkNeighborAlive) canceled for " +  this.neighborAddr + ", as it is down");
			this.cancel();
			
		}

		this.alive = false;

	}

	public synchronized void setAlive(boolean alive) {
		this.alive = alive;
	}

}
