
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
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import application.component.api.IPCResourceManager;

import com.google.protobuf.ByteString;

import rina.irm.impl.IRMImpl;
import rina.message.CDAP;
import rina.object.gpb.LinkStateRoutingEntrySet_t.routingEntrySetForwardedByNeighbor_t;
import rina.object.gpb.LinkStateRoutingEntrySet_t.routingEntrySet_t;
import rina.object.gpb.SubscriptionEvent_t.subscriptionEvent_t;
import rina.object.internal.ForwardingTable;
import rina.object.internal.Neighbor;
import rina.object.internal.Neighbors;
import rina.object.internal.SubscriptionEvent;
import rina.rib.impl.RIBImpl;
import rina.routing.util.LinkStateRoutingInfo;


/**
 * For now each publisher publishes only one attributes
 * Later we can optimized this
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Publisher extends Thread{

	private Log log = LogFactory.getLog(this.getClass());

	private RIBImpl rib = null;
	private IPCResourceManager irm = null;

	private String attribute = null;
	private LinkedList<String> subscriberList = null; // remember for IPC process, this is an Integer
	private double updatePeriod;

	private SubscriptionEvent event = null;

	private String IPCName = null;
	private String IPCInstance = null;

	private ForwardingTable forwardingTable = null;  

	private LinkStateRoutingInfo linkStateRoutingInfo = null;

	private Neighbors neighbors = null;

	private int rinaAddr = -1;

	private boolean stop = false;



	public Publisher(RIBImpl rib, IPCResourceManager irm,SubscriptionEvent event) 
	{
		this.rib = rib;
		this.irm = irm;
		this.event = event;
		this.attribute = this.event.getAtrributeList().get(0);
		this.updatePeriod = this.event.getUpdatePeriod();
		this.subscriberList = this.event.getMemberList();

		this.IPCName = (String) this.rib.getAttribute("ipcName");
		this.IPCInstance = (String)this.rib.getAttribute("ipcInstance");

		this.neighbors = (Neighbors)this.rib.getAttribute("neighbors");

		this.forwardingTable = (ForwardingTable) this.rib.getAttribute("forwardingTable");

		this.linkStateRoutingInfo = (LinkStateRoutingInfo) this.rib.getAttribute("linkStateRoutingInfo");

		this.rinaAddr = (Integer) this.rib.getAttribute("rinaAddr");

		this.start();
	}

	public void run()
	{
		this.log.info("Publiser started with event id " +  this.event.getSubscriptionID());

		String rinaAddrString = Integer.toString(this.rinaAddr);

		long sleepTime = Math.round(this.updatePeriod * 1000);

		//	this.log.debug("sleepTime in the publisher is " +  sleepTime);

		//int counter = 0;

		while(!stop)
		{
			//update every one in the subscriberList some time

			// it contains the publisher content in its value field
			//this will be encapusluated in the CDAP M_CREATE objvalue

			//	counter++;
			//		this.log.debug("Publisher publishes for the " +  counter + " times with frequency " +  this.updatePeriod + ", for content "
			//		+ this.attribute );

			this.updatePubValue();

			subscriptionEvent_t event_pub = this.event.getSendToSubscribers(rinaAddrString);

			if(event_pub == null)
			{
				this.log.error("Publisher stopped due to empty content.");
				this.stopPub();
				continue;
			}

			CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();

			obj.setByteval(ByteString.copyFrom(event_pub.toByteArray()));

			CDAP.CDAPMessage M_CREATE = rina.message.CDAPMessageGenerator.generateM_CREATE
			(      "subscription",
					"/daf/subscription",
					obj.buildPartial(),
					99
			);

			//	this.log.debug(" this.subscriberList.size():" +  this.subscriberList.size());

			for(int i = 0; i < this.subscriberList.size(); i++)
			{
				// NOTE: cast the subscriber type to int, as in IPC addr is an integer type not String 
				int subscriberAddr = Integer.parseInt( this.subscriberList.get(i) ) ; 

				//	this.log.debug("ssssssssssssssssssssssssubscriberAddr is " + subscriberAddr);

				int nextHop = this.forwardingTable.getNextHop(subscriberAddr);

				if( nextHop == -1)
				{

					//TESTME
					this.subscriberList.remove(Integer.toString(subscriberAddr));

					this.log.info("Next hop does not exist for dstAddr " + subscriberAddr + ", thus remove it from the subscriberList" );

					continue;
				}

				//				this.log.debug("next hop of " +  subscriberAddr + " is " +  nextHop);


				Neighbor neighbor = this.neighbors.getBeighbor(nextHop);


				if(neighbor == null)
				{


					//TESTME

					this.subscriberList.remove(Integer.toString(subscriberAddr));

					this.log.info("Next hop does not exist for dstAddr " + subscriberAddr + ", thus remove it from the subscriberList" );

					continue;
				}

				String dstIPCName = neighbor.getApName();

				String dstIPCInstance = neighbor.getApInstance();

				//				this.log.debug( "Next hop IPC info:" + dstIPCName + "/" + dstIPCInstance );

				int handleID = this.irm.allocateFlow(this.IPCName, this.IPCInstance, "Management", "1",
						dstIPCName, dstIPCInstance, "Management", "1");

				try {
					this.irm.send(handleID, M_CREATE.toByteArray());
					//			this.log.info( "  M_CREATE(subscription: pb) sent out over handleID " + handleID);

				} catch (Exception e)
				{
					this.log.error(e.getMessage());
					continue;
				}

			}

			try {

				Thread.sleep( sleepTime);
			} catch (InterruptedException e) {
				this.log.error(e.getMessage());
			}

		}
	}



	private void updatePubValue() {

		byte[] value = null;

		//Note: value may be set to null, if nothing exists

		if(this.attribute.equals("linkStateRoutingEntry"))
		{
			routingEntrySet_t routingEntrySet = this.linkStateRoutingInfo.getRoutingEntrySet();

			value = routingEntrySet.toByteArray();

		}else if(this.attribute.equals("linkStateRoutingEntryNeighborsReceived"))
		{

			routingEntrySetForwardedByNeighbor_t routingEntrySetForwarded = this.linkStateRoutingInfo.getRoutingEntrySetForwardToNeighbor();

			value = routingEntrySetForwarded.toByteArray();

		}else if (this.attribute.equals("checkNeighborAlive"))
		{
			value = Long.toString(System.currentTimeMillis()).getBytes(); // send the current time out 
		}

		this.event.setPubValue(value);
	}

	public synchronized void addSubsciber(String subscribeAddr)
	{
		if(!this.subscriberList.contains(subscribeAddr))
		{
			this.subscriberList.add(subscribeAddr);
		}
	}

	public synchronized void removeSubsciber(String subscribeAddr)
	{
		this.subscriberList.remove(subscribeAddr);
	}

	/**
	 * Stop the publisher
	 */
	public void stopPub() {

		this.stop = true;

	}



}
