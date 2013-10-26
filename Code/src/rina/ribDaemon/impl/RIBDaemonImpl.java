
/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.ribDaemon.impl;

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import application.component.api.IPCResourceManager;

import rina.irm.impl.IRMImpl;
import rina.object.gpb.SubscriptionEvent_t.eventType_t;
import rina.object.gpb.SubscriptionEvent_t.subscriptionEvent_t;
import rina.object.internal.SubscriptionEvent;
import rina.object.internal.SubscriptionEvent.EventType;
import rina.rib.impl.RIBImpl;
import rina.ribDaemon.api.RIBDaemonAPI;
import rina.ribDaemon.util.EventHandler;
import rina.ribDaemon.util.SubscriptionEvents;


/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class RIBDaemonImpl implements RIBDaemonAPI{

	private Log log = LogFactory.getLog(this.getClass());

	private RIBImpl rib = null;
	private IPCResourceManager irm = null;


	// this stores all the events created
	private SubscriptionEvents subscriptionEvents = null;

	//all events handler 
	private LinkedHashMap<Integer, EventHandler> eventHandlers = null;


	public RIBDaemonImpl(RIBImpl rib,IPCResourceManager irm )
	{
		this.rib = rib;
		this.irm = irm;
		this.subscriptionEvents = new SubscriptionEvents();
		this.eventHandlers = new  LinkedHashMap<Integer, EventHandler>();

	}


	/**
	 * return the event id
	 * This is called locally
	 */
	public  int createEvent(SubscriptionEvent subscriptionEvent) 
	{
//		this.log.debug("createEvent() is called");
		subscriptionEvent.print();


		//if the id returned is -1, means no such event exists before
		int id = this.subscriptionEvents.getEventID(subscriptionEvent);

		if( id == -1) // this event does not exist before, create a new one
		{
			id  = this.subscriptionEvents.addEvent(subscriptionEvent);
			this.eventHandlers.put(id, new EventHandler(this.rib, this.irm, subscriptionEvent));
		}
		

		return id;
	}


	public  void handleReceivedSubscription(subscriptionEvent_t subscriptionEvent)
	{
	//	this.log.debug("Subscription event received with event type: " + subscriptionEvent.getEventType().toString());
		
		if(subscriptionEvent.getEventType() == eventType_t.PUB)
		{
			this.handleReceivedPubEventRequest(subscriptionEvent);
			
		}else if(subscriptionEvent.getEventType() == eventType_t.SUB)
		{
			this.handleReceivedSubEventRequest(subscriptionEvent);

		}else
		{
			this.log.error("Unknown subscription event type received");
		}

	}

	/**
	 * This is called when a subscriptionEvent (SUB) object is received
	 * @param subscriptionEvent
	 */
	private  void handleReceivedSubEventRequest(subscriptionEvent_t subscriptionEvent)
	{
		int id = this.subscriptionEvents.getCorrespodingEventID(subscriptionEvent);

		if( id != -1) // existing such pub event to serve the sub 
		{

			String subscriber = subscriptionEvent.getMemberList(0); //assume only one subscriber in the event object
			         
			this.eventHandlers.get(id).addSubscriber(subscriber);

			this.log.debug("The request sub event found corresponging pub evnet on this process with id "  +  id + ", " + subscriber +  " added to its subscriber list, for content:"
					+ subscriptionEvent.getAttributeList(0));

		}else
		{
			this.log.error("The request sub event does not have a corresponding pub event on this process");
		}
	}

	/**
	 * This is called when a subscriptionEvent (PUB) object is received
	 * @param subscriptionEvent_t
	 */
	private  void handleReceivedPubEventRequest(subscriptionEvent_t  subscriptionEvent)
	{

		int id = this.subscriptionEvents.getCorrespodingEventID(subscriptionEvent);

		if( id != -1) // existing such sub event to accept the pub  content
		{
	//		this.log.debug("The pub event received updates corresponding sub event on this process");

			this.eventHandlers.get(id).updateSubEvent(subscriptionEvent.getValue().toByteArray());

		}else
		{
			this.log.error("The pub event does not have a corresponding sub event on this process, pub content discarded");
		}


	}



	public  void deleteEvent(int subscriptionID) 
	{
		if(!this.subscriptionEvents.contains(subscriptionID))
		{
			this.log.debug("subscriptionID: " + subscriptionID + " does not exist");
			return;
		}

		this.subscriptionEvents.removeEvent(subscriptionID);

		EventHandler eventHandler = this.eventHandlers.get(subscriptionID);

		eventHandler.delete();
	}


	public  Object readSub(int subID) {

		if(this.subscriptionEvents.contains(subID))
		{
			return this.subscriptionEvents.getSubEventValue(subID);
		}else 
		{
			this.log.error("Subscription(SUB) event with id:" +  subID + " does not exist." );
			return null;
		}
	}


	public  void writePub(int pubID, byte[] obj) {

		if(this.subscriptionEvents.contains(pubID))
		{
			this.subscriptionEvents.setPubEventValue(pubID, obj);
		}else
		{
			this.log.error("Subscription(PUB) event with id:" +  pubID + " does not exist." );
		}
	}





}
