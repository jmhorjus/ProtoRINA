/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */

package rina.object.internal;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.object.gpb.Flow_t.flow_t;

/**
 * This corresponds with the Flow in rina.object.gpb
 * Just for internal use
 * @author Yuefeng Wang. Computer Science Department, Boston University 
 * 
 * 
 * Flow object:
 * required rina.messages.applicationProcessNamingInfo_t sourceNamingInfo = 1;  			//The naming information of the source application process
	required rina.messages.applicationProcessNamingInfo_t destinationNamingInfo = 2; 		//The naming information of the destination application process
	required uint64 sourcePortId = 3; 										//The port id allocated to this flow by the source IPC process
	optional uint64 destinationPortId = 4; 									//The port id allocated to this flow by the destination IPC process
	required uint64 sourceAddress = 5; 										//The address of the source IPC process for this flow
	optional uint64 destinationAddress = 6; 									//The address of the destination IPC process for this flow
	repeated connectionId_t connectionIds = 7; 								//The identifiers of all the connections associated to this flow
	optional uint32 currentConnectionIdIndex = 8;								//Identifies the index of the current active connection in the flow
	optional uint32 state = 9; 												//
	optional rina.messages.qosSpecification_t qosParameters = 10; 			//the QoS parameters specified by the application process that requested this flow
	repeated property_t policies = 11; 											//the set of policies selected by the IPC process
	repeated property_t policyParemeters = 12; 								//the set of parameters associated to the policies
	optional bytes accessControl = 13; 										// ?
	optional uint32 maxCreateFlowRetries = 14; 								//Maximum number of retries to create the flow before giving up
	optional uint32 createFlowRetries = 15; 								//The current number of retries
	optional uint32 hopCount = 16; 
 *
 */

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Flow {


	private Log log = LogFactory.getLog(this.getClass());

	// Data Transfer AE handle
	// and this is set when the first msg is sent over this handle
	// in the flowAllocatorImple' send method.
	private int dataTransferHandleID = -1;
	
	private String underlyingIPCName = null;
	private String underlyingIPCInstance  = null;

	private ApplicationProcessNamingInfo srcApInfo;
	private ApplicationProcessNamingInfo dstApInfo;

	private long srcPortID = -1;
	private long dstPortID = -1;
	private long srcAddr = -1;
	private long dstAddr = -1;
	

	private LinkedList<ConnectionID> connectionIDs = null;

	//Fow now a lot of other attribtues are ignored

	public Flow(){}


	public Flow(ApplicationProcessNamingInfo srcApInfo, ApplicationProcessNamingInfo dstApInfo)
	{
		this.srcApInfo = srcApInfo;
		this.dstApInfo = dstApInfo;
		this.connectionIDs = new LinkedList<ConnectionID>();
	}

	/**
	 * Only ApName is used
	 * This is used when two ends are applications
	 * @param srcApName
	 * @param dstApName
	 */
	public Flow(String srcApName, String dstApName)
	{
		this.srcApInfo = new ApplicationProcessNamingInfo(srcApName);
		this.dstApInfo = new ApplicationProcessNamingInfo(dstApName);
		this.connectionIDs = new LinkedList<ConnectionID>();
	}
	
	

	/**
	 * All four attributed of ApInfo are used.
	 * Most case, this is used when IPC AE (Management and Data Transfer) talks to each other
	 * @param srcApName
	 * @param srcApInstance
	 * @param srcAeName
	 * @param srcAeInstance
	 * @param dstApName
	 * @param dstApInstance
	 * @param dstAeName
	 * @param dstAeInstance
	 */
	public Flow(String srcApName, String srcApInstance, String srcAeName, String srcAeInstance, 
			String dstApName,String dstApInstance, String dstAeName, String dstAeInstance)
	{
		this.srcApInfo = new ApplicationProcessNamingInfo(srcApName, srcApInstance, srcAeName, srcAeInstance);
		this.dstApInfo = new ApplicationProcessNamingInfo( dstApName, dstApInstance,  dstAeName,  dstAeInstance);

		this.connectionIDs = new LinkedList<ConnectionID>();
	}
	
	public Flow(String srcApName, String srcApInstance, String dstApName,String dstApInstance)
	{
		this.srcApInfo = new ApplicationProcessNamingInfo(srcApName, srcApInstance);
		this.dstApInfo = new ApplicationProcessNamingInfo( dstApName, dstApInstance);

		this.connectionIDs = new LinkedList<ConnectionID>();
	}

	public Flow(ApplicationProcessNamingInfo srcApInfo, ApplicationProcessNamingInfo dstApInfo,ConnectionID connectionID )
	{
		this.srcApInfo = srcApInfo;
		this.dstApInfo = dstApInfo;
		this.connectionIDs = new LinkedList<ConnectionID>();
		this.connectionIDs.add(connectionID);
	}

	public synchronized void  print()
	{
		this.log.debug("Print Flow object(apName/apInstance/aeName/aeInstance/addr/portID). SRC: " +  this.srcApInfo.getInfo() + "/" + this.srcAddr + "/" +  this.srcPortID + ". DEST:" + this.dstApInfo.getInfo()
				+ "/" + this.dstAddr + "/" + this.dstPortID );
		
	}
	
	public synchronized String getPrint()
	{
		String content = "(apName/apInstance/aeName/aeInstance/addr/portID). SRC: " +  this.srcApInfo.getInfo() + "/" + this.srcAddr + "/" +  this.srcPortID + ". DEST:" + this.dstApInfo.getInfo()
				+ "/" + this.dstAddr + "/" + this.dstPortID;
		
		return content;
	}

	public synchronized flow_t convert()
	{
		flow_t.Builder flow = flow_t.newBuilder();
		
		flow.setDestinationNamingInfo(this.dstApInfo.convert());
		flow.setSourceNamingInfo(this.srcApInfo.convert());
		flow.setSourcePortId(this.srcPortID);
		flow.setDestinationPortId(this.dstPortID); 
		flow.setSourceAddress(this.srcAddr); 
		flow.setDestinationAddress(this.dstAddr);
		
		return flow.buildPartial();
	}
	
	
	public synchronized ApplicationProcessNamingInfo getSrcApInfo() {
		return srcApInfo;
	}

	public synchronized void setSrcApInfo(ApplicationProcessNamingInfo srcApInfo) {
		this.srcApInfo = srcApInfo;
	}

	public synchronized ApplicationProcessNamingInfo getDstApInfo() {
		return dstApInfo;
	}

	public synchronized void setDstApInfo(ApplicationProcessNamingInfo dstApInfo) {
		this.dstApInfo = dstApInfo;
	}

	public synchronized long getSrcPortID() {
		return srcPortID;
	}

	public synchronized void setSrcPortID(long srcPortID) {
		this.srcPortID = srcPortID;
	}

	public synchronized long getDstPortID() {
		return dstPortID;
	}

	public synchronized void setDstPortID(long dstPortID) {
		this.dstPortID = dstPortID;
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

	public synchronized LinkedList<ConnectionID> getConnectionIDs() {
		return connectionIDs;
	}

	public synchronized void setConnectionIDs(LinkedList<ConnectionID> connectionIDs) {
		this.connectionIDs = connectionIDs;
	}



	public synchronized int getDataTransferHandleID() {
		return dataTransferHandleID;
	}



	public synchronized void setDataTransferHandleID(int dataTransferHandleID) {
		this.dataTransferHandleID = dataTransferHandleID;
	}


	public synchronized String getUnderlyingIPCName() {
		return underlyingIPCName;
	}


	public synchronized void setUnderlyingIPCName(String underlyingIPCName) {
		this.underlyingIPCName = underlyingIPCName;
	}


	public synchronized String getUnderlyingIPCInstance() {
		return underlyingIPCInstance;
	}


	public synchronized void setUnderlyingIPCInstance(String underlyingIPCInstance) {
		this.underlyingIPCInstance = underlyingIPCInstance;
	}

}
