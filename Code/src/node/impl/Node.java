/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */
package node.impl;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import application.impl.Application;
import application.impl.DummyApplication;
import rina.config.RINAConfig;
import rina.ddf.relayApplication.RelayApplicationProcess;
import rina.ipc.impl.IPCImpl;
import video.clientProxy.ClientProxy;
import video.clientProxy.RtpClientService;
import video.clientProxy.RtspClientService;
import video.serverProxy.ServerProxy;


/**
 * This class implements RINA Node, the container where application processes and IPC processes resides.
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Node {

	private  Log log = LogFactory.getLog(this.getClass());
	private  String nodeName = null;
	private  RINAConfig config = null;

	//key is IPCName + IPCInstance
	private	 LinkedHashMap<String, IPCImpl> ipcProcesses = null;
	private  LinkedList<String> difs = null;

	private Application app = null;


	public Node(String configurationFile)
	{
		this.config = new RINAConfig(configurationFile);	
		this.nodeName = this.config.getNodeName();

		this.initIPCs();
		this.initApps();

	}



	public Node(RINAConfig rinaConfig)
	{
		this.config = rinaConfig;
		this.nodeName = this.config.getNodeName();	

		this.initIPCs();
		this.initApps();
	}

	//this is to init all IPC processes all this node based on their own configuration file 
	//for now we assume there is only --one--- application process on the node
	private void initApps() {

		String apName = this.config.getApplicationName();
		String apInstance = this.config.getApplicationInstance();
		String serviceName = this.config.getServiceName();


		if(apName == null)
		{
			this.log.info("No application on this node.");
			return;
		}
		
		//used for user-defined service only
		RelayApplicationProcess relayApp = null;
		ServerProxy serverProxy = null;
		ClientProxy clientProxy = null;


		this.log.debug("Applciation process (apName/apInstance/serviceName):" + apName + "/" + apInstance + "/" + serviceName);

		//create the application
		//Add your own application construction code here to the new IF statement

		if(serviceName.equals("dummyService"))
		{
			this.app = new DummyApplication(apName,apInstance);

			this.log.debug("DummpApplication created");

		}else if(serviceName.equals("relay"))
		{
			String relayedApName = this.config.getRelayedApName();
			String relayedApInstance = this.config.getRelayedApInstance();



			relayApp = new RelayApplicationProcess(apName, apInstance, relayedApName, relayedApInstance);

			this.app = relayApp;

			this.log.debug("RelayApplication created, relayed ap info is " 
					+ relayedApName  + "/" + relayedApInstance );

		}else if(serviceName.equals("videoServerProxy"))
		{
			serverProxy = new ServerProxy(apName, apInstance);

			this.app = serverProxy;

			this.log.debug("Server Proxy created");

		}else if(serviceName.equals("videoClientProxy"))
		{
			String serverProxyName = this.config.getProperty("serverProxy.name");
			String serverProxyInstance = this.config.getProperty("serverProxy.instance");

			clientProxy = new ClientProxy(apName,apInstance,serverProxyName,serverProxyInstance);
			this.app = clientProxy;
			this.log.debug("Client Proxy created");
		}
		else // default application contruction
		{
			this.app = new Application(apName, apInstance);

			this.log.debug("Regular Application created");
		}

		boolean stop = true;
		int i = 1;

		while(stop)
		{
			String ipcKey = this.config.getUnderlyingIPCKey(i);

			if(ipcKey == null)
			{
				int total = i-1;
				this.log.debug(total + " underlyingIPC IPC proceses used by the application process");

				stop = false;
			}else
			{

				IPCImpl ipc = this.ipcProcesses.get(ipcKey);
				//add the ipc to the application as underlying IPC process

				app.addIPC(ipc);
				this.log.debug("ipc " + i + " is " + ipcKey);

				i++;
			}
		}


		//register corresponding service to IDD
		//the following states are at the bottom, because registration to IDD needs to use underlying IPC's transportation service
		// and,  IPC processes are added to the application process as underlying IPC by the while loop above

		if(serviceName.equals("relay"))
		{
			relayApp.registerRelayServiceToIDD();

		}else if(serviceName.equals("videoClientProxy"))
		{


			try {
				clientProxy.init();
			} catch (IOException e) {
				this.log.error("Failed to initialize client proxy transport service.");
				System.exit(-1);
			}



		}

		else
		{
			//add code here to register your own service to IDD
		}



	}

	//this is to init all application processes all this node based on their own configuration file 
	private void initIPCs() {

		this.ipcProcesses = new LinkedHashMap<String, IPCImpl>();
		this.difs = new LinkedList<String>();

		boolean stop = true;
		int i = 1;

		while(stop)
		{
			String configFile = this.config.getOnNodeIPCConfigFileName(i);

			String ipcName = this.config.getOnNodeIPCName(i);
			String ipcInstance = this.config.getOnNodeIPCInstance(i);
			String ipcDIF = this.config.getOnNodeIPCDIF(i);


			if(configFile == null)
			{
				int total = i-1;
				this.log.debug(total + " IPC proceses created on the node");

				stop = false;
			}else
			{
				IPCImpl ipc = new IPCImpl(configFile);

				//IPCImpl ipc = null;

				this.log.debug("IPC " + i + " residing on the node is created. ipcName/ipcInstance/ipcDIF/ipcConfiguration:" + 
						ipcName + "/" +  ipcInstance + "/" + ipcDIF +"/" + configFile);

				this.ipcProcesses.put(ipcName + ipcInstance , ipc);
				this.difs.add(ipcDIF);

				i++;
			}
		}


	}



	public synchronized Application getApp() {
		return app;
	}


}
