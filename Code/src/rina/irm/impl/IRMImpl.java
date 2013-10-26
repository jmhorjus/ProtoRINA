/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.irm.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.config.RINAConfig;
import rina.ipc.ae.DataTransferAE;
import rina.ipc.ae.ManagementAE;
import rina.ipc.impl.IPCImpl;
import rina.irm.util.HandleEntry;
import rina.irm.util.UnderlyingDIFsInfo;
import rina.irm.util.WireManager;
import rina.message.CDAP;
import rina.message.CDAP.CDAPMessage;
import rina.message.DTP;
import rina.object.gpb.ApplicationProcessNamingInfoMessage_t.applicationProcessNamingInfo_t;
import rina.object.gpb.Flow_t.connectionId_t;
import rina.object.gpb.Flow_t.flow_t;
import rina.object.gpb.IDDMessage_t.iddMessage_t;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.Flow;
import rina.object.internal.IDDRecord;
import rina.rib.impl.RIBImpl;
import rina.util.MessageQueue;
import application.component.api.IPCResourceManager;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;


/**
 * IRM (IPC Resource Manager) implementation
 * This is a component of IPC process
 * @author Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0
 *
 */
public class IRMImpl extends Thread implements IPCResourceManager{

	private Log log = LogFactory.getLog(IRMImpl.class);

	private RIBImpl rib = null;

	private MessageQueue msgQueue = null;
	private boolean running  = true;

	private RINAConfig config = null;

	//#rina_flag = 1 means regular IPC (non-dif Zero case)
	//#rina_flag = 2 means DIF Zero IPC (BU case), only one wire, and multiple DIF0 flows mapped on one wire
	//#rina_flag = 3 means DIF Zero IPC (RINA community case), each DIF0 flow  is a TCP connection
	//rina_flag = 3 ,and this is not used for now
	//this is now set by reading rina.ipc.level, instead of rina.ipc.flag.
	private int ipc_flag = -1;

	private DataTransferAE  dae  = null;
	private ManagementAE mae = null;

	/*
	 * Manager the wire at DIF0
	 */
	private WireManager wireManager = null;
	

	private String DIFName = null;
	private String IPCName = null; //apName
	private String IPCInstance = null; //apInstance
	private int IPCLevel = -1; //IPC level 

	private UnderlyingDIFsInfo underlyingDIFsInfo = null;
	private ApplicationProcessNamingInfo apInfo = null;



	private LinkedHashMap<Integer, HandleEntry > handleMap = null;
	//this one is used just for searching purpose
	private LinkedHashMap<String, HandleEntry> existingHandle = null;

	private int handleIDRange = 10000;


	public IRMImpl(RIBImpl rib)
	{
		this.msgQueue  = new MessageQueue();

		this.rib = rib;

		this.rib.addAttribute("irmMsgQueue", this.msgQueue);

		this.config = (RINAConfig)this.rib.getAttribute("config");

		this.IPCName = this.rib.getAttribute("ipcName").toString();
		this.IPCInstance = this.rib.getAttribute("ipcInstance").toString();
		this.DIFName = this.rib.getAttribute("difName").toString();
		
		this.IPCLevel = Integer.parseInt(this.rib.getAttribute("ipcLevel").toString());

		this.apInfo = (ApplicationProcessNamingInfo)this.rib.getAttribute("apInfo");

		this.underlyingDIFsInfo = new UnderlyingDIFsInfo(this.rib);
		this.rib.addAttribute("underlyingDIFsInfo", this.underlyingDIFsInfo);


		this.handleMap = new LinkedHashMap<Integer, HandleEntry >();
		this.rib.addAttribute("handleMap", this.handleMap);

		this.existingHandle = new LinkedHashMap<String, HandleEntry>();



		if(this.IPCLevel >= 1)//means regular IPC (non-dif Zero case)
		{
			this.ipc_flag = 1;

		}else if(this.IPCLevel == 0) //means DIF Zero IPC (BU case)
		{
			this.ipc_flag = 2;	
			//DIF0 has the wire manager
			this.wireManager = new WireManager(this.rib);
			
		}else
		{
			this.log.error("IPC Level error");
		}

		//This is for RINA community case in DIF0.Comment for now.		
		//		else if(this.config.getProperty("rina.ipc.flag").trim().equals("3"))//means DIF Zero IPC (RINA community case)
		//		{
		//			this.ipc_flag = 3;
		//			int controlTCPPortID = Integer.parseInt( this.config.getProperty("controlTCPPortID") );
		//			int dataTCPPortID = Integer.parseInt(this.config.getProperty("dataTCPPortID"));
		//			this.tcpManager =  new TCPFlowManager(this.IPCName, this.IPCInstance,controlTCPPortID,  dataTCPPortID);
		//
		//		}

		this.log.debug("IPCLevel is " +  this.IPCLevel + ", and this.ipc_flag is " + this.ipc_flag);


		this.start();


	}


	public void run()
	{
		this.log.info("IRM started");

		byte[] msg = null;
		while(this.running)
		{
			msg = this.msgQueue.getReceive();

			CDAP.CDAPMessage cdapMessage = null;

			try {

				cdapMessage = CDAPMessage.parseFrom(msg);

			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			this.handleCDAPMessage(cdapMessage);


		}

	}

	private void handleCDAPMessage(CDAPMessage cdapMessage) {

		//		this.log.info("CDAPMessage received, and opCode is " + cdapMessage.getOpCode());

		switch(cdapMessage.getOpCode()){

		case M_CREATE:

			if(this.ipc_flag == 2)
			{
				this.handleM_CREATE_flowOnWire(cdapMessage);
			}

			break;

		default:


			break;
		}

	}


	private void handleM_CREATE_flowOnWire(CDAPMessage cdapMessage)
	{
		String objClass = cdapMessage.getObjClass();

		if(objClass.equals("flow"))
		{
			this.log.info("M_CREATE(flow) recevied");

			CDAP.objVal_t objValue = null;
			flow_t  flow = null;
			try {

				//objValue = objVal_t.parseFrom(cdapMessage.getObjValue().toByteArray());


				objValue = cdapMessage.getObjValue();
				flow  = flow_t.parseFrom(objValue.getByteval().toByteArray());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			applicationProcessNamingInfo_t src = null;
			applicationProcessNamingInfo_t dst = null;
			try {
				src = flow.getSourceNamingInfo();
				dst = flow.getDestinationNamingInfo();
			} catch (Exception e1) {
				this.log.error(e1.getMessage());
				//	e.printStackTrace();
			}

			int srcPortID = (int)flow.getSourcePortId();


			String dstApName = dst.getApplicationProcessName();
			String dstApInstance = dst.getApplicationProcessInstance();
			String dstAeName = dst.getApplicationEntityName();
			String dstAeInstance = dst.getApplicationEntityInstance();

			String srcApName = src.getApplicationProcessName();
			String srcApInstance = src.getApplicationProcessInstance();
			String srcAeName = src.getApplicationEntityName();
			String srcAeInstance = src.getApplicationEntityInstance();

			this.log.debug("DEST info: " + dstApName + "/" +  dstApInstance + "/" + dstAeName + "/" + dstAeInstance);
			this.log.debug("SRC info: " + srcApName + "/" +  srcApInstance + "/" + srcAeName + "/" + srcAeInstance);



			//note: "dstApName + dstApInstance" is the identifier for an end of wire.
			int wireID = this.wireManager.getWireID(srcApName +  srcApInstance); 

			this.log.debug("wireID to " + srcApName + "/" + srcApInstance + " is " + wireID);


			int handleID = this.generateHandleID(); 

			HandleEntry he = new HandleEntry(dstApName, dstApInstance, dstAeName, dstAeInstance, 
					srcApName, srcApInstance, srcAeName, srcAeInstance,wireID);

			int dstPortID = this.wireManager.addDIF0FlowOnWire(wireID,he);

			he.setSrcPortID(dstPortID);
			he.setDstPortID(srcPortID);

			he.setHandleID(handleID);
			this.handleMap.put(handleID, he);

			String key =  dstApName + dstApInstance + dstAeName + dstAeInstance 
					+ srcApName + srcApInstance + srcAeName + srcAeInstance;

			this.log.debug("key in handleM_CREATE_flowOnWire method is " +  key);

			this.existingHandle.put(key, he);


			this.log.debug("Incoming handle created with handleID " + handleID);
			he.print();


			flow_t.Builder flow_reply = flow_t.newBuilder();

			connectionId_t.Builder cid = connectionId_t.newBuilder();
			cid.setQosId(1); // ignore this for now
			cid.setSourceCEPId(srcPortID); // Note: src cid is the portID for simplicity 
			cid.setDestinationCEPId(dstPortID);  // Note: src cid is the portID for simplicity 


			flow_reply.setDestinationNamingInfo(dst);
			flow_reply.setSourceNamingInfo(src);
			flow_reply.setSourcePortId(srcPortID);
			flow_reply.setDestinationPortId(dstPortID); // this is the one the other side needs

			flow_reply.addConnectionIds(cid.buildPartial());

			flow_reply.setSourceAddress(-1); //note this is a required field, but in BU DIF0 case, this is not used, as it is on wire.
			flow_reply.setDestinationAddress(-1);

			CDAP.objVal_t.Builder  flowObj = CDAP.objVal_t.newBuilder();

			flowObj.setByteval(ByteString.copyFrom(flow_reply.buildPartial().toByteArray()));

			int result = 0; //true all the time

			CDAP.CDAPMessage M_CREATE_R = rina.message.CDAPMessageGenerator.generateM_CREATE_R
					(       result,
							"flow",
							"/dif/resourceallocation/flowallocator/flow/",
							flowObj.buildPartial(),
							99
							);



			try {
				this.send(handleID, M_CREATE_R.toByteArray());

				this.log.info( " DTP message containing a M_CREATE_R(flow) sent out over the wire with handleID" + handleID);


			} catch (Exception e) {
				this.log.error(e.getMessage());
				this.log.error("Fail to send DTP message containing a M_CREATE_R(flow) over the wire with handleID " + handleID);
			}

			//			String dstAeName = dst.getApplicationEntityName();
			//			String dstAeInstance = dst.getApplicationEntityInstance();

			if(this.rib.getAttribute("enrolledState").toString().equals("true") && srcAeName.equals("Management")) 
			{
				this.mae.addNewHandle(handleID, he);
			}
			else if(this.rib.getAttribute("enrolledState").toString().equals("false") && srcAeName.equals("Management") )
			{
				this.log.debug("this is a handle to authenticator " + handleID);
				this.mae.addAuthenticatorHandle(handleID, he);
			}


			if(srcAeName.equals("Data Transfer"))
			{
				this.dae.addNewHandle(handleID, he);
			}


		}
	}


	private synchronized int getHandle (String srcApName, String srcApInstance, String srcAeName, String srcAeInstance, 
			String dstApName,String dstApInstance, String dstAeName, String dstAeInstance) 
	{
		int handleID = -1;

		if(srcApInstance == null)
		{
			srcApInstance = "1"; //by default
		}
		if(srcAeInstance == null)
		{
			srcAeInstance = "1"; //by default
		}
		if(dstApInstance == null)
		{
			dstApInstance = "1"; //by default
		}
		if(dstAeInstance == null)
		{
			dstAeInstance = "1"; //by default
		}


		String key = srcApName + srcApInstance + srcAeName + srcAeInstance 
				+ dstApName + dstApInstance + dstAeName + dstAeInstance;

		if(this.existingHandle.containsKey(key))
		{

			return this.existingHandle.get(key).getHandleID();
		}

		return handleID;
	}

	public synchronized int allocateFlow(String srcApName, String srcApInstance, String srcAeName, String srcAeInstance, 
			String dstApName,String dstApInstance, String dstAeName, String dstAeInstance) {

		//this.log.debug("allocateFlow is called" );

		int handleID = -1;

		if(srcApInstance == null)
		{
			srcApInstance = "1"; //by default
		}
		if(srcAeInstance == null)
		{
			srcAeInstance = "1"; //by default
		}
		if(dstApInstance == null)
		{
			dstApInstance = "1"; //by default
		}
		if(dstAeInstance == null)
		{
			dstAeInstance = "1"; //by default
		}


		//for now we assume only one  flow between two ae
		String key = srcApName + srcApInstance + srcAeName + srcAeInstance 
				+ dstApName + dstApInstance + dstAeName + dstAeInstance;

		//this.log.debug("irm handle key is " + key);

		if(this.existingHandle.containsKey(key))
		{
			//this.log.debug("the request to dest has a handle before, we will use the existing one");
			return this.existingHandle.get(key).getHandleID();
		}
		////////////////////////////////////////////////////////////////////		

		if(this.ipc_flag == 1)//means regular IPC (non-dif Zero case)
		{
			//TODO similar to DAF IRM
			//for a flow object, and give it to one of its underlying IPC, and eventually goes to its flow allocator

			IPCImpl ipc = this.getUnderlyingIPC(dstApName, dstApInstance);


			//no ipc can reach the remote dst application
			if(ipc == null)
			{
				//				//Dynamic DIF formation
				//				int success =  this.dynamicDIFFormation(srcApName, dstApName);
				//
				//				if(success == -1)
				//				{
				//					this.log.debug("DDF failed, return -1 as the handle");
				//					return -1;
				//				}else if(success == 0)
				//				{
				//					//called the allocate flow method again, as there is new IPC could relay now
				//					this.log.debug("DDF successful, and call the allocateFlow again.");
				//
				//					return 9999999;
				//					//return this.allocateFlow(srcApName,dstApName);
				//				}

				this.log.error("Underlying IPC cannot be found ");
				return -1; 

			}

			String ipcName = ipc.getIPCName();
			String ipcInstance = ipc.getIPCInstance();


			handleID = this.generateHandleID(); 

			HandleEntry he = new HandleEntry(srcApName,srcApInstance, srcAeName, srcAeInstance, dstApName,dstApInstance, dstAeName, dstAeInstance, ipcName, ipcInstance, handleID);

			this.handleMap.put(handleID, he);
			this.existingHandle.put(key, he);


			Flow flow = new Flow(srcApName,srcApInstance, srcAeName, srcAeInstance, dstApName, dstApInstance, dstAeName, dstAeInstance);

			ipc.allocateFlow(flow);

			flow.print();

			he.setSrcPortID((int)flow.getSrcPortID());
			he.setDstPortID((int)flow.getDstPortID());


			he.print();


			return handleID;

		}else if(this.ipc_flag == 2)//means DIF Zero IPC (BU case)
		{
			handleID = this.alloateBUDIF0Flow(srcApName, srcApInstance, srcAeName, srcAeInstance,
					dstApName, dstApInstance, dstAeName, dstAeInstance);
		}		
//		else if(this.ipc_flag == 3)//means DIF Zero IPC (RINA community case), in this case handleID is the flowID
//		{
//			handleID = this.tcpManager.allocateTCPFlow(srcApName, srcApInstance, srcAeName, srcAeInstance,
//					dstApName, dstApInstance, dstAeName, dstAeInstance);
//		}else 
//		{
//
//			this.log.error("invalid ipc_flag, IRM flow allocation failed");
//			return -1;
//		}

		this.log.info("IRM allocateFlow() is called, and HandleID generated is " +  handleID);

		return handleID;
	}


	private synchronized IPCImpl getUnderlyingIPC(String dstApName, String dstApInstance) {

		return this.underlyingDIFsInfo.getUnderlyingIPCToApp(dstApName, dstApInstance);

	}


	/**
	 * This method is used to allocated a BU DIF 0 Flow using the underlying wire(TCP flow)
	 * the wire is setup when the bootstrap,basically to multiplex flows on the the wire
	 * @param srcApName
	 * @param srcApInstance
	 * @param srcAeName
	 * @param srcAeInstance
	 * @param dstApName
	 * @param dstApInstance
	 * @param dstAeName
	 * @param dstAeInstance
	 * @return
	 */
	private synchronized int alloateBUDIF0Flow(String srcApName, String srcApInstance,String srcAeName, String srcAeInstance
			, String dstApName,String dstApInstance, String dstAeName, String dstAeInstance)
	{

		//note: "dstApName + dstApInstance" is the identifier for an end of wire.
		int wireID = this.wireManager.getWireID(dstApName +  dstApInstance); 


		int handleID = this.generateHandleID(); 

		HandleEntry he = new HandleEntry(srcApName, srcApInstance, srcAeName, srcAeInstance,
				dstApName, dstApInstance, dstAeName, dstAeInstance, wireID);

		int portID = this.wireManager.addDIF0FlowOnWire(wireID,he);

		this.log.debug("portID is " +  portID + ", wireID is " + wireID +", handleID is " + handleID );

		he.setHandleID(handleID);
		this.handleMap.put(handleID, he);

		String key = srcApName + srcApInstance + srcAeName + srcAeInstance 
				+ dstApName + dstApInstance + dstAeName + dstAeInstance;
		this.existingHandle.put(key, he);



		applicationProcessNamingInfo_t.Builder src = applicationProcessNamingInfo_t.newBuilder();
		src.setApplicationProcessName(srcApName);
		src.setApplicationProcessInstance(srcApInstance);
		src.setApplicationEntityName(srcAeName);
		src.setApplicationEntityInstance(srcAeInstance);

		applicationProcessNamingInfo_t.Builder dst = applicationProcessNamingInfo_t.newBuilder();
		dst.setApplicationProcessName(dstApName);
		dst.setApplicationProcessInstance(dstApInstance);
		dst.setApplicationEntityName(dstAeName);
		dst.setApplicationEntityInstance(dstAeInstance);

		connectionId_t.Builder cid = connectionId_t.newBuilder();
		cid.setQosId(1); // ignore this for now
		cid.setSourceCEPId(portID); // Note: src cid is the portID for simplicity 
		cid.setDestinationCEPId(-1); // For now the other side is unkonw so make it -1

		flow_t.Builder flow = flow_t.newBuilder();
		flow.setDestinationNamingInfo(dst.buildPartial());
		flow.setSourceNamingInfo(src.buildPartial());
		flow.setSourcePortId(portID);
		flow.setDestinationPortId(-1); // first time all dst port is -1
		flow.setSourceAddress(-1); //note this is a required field, but in BU DIF0 case, this is not used, as it is on wire.
		flow.setDestinationAddress(-1);

		flow.addConnectionIds(cid.buildPartial());

		CDAP.objVal_t.Builder  flowObj = CDAP.objVal_t.newBuilder();

		flowObj.setByteval(ByteString.copyFrom(flow.buildPartial().toByteArray()));

		CDAP.CDAPMessage M_CREATE = rina.message.CDAPMessageGenerator.generateM_CREATE
				(      "flow",
						"/dif/resourceallocation/flowallocator/flow/",
						flowObj.buildPartial(),
						99
						);

		DTP dtp_first = new DTP(M_CREATE);

		try {
			this.wireManager.send(wireID, dtp_first.toBytes());
			this.log.info( " DTP message containing a M_CREATE(flow) sent out over the wire");

		} catch (Exception e) {
			this.log.error(e.getMessage());
		}



		byte[] msg = this.receive(handleID);


		CDAP.CDAPMessage cdapMessage = null;

		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);

		} catch (Exception e) {

			this.log.error(e.getMessage());

		}

		if(cdapMessage.getOpCode().toString().equals("M_CREATE_R") && cdapMessage.getResult() == 0)
		{
			this.log.debug("we got the M_CREATE_R(flow) success");
		}else
		{

			this.log.debug("we got the M_CREATE_R(flow) fail");

			this.handleMap.remove(handleID);

			this.wireManager.removeDIF0FlowOnWire(wireID,he);

			return -1;
		}



		CDAP.objVal_t objValue = null;
		flow_t  flow_reply = null;
		try {

			//objValue = objVal_t.parseFrom(cdapMessage.getObjValue().toByteArray());


			objValue = cdapMessage.getObjValue();
			flow_reply  = flow_t.parseFrom(objValue.getByteval().toByteArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		int dstPortID = (int)flow_reply.getDestinationPortId();

		he.setDstPortID(dstPortID);

		//this IPC joined DIF already, so the handle is not for enrollment
		//We need to start an Management AE handler for each Management AE handle
		if(this.rib.getAttribute("enrolledState").toString().equals("true") && srcAeName.equals("Management")) 
		{
			this.mae.addNewHandle(handleID, he);
		}
		else if(this.rib.getAttribute("enrolledState").toString().equals("false") && srcAeName.equals("Management") )
		{
			this.log.debug("this is a handle to authenticator " + handleID);
			this.mae.addAuthenticatorHandle(handleID, he);
		}


		if(srcAeName.equals("Data Transfer"))
		{
			this.dae.addNewHandle(handleID, he);
		}

		return handleID;
	}

	

	public synchronized void deallocateAllHandle(String neighborApName,String neighborApInstance) {

		int managementHandle = this.getHandle(this.IPCName, this.IPCInstance, "Management", "1", neighborApName, neighborApInstance, "Management", "1");

		int dataTransferHandle = this.getHandle(this.IPCName, this.IPCInstance, "Data Transfer", "1", neighborApName, neighborApInstance, "Data Transfer", "1");

		this.log.debug("In deallocateAllHandle(), managementHandle is " + managementHandle + " and, dataTransferHandle is " + dataTransferHandle );

		this.deallocate(managementHandle);
		this.deallocate(dataTransferHandle);

	}

	//TODO
	public synchronized void deallocate(int handleID) {


		if(handleID  == -1)
		{
			this.log.error("handleID is -1, invalid");
			return;
		}

		if(this.ipc_flag == 1)//means regular IPC (non-dif Zero case)
		{
			//to do 
		}else if(this.ipc_flag == 2)//means DIF Zero IPC (BU case)
		{

			HandleEntry he = this.handleMap.get(handleID);

			this.handleMap.remove(handleID);

			String key = he.getKey();

			this.existingHandle.remove(key);

			this.log.debug("handle " +  handleID + " deallocated, and deallocate key is " + key  );

			//FIXME: check if the operation is complete or not 

		}
//	   else if(this.ipc_flag == 3)//means DIF Zero IPC (RINA community case), in this case handleID is the flowID
////		{
////			//to do 
////		}
		else 
		{
			//to do   
			this.log.error("invalid ipc_flag");
		}


	}


	public  void send(int handleID, byte[] msg) throws Exception {


		if(this.ipc_flag == 1)//means regular IPC (non-dif Zero case)
		{
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

		}else if(this.ipc_flag == 2)//means DIF Zero IPC (BU case)
		{
			if(this.handleMap.containsKey(handleID))
			{
				HandleEntry he = this.handleMap.get(handleID);

				int wireID = he.getWireID();
				int srcPortID = he.getSrcPortID();
				int dstPortID = he.getDstPortID();

				//	this.log.debug("irm send: srcPortID is " +  srcPortID + ", dstPortID is " + dstPortID);

				DTP dtp = new DTP((short)srcPortID, (short)dstPortID, msg);

				this.wireManager.send(wireID, dtp.toBytes());

			}else
			{
				this.log.error("handleID " +  handleID + " does not exist ");
				throw new Exception("Handle does not exist");
			}



		}
//		else if(this.ipc_flag == 3)//means DIF Zero IPC (RINA community case), in this case handleID is the flowID
//		{
//			//to do 
//		}
		else 
		{
			//to do   
			this.log.error("invalid ipc_flag");
		}

	}


	public  byte[] receive(int handleID) {

		if(this.ipc_flag == 1)//means regular IPC (non-dif Zero case)
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

		}else if(this.ipc_flag == 2)//means DIF Zero IPC (BU case)
		{
			HandleEntry he = this.handleMap.get(handleID);
			int wireID = he.getWireID();
			int portID = he.getSrcPortID();
			return this.wireManager.receive(wireID, portID);


		}
//		else if(this.ipc_flag == 3)//means DIF Zero IPC (RINA community case), in this case handleID is the flowID
//		{
//			//to do 
//		}
		else 
		{
			//to do   
			this.log.error("invalid ipc_flag");
		}

		return null;
	}


	private synchronized int generateHandleID()
	{
		int handleID = -1;

		handleID = (int)( Math.random()* this.handleIDRange); 

		while(this.handleMap.containsKey(handleID))
		{
			handleID = (int)( Math.random()* this.handleIDRange); 
		}

		this.log.debug("handle generated is " +  handleID);

		this.handleMap.put(handleID,  null);

		return handleID;
	}


	public synchronized int addIncomingHandle(Flow flow)
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


		if(flow.getSrcApInfo().getAeName().equals("Management"))
		{
			this.log.debug("Incoming flow is for Management AE.");
			this.mae.addNewHandle(handleID, he);

		}else if(flow.getSrcApInfo().getAeName().equals("Data Transfer"))
		{
			this.dae.addNewHandle(handleID, he);
			this.log.debug("Incoming flow is for Data Transfer AE.");

		}else
		{
			this.log.error("Incoming flow is neither for Management AE or Data Transfer AE");
		}

		return handleID;

	}


	public IDDRecord queryIDD(iddMessage_t iddRequestMsg) {


		IDDRecord ans = null;

		if(this.ipc_flag == 1) //non-DIF-0 case
		{
			IPCImpl ipc = this.getAnyUnderlyingIPCToIDD();

			this.log.debug("queryIDD(): underlying ipc used to talk to IDD is " + ipc.getIPCName() + "/" + ipc.getIPCInstance());

			ans = ipc.queryIDD(iddRequestMsg);

		}else if(this.ipc_flag == 2) // DIF-0 BU case
		{
			ans = this.wireManager.queryIDD(iddRequestMsg);

		}
//		else if(this.ipc_flag == 3) // DIF-0 RINA community case
//		{
//			// TO DO Later
//		}

		return ans;

	}

	public void registerToIDD(iddMessage_t iddRegMsg) {


		this.log.debug("registerToIDD() is called, and ipc_flag is " + this.ipc_flag);

		if(this.ipc_flag == 1) //non-DIF-0 case
		{
			IPCImpl ipc = this.getAnyUnderlyingIPCToIDD();

			this.log.debug("registerToIDD(): underlying ipc used to talk to IDD is " + ipc.getIPCName() + "/" + ipc.getIPCInstance());

			ipc.registerToIDD(iddRegMsg);

		}else if(this.ipc_flag == 2) // DIF-0 BU case
		{
			this.wireManager.registerToIDD(iddRegMsg);

		}
//		else if(this.ipc_flag == 3) // DIF-0 RINA community case
//		{
//			//TO DO Later
//		}


	}

	///////////////////////////////////////////////////////////////////////////
	public synchronized void addIPC(IPCImpl ipc) 
	{
		this.underlyingDIFsInfo.addIPC(ipc);
	}


	public synchronized void removeIPC(String IPCName, String IPCInstance)
	{	
		this.underlyingDIFsInfo.removeIPC(IPCName, IPCInstance);
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
	 * It will use any one of the underlying IPC to talk to IDD
	 * @return
	 */
	private synchronized IPCImpl getAnyUnderlyingIPCToIDD() {

		return this.underlyingDIFsInfo.getAnyUnderlyingIPC();
	}

	//////////////////////////////////////////////////////////////////////

//	public synchronized TCPFlowManager getTcpManager() {
//		return tcpManager;
//	}
//
//
//	public synchronized void setTcpManager(TCPFlowManager tcpManager) {
//		this.tcpManager = tcpManager;
//	}


	public synchronized DataTransferAE getDae() {
		return dae;
	}


	public synchronized void setDae(DataTransferAE dae) {
		this.dae = dae;
	}


	public synchronized ManagementAE getMae() {
		return mae;
	}


	public synchronized void setMae(ManagementAE mae) {
		this.mae = mae;
	}
















}
