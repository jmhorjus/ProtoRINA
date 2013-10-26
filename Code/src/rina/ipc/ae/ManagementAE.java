/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.ipc.ae;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.irm.impl.IRMImpl;
import rina.irm.util.HandleEntry;
import rina.message.CDAP;
import rina.message.CDAP.CDAPMessage;
import rina.object.gpb.ApplicationProcessNamingInfoMessage_t.applicationProcessNamingInfo_t;
import rina.object.gpb.DirectoryForwardingTableEntry_t.directoryForwardingTableEntrySet_t;
import rina.object.gpb.DirectoryForwardingTableEntry_t.directoryForwardingTableEntry_t;
import rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t;
import rina.object.gpb.IDDMessage_t.iddMessage_t;
import rina.object.gpb.IDDMessage_t.opCode_t;
import rina.object.gpb.EnrollmentMessage_t;
import rina.object.gpb.Member_t.member_t;
import rina.object.gpb.Member_t.members_t;
import rina.object.gpb.Neighbour_t.neighbor_t;
import rina.object.gpb.Neighbour_t.neighbors_t;
import rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.DirectoryForwardingTable;
import rina.object.internal.DirectoryForwardingTableEntry;
import rina.object.internal.ForwardingTable;
import rina.object.internal.IDDRecord;
import rina.object.internal.Neighbor;
import rina.object.internal.Neighbors;
import rina.rib.impl.RIBImpl;
import rina.routing.RoutingDaemon;
import rina.util.FlowInfoQueue;

import application.ae.ApplicationEntity;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;



/**
 * Management AE of IPC process
 * This Management AE is only used to handle authentication
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class ManagementAE extends ApplicationEntity {


	private  Log log = LogFactory.getLog(this.getClass());


	private IRMImpl irm = null;

	private RoutingDaemon routingDaemon = null;

	//stores all connections to this AE
	private LinkedHashMap<Integer, HandleEntry> handleEntries = null;

	private int authenticatorHandleID = -1;

	private boolean enrolled = false;

	//this is a RINA DIF internal address
	//it will be assigned by the authenticator after joining the DIF
	private int rinaAddr = -1;

	private String DIFName = null;

	private Neighbors neighbors = null;

	private ForwardingTable forwardingTable = null;  

	private DirectoryForwardingTable directoryForwardingTable = null;
	private LinkedHashMap<String, FlowInfoQueue>  callbackApplicaitonFlowInfoQueues = null;





	public ManagementAE(String ApName, String ApInstance, String AeInstance,  RIBImpl rib, IRMImpl irm)
	{
		super(ApName, ApInstance, "Management", AeInstance, rib);
		this.rib.addAttribute("managementAeMsgQueue", this.msgQueue);

		this.DIFName = (String)this.rib.getAttribute("difName");
		this.enrolled = (Boolean) this.rib.getAttribute("enrolledState");

		this.log.debug("this.enrolled " +  this.enrolled);

		this.irm = irm;
		this.irm.setMae(this);
		this.handleEntries = new LinkedHashMap<Integer, HandleEntry> ();

		this.neighbors = (Neighbors)this.rib.getAttribute("neighbors");
		this.directoryForwardingTable = (DirectoryForwardingTable) this.rib.getAttribute("directoryForwardingTable");

		this.callbackApplicaitonFlowInfoQueues = new LinkedHashMap<String, FlowInfoQueue>();
		this.rib.addAttribute("callbackApplicaitonFlowInfoQueues", this.callbackApplicaitonFlowInfoQueues);

		this.forwardingTable = (ForwardingTable)this.rib.getAttribute("forwardingTable");


		if(this.enrolled == false)
		{

			try // the reason to have this try/catch is in rib such info may not exist, which throw exceptions
			{ 	
				String authenticatorApName = null;
				String authenticatorApInstance = null;


				if(this.rib.getAttribute("authenticatorApName") == null)
				{
					this.log.debug("authenticator info is unknow about the DIF " + this.DIFName + ", querry IDD");

					IDDRecord difRecord = this.queryIDD(this.DIFName);

					ApplicationProcessNamingInfo authenticatorApInfo = difRecord.getAuthenticatorNameInfoList().get(0);

					authenticatorApName = authenticatorApInfo.getApName();
					authenticatorApInstance = authenticatorApInfo.getApInstance();

					this.rib.addAttribute("authenticatorApName", authenticatorApName );
					this.rib.addAttribute("authenticatorApInstance", authenticatorApInstance);

				}else
				{
					this.log.debug("authenticator info is known about the DIF " + this.DIFName);

					authenticatorApName = this.rib.getAttribute("authenticatorApName").toString();
					authenticatorApInstance  = this.rib.getAttribute("authenticatorApInstance").toString();	
				}

				this.log.debug("authenticatorApName:" + authenticatorApName + ", authenticatorApInstance:" +authenticatorApInstance);

				this.enrollment(authenticatorApName, authenticatorApInstance, null, null);


			}catch(Exception e)
			{

				this.log.info(" Enrollment fails,  and this IPC remains isolated.");
			}



		}else // this if an authenticator from the beginning
		{
			this.rinaAddr = (Integer)this.rib.getAttribute("rinaAddr");

			LinkedList<String>  underlyingDIFs = (LinkedList<String>) this.rib.getAttribute("underlyingDIFs");

			this.rib.addMember(this.rinaAddr, this.apName, this.apInstance, underlyingDIFs);// apName + apInstance identifies an IPC process
			// from there, this ipc is also an authenticator, which can enroll new IPC into the DIF.


		}

		//start the routing Daemon 
		this.routingDaemon = new RoutingDaemon(this.rib, this.irm);
		this.rib.addAttribute("routingDaemon", this.routingDaemon);


	}


	public void enrollment(String dstApName, String dstApInstance, String dstAeName, String dstAeInstance)
	{
		//by default management AE  is responsible for authentication
		if(dstAeName == null)
		{
			dstAeName = "Management";
		}

		//by default management AE Instance "1"
		if(dstAeInstance  == null)
		{
			dstAeInstance = "1";
		}


		this.log.info("Enrollment procedure started, and authenticator info:  " + 
				dstApName + "/" + dstApInstance + "/"+ dstAeName + "/" + dstAeInstance);

		this.authenticatorHandleID = this.irm.allocateFlow(this.apName, this.apInstance, this.aeName, this.aeInstance,
				dstApName, dstApInstance, dstAeName, dstAeInstance);

		this.log.info("authenticatorHandleID  is " + this.authenticatorHandleID);


		CDAP.CDAPMessage M_CONNECT = null;

		//	   AUTH_NONE;
		//     AUTH_PASSWD;
		//     AUTH_SSHRSA;
		//     AUTH_SSHDSA;

		if(this.rib.getAttribute("authenPolicy").toString().equals("AUTH_PASSWD"))
		{

			this.log.debug("authenPolicy : AUTH_PASSWD");

			CDAP.authValue_t.Builder authValue = CDAP.authValue_t.newBuilder();


			String userName = (String)this.rib.getAttribute("userName");

			String passWord = (String)this.rib.getAttribute("passWord");

			//this might be a list //TODO
			LinkedList<String> underlyingDIFs =  (LinkedList<String>)this.rib.getAttribute("underlyingDIFs");

			this.log.debug("Enrollment info - userName/passWord/underlyingDIFs: " + userName + "/" + passWord + "/" + underlyingDIFs);

			authValue.setAuthName(userName);
			authValue.setAuthPassword(passWord);

			underlyingDIFs_t.Builder underlyingDIFs_tosend = underlyingDIFs_t.newBuilder();

			for(int i=0; i< underlyingDIFs.size(); i++ )
			{
				underlyingDIFs_tosend.addUnderlyingDIFs(underlyingDIFs.get(i));
			}

			authValue.setAuthOther(underlyingDIFs_tosend.buildPartial().toByteString());

			M_CONNECT = rina.message.CDAPMessageGenerator.generateM_CONNECT
					(
							CDAP.authTypes_t.AUTH_PASSWD, 
							authValue.buildPartial(),
							dstAeInstance,//destAEInst
							dstAeName,//destAEName
							dstApInstance,//destApInst
							dstApName,//destApName
							110,
							this.aeInstance,//srcAEInst
							this.aeName,//srcAEName
							this.apInstance,//srcApInst
							this.apName//srcApName
							);
		}else if(this.rib.getAttribute("authenPolicy").toString().equals("AUTH_NONE"))
		{
			this.log.debug("authenPolicy: AUTH_NONE");

			M_CONNECT = rina.message.CDAPMessageGenerator.generateM_CONNECT
					(
							dstAeInstance,//destAEInst
							dstAeName,//destAEName
							dstApInstance,//destApInst
							dstApName,//destApName
							110,
							this.aeInstance,//srcAEInst
							this.aeName,//srcAEName
							this.apInstance,//srcApInst
							this.apName//srcApName
							);

		}else
		{
			//Implement your own authentication policy
		}

		try {
			this.irm.send(this.authenticatorHandleID, M_CONNECT.toByteArray());
			this.log.info( "M_CONNECT sent to authenticator.");

		} catch (Exception e) {
			this.log.error(e.getMessage());
		}


		while(!enrolled)
		{

			byte[] msg  = this.irm.receive(this.authenticatorHandleID);

			CDAP.CDAPMessage cdapMessage = null;

			try {
				cdapMessage = CDAP.CDAPMessage.parseFrom(msg);

			} catch (Exception e) {

				this.log.error(e.getMessage());

			}

			//			this.log.info("CDAPMessage received, and opcode is " + cdapMessage.getOpCode());

			this.processEnrollmentCDAPMessage(cdapMessage);
		}


		this.enrolled = true;
		this.rib.addAttribute("enrolledState", this.enrolled);

		HandleEntry he = this.handleEntries.get(this.authenticatorHandleID);

		//after the enrollment is done, a handler is created to handle the communication with the authenticator
		new ManagementAEHandler(this.authenticatorHandleID, he, this.rib, this.irm);

		// from there, this ipc is also an authenticator, which can enroll new IPC into the DIF.
		this.start();

	}


	private void processEnrollmentCDAPMessage(CDAPMessage cdapMessage) {

		switch(cdapMessage.getOpCode()){


		case M_CREATE:

			this.handleEnrollment_M_CREATE(cdapMessage);

			break;

		case M_START:


			this.handleEnrollment_M_START(cdapMessage);

			break;

		case M_START_R:


			this.handleEnrollment_M_START_R(cdapMessage);

			break;

		case M_STOP:

			this.handleEnrollment_M_STOP(cdapMessage);

			break;


		case M_CONNECT_R:

			this.handleEnrollment_M_CONNECT_R(cdapMessage);

			break;



		default:

			break;
		}

	}




	// M_START_R during Enrollment
	private void handleEnrollment_M_START_R(CDAPMessage cdapMessage) {

		if(cdapMessage.getObjClass().equals("address"))
		{
			this.rinaAddr = (int) cdapMessage.getObjValue().getInt64Val();
			this.rib.addAttribute("rinaAddr", this.rinaAddr);
			this.log.info("RINA address received from authenticator: " +  this.rinaAddr);

		}
	}


	private void handle_M_READ(CDAPMessage cdapMessage) {

		if(cdapMessage.getObjClass().equals("watchdog timer"))
		{

			CDAP.CDAPMessage M_READ_R = rina.message.CDAPMessageGenerator.generateM_READ_R
					(      cdapMessage.getObjClass(),
							cdapMessage.getObjName(),
							cdapMessage.getInvokeID()
							);


			try {
				this.irm.send(this.authenticatorHandleID, M_READ_R.toByteArray());

				this.log.debug("M_READ_R(watchdog timer) sent " );

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}



	// M_START during Enrollment
	private void handleEnrollment_M_START(CDAPMessage cdapMessage) {

		if (cdapMessage.getObjClass().equals("operationstatus"))
		{
			this.enrolled = true;
			this.rib.addAttribute("enrolledState", true);
			this.log.debug("Enrollment procedure finished." );

			//put its undelrying DIF info of itself in the rib 
			//this will be used when enroll new member to tell its who is its neighbor

			//LinkedList<String>  underlyingDIFs = (LinkedList<String>) this.rib.getAttribute("underlyingDIFs");
			//this.rib.addUnderlyingDIFsInfo(this.rinaAddr, underlyingDIFs);

		}

	}

	// M_CONNECT_R during Enrollment
	private void handleEnrollment_M_CONNECT_R(CDAPMessage cdapMessage) {

		CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();

		enrollmentInformation_t ei_msg = EnrollmentMessage_t.generate(this.rinaAddr,"STOPPED");

		obj.setByteval(ei_msg.toByteString());

		CDAP.CDAPMessage M_START_Enroll = rina.message.CDAPMessageGenerator.generateM_START
				(       "enrollment information",
						"/daf/management/enrollment", 
						obj.buildPartial(),
						21
						);


		try {

			this.irm.send(this.authenticatorHandleID, M_START_Enroll.toByteArray());

			this.log.info("M_START(enrollment information) sent. " );

		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.log.error(e.getMessage());
			//e.printStackTrace();
		}


	}

	// M_STOP during Enrollment
	private void handleEnrollment_M_STOP(CDAPMessage cdapMessage) {

		if(cdapMessage.getObjClass().equals("enrollment information"))
		{
			CDAP.CDAPMessage M_STOP_R = rina.message.CDAPMessageGenerator.generateM_STOP_R
					(       0,
							"enrollment information",
							cdapMessage.getInvokeID()
							);


			try {
				this.irm.send(this.authenticatorHandleID, M_STOP_R.toByteArray());

				this.log.info("M_STOP_R(enrollment information) sent. " );


			} catch (Exception e1) {
				// TODO Auto-generated catch block
				this.log.error(e1.getMessage());
				//e1.printStackTrace();
			}
		}
	}


	// M_CREATE during Enrollment
	// this is different  with he M_CREATE hander in the ManagementAEhandler.java
	// here only for enrollment, so don't have to broadcast info it received to its neighbor when receiving new info such as 
	// new DIF member, new Application registration
	//NOTE: if M_CREATE here is modified, check if in ManagementAEhandler.java 's handle M_CREATE anything needs also changing
	private void handleEnrollment_M_CREATE(CDAPMessage cdapMessage) {

		this.log.info("M_CREATE ("+ cdapMessage.getObjClass() + ")  received." );

		if(cdapMessage.getObjClass().equals("directoryforwardingtableentry set"))
		{


			directoryForwardingTableEntrySet_t sets = null;
			try {
				sets =  directoryForwardingTableEntrySet_t.parseFrom(cdapMessage.getObjValue().getByteval());
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int num = sets.getDirectoryForwardingTableEntryCount();

			this.log.info(num + " directoryforwardingtable entry received." );


			for(int i =0 ;i < num; i++)
			{
				directoryForwardingTableEntry_t entry = sets.getDirectoryForwardingTableEntry(i);

				applicationProcessNamingInfo_t apInfo_t = entry.getApplicationName();

				String apName = apInfo_t.getApplicationProcessName();
				String apInstance = apInfo_t.getApplicationProcessInstance();

				ApplicationProcessNamingInfo apInfo = null;

				if(apInstance == "") //apIntance may not be used for application
				{
					apInfo = new ApplicationProcessNamingInfo(apName);
				}else //if application is an IPC, then apInstance is used for sure
				{
					apInfo = new ApplicationProcessNamingInfo(apName, apInstance);
				}

				DirectoryForwardingTableEntry directoryForwardingTableEntry =
						new  DirectoryForwardingTableEntry(apInfo, entry.getIpcProcessAddress(), entry.getTimestamp());

				this.directoryForwardingTable.addEntry(directoryForwardingTableEntry);

				this.log.debug("directoryForwardingTableEntry added info (apName/apInstance/ipcaddr/timestamp):  " +  apName + "/" +  apInstance + "/"
						+ entry.getIpcProcessAddress() + "/" + entry.getTimestamp());


			}


		}else if (cdapMessage.getObjClass().equals("neighbor set"))
		{

			//NOTE: this is different from M_CRATE handle in MAEHander.java
			//after the enrollment, routing deamon will be started, where ipc will sub to all its direct neighbor

			neighbors_t neighbors_obj = null;

			try {
				neighbors_obj = neighbors_t.parseFrom(cdapMessage.getObjValue().getByteval());
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			int num = neighbors_obj.getNeighborCount();

			this.log.info(num + " neihgbor entry received." );

			for(int i =0 ;i < num; i++)
			{

				neighbor_t neighbor_obj = neighbors_obj.getNeighbor(i);

				String apName = neighbor_obj.getApplicationProcessName();
				String apInstance = neighbor_obj.getApplicationProcessInstance();
				long addr = neighbor_obj.getAddress();

				Neighbor neighbor = new Neighbor(apName,apInstance,addr);

				this.neighbors.addNeighbor(neighbor);


				//add direct neighbor to its forwarding table 
				//this is bootstrap
				this.forwardingTable.addNextHop((int)neighbor.getAddr(), (int) neighbor.getAddr());


				this.log.debug("neighbor entry added info (apName/apInstance/addr): " +  apName + "/" +  apInstance + "/" + addr);

			}




		}else if(cdapMessage.getObjClass().equals("qoscube set"))
		{

		}else if(cdapMessage.getObjClass().equals("datatransercons"))
		{

		}else if(cdapMessage.getObjClass().equals("member set"))  
		{

			//NOTE: this is different from M_CRATE handle in MAEHander.java
			//after the enrollment, routing deamon will be started, where ipc will sub to all its direct neighbor

			members_t sets = null;

			try {
				sets =  members_t.parseFrom(cdapMessage.getObjValue().getByteval());
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int num = sets.getMemberCount();

			this.log.debug( num + " DIF member entry recevied ." );


			for(int i =0 ;i < num; i++)
			{
				member_t entry = sets.getMember(i);

				String IPCApName = entry.getApplicationProcessName();
				String IPCApInstance = entry.getApplicationProcessInstance();
				int addr = (int) entry.getAddress();


				LinkedList<String> underlyingDIFs = new LinkedList<String> ();

				for(int count = 0 ; count < entry.getUnderlyingDIFsCount(); count++)
				{
					underlyingDIFs.add(entry.getUnderlyingDIFs(count));
				}

				this.rib.addMember(addr, IPCApName, IPCApInstance,underlyingDIFs );
				this.log.debug("The rib updates its information(member list) about the IPC: " + IPCApName + "/" + IPCApInstance  + "/" + addr);

			}



		}

	}



	public synchronized void addNewHandle(int handleID ,HandleEntry he)
	{
		this.handleEntries.put(handleID, he);

		//start a thread there to handle new flow with the handle
		new ManagementAEHandler(handleID, he, this.rib, this.irm);

	}


	public synchronized void addAuthenticatorHandle(int handleID ,HandleEntry he)
	{
		this.handleEntries.put(handleID, he);

	}




	// this ipc first stores this in its RIB forwardingDirectory,
	// then send to all its neighbors


	public void registerApplication(ApplicationProcessNamingInfo apInfo, FlowInfoQueue flowInfoQueue) {

		this.log.info("registerApplication() is called");

		DirectoryForwardingTableEntry entry = new DirectoryForwardingTableEntry(apInfo, this.rinaAddr,System.currentTimeMillis());

		//put into its own RIB
		this.directoryForwardingTable.addEntry(entry);

		//this is the interface IPC uses to talk to apps on top of it

		String key = apInfo.getApName() + apInfo.getApInstance() + apInfo.getAeName() + apInfo.getAeInstance();


		this.callbackApplicaitonFlowInfoQueues.put(key, flowInfoQueue);

		this.log.debug("Application flowInfoQueue added with key " + key);

		CDAP.objVal_t.Builder  objM_CREATE_directoryforwardingtableentries = CDAP.objVal_t.newBuilder();

		directoryForwardingTableEntrySet_t.Builder directoryForwardingTableEntrySet =  directoryForwardingTableEntrySet_t.newBuilder();

		directoryForwardingTableEntrySet.addDirectoryForwardingTableEntry(entry.convert());

		objM_CREATE_directoryforwardingtableentries.setByteval(ByteString.copyFrom(directoryForwardingTableEntrySet.buildPartial().toByteArray()));

		LinkedList<Neighbor> neighborList = this.neighbors.getNeighborList();

		for(int j = 0; j< neighborList.size(); j++ )
		{
			Neighbor neighbor = neighborList.get(j);

			String dstipcName = neighbor.getApName();
			String dstipcInstance = neighbor.getApInstance();

			neighbor.print();

			int handle = this.irm.allocateFlow(this.apName, this.apInstance, "Management", "1", 
					dstipcName, dstipcInstance, "Management", "1");

			this.log.debug("handle (to management ae where IPCInfo:" + dstipcName + "/" + dstipcInstance + ") is " + handle);


			CDAP.CDAPMessage M_CREATE = rina.message.CDAPMessageGenerator.generateM_CREATE
					(      "directoryforwardingtableentry set",
							"/dif/management/flowallocator/directoryforwardingtableentries",
							objM_CREATE_directoryforwardingtableentries.buildPartial(),
							56
							);

			try {

				//send it to every neighbor
				this.irm.send(handle, M_CREATE.toByteArray());
				this.log.debug("M_CREATE(directoryforwardingtableentry set) containing one directory forwarding table entry sent due to application registration" );

			} catch (Exception e1) {

				this.log.error(e1.getMessage());
				//e1.printStackTrace();
			}

		}


	}


	public IDDRecord queryIDD(String DIFName) {

		this.log.debug("query IDD about DIF:" +  DIFName);

		IDDRecord ans = null;

		iddMessage_t.Builder reqMsg = iddMessage_t.newBuilder();
		reqMsg.setOpCode(opCode_t.Request);
		reqMsg.setDifName(DIFName);

		ans = this.irm.queryIDD(reqMsg.buildPartial());

		return ans;
	}



	public IDDRecord queryIDD(ApplicationProcessNamingInfo apInfo) {

		IDDRecord ans = null;

		iddMessage_t.Builder reqMsg = iddMessage_t.newBuilder();
		reqMsg.setOpCode(opCode_t.Request);

		reqMsg.setApplicationNameInfo(apInfo.convert());

		ans = this.irm.queryIDD(reqMsg.buildPartial());

		return ans;
	}


	public void registerToIDD(iddMessage_t iddRegMsg) {

		this.irm.registerToIDD(iddRegMsg);

	}










}
