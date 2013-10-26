package rina.ipc.impl;


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

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.config.RINAConfig;
import rina.flowAllocator.impl.FlowAllocatorImpl;
import rina.ipc.ae.DataTransferAE;
import rina.ipc.ae.ManagementAE;
import rina.ipc.api.IPC;
import rina.irm.impl.IRMImpl;
import rina.object.gpb.IDDMessage_t.iddMessage_t;
import rina.object.gpb.IDDMessage_t.iddResponse_t;
import rina.object.gpb.IDDMessage_t.opCode_t;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.DirectoryForwardingTable;
import rina.object.internal.Flow;
import rina.object.internal.ForwardingTable;
import rina.object.internal.IDDRecord;
import rina.object.internal.Neighbors;
import rina.rib.impl.RIBImpl;
import rina.ribDaemon.impl.RIBDaemonImpl;
import rina.util.FlowInfoQueue;


public class IPCImpl extends Thread implements IPC  {

	private Log log = LogFactory.getLog(this.getClass());

	private RINAConfig config = null;
	private RIBImpl rib = null;
	private RIBDaemonImpl ribDaemon = null;
	private IRMImpl irm = null;
	private DataTransferAE  dae  = null;
	private ManagementAE mae = null;
	private FlowAllocatorImpl  flowAllocator = null;



	private String IPCName = null;
	private String IPCInstance = null;
	private String DIFName = null;
	private int IPCLevel = -1;
	
	//IPC process can be considered as an application
	//apInfo is used when the IPC is using other IPC processes as underlying IPC process
	//when this IPC can be seen as an application
	private ApplicationProcessNamingInfo apInfo = null;
	
	
	//this is the place where underlying  IPCs give feed back  to application, mainly incoming flow creation 
	// all underlying IPCs can access this message queue
	private FlowInfoQueue flowInfoQueue = null;
	
	private boolean listen = true;
	

	
	/**
	 * It contains all the underlying DIFs of this IPC,and this info will be used when joining the DIF, 
	 * The enroller will based on this information to tell who is direct neighbor
	 */
	private LinkedList<String> underlyingDIFs = null;

	private boolean enrolled = false;

	private DirectoryForwardingTable directoryForwardingTable= null;

	private Neighbors neighbors = null;

	private ForwardingTable forwardingTable = null;  

	private String routingProtocol = null;

	private String linkCostPolicy = null;
	
	//policy to generate address for new members
	private String addressPolicy = null;
	


	public IPCImpl(String configurationFile)
	{

		this.config = new RINAConfig(configurationFile);

		this.rib = new RIBImpl();
		this.rib.addAttribute("config",  config);


		this.IPCName = this.config.getIPCName();
		this.DIFName = this.config.getDIFName();
		this.IPCInstance = this.config.getIPCInstance();
		this.IPCLevel = Integer.parseInt(this.config.getIPCLevel().trim());
		
		this.apInfo = new ApplicationProcessNamingInfo( this.IPCName,  this.IPCInstance);
		this.rib.addAttribute("apInfo", this.apInfo);
		
		this.flowInfoQueue = new FlowInfoQueue();
		this.rib.addAttribute("flowInfoQueue", this.flowInfoQueue);
		
		this.enrolled  = this.config.getEnrolledState();
		this.underlyingDIFs = this.config.getUnderlyingDIFs();

		this.routingProtocol = this.config.getRoutingProtocol();
		this.rib.addAttribute("routingProtocol", this.routingProtocol);

		this.linkCostPolicy = this.config.getLinkCostPolity();
		this.rib.addAttribute("linkCostPolicy", this.linkCostPolicy);
		
		
		this.addressPolicy = this.config.getAddressPolicy();
		this.rib.addAttribute("addressPolicy", this.addressPolicy);
		
		



		if(this.enrolled == false) 
		{
			//put the following info got from configuration file into the RIB, so ManagementAE could use to enroll
			//but sometime, the following info may not exist in the configuration file
			//in the latter case, it has to get such info from outside (maybe IDD)

			this.rib.addAttribute("authenticatorApName", this.config.getAuthenticatorApName() );
			this.rib.addAttribute("authenticatorApInstance", this.config.getAuthenticatorApInstance());
			this.rib.addAttribute("authenPolicy", this.config.getAuthenPolicy());
			this.rib.addAttribute("userName", this.config.getUserName());
			this.rib.addAttribute("passWord", this.config.getPassWord());

		}else // this is an authenticator
		{
			this.rib.addAttribute("rinaAddr", this.config.getRINAAddr());	
		}

		this.rib.addAttribute("difName",  this.DIFName);
		this.rib.addAttribute("ipcName",  this.IPCName);
		this.rib.addAttribute("ipcInstance",  this.IPCInstance);
		this.rib.addAttribute("ipcLevel", this.IPCLevel);
		this.rib.addAttribute("enrolledState", this.enrolled);
		this.rib.addAttribute("underlyingDIFs", this.underlyingDIFs);


		this.directoryForwardingTable = new DirectoryForwardingTable();	
		this.neighbors = new Neighbors();
		this.rib.addAttribute("directoryForwardingTable", this.directoryForwardingTable);
		this.rib.addAttribute("neighbors", this.neighbors);

		this.forwardingTable = new ForwardingTable(this.neighbors);
		this.rib.addAttribute("forwardingTable", this.forwardingTable);



		this.irm = new IRMImpl(this.rib);

		this.flowAllocator = new FlowAllocatorImpl(this.rib, this.irm);
		//put this pointer to the RIB, it might be used to deal with flow allocation
		this.rib.addAttribute("flowAllocator", this.flowAllocator);

		this.ribDaemon = new RIBDaemonImpl(this.rib, this.irm);
		this.rib.addAttribute("ribDaemon", this.ribDaemon);


		this.dae = new DataTransferAE(this.IPCName, this.IPCInstance, "1", this.rib, this.irm);	
		this.mae = new ManagementAE(this.IPCName, this.IPCInstance, "1", this.rib, this.irm); 


		//if this IPC is a authenticator then register itself to IDD, such that new member could find it and join
		//the DIF through it.
		if(this.enrolled == true) 
		{
			this.registerDIFToIDD();
		}

	}


	/**
	 * construct an IPC process, and pass it with a list of underlying IPC processes
	 * @param config
	 * @param underlyigIPCList
	 */
	public IPCImpl(RINAConfig config, LinkedList<IPCImpl>  underlyigIPCList)
	{
		
	    this.config = config;

		this.rib = new RIBImpl();
		this.rib.addAttribute("config",  config);


		this.IPCName = this.config.getIPCName();
		this.DIFName = this.config.getDIFName();
		this.IPCInstance = this.config.getIPCInstance();
		this.IPCLevel = Integer.parseInt(this.config.getIPCLevel().trim());
		
		this.apInfo = new ApplicationProcessNamingInfo( this.IPCName,  this.IPCInstance);
		this.rib.addAttribute("apInfo", this.apInfo);
		
		this.flowInfoQueue = new FlowInfoQueue();
		this.rib.addAttribute("flowInfoQueue", this.flowInfoQueue);
	
		this.enrolled  = this.config.getEnrolledState();
		this.underlyingDIFs = this.config.getUnderlyingDIFs();

		this.routingProtocol = this.config.getRoutingProtocol();
		this.rib.addAttribute("routingProtocol", this.routingProtocol);

		this.linkCostPolicy = this.config.getLinkCostPolity();
		this.rib.addAttribute("linkCostPolicy", this.linkCostPolicy);
		
		this.addressPolicy = this.config.getAddressPolicy();
		this.rib.addAttribute("addressPolicy", this.addressPolicy);



		if(this.enrolled == false) 
		{
			//put the following info got from configuration file into the RIB, so ManagementAE could use to enroll
			//but sometime, the following info may not exist in the configuration file
			//in the latter case, it has to get such info from outside (maybe IDD)

			this.rib.addAttribute("authenticatorApName", this.config.getAuthenticatorApName() );
			this.rib.addAttribute("authenticatorApInstance", this.config.getAuthenticatorApInstance());
			this.rib.addAttribute("authenPolicy", this.config.getAuthenPolicy());
			this.rib.addAttribute("userName", this.config.getUserName());
			this.rib.addAttribute("passWord", this.config.getPassWord());

		}else // this is an authenticator
		{
			this.rib.addAttribute("rinaAddr", this.config.getRINAAddr());	
		}

		this.rib.addAttribute("difName",  this.DIFName);
		this.rib.addAttribute("ipcName",  this.IPCName);
		this.rib.addAttribute("ipcInstance",  this.IPCInstance);
		this.rib.addAttribute("ipcLevel", this.IPCLevel);
		this.rib.addAttribute("enrolledState", this.enrolled);
		this.rib.addAttribute("underlyingDIFs", this.underlyingDIFs);


		this.directoryForwardingTable = new DirectoryForwardingTable();	
		this.neighbors = new Neighbors();
		this.rib.addAttribute("directoryForwardingTable", this.directoryForwardingTable);
		this.rib.addAttribute("neighbors", this.neighbors);

		this.forwardingTable = new ForwardingTable(this.neighbors);
		this.rib.addAttribute("forwardingTable", this.forwardingTable);



		this.irm = new IRMImpl(this.rib);

		this.flowAllocator = new FlowAllocatorImpl(this.rib, this.irm);
		//put this pointer to the RIB, it might be used to deal with flow allocation
		this.rib.addAttribute("flowAllocator", this.flowAllocator);

		this.ribDaemon = new RIBDaemonImpl(this.rib, this.irm);
		this.rib.addAttribute("ribDaemon", this.ribDaemon);

		// add to its underlying IPCs
		for(int i = 0; i< underlyigIPCList.size(); i++)
		{
			this.addIPC(underlyigIPCList.get(i));
		}

		this.dae = new DataTransferAE(this.IPCName, this.IPCInstance, "1", this.rib, this.irm);	
		this.mae = new ManagementAE(this.IPCName, this.IPCInstance, "1", this.rib, this.irm); 

	
		//if this IPC is a authenticator then register itself to IDD, such that new member could find it and join
		//the DIF through it.
		if(this.enrolled == true) 
		{
			this.registerDIFToIDD();
		}

	}

	
	public void run()
	{
		this.log.debug("IPC process started");
		
		while(this.listen)
		{
			Flow flow = this.flowInfoQueue.getFlowInfo();

			//create a handle for the incoming flow request

			int handleID = this.irm.addIncomingHandle(flow);
	
	
		}
	}


	// The following four are related to flow Allocator
	// TODO
	public int allocateFlow(Flow flow) {

		return this.flowAllocator.submitAllocationRequest(flow);

	}



	public void deallocateFlow(int portID) {

		this.flowAllocator.deallocateFlow(portID);

	}



	public void send(int flowID, byte[] msg) throws Exception {

		this.flowAllocator.send(flowID,msg);
	}



	public byte[] receive(int flowID) {

		return this.flowAllocator.receive(flowID);
	}


	/**
	 * This checks if this ipc can reach the remote application with name apName
	 * @param apName
	 * @return
	 */
	public synchronized boolean checkRemoteApp(String apName) {

		return this.directoryForwardingTable.checkAppReachability(apName);

	}

	/**
	 * This checks if this ipc can reach the remote application with apInfo
	 * @param apName
	 * @return
	 */
	public synchronized boolean checkRemoteApp(ApplicationProcessNamingInfo apInfo) {

		return this.directoryForwardingTable.checkAppReachability(apInfo);

	}



	public synchronized FlowAllocatorImpl getFlowAllocator() {
		return flowAllocator;
	}



	public synchronized RIBImpl getRib() {
		return rib;
	}


	public synchronized IRMImpl getIrm() {
		return irm;
	}



	// this ipc first stores this in its RIB forwardingDirectory,
	// then send to all its neighbors
	public void registerApplication(ApplicationProcessNamingInfo apInfo, FlowInfoQueue flowInfoQueue) {

		System.out.println("ccccccccccccccccc" +  apInfo.getPrint() );
		
		this.mae.registerApplication( apInfo,  flowInfoQueue);

	}


	public void deregisterApplication(ApplicationProcessNamingInfo apInfo) {
		// TODO Auto-generated method stub

	}



	public IDDRecord queryIDD(String DIFName)
	{
		return this.mae.queryIDD(DIFName);
	}

	public IDDRecord queryIDD(ApplicationProcessNamingInfo apInfo)
	{
		return this.mae.queryIDD(apInfo);
	}

	public IDDRecord queryIDD(iddMessage_t iddRequestMsg)
	{
		return this.irm.queryIDD(iddRequestMsg);
	}

	/**
	 * Register itself as the DIF authenticator
	 */

	public void registerDIFToIDD()
	{
		iddMessage_t.Builder iddRegMsg = iddMessage_t.newBuilder();

		iddRegMsg.setOpCode(opCode_t.Register);
		iddRegMsg.setDifName(this.DIFName);

		ApplicationProcessNamingInfo authenticatorInfo 
		= new ApplicationProcessNamingInfo(this.IPCName, this.IPCInstance);

		iddRegMsg.addAuthenticatorNameInfo(authenticatorInfo.convert());		

		iddRegMsg.setTimeStamp(System.currentTimeMillis());		

		this.registerToIDD(iddRegMsg.buildPartial());

	}

	public void registerAppToIDD(ApplicationProcessNamingInfo apInfo)
	{
		iddMessage_t.Builder iddRegMsg = iddMessage_t.newBuilder();

		iddRegMsg.setOpCode(opCode_t.Register);

		iddRegMsg.setApplicationNameInfo(apInfo.convert());

		iddResponse_t.Builder appRecord  = iddResponse_t.newBuilder();
		appRecord.setDifName(this.DIFName);

		ApplicationProcessNamingInfo ipcProcessInfo 
		= new ApplicationProcessNamingInfo(this.IPCName, this.IPCInstance);

		appRecord.setIpcProcessNameInfo(ipcProcessInfo.convert());

		//add supporing DIFs of this IPC process
		for(int i = 0; i < this.underlyingDIFs.size(); i++)
		{
			appRecord.addSupportingDIFNames(this.underlyingDIFs.get(i));
		}
		
		iddRegMsg.addIddResponse(appRecord.buildPartial());

		iddRegMsg.setTimeStamp(System.currentTimeMillis());		

		this.registerToIDD(iddRegMsg.buildPartial());

	}

	public void registerToIDD(iddMessage_t iddRegMsg)
	{
		this.mae.registerToIDD(iddRegMsg);
	}

	
	public void addIPC(IPCImpl ipc) {
		
		this.irm.addIPC(ipc);
		
	}



	public synchronized String getIPCName() {
		return IPCName;
	}


	public synchronized void setIPCName(String iPCName) {
		IPCName = iPCName;
	}


	public synchronized String getIPCInstance() {
		return IPCInstance;
	}


	public synchronized void setIPCInstance(String iPCInstance) {
		IPCInstance = iPCInstance;
	}




	public synchronized String getDIFName() {
		return DIFName;
	}



	public synchronized void setDIFName(String dIFName) {
		DIFName = dIFName;
	}




	public synchronized DirectoryForwardingTable getDirectoryForwardingTable() {
		return directoryForwardingTable;
	}





	public synchronized void setDirectoryForwardingTable(
			DirectoryForwardingTable directoryForwardingTable) {
		this.directoryForwardingTable = directoryForwardingTable;
	}





}
