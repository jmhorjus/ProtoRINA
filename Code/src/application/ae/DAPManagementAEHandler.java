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

package application.ae;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.config.RINAConfig;
import rina.ipc.impl.IPCImpl;
import rina.irm.util.HandleEntry;
import rina.message.CDAP;
import rina.message.CDAP.CDAPMessage;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.rib.impl.RIBImpl;
import application.component.impl.IPCResourceManagerImpl;

/**
 * Handler for each handle of management AE
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class DAPManagementAEHandler extends Thread {

	private  Log log = LogFactory.getLog(this.getClass());

	private IPCResourceManagerImpl ipcManager = null;

	private ApplicationProcessNamingInfo apInfo = null;

	private int handleID = -1;

	private RIBImpl rib = null;

	private HandleEntry handleEntry = null;

	public DAPManagementAEHandler(int handleID, HandleEntry handleEntry, RIBImpl rib, IPCResourceManagerImpl ipcManager) 
	{
		this.handleID = handleID;
		this.ipcManager = ipcManager;
		this.rib = rib;
		this.apInfo =  (ApplicationProcessNamingInfo)this.rib.getAttribute("apInfo");
		this.handleEntry = handleEntry;
		this.start();
	}


	public void run()
	{
		this.log.info("DAP ManagementAE Handler  started");
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

	//mainly to handle for the DDF case for now
	private void handleReceviedCDAPMsg(CDAPMessage cdapMessage) {

		if(cdapMessage.getOpCode().toString().equals("M_CREATE"))
		{
			if(cdapMessage.getObjName().equals("/daf/fork/ipc/") )
			{
				this.log.debug("M_CREATE (fork ipc) recevied during DDF, and this app is going to fork " +
						"an ipc so that other app can reach it throught the new ipc");

				String DIFName  = cdapMessage.getObjValue().getStrval();

				this.log.debug("The name of the relayed DIF is " +  DIFName);

				//NOTE: TOTO: here may be a flag is needed so for each relayed DIF, only one new IPC is need to be forked
				// NOW we don't have the flag for simplicity 

				//config the IPC process 
				RINAConfig ipcConfig = new RINAConfig();

				//ipcConfig.setProperty("rina.ipc.flag", "1"); //non-DIF zero ipc

				//NOTE: Here we set the level to be 1 FIXME
				ipcConfig.setProperty("rina.ipc.level", "1");
				ipcConfig.setProperty("rina.ipc.name", "forked:ipc:" + this.apInfo.getApName() + ":" + this.apInfo.getApInstance());
				ipcConfig.setProperty("rina.ipc.instance", "1");
				//This IPC will join that DIF
				ipcConfig.setProperty("rina.dif.enrolled", "false");
				ipcConfig.setProperty("rina.dif.name", DIFName);

				ipcConfig.setProperty("rina.ipc.userName", "BU");
				ipcConfig.setProperty("rina.ipc.passWord", "BU");
				ipcConfig.setProperty("rina.enrollment.authenPolicy", "AUTH_PASSWD");
				ipcConfig.setProperty("rina.routing.protocol","linkState");
				ipcConfig.setProperty("rina.routingEntrySubUpdatePeriod","2");
				ipcConfig.setProperty("rina.checkNeighborPeriod","2");
				ipcConfig.setProperty("rina.linkCost.policy","hop");
				ipcConfig.setUnderlyingDIFs(this.ipcManager.getUnderlyingDIFs());


				LinkedList<IPCImpl>  underlyigIPCList = this.ipcManager.getUnderlyingIPCs();

				IPCImpl newIPC = new IPCImpl(ipcConfig, underlyigIPCList);

				newIPC.start();

				this.log.debug("new IPC process is forked on request.");

				//add the new ipc to the app as its underlying IPC
				this.ipcManager.addIPC(newIPC);

				//send M_CREATE_R to relay app 

				//always true now
				int result = 0;

				CDAP.CDAPMessage M_CREATE_R_FORK_IPC = rina.message.CDAPMessageGenerator.generateM_CREATE_R(
						result,
						"fork ipc",
						"/daf/fork/ipc/",
						99
						);


				try {
					this.ipcManager.send(this.handleID, M_CREATE_R_FORK_IPC.toByteArray());
					this.log.debug("M_CREATE_R (fork ipc) sent ");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}
		}

	}

}
