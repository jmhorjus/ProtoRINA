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

import rina.object.gpb.SubscriptionEvent_t.eventType_t;
import rina.object.gpb.SubscriptionEvent_t.subscriptionEvent_t;
import rina.object.internal.SubscriptionEvent;
import rina.object.internal.SubscriptionEvent.EventType;
import rina.rib.impl.RIBImpl;

/**
 * This stores all the information about the subscription event created
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class SubscriptionEvents {

	private Log log = LogFactory.getLog(RIBImpl.class);

	private int IDRange = 100000;


	// The subscription events and their handler
	private LinkedHashMap<Integer,SubscriptionEvent> events = null;
	//for searching purpose only, bacially this is the value set of the above attributes
	private LinkedList<SubscriptionEvent> eventsList = null;

	private LinkedHashMap<String, Integer> pubContentToID = null;
	private LinkedHashMap<String, Integer> subContentToID = null;


	public SubscriptionEvents()
	{
		this.events = new LinkedHashMap<Integer,SubscriptionEvent>();
		this.eventsList = new LinkedList<SubscriptionEvent>();
		this.pubContentToID = new LinkedHashMap<String, Integer>();
		this.subContentToID =  new LinkedHashMap<String, Integer>();
	}


	public int addEvent(SubscriptionEvent subscriptionEvent)
	{

		int id = this.generateSubscriptionEventID();

		String attribute = subscriptionEvent.getAtrributeList().get(0);//assume one attribute
		double updatePeriod = subscriptionEvent.getUpdatePeriod();

		subscriptionEvent.setSubscriptionID(id);

		this.events.put(id, subscriptionEvent);

		this.eventsList.add(subscriptionEvent);


		
		
		if(subscriptionEvent.getEventType() == EventType.PUB)
		{//the following is used for searching purpose
			String key = attribute + updatePeriod;
			this.pubContentToID.put(key, id);
		}else if (subscriptionEvent.getEventType() == EventType.SUB)
		{
			//the following is used for searching purpose
			
			String publisher = subscriptionEvent.getMemberList().get(0);
			String key = publisher + attribute + updatePeriod;
			this.subContentToID.put(key, id);
		}

		return id;

	}


	public void removeEvent(int eventID)
	{
		if(!this.events.containsKey(eventID))
		{
			this.log.debug("Event with id " +  eventID  + "  does not exist");
			return;
		}
		
		SubscriptionEvent subscriptionEvent = this.events.get(eventID);

		this.events.remove(eventID);

		this.eventsList.remove(subscriptionEvent);
		
		String member = subscriptionEvent.getMemberList().get(0);
		
		String attribute = subscriptionEvent.getAtrributeList().get(0);//assume one attribute
		
		double updatePeriod = subscriptionEvent.getUpdatePeriod();
		
	
		
		if(subscriptionEvent.getEventType() == EventType.PUB)
		{
			String key = attribute + updatePeriod; 
			this.pubContentToID.remove(key);
			
		}else if (subscriptionEvent.getEventType() == EventType.SUB)
		{
			String key = member + attribute + updatePeriod; // for sub event, memberlist is the publisher's name 
			this.subContentToID.remove(key);
		}
		
		this.log.debug("Event with id " +  eventID + " removed");
			
	}



	public boolean contains(int eventID)
	{
		return this.events.containsKey(eventID);
	}


	private synchronized int generateSubscriptionEventID()
	{
		int id = -1;

		id = (int)( Math.random()* this.IDRange); 

		while(this.events.containsKey(id))
		{
			id = (int)( Math.random()* this.IDRange); 
		}

		
		this.log.debug("Subscription ID generated is " +  id );
		this.events.put(id, null);
		
		return id;
	}


	public SubscriptionEvent getEvent(int ID)
	{
		return this.events.get(ID);
	}
	
	
	public Object getSubEventValue(int ID)
	{
		return this.events.get(ID).getSubValue();
	}

	public void setPubEventValue(int ID, byte[] value)
	{
		this.events.get(ID).setPubValue(value);
	}
	
	
	/**
	 * return the id given an event
	 * return -1, if such event does not exists
	 * @param subscriptionEvent
	 * @return
	 */
	public int getEventID(SubscriptionEvent subscriptionEvent) {

		int id = -1;

		
		String attribute = subscriptionEvent.getAtrributeList().get(0);
		double updatePeriod = subscriptionEvent.getUpdatePeriod();

		if(subscriptionEvent.getEventType() == EventType.SUB)
		{
			String member = subscriptionEvent.getMemberList().get(0); 
			id = this.getSubEventID(member, attribute, updatePeriod);
			
		}else if (subscriptionEvent.getEventType() == EventType.PUB)
		{
			id = this.getPubEventID(attribute, updatePeriod);
		}

		return id;
	}

	/**
	 * Note: this param is an subscriptionEvent_t type which is a GPB type
	 * @param subscriptionEvent
	 * @return
	 */
	public int getEventID(subscriptionEvent_t subscriptionEvent) {

		int id = -1;
		
		String attribute = subscriptionEvent.getAttributeList(0);
		double updatePeriod = subscriptionEvent.getUpdatePeriod();

		if(subscriptionEvent.getEventType() == eventType_t.SUB)
		{
			String member = subscriptionEvent.getMemberList(0); 
			id = this.getSubEventID(member,attribute, updatePeriod); 
			
		}else if (subscriptionEvent.getEventType() == eventType_t.PUB)
		{
	
			id = this.getPubEventID(attribute, updatePeriod); 			
		}

		return id;
	}
	
	/**
	 * Note: this param is an subscriptionEvent_t type which is a GPB type
	 * @param subscriptionEvent
	 * @return
	 */
	public int getCorrespodingEventID(subscriptionEvent_t subscriptionEvent) {

		int id = -1;

	
		String attribute = subscriptionEvent.getAttributeList(0);
		double updatePeriod = subscriptionEvent.getUpdatePeriod();

		if(subscriptionEvent.getEventType() == eventType_t.SUB)
		{
			id = this.getPubEventID(attribute, updatePeriod); // check if there is such local pub event to sever the sub request
			
		}else if (subscriptionEvent.getEventType() == eventType_t.PUB)
		{
			String member = subscriptionEvent.getMemberList(0); 
			
			id = this.getSubEventID(member,attribute, updatePeriod); // check if there is such local sub event request such pub event
			
		}

		return id;
	}
	
	
	/**
	 * get the event id of a sub event, and the searching key is publisher + attribute + updatePeriod
	 * @param subscriptionEvent
	 * @return
	 */
	private int getSubEventID(String publisher, String attribute, double updatePeriod) {
		
		int id = -1;

		String content = publisher + attribute +  updatePeriod;
		
//		System.out.println("ssssssssssssssssssssssss sub event is " +  content);
		
		if(this.subContentToID.containsKey(content))
		{
			id = this.subContentToID.get(content);
		}

		return id;
	}


	/**
	 * get the event id of a pub event, and the searching key is attribute + updatePeriod
	 * @param subscriptionEvent
	 * @return
	 */
	private int getPubEventID(String attribute, double updatePeriod ) {

		int id = -1;

		String content = attribute +  updatePeriod;
		
//		System.out.println("ppppppppppppppppppppp pub content is " +  content);

		if(this.pubContentToID.containsKey(content))
		{
			id = this.pubContentToID.get(content);
		}

		return id;
	}






}
