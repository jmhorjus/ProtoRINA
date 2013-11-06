/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */

package application.component.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.config.RINAConfig;
import rina.ipc.impl.IPCImpl;
import rina.irm.util.HandleEntry;
import rina.irm.util.UnderlyingDIFsInfo;
import rina.message.CDAP;
import rina.object.gpb.IDDMessage_t.iddMessage_t;
import rina.object.gpb.IDDMessage_t.iddResponse_t;
import rina.object.gpb.IDDMessage_t.opCode_t;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.Flow;
import rina.object.internal.IDDRecord;
import rina.rib.impl.RIBImpl;
import rina.util.FlowInfoQueue;
import application.ae.DAPManagementAE;
import application.component.api.IPCResourceManager;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * This is a component of an application process, which manages of all underlying IPC process
 * Note: In an IPC process, there is a component called IRM (IPC Resource Manager).  IPC manager here is the same 
 * with non-DIF0 IPC process's IRM.
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class IPCResourceManagerImpl implements IPCResourceManager {


	private  Log log = LogFactory.getLog(this.getClass());

	private RIBImpl rib = null;

	private UnderlyingDIFsInfo underlyingDIFsInfo = null;

	private ApplicationProcessNamingInfo apInfo = null;

	//private boolean listen = true;

	//this is to used get flow allocation info from underlying ipc
	private FlowInfoQueue flowInfoQueue = null;


	private LinkedHashMap<Integer, HandleEntry > handleMap = null;
	//this one is used just for searching purpose
	private LinkedHashMap<String, HandleEntry> existingHandle = null;
	private int handleIDRange = 10000;


	private DAPManagementAE mae = null;


	public IPCResourceManagerImpl(RIBImpl rib)
	{
		this.rib = rib;
		this.underlyingDIFsInfo = new UnderlyingDIFsInfo(this.rib);
		this.apInfo = (ApplicationProcessNamingInfo)this.rib.getAttribute("apInfo");
		this.flowInfoQueue = (FlowInfoQueue)this.rib.getAttribute("flowInfoQueue");

		this.handleMap = new LinkedHashMap<Integer, HandleEntry >();
		this.rib.addAttribute("handleMap", this.handleMap);
		this.existingHandle = new LinkedHashMap<String, HandleEntry>();

	}


	//	public void run()
	//	{
	//		while(this.listen)
	//		{
	//			Flow flow = this.flowInfoQueue.getFlowInfo();
	//
	//			//create a handle for the incoming flow request
	//
	//			this.addIncomingHandle(flow);
	//
	//		}
	//	}

	public int addIncomingHandle(Flow flow)
	{
		int handleID = this.generateHandleID();

		HandleEntry he = new HandleEntry(flow.getSrcApInfo().getApName(), flow.getSrcApInfo().getApInstance(),
				flow.getSrcApInfo().getAeName(),flow.getSrcApInfo().getAeInstance(),
				flow.getDstApInfo().getApName(), flow.getDstApInfo().getApInstance(),
				flow.getDstApInfo().getAeName(), flow.getDstApInfo().getAeInstance(),
				flow.getUnderlyingIPCName(), flow.getUnderlyingIPCInstance(),
				handleID);

		he.setSrcPortID((int)flow.getSrcPortID());
		he.setDstPortID((int)flow.getDstPortID());

		String key = flow.getDstApInfo().getApName() + flow.getDstApInfo().getApInstance();

		this.handleMap.put(handleID, he);
		this.existingHandle.put(key, he);

		this.log.debug("New Incoming handle added, and flow info:" +  flow.getPrint());

		return handleID;

	}


	public synchronized void addIPC(IPCImpl ipc)
	{
		this.underlyingDIFsInfo.addIPC(ipc);
	}



	public synchronized void removeIPC(String IPCName, String IPCInstance)
	{	
		this.underlyingDIFsInfo.removeIPC(IPCName, IPCInstance);

		//check all existing flows, if it is using this IPC process, remove that handle
		
		
		int totalHandleNumber = this.handleMap.size();
		
		Object[] keyArray = this.handleMap.keySet().toArray();
		
		for(int i = 0; i < totalHandleNumber; i++)
		{
			int handleID = Integer.parseInt(keyArray[i].toString());
			
			HandleEntry he = this.handleMap.get(handleID);
			
			he.print();
			
			if(he.getUnderlyingIPCName().equals(IPCName) && he.getUnderlyingIPCInstance().equals(IPCInstance))
			{
				this.deallocate(handleID);
			}
		}
		
		this.log.debug("IPC " + IPCName + "/" + IPCInstance + " removed ");
	}


	public synchronized LinkedList<String> getUnderlyingDIFs()
	{
		return this.underlyingDIFsInfo.getUnderlyingDIFs();
	}

	public synchronized LinkedList<IPCImpl> getUnderlyingIPCs()
	{
		return this.underlyingDIFsInfo.getUnderlyingIPCList();
	}


	/**
	 * Return the handle
	 */
	public int allocateFlow(String srcApName, String dstApName) {

		return this.allocateFlow(srcApName, "", dstApName, "");		

	}





	// The following is a back up of previous method	
	//	/**
	//	 * Return the handle
	//	 */
	//	public int allocateFlow(String srcApName, String dstApName) {
	//
	//
	//		
	//		
	//		//for now we assume only one flow between two application
	//		
	//		String key = srcApName + dstApName;
	//
	//		//this.log.debug("irm handle key is " + key);
	//
	//		if(this.existingHandle.containsKey(key))
	//		{
	//			//this.log.debug("the request to dest has a handle before, we will use the existing one");
	//			return this.existingHandle.get(key).getHandleID();
	//		}
	//		////////////////////////////////////////////////////////////////////
	//
	//
	//		IPCImpl ipc = this.getUnderlyingIPC(dstApName);
	//
	//		//no ipc can reach the remote dst application
	//		if(ipc == null)
	//		{
	//			
	//			//Dynamic DIF formation
	//			int success =  this.dynamicDIFFormation(srcApName, dstApName);
	//			
	//			if(success == -1)
	//			{
	//				this.log.debug("DDF failed, return -1 as the handle");
	//				return -1;
	//			}else if(success == 0)
	//			{
	//				//called the allocate flow method again, as there is new IPC could relay now
	//				this.log.debug("DDF successful");
	//				
	//				
	//				//TESTNOW
	//				return 999;
	//				//return this.allocateFlow(srcApName,dstApName);
	//			}
	//			
	//		}
	//
	//		String ipcName = ipc.getIPCName();
	//		String ipcInstance = ipc.getIPCInstance();
	//
	//
	//		int handleID = this.generateHandleID(); 
	//
	//		HandleEntry he = new HandleEntry(srcApName, dstApName, ipcName, ipcInstance, handleID);
	//
	//
	//
	//		this.handleMap.put(handleID, he);
	//		this.existingHandle.put(key, he);
	//
	//
	//		Flow flow = new Flow(srcApName, dstApName);
	//
	//		ipc.allocateFlow(flow);
	//
	//		flow.print();
	//
	//		he.setSrcPortID((int)flow.getSrcPortID());
	//		he.setDstPortID((int)flow.getDstPortID());
	//
	//
	//		he.print();
	//
	//
	//		return handleID;
	//
	//	}


	/**
	 * This is to dynamically create a DIF, and this is called when an application wants to allocate a flow to another application 
	 * but there is no existing underlying DIF could help to reach the destination application.
	 * At this time, an new DIF needs to be created to provide the transportation service, however there should be an application pre-registered to provide the relay services.
	 * @param srcApName
	 * @param dstApName
	 */

	private synchronized int dynamicDIFFormation(String srcApName, String srcApInstance, String dstApName, String dstApInstance) {

		this.log.debug("Dynamic DIF Formation is called, [srcApName/srcApInstance/dstApName/dstApInstance]: " 
				+  srcApName + "/" + srcApInstance + "/" + dstApName + "/" + dstApInstance);

		int success = -1;

		//application name is in the formation: relay + dstApName + dstApInstance
		//for example for  "app""1", the relay application has a name "relay:app1"
		IDDRecord iddRecord = this.queryServiceToIDD("relay:" + dstApName +  dstApInstance);

		if(iddRecord == null)
		{
			return success;
		}else
		{
			this.log.debug("relay service found");
			iddRecord.print();
			success = 0;
		}

		//send request to the app provides relay service, and fork ipc process to joins the DIF

		//Remember we use IPCProcessInfo to carry the app info providing the relay service 

		ApplicationProcessNamingInfo relayApp = iddRecord.getAppRecordList().get(0).getIpcProcessInfo();

		String relayApName = relayApp.getApName();
		String relayApInstance = relayApp.getApInstance();

		this.log.debug("App that provides relay services to [apName/ApInstance:" + dstApName + "/" + dstApInstance + "]is "  +  relayApName + "/" + relayApInstance);

		//To Relay application
		//Note: not to the Management AE of relay application, the handle just to relay application
		int handleToRelayAp = this.allocateFlow(srcApName,srcApInstance, "Management", "1",
				relayApName, relayApInstance, "", "");

		this.log.debug("handle to relay app: " + relayApName + "/" + relayApInstance  + " is  " + handleToRelayAp);

		//send a CDAP message M_CREATE to relay app to form the DIF, and fork and IPC to joins the DIF

		CDAP.CDAPMessage M_CREATE = rina.message.CDAPMessageGenerator.generateM_CREATE
				(      "relay dif",
						"/daf/relay/dif/",
						99
						);

		try {
			this.send(handleToRelayAp, M_CREATE.toByteArray());
			this.log.debug("M_CREATE(relay dif) sent to relay ap");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			success = -1;
			return success;
		}

		//wait for M_CREATE_R containing relay DIF info

		byte[] reply = this.receive(handleToRelayAp);

		String relayDIFName = null;

		try 
		{
			CDAP.CDAPMessage M_CREATE_R = CDAP.CDAPMessage.parseFrom(reply);

			this.log.debug("M_CREATE_R (relay dif) received");

			if(M_CREATE_R.getResult() == 0)
			{
				relayDIFName = M_CREATE_R.getObjValue().getStrval();
				this.log.debug("Relay DIF found, and DIF name is " +  relayDIFName);
			}else
			{
				this.log.error("Relay DIF not found");
				success = -1;
				return success;
			}


		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			success = -1;
			return success;
		}


		//fork an IPC process to join that DIF

		//config the IPC process 
		RINAConfig ipcConfig = new RINAConfig();

		//ipcConfig.setProperty("rina.ipc.flag", "1"); //non-DIF zero ipc

		//NOTE: Here we set the level to be 1 FIXME
		ipcConfig.setProperty("rina.ipc.level", "1");
		ipcConfig.setProperty("rina.ipc.name", "forked:ipc:" + this.apInfo.getApName() + ":" + this.apInfo.getApInstance());
		ipcConfig.setProperty("rina.ipc.instance", "1");
		//This IPC will join that DIF
		ipcConfig.setProperty("rina.dif.enrolled", "false");
		ipcConfig.setProperty("rina.dif.name", relayDIFName);

		ipcConfig.setProperty("rina.ipc.userName", "BU");
		ipcConfig.setProperty("rina.ipc.passWord", "BU");
		ipcConfig.setProperty("rina.enrollment.authenPolicy", "AUTH_PASSWD");
		ipcConfig.setProperty("rina.routing.protocol","linkState");
		ipcConfig.setProperty("rina.routingEntrySubUpdatePeriod","2");
		ipcConfig.setProperty("rina.checkNeighborPeriod","2");
		ipcConfig.setProperty("rina.linkCost.policy","hop");
		ipcConfig.setUnderlyingDIFs(this.getUnderlyingDIFs());


		LinkedList<IPCImpl>  underlyigIPCList = this.getUnderlyingIPCs();

		IPCImpl newIPC = new IPCImpl(ipcConfig, underlyigIPCList);

		newIPC.start();

		this.log.debug("new IPC process is forked on request.");

		//add the new ipc to the app as its underlying IPC
		this.addIPC(newIPC);

		return success;

	}


	//query IDD for the relay service 

	private IDDRecord queryServiceToIDD(String serviceName) {

		IDDRecord iddRecord = null;

		IPCImpl ipc = this.getAnyUnderlyingIPCToIDD();

		//NOTE: for relay service. apName is "relay", apInstance is dest app.apName +  dest app.spInstance
		iddRecord = ipc.queryIDD(new ApplicationProcessNamingInfo(serviceName)) ;

		return iddRecord;

	}

	public void registerServiceToIDD(String serviceName) {


		IPCImpl ipc = this.getAnyUnderlyingIPCToIDD();

		iddMessage_t.Builder iddRegMsg = iddMessage_t.newBuilder();

		iddRegMsg.setOpCode(opCode_t.Register);

		//here  app name querryed is service name
		iddRegMsg.setApplicationNameInfo(new ApplicationProcessNamingInfo(serviceName).convert());

		iddResponse_t.Builder appRecord  = iddResponse_t.newBuilder();

		//here we use ipcProcessInfo to carry the app info which provides the service
		appRecord.setIpcProcessNameInfo(this.apInfo.convert());

		iddRegMsg.addIddResponse(appRecord.buildPartial());

		iddRegMsg.setTimeStamp(System.currentTimeMillis());		

		ipc.registerToIDD(iddRegMsg.buildPartial());

	}


	/**
	 * It will use any one of the underlying IPC to talk to IDD
	 * @return
	 */
	private synchronized IPCImpl getAnyUnderlyingIPCToIDD() {

		return this.underlyingDIFsInfo.getAnyUnderlyingIPC();
	}

	public  int allocateFlow(String srcApName,  String srcApInstance, String dstApName, String dstApInstance)
	{
		return this.allocateFlow(srcApName, srcApInstance, "", "", dstApName, dstApInstance, "", "");
	}


	/**
	 * old method, just for record now.
	 */
	//	public int allocateFlow(String srcApName,  String srcApInstance, String dstApName, String dstApInstance) {
	//
	//
	//		//for now we assume only one flow between two application
	//
	//		String key = srcApName + srcApInstance +  dstApName +  dstApInstance;
	//
	//		//this.log.debug("irm handle key is " + key);
	//
	//		if(this.existingHandle.containsKey(key))
	//		{
	//			//	this.log.debug("the request to dest has a handle before, we will use the existing one");
	//			return this.existingHandle.get(key).getHandleID();
	//		}
	//		////////////////////////////////////////////////////////////////////
	//
	//
	//		IPCImpl ipc = this.getUnderlyingIPC(dstApName, dstApInstance);
	//
	//		//no ipc can reach the remote dst application
	//		if(ipc == null)
	//		{
	//			//Dynamic DIF formation
	//			int success =  this.dynamicDIFFormation(srcApName, dstApName);
	//
	//			if(success == -1)
	//			{
	//				this.log.debug("DDF failed, return -1 as the handle");
	//				return -1;
	//			}else if(success == 0)
	//			{
	//				//called the allocate flow method again, as there is new IPC could relay now
	//				this.log.debug("DDF successful, and call the allocateFlow again.");
	//
	//				try {
	//					int sleepTime = 5;
	//					this.log.debug("Sleep for " +  sleepTime + " seconds such that the routing in the new DIF converge for the first time");
	//					Thread.sleep(1000*5);
	//
	//				} catch (InterruptedException e) {
	//					// TODO Auto-generated catch block
	//					e.printStackTrace();
	//				}
	//
	//				return this.allocateFlow(srcApName,dstApName);
	//			}
	//		}
	//
	//		String ipcName = ipc.getIPCName();
	//		String ipcInstance = ipc.getIPCInstance();
	//
	//
	//		int handleID = this.generateHandleID(); 
	//
	//		HandleEntry he = new HandleEntry(srcApName,srcApInstance, dstApName,dstApInstance, ipcName, ipcInstance, handleID);
	//
	//		this.handleMap.put(handleID, he);
	//		this.existingHandle.put(key, he);
	//
	//
	//		Flow flow = new Flow(srcApName,srcApInstance, "", "", dstApName, dstApInstance,"", "");
	//
	//		ipc.allocateFlow(flow);
	//
	//		flow.print();
	//
	//		he.setSrcPortID((int)flow.getSrcPortID());
	//		he.setDstPortID((int)flow.getDstPortID());
	//
	//
	//		he.print();
	//
	//
	//		return handleID;
	//
	//	}


	/**
	 * This returns the IPC  which can reach the application 
	 * @param dstApName
	 * @return
	 */
	private synchronized IPCImpl getUnderlyingIPC(String dstApName, String dstApInstance) {

		return this.underlyingDIFsInfo.getUnderlyingIPCToApp(dstApName, dstApInstance);

	}




	public synchronized void deallocate(int handleID) {

		this.log.debug("dellocate is called to remove handle with ID " +  handleID);
		
		HandleEntry he = this.handleMap.get(handleID);
				
		this.handleMap.remove(handleID);
		
		String key = he.getKey();
		
		this.existingHandle.remove(key);

		this.log.debug("handle " +  handleID + " removed");
	}



	public void send(int handleID, byte[] msg) throws Exception {

		if(this.handleMap.containsKey(handleID))
		{
			HandleEntry he = this.handleMap.get(handleID);

			String underlyingIPCName = he.getUnderlyingIPCName();

			String underlyingIPCNameInstance = he.getUnderlyingIPCInstance();

			int srcPortID = he.getSrcPortID();

			this.log.debug("irm send on ipc " + underlyingIPCName + "/" + underlyingIPCNameInstance + ", portID: " + srcPortID);

			IPCImpl ipc = this.underlyingDIFsInfo.getUnderlyingIPC(underlyingIPCName, underlyingIPCNameInstance);

			ipc.send(srcPortID, msg);

		}else
		{
			this.log.error("handleID " +  handleID + " does not exist ");
			throw new Exception("Handle does not exist");
		}

	}



	public byte[] receive(int handleID)
	{
		byte[] msg = null;

		if(this.handleMap.containsKey(handleID))
		{
			HandleEntry he = this.handleMap.get(handleID);

			String underlyingIPCName = he.getUnderlyingIPCName();

			String underlyingIPCInstance = he.getUnderlyingIPCInstance();

			int srcPortID = he.getSrcPortID();

			IPCImpl ipc = this.underlyingDIFsInfo.getUnderlyingIPC(underlyingIPCName, underlyingIPCInstance);

			he.print();

			if(ipc !=null)
			{
				msg = ipc.receive(srcPortID);
			}else
			{
				this.log.error("Underlying IPC not found");
			}
		}

		return msg;
	}


	private synchronized int generateHandleID()
	{
		int handleID = -1;

		handleID = (int)( Math.random()* this.handleIDRange); 

		while(this.handleMap.containsKey(handleID))
		{
			handleID = (int)( Math.random()* this.handleIDRange); 
		}

		this.log.debug("Handle generated is " +  handleID);

		this.handleMap.put(handleID, null);

		return handleID;
	}


	public RIBImpl getRib() {
		return rib;
	}


	public void setRib(RIBImpl rib) {
		this.rib = rib;
	}


	public void setMae(DAPManagementAE mae) {	
		this.mae = mae;	
	}




	public synchronized int allocateFlow(String srcApName, String srcApInstance, String srcAeName, String srcAeInstance,
			String dstApName, String dstApInstance, String dstAeName, String dstAeInstance)
	{
		//assumme there are only one flow between two aes
		String key = srcApName + srcApInstance + srcAeName + srcAeInstance
				+  dstApName +  dstApInstance + dstAeName + dstAeInstance;

		this.log.debug("irm handle key is " + key);

		if(this.existingHandle.containsKey(key))
		{
			//	this.log.debug("the request to dest has a handle before, we will use the existing one");
			return this.existingHandle.get(key).getHandleID();
		}
		////////////////////////////////////////////////////////////////////


		IPCImpl ipc = this.getUnderlyingIPC(dstApName, dstApInstance);

		//no ipc can reach the remote dst application
		if(ipc == null)
		{
			//Dynamic DIF formation
			int success =  this.dynamicDIFFormation(srcApName,srcApInstance, dstApName, dstApInstance);

			if(success == -1)
			{
				this.log.debug("DDF failed, return -1 as the handle");
				return -1;
			}else if(success == 0)
			{
				//called the allocate flow method again, as there is new IPC could relay now
				this.log.debug("DDF successful, and call the allocateFlow again.");

				try {
					int sleepTime = 5;
					this.log.debug("Sleep for " +  sleepTime + " seconds such that the routing in the new DIF converge for the first time");
					Thread.sleep(1000*5);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return this.allocateFlow(srcApName,srcApInstance, dstApName, dstApInstance);
			}
		}

		String ipcName = ipc.getIPCName();
		String ipcInstance = ipc.getIPCInstance();


		int handleID = this.generateHandleID(); 

		HandleEntry he = new HandleEntry(srcApName,srcApInstance, srcAeName, srcAeInstance,
				dstApName,dstApInstance, dstAeName, dstAeInstance, ipcName, ipcInstance, handleID);


		this.handleMap.put(handleID, he);
		this.existingHandle.put(key, he);


		Flow flow = new Flow(srcApName,srcApInstance, srcAeName, srcAeInstance,
				dstApName,dstApInstance, dstAeName, dstAeInstance);

		ipc.allocateFlow(flow);

		flow.print();

		he.setSrcPortID((int)flow.getSrcPortID());
		he.setDstPortID((int)flow.getDstPortID());


		he.print();

		return handleID;
	}



	public synchronized HandleEntry getHandleEntry (int handleID)
	{
		if(this.handleMap.containsKey(handleID))
		{
			return this.handleMap.get(handleID);
		}else
		{
			return null;
		}
	}

}
