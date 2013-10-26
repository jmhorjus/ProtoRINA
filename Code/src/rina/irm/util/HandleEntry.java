/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University 
 */

package rina.irm.util;	

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class HandleEntry
{
	private Log log = LogFactory.getLog(this.getClass());

	private int handleID;
	
	private String srcApName;
	private String srcApInstance;
	private String srcAeName;
	private String srcAeInstance;

	private String dstApName;
	private String dstApInstance;
	private String dstAeName;
	private String dstAeInstance;

	private String underlyingIPCName;//underlying IPC name
	private String underlyingIPCInstance; //underlying IPC 

	private int srcPortID; //underlying IPC port for this flow allocation
	private int dstPortID; 

	private int wireID; // used when the handle is mapped to a port on the wire, for DIF zero

	/**
	 * Dummy constructor
	 */
	public HandleEntry(){}

	public HandleEntry(String srcApName, String srcApInstance,String srcAeName, String srcAeInstance
			, String dstApName,String dstApInstance, String dstAeName, String dstAeInstance)
	{
		this.srcApName = srcApName;
		this.srcApInstance = srcApInstance;
		this.srcAeName = srcAeName;
		this.srcAeInstance = srcAeInstance;
		this.dstApName = dstApName;
		this.dstApInstance = dstApInstance;
		this.dstAeName = dstAeName;
		this.dstAeInstance = dstAeInstance;

	}

	public HandleEntry(String srcApName, String srcApInstance,String srcAeName, String srcAeInstance
			, String dstApName,String dstApInstance, String dstAeName, String dstAeInstance, int wireID)
	{
		this.srcApName = srcApName;
		this.srcApInstance = srcApInstance;
		this.srcAeName = srcAeName;
		this.srcAeInstance = srcAeInstance;
		this.dstApName = dstApName;
		this.dstApInstance = dstApInstance;
		this.dstAeName = dstAeName;
		this.dstAeInstance = dstAeInstance;
		this.wireID = wireID;

	}
	
	public HandleEntry(String srcApName, String srcApInstance,String srcAeName, String srcAeInstance
			, String dstApName,String dstApInstance, String dstAeName, String dstAeInstance,
			String underlyingIPCName, String underlyingIPCInstance, int handleID)
	{
		this.srcApName = srcApName;
		this.srcApInstance = srcApInstance;
		this.srcAeName = srcAeName;
		this.srcAeInstance = srcAeInstance;
		this.dstApName = dstApName;
		this.dstApInstance = dstApInstance;
		this.dstAeName = dstAeName;
		this.dstAeInstance = dstAeInstance;
		
		this.underlyingIPCName = underlyingIPCName;
		this.underlyingIPCInstance = underlyingIPCInstance;
		
		this.handleID = handleID;
	}
	
	public HandleEntry(String srcApName, String dstApName, String underlyingIPCName, String underlyingIPCInstance, int handleID)
	{
		this.srcApName = srcApName;
		this.dstApName = dstApName;
		
		this.underlyingIPCName = underlyingIPCName;
		this.underlyingIPCInstance = underlyingIPCInstance;
		
		this.handleID = handleID;
	}
	
	public HandleEntry(String srcApName, String srcAeName, String dstApName, String dstAeName,  String underlyingIPCName, String underlyingIPCInstance, int handleID)
	{
		this.srcApName = srcApName;
		this.srcAeName = srcAeName;
		this.dstApName = dstApName;
		this.dstAeName = dstAeName;
		
		this.underlyingIPCName = underlyingIPCName;
		this.underlyingIPCInstance = underlyingIPCInstance;
		
		this.handleID = handleID;
	}
	
	
	public String getKey()
	{
		String key  = null;
		
			key = this.srcApName  + this.srcApInstance + this.srcAeName +  this.srcAeInstance
			+ this.dstApName  +  this.dstApInstance  +this.dstAeName + this.dstAeInstance;
			
			return key;
	}

	public void print()
	{
		this.log.debug("print handleEntry.  SRC(srcApName/srcApInstance/srcAeName/srcAeInstance): " + this.srcApName + "/" + this.srcApInstance + "/" + 
				this.srcAeName + "/" +  this.srcAeInstance  +", DEST(dstApName/dstApInstance/dstAeName/dstAeInstance: " + this.dstApName + "/" +  this.dstApInstance +
				"/" +this.dstAeName + "/" + this.dstAeInstance + ". HandleID: " + this.handleID + ". underlying IPC info: " +
				this.underlyingIPCName + "/" + this.underlyingIPCInstance + ". srcPortId/dstPortId: " + this.srcPortID + "/" + this.dstPortID + ", wire ID " + this.wireID);
	}


	public synchronized String getSrcApName() {
		return srcApName;
	}

	public synchronized void setSrcApName(String srcApName) {
		this.srcApName = srcApName;
	}

	public synchronized String getSrcApInstance() {
		return srcApInstance;
	}

	public synchronized void setSrcApInstance(String srcApInstance) {
		this.srcApInstance = srcApInstance;
	}

	public synchronized String getSrcAeName() {
		return srcAeName;
	}

	public synchronized void setSrcAeName(String srcAeName) {
		this.srcAeName = srcAeName;
	}

	public synchronized String getSrcAeInstance() {
		return srcAeInstance;
	}

	public synchronized void setSrcAeInstance(String srcAeInstance) {
		this.srcAeInstance = srcAeInstance;
	}

	public synchronized String getDstApName() {
		return dstApName;
	}

	public synchronized void setDstApName(String dstApName) {
		this.dstApName = dstApName;
	}

	public synchronized String getDstApInstance() {
		return dstApInstance;
	}

	public synchronized void setDstApInstance(String dstApInstance) {
		this.dstApInstance = dstApInstance;
	}

	public synchronized String getDstAeName() {
		return dstAeName;
	}

	public synchronized void setDstAeName(String dstAeName) {
		this.dstAeName = dstAeName;
	}

	public synchronized String getDstAeInstance() {
		return dstAeInstance;
	}

	public synchronized void setDstAeInstance(String dstAeInstance) {
		this.dstAeInstance = dstAeInstance;
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


	public synchronized int getWireID() {
		return wireID;
	}

	public synchronized void setWireID(int wireID) {
		this.wireID = wireID;
	}

	public synchronized int getSrcPortID() {
		return srcPortID;
	}

	public synchronized void setSrcPortID(int srcPortID) {
		this.srcPortID = srcPortID;
	}

	public synchronized int getDstPortID() {
		return dstPortID;
	}

	public synchronized void setDstPortID(int dstPortID) {
		this.dstPortID = dstPortID;
	}

	public synchronized int getHandleID() {
		return handleID;
	}

	public synchronized void setHandleID(int handleID) {
		this.handleID = handleID;
	}

}