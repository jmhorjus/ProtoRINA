/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.tcp.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import rina.irm.util.HandleEntry;
import rina.rib.impl.RIBImpl;
import rina.tcp.TCPFlow;
import rina.util.MessageQueue;


/**
 * This is to store all TCP flows allocated
 * @author Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0
 */


public class TCPFlowAllocated {


	private Log log = LogFactory.getLog(TCPFlowAllocated.class);
	
	private RIBImpl rib = null;

	private LinkedHashMap<Integer, TCPFlow> tcpFlowAllocated = null; 
	private LinkedHashMap<Integer, MessageQueue> tcpFlowMsgQueue = null;

	private int flowIDRange = 10000;

	//between two applications there is only one wire
	// the key "name" = src application name + dst application name  (BU wire case)
	// 				  Or src application name + src entity name + dst application name + dst entity name (RINA community)

	private LinkedHashMap<String, Integer> nameToFlowID = null;


	//Only used for RINA community case
	//the following attributes are not used for BU case
	private String ApName = null;
	private String ApInstance = "1"; // by default

	//BU case use this
	private LinkedHashMap<Integer, WireListenerTCP> wireListeners = null;

	//RINA community case use this 
	private WellKnownRINAAddr wellKnownRINAAddr = null;


	/**
	 * BU case
	 */
	public TCPFlowAllocated(RIBImpl rib)
	{

		this.tcpFlowAllocated = new LinkedHashMap<Integer, TCPFlow> ();
		this.tcpFlowMsgQueue = new LinkedHashMap<Integer, MessageQueue> ();
		this.nameToFlowID = new  LinkedHashMap<String,Integer> ();
		this.rib = rib;
		this.wireListeners = new LinkedHashMap<Integer, WireListenerTCP>();
		

	}

	/**
	 * This is used when talking with RINA community
	 * @param ApName
	 * @param ApInstance
	 * @param AeName
	 */
	public TCPFlowAllocated(String ApName, String ApInstance)
	{

		this.tcpFlowAllocated = new LinkedHashMap<Integer, TCPFlow> ();
		this.tcpFlowMsgQueue = new LinkedHashMap<Integer, MessageQueue> ();
		this.nameToFlowID = new  LinkedHashMap<String,Integer> ();

		this.ApName = ApName;

		if(ApInstance!=null)
		{
			this.ApInstance = ApInstance;
		}


		this.wellKnownRINAAddr = new WellKnownRINAAddr(ApName, ApInstance);

	}



	/**
	 * Name is the concatenation of srcName + dstName
	 * @param name
	 * @param tcpFlow
	 * @return
	 */
	public synchronized  int addTCPFlow(String name, TCPFlow tcpFlow)
	{


		int flowID = this.generateFlowId();

		this.nameToFlowID.put(name, flowID);

		tcpFlow.setFlowID(flowID);

		this.tcpFlowAllocated.put(flowID, tcpFlow);

		this.tcpFlowMsgQueue.put(flowID, tcpFlow.getMsgQueue());

		this.log.info(" TCP flow to " + name + " added  with flowID " + flowID);

		return flowID;

	}

	public synchronized void removeTCPFlow(int flowID)
	{

		TCPFlow flow = this.tcpFlowAllocated.get(flowID);
		
		String src = flow.getSrcName();
		String dst = flow.getDstName();

		this.nameToFlowID.remove(src+dst);

		flow.close();

		this.tcpFlowAllocated.remove(flowID);
		this.tcpFlowMsgQueue.remove(flowID);

		this.log.info(" TCP flow " + flowID + "removed because of the TCP flow creation failed");


	}

	/**
	 * This will return the first flow allocated between two applications or application entities
	 * since there might be multiple flows
	 * @param name
	 * @return
	 */
	public synchronized TCPFlow getTCPFlow(String name)
	{
		if( this.nameToFlowID.containsKey(name) )
		{
			return this.tcpFlowAllocated.get(this.nameToFlowID.get(name));
		}else
		{
			return null;
		}

	}

	/**
	 * This will return the first flow id allocated between two applications or application entities
	 * since there might be multiple flows
	 * @param name
	 * @return
	 */
	public synchronized int  getTCPFlowID(String name)
	{
		if( this.nameToFlowID.containsKey(name) )
		{
			return this.nameToFlowID.get(name);
		}else
		{
			return -1;
		}

	}

	public synchronized TCPFlow getTCPFlow(int flowID)
	{	
		if( this.tcpFlowAllocated.containsKey(flowID) )
		{
			return this.tcpFlowAllocated.get(flowID);
		}else
		{
			return null;
		}

	}


	public synchronized boolean hasTCPFlow(String name)
	{
		return this.nameToFlowID.containsKey(name);
	}



	public synchronized int generateFlowId()
	{
		int flowID = -1;

		flowID = (int)( Math.random()* (double)flowIDRange); 

		while(this.tcpFlowAllocated.containsKey(flowID) )
		{
			flowID = (int)( Math.random()* (double)flowIDRange); 
		}

		return flowID;
	}


	public synchronized void addWireListener(int wireID, WireListenerTCP wireListener)
	{

		this.wireListeners.put(wireID, wireListener);

		this.log.info("WireListener attached for TCP flow(wire): " +  wireID);
	}

	public synchronized void removeWireListener(int wireID)
	{
		this.wireListeners.get(wireID).close();
		this.wireListeners.remove(wireID);
		this.log.info("WireListener removed for TCP flow(wire): " +  wireID);
	}


	public synchronized int  addDIF0FlowOnWire(int wireID, HandleEntry he)
	{
		return this.wireListeners.get(wireID).addDIF0Flow(he);
	}
	
	public synchronized void removeDIF0FlowOnWire(int wireID, HandleEntry he) {
		
		this.wireListeners.get(wireID).removeDIF0Flow(he.getSrcPortID());
		
	}


	public  synchronized void send(int flowID, byte[] msg) throws Exception {

		this.tcpFlowAllocated.get(flowID).send(msg);

	}

	public byte[] receive(int wireID, int portID) {

		return this.wireListeners.get(wireID).receive(portID);

	}
	

	public byte[] receive(int flowID) {

		return this.tcpFlowMsgQueue.get(flowID).getReceive();

	}
	


	public synchronized LinkedHashMap<Integer, MessageQueue> getTcpFlowMsgQueue() {
		return tcpFlowMsgQueue;
	}

	public synchronized void setTcpFlowMsgQueue(
			LinkedHashMap<Integer, MessageQueue> tcpFlowMsgQueue) {
		this.tcpFlowMsgQueue = tcpFlowMsgQueue;
	}



	public synchronized String getApName() {
		return ApName;
	}

	public synchronized void setApName(String apName) {
		ApName = apName;
	}

	public synchronized String getApInstance() {
		return ApInstance;
	}

	public synchronized void setApInstance(String apInstance) {
		ApInstance = apInstance;
	}



	public synchronized WellKnownRINAAddr getWellKnownRINAAddr() {
		return wellKnownRINAAddr;
	}

	public synchronized void setWellKnownRINAAddr(
			WellKnownRINAAddr wellKnownRINAAddr) {
		this.wellKnownRINAAddr = wellKnownRINAAddr;
	}

	public synchronized RIBImpl getRib() {
		return rib;
	}



}
