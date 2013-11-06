
/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */

package rina.irm.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.ipc.impl.IPCImpl;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.rib.impl.RIBImpl;
import rina.util.FlowInfoQueue;
import rina.util.MessageQueue;

/**
 * This stores the underlying DIFs infomation
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class UnderlyingDIFsInfo {

	private  Log log = LogFactory.getLog(this.getClass());

	private RIBImpl rib = null;

	private ApplicationProcessNamingInfo apInfo = null;

	private LinkedHashMap<String, IPCImpl> underlyingIPCs = null;
	private LinkedList<IPCImpl>  underlyingIPCList = null;



	public UnderlyingDIFsInfo(RIBImpl rib)
	{
		this.rib = rib;
		
		this.apInfo = (ApplicationProcessNamingInfo)this.rib.getAttribute("apInfo");

		this.underlyingIPCs = new LinkedHashMap<String, IPCImpl> ();
		this.underlyingIPCList = new LinkedList<IPCImpl>();


	}

	public synchronized void addIPC(IPCImpl ipc)
	{
		String ipcName = ipc.getIPCName();
		String ipcInstance = ipc.getIPCInstance();

		String ipcID = ipcName + ipcInstance;

		if( ! this.underlyingIPCs.containsKey(ipcID))
		{
			this.underlyingIPCs.put(ipcID, ipc);
			this.underlyingIPCList.add(ipc);

			ipc.registerApplication(this.apInfo, (FlowInfoQueue)this.rib.getAttribute("flowInfoQueue"));
			

			this.log.info("underlying IPC (" + ipcName + "/" + ipcInstance  + ") added");
		}

	}

	public synchronized void removeIPC(String IPCName, String IPCInstance)
	{
		String ipcID = IPCName + IPCInstance;

		if(this.underlyingIPCs.containsKey(ipcID ))
		{

			IPCImpl ipc = this.underlyingIPCs.get(ipcID);

			this.underlyingIPCs.remove(ipcID);
			this.underlyingIPCList.remove(ipc);

			//for now we don't unregister the ap from the ipc
			//as when IPC is removed, it most like means the applicaiotn cannot use the ipc anymore.
			
			//ipc.deregisterApplication(this.apInfo);

			this.log.info("underlying IPC (" + IPCName + "/" + IPCInstance  + ") removed");
		}else
		{
			this.log.error("underlying IPC (" + IPCName + "/" + IPCInstance  + ") does not exist");
		}

	}

	/**
	 * This returns the IPC that can reach a certain remote application
	 * @param dstApName
	 * @return
	 */
	public synchronized IPCImpl getUnderlyingIPCToApp(String apName, String apInstance) {

		IPCImpl ipc = null;

		for(int i =0 ;i < this.underlyingIPCList.size(); i++)
		{ 
			ipc = this.underlyingIPCList.get(i);

			if (ipc.checkRemoteApp(new ApplicationProcessNamingInfo(apName, apInstance)))
			{
				this.log.debug("Underlying IPC found to reach application, and its info:" + ipc.getIPCName() + "/" +  ipc.getIPCInstance());
				return ipc;
			}

		}

		//nobody can reach the dst applicaiton 
		return null;

	}

	public synchronized IPCImpl getUnderlyingIPC(String ipcName, String ipcInstance)
	{
		String ipcID = ipcName + ipcInstance;

		return this.underlyingIPCs.get(ipcID);
	}


	/**
	 * This one is used to get any undelrying IPC to talk to IDD
	 * @return
	 */
	public synchronized IPCImpl getAnyUnderlyingIPC()
	{
		return this.underlyingIPCList.get(0);
	}

	public synchronized LinkedList<String> getUnderlyingDIFs() 
	{
		LinkedList<String> difList = new LinkedList<String>();

		for(int i = 0; i< this.underlyingIPCList.size();i++)
		{
			String difName = this.underlyingIPCList.get(i).getDIFName();

			if(!difList.contains(difName))
			{
				difList.add(difName);
			}
		}

		return difList;

	}

	public synchronized LinkedList<IPCImpl> getUnderlyingIPCList() {
		return underlyingIPCList;
	}

	public synchronized void setUnderlyingIPCList(
			LinkedList<IPCImpl> underlyingIPCList) {
		this.underlyingIPCList = underlyingIPCList;
	}




}
