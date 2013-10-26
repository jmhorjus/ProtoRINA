
/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */

package rina.object.internal;

import rina.object.gpb.ApplicationProcessNamingInfoMessage_t.applicationProcessNamingInfo_t;


/**
 * This corresponds with the ApplicationProcessNamingInfo in rina.object.gpb
 * Just for internal use
 * @author Yuefeng Wang. Computer Science Department, Boston University 
 *
 */
public class ApplicationProcessNamingInfo {
	
	
	
	private String apName = "";
	private String apInstance = "";
	private String aeName = "";
	private String aeInstance = "";
	
	public ApplicationProcessNamingInfo(){}
	
	public ApplicationProcessNamingInfo(String apName, String apInstance, String aeName, String aeInstance)
	{
		this.apName = apName;
		this.apInstance = apInstance;
		this.aeName = aeName;
		this.aeInstance = aeInstance;
	}
	
	public ApplicationProcessNamingInfo(String apName, String apInstance)
	{
		this.apName = apName;
		this.apInstance = apInstance;
	}

	public ApplicationProcessNamingInfo(String apName)
	{
		this.apName = apName;
	}
	
	public ApplicationProcessNamingInfo (applicationProcessNamingInfo_t appInfo)
	{
		this.apName = appInfo.getApplicationProcessName();
		this.apInstance  = appInfo.getApplicationProcessInstance();
		this.aeName = appInfo.getApplicationEntityName();
		this.aeInstance = appInfo.getApplicationEntityInstance();
	}
	
	
	
	public applicationProcessNamingInfo_t convert()
	{
		applicationProcessNamingInfo_t.Builder applicationProcessNamingInfo = applicationProcessNamingInfo_t.newBuilder();
		
		applicationProcessNamingInfo.setApplicationProcessName(this.apName);
		applicationProcessNamingInfo.setApplicationProcessInstance(this.apInstance);
		applicationProcessNamingInfo.setApplicationEntityName(this.aeName);
		applicationProcessNamingInfo.setApplicationEntityInstance(this.aeInstance);
		
		return applicationProcessNamingInfo.buildPartial();
	}

	public synchronized String getInfo()
	{
		String all = this.apName + "/" + this.apInstance + "/" + this.aeName + "/" + this.aeInstance;
		return all;
	}
	
	public synchronized String getApName() {
		return apName;
	}

	public synchronized void setApName(String apName) {
		this.apName = apName;
	}

	public synchronized String getApInstance() {
		return apInstance;
	}

	public synchronized void setApInstance(String apInstance) {
		this.apInstance = apInstance;
	}

	public synchronized String getAeName() {
		return aeName;
	}

	public synchronized void setAeName(String aeName) {
		this.aeName = aeName;
	}

	public synchronized String getAeInstance() {
		return aeInstance;
	}

	public synchronized void setAeInstance(String aeInstance) {
		this.aeInstance = aeInstance;
	}
	
	public String getPrint()
	{
		String content = "[apName/apInstance/aeName/aeInstance]:" + this.apName + "/" + this.apInstance + "/" + this.aeName + "/" +  this.aeInstance;
		return content;
	}
	
	

}
