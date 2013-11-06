/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */
package application.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import application.ae.DAPManagementAE;
import application.component.impl.IPCResourceManagerImpl;
import application.component.util.RegularHandler;
import application.component.util.DummyHandler;

import rina.config.RINAConfig;
import rina.ipc.impl.IPCImpl;
import rina.irm.util.HandleEntry;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.Flow;
import rina.rib.impl.RIBImpl;
import rina.ribDaemon.impl.RIBDaemonImpl;
import rina.util.FlowInfoQueue;
import rina.util.MessageQueue;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Application extends Thread {

	protected  Log log = LogFactory.getLog(this.getClass());

	protected String apName = "";
	protected String apInstance = "";
	protected String aeName = "";
	protected String aeInstance = "";

	protected ApplicationProcessNamingInfo apInfo = null;
	
	protected DAPManagementAE mae = null;

	protected RIBImpl rib = null;
	protected RIBDaemonImpl ribDaemon = null;

	protected IPCResourceManagerImpl ipcManager = null;

	//this is the place where underlying  IPCs give feed back  to application, mainly incoming flow creation 
	// all underlying IPCs can access this message queue
	protected FlowInfoQueue flowInfoQueue = null;

	protected boolean listen = true;

	protected  RINAConfig rinaConfig = null;
	
	

	/**
	 * 
	 * @param configurationFile
	 */
	public Application(String configurationFile)
	{
		this.rinaConfig = new RINAConfig(configurationFile);	
		
		this.apName = this.rinaConfig.getApplicationName();
		this.apInstance = this.rinaConfig.getApplicationInstance();

		this.apInfo = new ApplicationProcessNamingInfo( apName,  apInstance,  aeName,  aeInstance);

		this.log.info(this.apInfo.getPrint());

		this.rib = new RIBImpl();
		this.rib.addAttribute("apInfo", this.apInfo);

		this.flowInfoQueue = new FlowInfoQueue();
		this.rib.addAttribute("flowInfoQueue", this.flowInfoQueue);

		this.ipcManager = new IPCResourceManagerImpl(this.rib);
		this.rib.addAttribute("ipcManager", this.ipcManager);

		this.ribDaemon = new RIBDaemonImpl(this.rib, this.ipcManager);
		this.rib.addAttribute("ribDaemon", this.ribDaemon);

		this.mae = new DAPManagementAE(this.aeName, this.apInstance, "1", this.rib, this.ipcManager);
		
		this.initUnderylyIPCs(this.rinaConfig);

		this.start();

	}


	/**
	 * 
	 * @param apName
	 * @param apInstance
	 */
	public Application(String apName, String apInstance)
	{
		this.apName = apName;

		if(apInstance != null)
		{
			this.apInstance = apInstance;
		}

		this.apInfo = new ApplicationProcessNamingInfo( apName,  apInstance,  aeName,  aeInstance);

		this.rib = new RIBImpl();
		this.rib.addAttribute("apInfo", this.apInfo);

		this.flowInfoQueue = new FlowInfoQueue();
		this.rib.addAttribute("flowInfoQueue", this.flowInfoQueue);

		this.ipcManager = new IPCResourceManagerImpl(this.rib);
		this.rib.addAttribute("ipcManager", this.ipcManager);

		
		this.mae = new DAPManagementAE(this.aeName, this.apInstance, "1", this.rib, this.ipcManager);
		
		this.start();

	}




	/**
	 * The application is configured in a configuration object, which reads from a configuration file
	 * @param rinaConfig
	 */
	public Application(RINAConfig rinaConfig)
	{

		this.rinaConfig = rinaConfig;

		this.apName = this.rinaConfig.getApplicationName();
		this.apInstance = this.rinaConfig.getApplicationInstance();

		this.apInfo = new ApplicationProcessNamingInfo( apName,  apInstance,  aeName,  aeInstance);

		this.log.info(this.apInfo.getPrint());

		this.rib = new RIBImpl();
		this.rib.addAttribute("apInfo", this.apInfo);

		this.flowInfoQueue = new FlowInfoQueue();
		this.rib.addAttribute("flowInfoQueue", this.flowInfoQueue);

		this.ipcManager = new IPCResourceManagerImpl(this.rib);
		this.rib.addAttribute("ipcManager", this.ipcManager);

		this.initUnderylyIPCs(this.rinaConfig);

		this.mae = new DAPManagementAE(this.aeName, this.apInstance, "1", this.rib, this.ipcManager);
		
		this.start();

	}


	private void initUnderylyIPCs(RINAConfig rinaConfig) {

		boolean stop = true;
		int i = 1;


		while(stop)
		{
			String configFile = this.rinaConfig.getUnderlyIPCConfigFileName(i);

			if(configFile == null)
			{
				int total = i-1;
				this.log.debug("No more undelryingIPC configuration file, " + total + " files read");

				stop = false;
			}else
			{

				IPCImpl IPC = new IPCImpl(configFile);

				this.log.debug("IPC is created, and configuration file is " + configFile);

				this.addIPC(IPC);
				i++;
			}
		}

		this.log.debug("Application is done initialization from file.");

	}


	public void run()
	{
		this.log.debug("Application started");

		while(this.listen)
		{
			Flow flow = this.flowInfoQueue.getFlowInfo();

			//create a handle for the incoming flow request

			int handleID = this.ipcManager.addIncomingHandle(flow);

			//attach different handler for different incoming flows
			//Management messages goes to Management AE
			//Application messages goes to application specific handler
			
			this.processIncomingFlow(handleID);
			
		}
	}


	private void processIncomingFlow(int handleID) 
	{
		
		HandleEntry he = this.ipcManager.getHandleEntry(handleID);
		
		if(he.getSrcAeName().equals("Management"))
		{
			
			this.mae.addNewHandle(handleID,he);
			
		}else
		{
			//regular application handler
			this.attachHandler(handleID, he);

		}
		
	}


	//overload this method in user-specific  class
	public void attachHandler(int handleID, HandleEntry he) {

		new RegularHandler(handleID, this.ipcManager,he, this.rib);

	}


	public void addIPC(IPCImpl ipc)
	{
		this.ipcManager.addIPC(ipc);
	}
	
	public void removeIPC(IPCImpl ipc)
	{
		String ipcName = ipc.getIPCName();
		String ipcInstance = ipc.getIPCInstance();
		
		this.removeIPC(ipcName,ipcInstance);
	}
	
	
	public void removeIPC(String ipcName, String ipcInstance)
	{
		
		this.ipcManager.removeIPC(ipcName, ipcInstance);
	
	}

	public IPCResourceManagerImpl getIpcManager() {
		return ipcManager;
	}


	public void registerServiceToIDD(String serviceName) {

		this.ipcManager.registerServiceToIDD(serviceName);
	}




}
