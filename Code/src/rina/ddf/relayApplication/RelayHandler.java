/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */
package rina.ddf.relayApplication;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.config.RINAConfig;
import rina.ipc.impl.IPCImpl;
import rina.irm.util.HandleEntry;

import rina.message.CDAP;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.Flow;
import rina.rib.impl.RIBImpl;

import application.component.impl.IPCResourceManagerImpl;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class RelayHandler extends Thread{

	private  Log log = LogFactory.getLog(this.getClass());

	private ApplicationProcessNamingInfo apInfo = null;

	private IPCResourceManagerImpl ipcManager = null;

	private RIBImpl rib = null;

	private int handleID = -1;
	
	private HandleEntry he = null;

	private String relayedApName = null;
	private String relayedApInstance = null;

	public RelayHandler(int handleID, IPCResourceManagerImpl ipcManager, HandleEntry he, RIBImpl rib )
	{
		this.handleID = handleID;
		this.ipcManager = ipcManager;
		this.he = he;
		this.rib = rib;
		this.apInfo =  (ApplicationProcessNamingInfo)this.rib.getAttribute("apInfo");
		this.relayedApName = (String)this.rib.getAttribute("relayedApName");
		this.relayedApInstance = (String)this.rib.getAttribute("relayedApInstance");
		this.start();
	}

	public void run()
	{
		this.log.info("Relay handler started");

		CDAP.CDAPMessage cdapMessage  = null;

		while(true)
		{
			byte[] msg =  this.ipcManager.receive(this.handleID);


			try {
				cdapMessage = CDAP.CDAPMessage.parseFrom(msg);

				this.handleReceviedCDAPMsg(cdapMessage);

			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void handleReceviedCDAPMsg(CDAP.CDAPMessage cdapMessage) 
	{

		if(cdapMessage.getOpCode().toString().equals("M_CREATE"))
		{
			if(cdapMessage.getObjName().equals("/daf/relay/dif/"))
			{
				//check whether the DIF providing relay service is formed or not
				//here we assume we only form one dif for each relay app
				//so all client will create IPC to join this dif

				String DIFName = "relay:DIF:" +  this.relayedApName;

				if(  (Boolean)this.rib.getAttribute("difFormedFlag") == false)
				{
					//create new DIF by form a new authenticator, and send the DIF info to client


					this.log.debug("DIF for relaying ap " +  this.relayedApName + " is crated with DIF name " +  DIFName);


					//config the IPC process 
					RINAConfig ipcConfig = new RINAConfig();

					//ipcConfig.setProperty("rina.ipc.flag", "1"); //non-DIF zero ipc
					ipcConfig.setProperty("rina.ipc.level", "1");
					ipcConfig.setProperty("rina.ipc.name", "relay:ipc:" + this.relayedApName);
					ipcConfig.setProperty("rina.ipc.instance", "1");
					ipcConfig.setProperty("rina.dif.enrolled", "true");
					ipcConfig.setProperty("rina.dif.name", DIFName);
					ipcConfig.setProperty("rina.ipc.userName", "BU");
					ipcConfig.setProperty("rina.ipc.passWord", "BU");
					ipcConfig.setProperty("rina.enrollment.authenPolicy", "AUTH_PASSWD");
					ipcConfig.setProperty("rina.routing.protocol","linkState");
					ipcConfig.setProperty("rina.routingEntrySubUpdatePeriod","2");
					ipcConfig.setProperty("rina.checkNeighborPeriod","2");
					ipcConfig.setProperty("rina.linkCost.policy","hop");
					ipcConfig.setUnderlyingDIFs(this.ipcManager.getUnderlyingDIFs());
					ipcConfig.setProperty("rina.address","500");

					///////////////////////////////////////////////////////////////////////////

					//add all underying IPC of this app to the new created IPC, so that they are also its underlying IPC
					LinkedList<IPCImpl>  underlyigIPCList = this.ipcManager.getUnderlyingIPCs();


					IPCImpl relayDIFIPC = new IPCImpl(ipcConfig, underlyigIPCList);
					
					relayDIFIPC.start();
					
					this.log.debug("relayDIFIPC created.");

					//add the new ipc to the app as its underlying IPC
					this.ipcManager.addIPC(relayDIFIPC);

					this.rib.addAttribute("difFormedFlag", true);


				}


				//(1) ask the target app's IRM to form an IPC to join this DIF by an M_CREATE
				
				//wait for an M_CREATE_R when done 
				
				CDAP.objVal_t.Builder  objToDst = CDAP.objVal_t.newBuilder();
				objToDst.setStrval(DIFName);

				CDAP.CDAPMessage M_CREATE_Fork_IPC = rina.message.CDAPMessageGenerator.generateM_CREATE
						(      "fork ipc",
								"/daf/fork/ipc/",
								objToDst.buildPartial(),
								99
								);

				//Note:relayedApName is the dst ap
				//relay CDAP message is handled by ManagementAE
				//But here, src is just the relay application, not the management AE of the application
				//Similar to code in the "dynamicDIFFormation" method in the fiel  "application.component.impl/IPCResourceManagerImpl.java" where
				// the dst is just the relay application not the management AE
				int handleToDstApp = this.ipcManager.allocateFlow(this.apInfo.getApName(),this.apInfo.getApInstance(), "", "",
						this.relayedApName, this.relayedApInstance, "Management", "1");

				try {
					this.ipcManager.send(handleToDstApp, M_CREATE_Fork_IPC.toByteArray());

					this.log.debug("M_CREATE (fork ipc)  sent to relayed  app " + this.relayedApName +
							", now wait for M_CREATE_R (fork ipc)");

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					this.log.error("Error when sending M_CREATE (fork ipc). return with nothing done");
					return;
				}

				
				byte[] reply = this.ipcManager.receive(handleToDstApp);
				CDAP.CDAPMessage M_CREATE_R_FORK_IPC = null;
				
				int result_fork_ipc = -1;
				
				try {
					
					 M_CREATE_R_FORK_IPC = CDAP.CDAPMessage.parseFrom(reply);
					 
					 result_fork_ipc =  M_CREATE_R_FORK_IPC.getResult();
					
					 this.log.debug("M_CREATE_R (fork ipc) received from " + this.relayedApName + 
							 ", with result " + result_fork_ipc );
				} catch (InvalidProtocolBufferException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					this.log.error("Error when waiting for M_CREATE_R (fork ipc). return with nothing done");
					return;
					
				}

				if(result_fork_ipc != 0)
				{
					this.log.error("Asking relayed app  to fork ipc, but replywith negative result.return with nothing done");
					return;
				}

				//(2) send DIF info to client ap

				//always true now
				int result  = 0;

				CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();
				obj.setStrval(DIFName);

				CDAP.CDAPMessage M_CREATE_R = rina.message.CDAPMessageGenerator.generateM_CREATE_R(
						result,
						"relay dif",
						"/daf/relay/dif/",
						obj.buildPartial(),
						99
						);

				try {
					this.ipcManager.send(this.handleID, M_CREATE_R.toByteArray() );

					this.log.debug("M_CREATE_R (relay dif) sent.");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}else
		{

		}

	}

}
