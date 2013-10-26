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


import rina.object.gpb.IDDMessage_t.iddMessage_t;
import rina.object.gpb.IDDMessage_t.iddResponse_t;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 * This is IDD Record stored in the RIB 
 */
public class IDDRecord {

	private Log log = LogFactory.getLog(this.getClass());


	public enum Type { DIF, APP };

	private Type type = null;

	//For DIF Name query
	private String DIFName = null;
	private LinkedList<ApplicationProcessNamingInfo> authenticatorNameInfoList = null;

	//For App Name query
	private ApplicationProcessNamingInfo applicationProcessInfo = null;
	private LinkedList<AppRecord> appRecordList = null;


	private long timeStamp ;


	public IDDRecord (String DIFName,LinkedList<ApplicationProcessNamingInfo> authenticatorNameInfoList, long timeStamp )
	{
		this.DIFName = DIFName;
		this.authenticatorNameInfoList = authenticatorNameInfoList;
		this.timeStamp = timeStamp;
		this.type = Type.DIF;

	}

	public IDDRecord (ApplicationProcessNamingInfo applicationProcessInfo, LinkedList<AppRecord> appRecordList, long timeStamp)
	{
		this.applicationProcessInfo = applicationProcessInfo;
		this.appRecordList = appRecordList;
		this.timeStamp = timeStamp;
		this.type = Type.APP;

	}

	public IDDRecord(iddMessage_t iddMsg)
	{
		if ( iddMsg.getOpCode().toString().equals("Response") || iddMsg.getOpCode().toString().equals("Register") )
		{

			if (iddMsg.hasApplicationNameInfo()) // Application Name query 
			{
				this.type = Type.APP;

				this.log.debug("This is a Applciation Name IDD Record");

				this.applicationProcessInfo = new ApplicationProcessNamingInfo( iddMsg.getApplicationNameInfo() );

				this.timeStamp = iddMsg.getTimeStamp();

				this.appRecordList = new LinkedList<AppRecord>();

				for(int i = 0 ; i < iddMsg.getIddResponseCount(); i++)
				{
					this.appRecordList.add(new AppRecord(iddMsg.getIddResponse(i)));
				}

			}else // DIF Name query
			{
				this.type = Type.DIF;

				this.log.debug("This is a DIF Name IDD Record");

				this.DIFName = iddMsg.getDifName();

				this.authenticatorNameInfoList = new LinkedList<ApplicationProcessNamingInfo>();
				this.timeStamp = iddMsg.getTimeStamp();

				//All authenticators' info
				for(int i = 0 ; i < iddMsg.getAuthenticatorNameInfoCount(); i++)
				{
					this.authenticatorNameInfoList.add
					(new ApplicationProcessNamingInfo(iddMsg.getAuthenticatorNameInfoList().get(i)));
				}

			}


		}else if (iddMsg.getOpCode().toString().equals("Request"))
		{
			this.log.error("IDD Record cannot be constucted from an IDD Message with  Request");

		}else 
		{
			this.log.error("IDD Record constucted from an IDD Message Error");
		}
		
		
	}

	//this method is mainly used in the IDDHandler.java for App registration 
	// as AppRecord is an inner class so it cannot be directly constructed outside the class
	public void addAppRecord(iddResponse_t iddResponse) 
	{	
		this.appRecordList.add(new AppRecord(iddResponse));
	}


	public void print()
	{

		String text = this.type.toString();

		if(this.type.toString().equals("DIF"))
		{
			text =  text + "[" + this.DIFName + "]";
			
			for(int i = 0; i < this.authenticatorNameInfoList.size(); i++)
			{
				text = text + "/" + "AuthenticatorInfo#" +  i + "{" +this.authenticatorNameInfoList.get(i).getInfo() + "}";
			}
		}else
		{
			text = text + "[" + this.applicationProcessInfo.getInfo() + "]";
			
			for(int i = 0; i< this.appRecordList.size(); i++)
			{
				AppRecord appRecord = this.appRecordList.get(i);
				
				text = text + "/" + "AppRecord#" + i + "{" + appRecord.getDIFName() + "/[" + 
				appRecord.getIpcProcessInfo().getInfo() + "]" + appRecord.getSupportingDIFNameList() + "}";
			}
		}

		this.log.debug("Print IDD Record " +  text + "/timestamp:" + this.timeStamp);
	}

	public synchronized Log getLog() {
		return log;
	}

	public synchronized void setLog(Log log) {
		this.log = log;
	}

	public synchronized String getDIFName() {
		return DIFName;
	}

	public synchronized void setDIFName(String dIFName) {
		DIFName = dIFName;
	}

	public synchronized LinkedList<ApplicationProcessNamingInfo> getAuthenticatorNameInfoList() {
		return authenticatorNameInfoList;
	}

	public synchronized void setAuthenticatorNameInfoList(
			LinkedList<ApplicationProcessNamingInfo> authenticatorNameInfoList) {
		this.authenticatorNameInfoList = authenticatorNameInfoList;
	}

	public synchronized ApplicationProcessNamingInfo getApplicationProcessInfo() {
		return applicationProcessInfo;
	}

	public synchronized void setApplicationProcessInfo(
			ApplicationProcessNamingInfo applicationProcessInfo) {
		this.applicationProcessInfo = applicationProcessInfo;
	}

	public synchronized LinkedList<AppRecord> getAppRecordList() {
		return appRecordList;
	}

	public synchronized void setAppRecordList(LinkedList<AppRecord> appRecordList) {
		this.appRecordList = appRecordList;
	}

	public synchronized long getTimeStamp() {
		return timeStamp;
	}

	public synchronized void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public synchronized Type getType() {
		return type;
	}

	public synchronized void setType(Type type) {
		this.type = type;
	}

	//AppRecord corresponds with the iddResponse_t type in the IDDMessage_t

	public class AppRecord
	{
		private String DIFName = null;
		private ApplicationProcessNamingInfo ipcProcessInfo = null;
		private LinkedList<String> supportingDIFNameList = null;

		public AppRecord(String DIFName, ApplicationProcessNamingInfo ipcProcessInfo,LinkedList<String> supportingDIFNameList)
		{
			this.DIFName = DIFName;
			this.ipcProcessInfo = ipcProcessInfo;
			this.supportingDIFNameList = supportingDIFNameList;
		}


		public AppRecord(iddResponse_t iddResponse)
		{
			this.DIFName = iddResponse.getDifName();
			this.ipcProcessInfo =  new ApplicationProcessNamingInfo(iddResponse.getIpcProcessNameInfo());

			this.supportingDIFNameList = new LinkedList<String>();

			for(int i = 0; i < iddResponse.getSupportingDIFNamesCount(); i++)
			{
				this.supportingDIFNameList.add( iddResponse.getSupportingDIFNamesList().get(i) );
			}
		}


		//convert to iddReponse_t type
		public iddResponse_t convert()
		{
			iddResponse_t.Builder res = iddResponse_t.newBuilder();

			res.setDifName(this.DIFName);
			res.setIpcProcessNameInfo(this.ipcProcessInfo.convert());

			for(int i = 0 ;i < this.supportingDIFNameList.size(); i++)
			{
				res.addSupportingDIFNames(this.supportingDIFNameList.get(i));
			}


			return res.buildPartial();
		}

		public synchronized String getDIFName() {
			return DIFName;
		}

		public synchronized void setDIFName(String dIFName) {
			DIFName = dIFName;
		}

		public synchronized ApplicationProcessNamingInfo getIpcProcessInfo() {
			return ipcProcessInfo;
		}

		public synchronized void setIpcProcessInfo(
				ApplicationProcessNamingInfo ipcProcessInfo) {
			this.ipcProcessInfo = ipcProcessInfo;
		}

		public synchronized LinkedList<String> getSupportingDIFNameList() {
			return supportingDIFNameList;
		}

		public synchronized void setSupportingDIFNameList(
				LinkedList<String> supportingDIFNameList) {
			this.supportingDIFNameList = supportingDIFNameList;
		}


	}


}
