/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */

package rina.routing;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.config.RINAConfig;
import rina.irm.impl.IRMImpl;
import rina.object.internal.ForwardingTable;
import rina.object.internal.Neighbor;
import rina.object.internal.Neighbors;
import rina.object.internal.SubscriptionEvent;
import rina.object.internal.SubscriptionEvent.EventType;
import rina.rib.impl.RIBImpl;
import rina.ribDaemon.impl.RIBDaemonImpl;
import rina.routing.util.CheckNeighborTimerTask;
import rina.routing.util.LinkStateRoutingInfo;

/**
 * Routing Daemon is responsible for the routing in the DIF.
 * Current we support link state protocol, but programmer can add new policies.
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */

public class RoutingDaemon {

	private Log log = LogFactory.getLog(this.getClass());

	private RINAConfig config = null;

	private String routingProtocol = null;

	private RIBImpl rib = null;
	private IRMImpl irm = null;

	private RIBDaemonImpl ribDaemon = null;

	private Neighbors neighbors = null;

	private int rinaAddr = -1;

	//Link State Routing
	//Later will support other routing, such as distance vector
	private LinkStateRoutingInfo linkStateRoutingInfo = null;

	private ForwardingTable forwardingTable = null;  

	private double routingEntrySubUpdatePeriod = -1;
	
	private String linkCostPolicy = null;

	public static double inf =  99999999;
	public static double checkNeighborPeriod = -1;
	
	


	// The following is to check if the neighbor is alive
	private Timer timer = null;
	private LinkedHashMap<Integer, CheckNeighborTimerTask> allCheckNeighborTimerTask = null;

	public RoutingDaemon(RIBImpl rib, IRMImpl irm)
	{
		this.rib = rib;
		this.irm = irm;		

		this.config = (RINAConfig)this.rib.getAttribute("config");

		this.routingEntrySubUpdatePeriod = this.config.getRoutingEntrySubUpdatePeriod();
		this.rib.addAttribute("routingEntrySubUpdatePeriod", this.routingEntrySubUpdatePeriod);

		this.checkNeighborPeriod = this.config.getCheckNeighborPeriod();
		this.rib.addAttribute("checkNeighborPeriod", this.checkNeighborPeriod);
		
		this.linkCostPolicy = this.config.getLinkCostPolity();
		this.rib.addAttribute("linkCostPolicy", this.linkCostPolicy);
		
		

		this.rinaAddr = (Integer)this.rib.getAttribute("rinaAddr");

		this.forwardingTable = (ForwardingTable )this.rib.getAttribute("forwardingTable");

		this.routingProtocol = (String) this.rib.getAttribute("routingProtocol");

		//POLICY HOLDER
		if(this.routingProtocol.equals("linkState"))
		{
			this.linkStateRoutingInfo = new LinkStateRoutingInfo(this.rinaAddr, this.forwardingTable);
			this.rib.addAttribute("linkStateRoutingInfo", this.linkStateRoutingInfo);

			this.timer = new Timer();
			this.allCheckNeighborTimerTask = new LinkedHashMap<Integer, CheckNeighborTimerTask>();

			this.ribDaemon = (RIBDaemonImpl) this.rib.getAttribute("ribDaemon");
			this.neighbors = (Neighbors) this.rib.getAttribute("neighbors");


			this.initPub();

			this.initSub();

		}else if (this.routingProtocol.equals("myNewRoutingPolicy"))
		{
			//implement new routing policy here
		}


		this.log.info("Routing Daemon started, and routing protocol used is " + this.routingProtocol);


	}



	/**
	 * Init sub events needed for Routing Daemon
	 */ 
	private void initSub() {



		LinkedList<Neighbor> neighborList = this.neighbors.getNeighborList();

		//create sub event of routing entry
		for(int i = 0; i< neighborList.size(); i ++)
		{
			String publisher = Long.toString( neighborList.get(i).getAddr() );

			this.addLinkStateRoutingEntrySubEvent(publisher);

		}


	}

	/**
	 * Init sub events needed for Routing Daemon
	 */ 
	private void initPub() {




		//first it is a neighbor discover pub event, pub to its neighbor, to check if it is alive or not
		//this event should be more frequent than the routing sub events(event1 and event2), which will send update to neighbor alive based on the neighbor discover event

		SubscriptionEvent event = new SubscriptionEvent(EventType.PUB, this.checkNeighborPeriod, "checkNeighborAlive");
		this.ribDaemon.createEvent(event);

		//two things are pubed here, one is its direct routing entries
		//but also the routing entries it received from its neighbors, basically flooding


		SubscriptionEvent event1 = new SubscriptionEvent(EventType.PUB, this.routingEntrySubUpdatePeriod, "linkStateRoutingEntry");
		this.ribDaemon.createEvent(event1);

		SubscriptionEvent event2 = new SubscriptionEvent(EventType.PUB, this.routingEntrySubUpdatePeriod, "linkStateRoutingEntryNeighborsReceived");
		this.ribDaemon.createEvent(event2);



	}

	public void addLinkStateRoutingEntrySubEvent(String publisher)
	{

		//Sleep 1 seconds on purpose.
		//This is extremely useful after a member joins a DIF, the DIF manager subscriber to its routing Entry 
		// as it takes some time for the new member to start is routing daemon after the enrollment is done


		this.log.debug("add routing entry sub to publisher :" + publisher);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//first it is a neighbor discover sub event, sub to all it neighbor, to check if it is alive or not
		//this event should be more frequent than the routing sub events(event1 and event2), which will send update to neighbor alive based on the neighbor discover event

		SubscriptionEvent event = new SubscriptionEvent(EventType.SUB, this.checkNeighborPeriod, "checkNeighborAlive", publisher);

		int eventID = this.ribDaemon.createEvent(event);

		this.log.debug("sub (checkNeighborAlive) id  to " +  publisher + " is " +  eventID);


		//two things are subed here, one is neigbor's direct routing entry,
		//but also the routing entries the neighbor received from their neighbors, basically flooding

		SubscriptionEvent event1 = new SubscriptionEvent(EventType.SUB, routingEntrySubUpdatePeriod, "linkStateRoutingEntry", publisher);

		int event1ID = this.ribDaemon.createEvent(event1);

		this.log.debug("sub (linkStateRoutingEntry) id  to " +  publisher + " is " +  event1ID);


		SubscriptionEvent event2 = new SubscriptionEvent(EventType.SUB, routingEntrySubUpdatePeriod, "linkStateRoutingEntryNeighborsReceived", publisher);

		int event2ID = this.ribDaemon.createEvent(event2);

		this.log.debug("sub (linkStateRoutingEntryNeighborsReceived) id  to " +  publisher + " is " +  event2ID);


	}

	public synchronized void addCheckNeighborTimerTask(int neighborAddr)
	{
		if(!this.allCheckNeighborTimerTask.containsKey(neighborAddr)) // no check timer task, create one
		{

			CheckNeighborTimerTask task = null;


			task = new CheckNeighborTimerTask(neighborAddr,this.neighbors,this.linkStateRoutingInfo,this.irm);


			this.allCheckNeighborTimerTask.put(neighborAddr, task);

			//here adding extra time  is used to prevent sync of pub and check time

			double extraTime = 1;

			System.err.println(" RoutingDaemon.checkNeighborPeriod " +  this.checkNeighborPeriod );

			this.timer.schedule(task, (long)0, (long)(  this.checkNeighborPeriod + extraTime)* 1000);

		}else
		{
			//	this.log.debug(" CheckNeighborTimerTask exists already for " + neighborAddr);
		}
	}

	public synchronized void updateChckNeighborTimerTask(int neighborAddr)
	{
		if(this.allCheckNeighborTimerTask.containsKey(neighborAddr))
		{
			this.allCheckNeighborTimerTask.get(neighborAddr).setAlive(true);
		}

	}



}
