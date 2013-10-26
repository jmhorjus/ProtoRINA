/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.ribDaemon.util;

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import application.component.api.IPCResourceManager;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import rina.irm.impl.IRMImpl;
import rina.message.CDAP;
import rina.object.gpb.LinkStateRoutingEntrySet_t.routingEntrySetForwardedByNeighbor_t;
import rina.object.gpb.LinkStateRoutingEntrySet_t.routingEntrySet_t;
import rina.object.gpb.SubscriptionEvent_t.subscriptionEvent_t;
import rina.object.internal.ForwardingTable;
import rina.object.internal.LinkStateRoutingEntry;
import rina.object.internal.Neighbor;
import rina.object.internal.Neighbors;
import rina.object.internal.SubscriptionEvent;
import rina.object.internal.SubscriptionEvent.EventType;
import rina.rib.impl.RIBImpl;
import rina.routing.RoutingDaemon;
import rina.routing.util.LinkStateRoutingInfo;

/**
 * Event handler of subscription event
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class EventHandler{


	private Log log = LogFactory.getLog(this.getClass());

	private RIBImpl rib = null;
	private IPCResourceManager irm = null;
	private SubscriptionEvent event = null;
	private String attribute = null; // assume only one attribute for now

	private int rinaAddr;

	private String IPCName = null;
	private String IPCInstance = null;

	private ForwardingTable forwardingTable = null;  

	private Neighbors neighbors = null;

	//for now we assume event's attribute list only contains one attribute
	// otherwise, here will be a list of publisher, since it might publish many things
	private Publisher publisher = null;

	private LinkStateRoutingInfo linkStateRoutingInfo = null;

	private RoutingDaemon routingDaemon = null;

	private String linkCostPolicy = null;

	//publisher's rina Addr
	private int publisherAddr;


	public EventHandler(RIBImpl rib,IPCResourceManager irm, SubscriptionEvent event)
	{
		this.rib = rib;
		this.irm = irm;

		this.rinaAddr = Integer.parseInt( this.rib.getAttribute("rinaAddr").toString() );
		this.neighbors = (Neighbors)this.rib.getAttribute("neighbors");

		this.forwardingTable = (ForwardingTable) this.rib.getAttribute("forwardingTable");

		this.IPCName = (String) this.rib.getAttribute("ipcName");
		this.IPCInstance = (String)this.rib.getAttribute("ipcInstance");

		this.linkStateRoutingInfo = (LinkStateRoutingInfo)  this.rib.getAttribute("linkStateRoutingInfo");

		this.routingDaemon = (RoutingDaemon) this.rib.getAttribute("routingDaemon");
		this.linkCostPolicy = (String) this.rib.getAttribute("linkCostPolicy");

		this.event = event;	
		this.attribute = this.event.getAtrributeList().get(0);
		this.handleEvent();

	}

	public void handleEvent()
	{
		if(this.event.getEventType() == EventType.PUB)
		{   
			this.publisher = new Publisher(this.rib,this.irm, this.event); ////  if it is a PUB event, it needs to publish to its subscribers

			int num = this.event.getMemberList().size();

			if( num>=1)
			{
				for(int i = 0; i < num; i++)
				{
					String subscriber = this.event.getMemberList().get(i);//only one member in the request event
					this.publisher.addSubsciber(subscriber);
				}
			}


		}else if(this.event.getEventType() == EventType.SUB)
		{
			this.sendSubRequest(); //send sub request M_START(Subscription Event) to publisher
		}
	}



	public void addSubscriber(String subscriber)
	{
		this.publisher.addSubsciber(subscriber);
	}


	private void sendSubRequest() {

		this.publisherAddr  = Integer.parseInt(this.event.getMemberList().get(0)); // only sub to one publisher each time

		subscriptionEvent_t event_sub = this.event.getSendToPublisher(Integer.toString(this.rinaAddr) );


		CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();

		obj.setByteval(ByteString.copyFrom(event_sub.toByteArray()));

		CDAP.CDAPMessage M_CREATE = rina.message.CDAPMessageGenerator.generateM_CREATE
		(      "subscription",
				"/daf/subscription",
				obj.buildPartial(),
				99
		);


		int nextHop = this.forwardingTable.getNextHop(this.publisherAddr);

		this.log.debug("next hop of " +  this.publisherAddr + " is " +  nextHop);


		Neighbor neighbor = this.neighbors.getBeighbor(nextHop);


		if(neighbor == null)
		{
			this.log.info("Next hop does not exist for dstAddr " + this.publisherAddr );	
			return;
		}

		String dstIPCName = neighbor.getApName();

		String dstIPCInstance = neighbor.getApInstance();

		this.log.debug( "Next hop IPC info:" + dstIPCName + "/" + dstIPCInstance );

		try {

			int handleID = this.irm.allocateFlow(this.IPCName, this.IPCInstance, "Management", "1",
					dstIPCName, dstIPCInstance, "Management", "1");

			this.irm.send(handleID, M_CREATE.toByteArray());
			this.log.info( "M_CREATE(subscription: sub) sent out over handleID " + handleID);

		} catch (Exception e)
		{
			this.log.error(e.getMessage());
			System.err.println(e.getMessage());
			return;
		}
	}

	public void updateSubEvent(byte[] value)
	{

		if(this.attribute.equals("linkStateRoutingEntry"))
		{
	//		this.log.debug("linkStateRoutingEntry pub event received from " +  this.publisherAddr);

			routingEntrySet_t routingEntrySet = null;

			try {
				routingEntrySet = routingEntrySet_t.parseFrom(value);
			} catch (InvalidProtocolBufferException e) {
				this.log.error(e.getMessage());
			}

			long senderAddr = routingEntrySet.getAddr();


			if( senderAddr == this.publisherAddr) //direct routingEntry, not a forwarded one
			{

				double cost = 0.0 ;
				
				 //POLICY HOLDER : different ways to determine the link cost
				if(this.linkCostPolicy.equals("hop")) // hop count is the cost
				{
					cost = 1;
					
				}else if (this.linkCostPolicy.equals("myNewLinkCostPolicy"))
				{
					//implement new link cost policy here
				}
				else
				{

//					//this.log.info("Receive pub event(linkStateRoutingEntry) from " + this.publisherAddr +  ", determine the cost to this neighbor");
//
//					long timeSent = routingEntrySet.getTimestamp();
//
//					long timeCurrent = System.currentTimeMillis();
//
//					//double cost =  timeCurrent - timeSent;
//
//					//manually set
//
//					if(this.rinaAddr == 11 | senderAddr == 11 || this.rinaAddr == 14 || senderAddr == 14  )
//					{
//						cost = 9;
//					}
//					else
//					{
//						cost = 10;
//					}
				}

				//this.log.debug("timeSent/timeCurrent/cost: " +  timeSent + "/" +  timeCurrent + "/" + cost );

				this.linkStateRoutingInfo.addCostToNeighbor( (int)senderAddr, cost);


				if(routingEntrySet.getRoutingEntrySetCount() == 0  )
				{
					this.log.info("This is first time receiving linkStateRoutingEntry from " + senderAddr +  "and its content is empty, so no need to addRoutingEntrySet");
					return;
				}

			}

			this.linkStateRoutingInfo.addForwardedRoutingEntrySet(routingEntrySet);
			//this.log.debug("addForwardedRoutingEntrySet() is called becaused linkStateRoutingEntry update received from " + this.publisherAddr); 


			boolean updateForwaringTable = this.linkStateRoutingInfo.addRoutingEntrySet(routingEntrySet);



			if(updateForwaringTable) // information changes, update forwarding table
			{
				this.log.debug("linkStateRoutingEntry received, and updateForwaringTable is true, so update FT"); 

				this.linkStateRoutingInfo.buildForwrdingTable();
			}else
			{
				//			System.out.println("linkStateRoutingEntry received, nothing changed: " + this.linkStateRoutingInfo.getMap());
				//			System.out.println("linkStateRoutingEntry received, nothing changed: " + this.forwardingTable.getForwardingTable());

			}

		}else if(this.attribute.equals("linkStateRoutingEntryNeighborsReceived"))
		{

			try {
				routingEntrySetForwardedByNeighbor_t routingEntrySetForwardedByNeighbor 
				= routingEntrySetForwardedByNeighbor_t.parseFrom(value);

				int num = routingEntrySetForwardedByNeighbor.getRoutingEntrySetForwardedByNeighborCount();

	//			this.log.debug("linkStateRoutingEntryNeighborsReceived pub event received has "  + num + " entries inside, from "  + this.publisherAddr);

				boolean updateForwaringTable = false;

				for(int i = 0; i < num ; i++ )
				{
					//System.out.println("000000000000000000000000000000 i is " +  i );

					routingEntrySet_t routingEntrySet = routingEntrySetForwardedByNeighbor.getRoutingEntrySetForwardedByNeighbor(i);

					//this.printRoutingEntrySet(routingEntrySet);

					this.linkStateRoutingInfo.addForwardedRoutingEntrySet(routingEntrySet);

					boolean result = this.linkStateRoutingInfo.addRoutingEntrySet(routingEntrySet);

					updateForwaringTable = updateForwaringTable || result;

				}

				if(updateForwaringTable) // information changes, update forwarding table
				{
					this.log.debug("linkStateRoutingEntryNeighborsReceived received, and updateForwaringTable is true, so update FT"); 

					this.linkStateRoutingInfo.buildForwrdingTable();
				}else
				{
					//System.out.println("linkStateRoutingEntryNeighborsReceived received, nothing changed: " + this.linkStateRoutingInfo.getMap());
					//System.out.println("linkStateRoutingEntryNeighborsReceived received, nothing changed: " + this.forwardingTable.getForwardingTable());

				}


			} catch (Exception e) {
				this.log.error(e.getMessage());

				//e.printStackTrace();

				//System.out.println("There is serious error here!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
		}else if (this.attribute.equals("checkNeighborAlive")) // check if the neighbor is alive or not
		{
			// in the Publisher.java for this event, actually the event value is the timestamp when the messgae is sent


			//			this.log.debug("checkNeighborAlive pub event received from " +  this.publisherAddr);

			while(this.routingDaemon == null)
			{
				this.routingDaemon = (RoutingDaemon)this.rib.getAttribute("routingDaemon");
			}


			this.routingDaemon.updateChckNeighborTimerTask(this.publisherAddr);
			this.routingDaemon.addCheckNeighborTimerTask( this.publisherAddr);

		}

	}

	private void printRoutingEntrySet(routingEntrySet_t entrySet) {

		int n = entrySet.getRoutingEntrySetCount();

		this.log.debug("Pub event linkStateRoutingEntryNeighborsReceived: Now print entrySet: origin's addr/timestamp:" +  entrySet.getAddr() + "/" 
				+  entrySet.getTimestamp() + ", and number of the entries inside it is  " + n );


		for(int j = 0; j < n; j++)
		{
			LinkStateRoutingEntry entry = new LinkStateRoutingEntry(entrySet.getRoutingEntrySet(j));

			this.log.debug( j + " content is: " +  entry.getPrint()) ;
		}


	}

	//stop the event handler
	public void delete() {

		if(this.event.getEventType() == EventType.PUB)
		{   
			//or stop publishing event to subscriber (Publisher thread)
			this.publisher.stopPub();

		}else if(this.event.getEventType() == EventType.SUB)
		{
			//stop the subscription to a publisher (Send a M_STOP to publisher)
		}


	}


}
