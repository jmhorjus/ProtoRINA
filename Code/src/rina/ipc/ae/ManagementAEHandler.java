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

import rina.flowAllocator.impl.FlowAllocatorImpl;
import rina.irm.impl.IRMImpl;
import rina.irm.util.HandleEntry;
import rina.message.CDAP;
import rina.message.CDAP.CDAPMessage;
import rina.object.gpb.ApplicationProcessNamingInfoMessage_t.applicationProcessNamingInfo_t;
import rina.object.gpb.DirectoryForwardingTableEntry_t.directoryForwardingTableEntrySet_t;
import rina.object.gpb.DirectoryForwardingTableEntry_t.directoryForwardingTableEntry_t;
import rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t;
import rina.object.gpb.Flow_t.flow_t;
import rina.object.gpb.Member_t.member_t;
import rina.object.gpb.Member_t.members_t;
import rina.object.gpb.Neighbour_t.neighbor_t;
import rina.object.gpb.Neighbour_t.neighbors_t;
import rina.object.gpb.SubscriptionEvent_t.subscriptionEvent_t;
import rina.object.gpb.UnderlyingDIFs_t.underlyingDIFs_t;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.DirectoryForwardingTable;
import rina.object.internal.DirectoryForwardingTableEntry;
import rina.object.internal.Flow;
import rina.object.internal.ForwardingTable;
import rina.object.internal.Member;
import rina.object.internal.Neighbor;
import rina.object.internal.Neighbors;
import rina.rib.impl.RIBImpl;
import rina.ribDaemon.impl.RIBDaemonImpl;
import rina.routing.RoutingDaemon;
import rina.util.FlowInfoQueue;
import rina.util.MessageQueue;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;


/**
 * Each flow (handleID) to Management AE  is attached with a ManagementAEHandler
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class ManagementAEHandler extends Thread {

	private  Log log = LogFactory.getLog(this.getClass());

	private boolean listen = true;

	private String apName = null;
	private String apInstance = null;
	private String aeName = null;
	private String aeInstance = null;

	private long rinaAddr = -1;


	private int handleID = -1;

	private String dstIPCName = null;
	private String dstIPCInstance = null;

	private int dstRINAAddr = -1;


	//this is an attribute  to calculate who is the direct neighbor for a new ipc member 
	// for DIF zero it is  ip nework name
	private LinkedList<String> dstIPCUnderlyingDIFs = null;


	private HandleEntry he = null;


	private RIBImpl rib = null;
	private IRMImpl irm = null;
	private RIBDaemonImpl ribDaemon = null;


	private RoutingDaemon routingDaemon = null;

	private DirectoryForwardingTable directoryForwardingTable = null;
	private Neighbors neighbors = null;

	private LinkedHashMap<String, FlowInfoQueue>  callbackApplicaitonFlowInfoQueues = null;

	private ForwardingTable forwardingTable = null;  

	private String routingProtocol = null;

	/**
	 * This is an authenticator AE thread
	 * @param handleID
	 * @param he
	 * @param rib
	 * @param irm
	 */
	public ManagementAEHandler ( int handleID, HandleEntry he, RIBImpl rib, IRMImpl irm)
	{
		this.handleID = handleID;
		this.he = he;

		this.apName = this.he.getSrcApName();
		this.apInstance = this.he.getSrcApInstance();
		this.aeName = this.he.getSrcAeName();
		this.aeInstance = this.he.getSrcAeInstance();

		this.dstIPCName = this.he.getDstApName();
		this.dstIPCInstance = this.he.getDstApInstance();

		this.rib = rib;
		this.irm = irm;

		this.ribDaemon = (RIBDaemonImpl) this.rib.getAttribute("ribDaemon");

		this.directoryForwardingTable = (DirectoryForwardingTable) this.rib.getAttribute("directoryForwardingTable");
		this.callbackApplicaitonFlowInfoQueues = (LinkedHashMap<String, FlowInfoQueue>)this.rib.getAttribute("callbackApplicaitonFlowInfoQueues");

		this.neighbors = (Neighbors)this.rib.getAttribute("neighbors");

		this.forwardingTable = ( ForwardingTable)this.rib.getAttribute("forwardingTable");

		this.rinaAddr = Long.parseLong(this.rib.getAttribute("rinaAddr").toString());

		this.routingProtocol = (String)this.rib.getAttribute("routingProtocol");

		this.routingDaemon = (RoutingDaemon)this.rib.getAttribute("routingDaemon");

		this.start();
	}




	public void run()
	{
		this.log.info("ManagementAE Handler started on handleID " + this.handleID);

		while(this.listen)
		{
			byte[] msg  = this.irm.receive(this.handleID);

			CDAP.CDAPMessage cdapMessage = null;

			try {
				cdapMessage = CDAP.CDAPMessage.parseFrom(msg);

			} catch (Exception e)
			{
				this.log.error(e.getMessage());
			}

			this.processCDAPMessage(cdapMessage);
		}
	}

	private void processCDAPMessage(CDAPMessage cdapMessage) {

		//	this.log.info("CDAPMessage received, and opcode is " + cdapMessage.getOpCode());

		switch(cdapMessage.getOpCode()){

		case M_CONNECT:

			this.handle_M_CONNECT(cdapMessage);

			break;

		case M_START:

			this.handle_M_START(cdapMessage);

			break;


		case M_STOP_R:

			this.handle_M_STOP_R(cdapMessage);

			break;

		case M_CREATE:

			this.handle_M_CREATE(cdapMessage);

			break;

		case M_CREATE_R:

			this.handle_M_CREATE_R(cdapMessage);

			break;


		default:

			break;
		}


	}

	private void handle_M_CREATE_R(CDAPMessage cdapMessage) {

		if(cdapMessage.getObjClass().equals("flow")) 
			//this is a msg the FAI is waiting, so put in the FAI msg Queue
		{
			FlowAllocatorImpl flowAllocator =   (FlowAllocatorImpl) this.rib.getAttribute("flowAllocator");

			this.log.info("M_CREATE_R(flow) recevied");

			CDAP.objVal_t objValue = null;
			flow_t  flow = null;
			try {

				objValue = cdapMessage.getObjValue();
				flow  = flow_t.parseFrom(objValue.getByteval().toByteArray());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



			//check if it needs relaying. This happens for a multi-hop flow creation.
			long DestRINAAddr = flow.getDestinationAddress();


			this.log.debug("CDAP Message M_CREATE_R(flow)'s dest RINA addr " + DestRINAAddr + ", this IPC's addr is " + this.rinaAddr);

			if(DestRINAAddr != this.rinaAddr)

			{
				this.relayCDAPMessage(cdapMessage,DestRINAAddr);

				this.log.debug(" Not for this IPC,  relay CDAP message to next hop " );
				return;
			}


			int dstPort = (int)flow.getDestinationPortId();

			( ( LinkedHashMap<Integer, MessageQueue> ) this.rib.getAttribute("flowQueues") ).get(dstPort).addReceive(cdapMessage.toByteArray());


		}

	}




	private void handle_M_CREATE(CDAPMessage cdapMessage) {

		if(cdapMessage.getObjClass().equals("flow")) 
		{

			FlowAllocatorImpl flowAllocator =   (FlowAllocatorImpl) this.rib.getAttribute("flowAllocator");

			this.log.info("M_CREATE(flow) recevied");

			CDAP.objVal_t objValue = null;
			flow_t  flow = null;
			try {

				objValue = cdapMessage.getObjValue();
				flow  = flow_t.parseFrom(objValue.getByteval().toByteArray());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			long DestRINAAddr = flow.getDestinationAddress();
			//check if it needs relaying. This happens for a multi-hop flow creation.


			this.log.debug("CDAP Message M_CREATE(flow)'s dest RINA addr " + DestRINAAddr + ", this IPC's addr is " + this.rinaAddr);

			if(DestRINAAddr != this.rinaAddr)

			{
				this.relayCDAPMessage(cdapMessage,DestRINAAddr);

				this.log.debug(" Not for this IPC,  relay CDAP message to next hop " );
				return;
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

			long srcPortID = flow.getSourcePortId();
			long srcAddr = flow.getSourceAddress();



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

			ApplicationProcessNamingInfo srcApInfo = null;
			ApplicationProcessNamingInfo dstApInfo = null; 

			if(dstApInstance == "") //only apName is used
			{
				dstApInfo = new ApplicationProcessNamingInfo(dstApName);
				srcApInfo = new ApplicationProcessNamingInfo(srcApName);
			}
			if(dstAeName == "" && dstAeInstance =="" ) // only apName and apInstance is used
			{
				dstApInfo = new ApplicationProcessNamingInfo(dstApName, dstApInstance);
				srcApInfo = new ApplicationProcessNamingInfo(srcApName, srcApInstance);
			}else // all apName, apInstance, aeName, aeInstance are used, for now this means this app is an IPC process
			{
				dstApInfo = new ApplicationProcessNamingInfo(dstApName, dstApInstance, dstAeName, dstAeInstance);
				srcApInfo = new ApplicationProcessNamingInfo(srcApName, srcApInstance, srcAeName, srcAeInstance);
			}


			Flow flowRequest = new Flow(dstApInfo,srcApInfo);
			flowRequest.setDstAddr(srcAddr);
			flowRequest.setDstPortID(srcPortID);

			flowRequest.setUnderlyingIPCName(this.apName);
			flowRequest.setUnderlyingIPCInstance(this.apInstance);

			//call flowAllocator API to process this flow request
			int flowID = flowAllocator.receiveAllocationRequest(flowRequest);

			flowRequest.print();

			//String key = dstApName + dstApInstance + dstAeName + dstAeInstance;
			//TESTME
			String key = dstApName +  dstApInstance;

			//told the application, an incoming flow created

			this.log.debug("key is " + key);

			if(this.callbackApplicaitonFlowInfoQueues.containsKey(key))
			{
				System.out.println("this.callbackApplicaitonFlowInfoQueues.containsKey(key): true");
			}else
			{
				System.out.println("this.callbackApplicaitonFlowInfoQueues.containsKey(key): false");
			}

			this.callbackApplicaitonFlowInfoQueues.get(key).addFlowInfo(flowRequest);

			this.log.debug("flowID is " +  flowID);

		}else if(cdapMessage.getObjClass().equals("directoryforwardingtableentry set")) 
		{

			directoryForwardingTableEntrySet_t sets = null;
			try {
				sets =  directoryForwardingTableEntrySet_t.parseFrom(cdapMessage.getObjValue().getByteval());
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int num = sets.getDirectoryForwardingTableEntryCount();

			this.log.info( num + " directoryforwardingtable entry received." );

			//the one is used to update its neighbors
			directoryForwardingTableEntrySet_t.Builder sets_to_send = directoryForwardingTableEntrySet_t.newBuilder();

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

				directoryForwardingTableEntry.print();

				if( this.directoryForwardingTable.checkEntry(directoryForwardingTableEntry) == false )
					//it does not contains this entry
				{
					this.directoryForwardingTable.addEntry(directoryForwardingTableEntry);

					this.log.debug("directoryForwardingTableEntry added info (apName/apInstance/ipcaddr/timestamp):  " +  apName + "/" +  apInstance + "/"
							+ entry.getIpcProcessAddress() + "/" + entry.getTimestamp());

					sets_to_send.addDirectoryForwardingTableEntry(entry);

				}else
				{
					this.log.debug("DirectoryForwardingTableEntry received exists, discarded");

				}


			}

			directoryForwardingTableEntrySet_t send = sets_to_send.buildPartial();

			// send update to its neighbors

			if(send.getDirectoryForwardingTableEntryCount() >=1)
			{
				this.log.debug("There exists new entry sending to neighbors");

				LinkedList<Neighbor> neighborList = this.neighbors.getNeighborList();

				for(int j = 0; j< neighborList.size(); j++ )
				{
					Neighbor neighbor = neighborList.get(j);

					String dstipcName = neighbor.getApName();
					String dstipcInstance = neighbor.getApInstance();

					int handle = this.irm.allocateFlow(this.apName, this.apInstance, "Management", "1", 
							dstipcName, dstipcInstance, "Management", "1");

					CDAP.objVal_t.Builder  objM_CREATE_directoryforwardingtableentries = CDAP.objVal_t.newBuilder();
					objM_CREATE_directoryforwardingtableentries.setByteval(ByteString.copyFrom(send.toByteArray()));

					CDAP.CDAPMessage M_CREATE = rina.message.CDAPMessageGenerator.generateM_CREATE
							(      "directoryforwardingtableentry set",
									"/dif/management/flowallocator/directoryforwardingtableentries",
									objM_CREATE_directoryforwardingtableentries.buildPartial(),
									56
									);


					try {
						this.irm.send(handle, M_CREATE.toByteArray());

						this.log.debug("M_CREATE(directoryforwardingtableentry set) updates sent over handle " + handle);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						this.log.error(e.getMessage());
					}
				}

			}else
			{
				this.log.debug("No new entry sending to neighbors");
			}

		}else if(cdapMessage.getObjClass().equals("neighbor set")) //new direct neighbor joins the DIF
		{
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

				//add direct neighbor to its forwarding table 
				//this is bootstrap
				this.forwardingTable.addNextHop((int)neighbor.getAddr(), (int) neighbor.getAddr());

				boolean existBefore = this.neighbors.addNeighbor(neighbor);

				if(!existBefore) //new neighbor, subscribte to its routing entry 
				{

					while(this.routingDaemon == null)
					{
						this.routingDaemon = (RoutingDaemon)this.rib.getAttribute("routingDaemon");
					}

					if(this.routingProtocol.equals("linkState"))
					{

						this.routingDaemon.addLinkStateRoutingEntrySubEvent( Long.toString(neighbor.getAddr() ) );

					}else if(this.routingProtocol.equals("distanceVector"))
					{
						//TODO
					}
				}

				this.log.debug("neighbor entry added info (apName/apInstance/addr): " 
						+  apName + "/" +  apInstance + "/" + addr);

			}

		}else if(cdapMessage.getObjClass().equals("subscription")) 
		{

			//		this.log.debug("M_CREAET (subscription) received ");

			try {
				subscriptionEvent_t event_t = subscriptionEvent_t.parseFrom(cdapMessage.getObjValue().getByteval());
				this.ribDaemon.handleReceivedSubscription(event_t);

			} catch (InvalidProtocolBufferException e) {

				this.log.error(e.getMessage());
			}
		}else if(cdapMessage.getObjClass().equals("member set")) 
		{

			members_t sets = null;
			try {
				sets =  members_t.parseFrom(cdapMessage.getObjValue().getByteval());
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int num = sets.getMemberCount();

			this.log.debug( num + " member entry recevied ." );

			//the one is used to update its neighbors
			members_t.Builder sets_to_send = members_t.newBuilder();

			for(int i =0 ;i < num; i++)
			{
				member_t entry = sets.getMember(i);

				String IPCApName = entry.getApplicationProcessName();
				String IPCApInstance = entry.getApplicationProcessInstance();
				int addr = (int) entry.getAddress();

				if(this.rib.containMember(IPCApName, IPCApInstance, addr))
				{
					this.log.debug("Discard, the rib already has information(member list) about the IPC: " + IPCApName + "/" + IPCApInstance + "/" + addr);
				}else
				{

					LinkedList<String> underlyingDIFs = new LinkedList<String> ();

					for(int count = 0 ; count < entry.getUnderlyingDIFsCount(); count++)
					{
						underlyingDIFs.add(entry.getUnderlyingDIFs(count));
					}

					this.rib.addMember(addr, IPCApName, IPCApInstance,underlyingDIFs );

					this.log.debug("The rib updates its information(member list) about the IPC: " + IPCApName + "/" + IPCApInstance 
							+ "/" + addr + "/" +underlyingDIFs  );




					//////////////////////////////////////////////////////////////////////////////////////////////
					//check if any member is its direct neighbor, if so, add to direct neighbor  and sub to it 

					LinkedList<String> itselfUnderlyingDIF = ( LinkedList<String> )this.rib.getAttribute("underlyingDIFs");

					System.out.println("itselfUnderlyingDIF is " + itselfUnderlyingDIF + ", and underlyingDIFs is " + underlyingDIFs);

					for(int counter = 0; counter < underlyingDIFs.size(); counter++)
					{
						if(itselfUnderlyingDIF.contains(underlyingDIFs.get(counter))) // they have common underlying DIF, it is direct neighbor
						{


							Neighbor neighbor = new Neighbor(IPCApName,IPCApInstance,addr);

							//add direct neighbor to its forwarding table 
							//this is bootstrap
							this.forwardingTable.addNextHop((int)neighbor.getAddr(), (int) neighbor.getAddr());

							boolean existBefore = this.neighbors.addNeighbor(neighbor);

							if(!existBefore) //new neighbor, subscribe to its routing entry 
							{

								while(this.routingDaemon == null)
								{
									this.routingDaemon = (RoutingDaemon)this.rib.getAttribute("routingDaemon");
								}

								if(this.routingProtocol.equals("linkState"))
								{

									this.routingDaemon.addLinkStateRoutingEntrySubEvent( Long.toString(neighbor.getAddr() ) );

								}else if(this.routingProtocol.equals("distanceVector"))
								{
									//TODO
								}
							}

							this.log.debug("member set received, and found new direct neighbor,neighbor entry added info (apName/apInstance/addr): " 
									+  apName + "/" +  apInstance + "/" + addr);


							break;//no need to check the rest underlying DIFs
						}


					}
					///////////////////////////////////////////////////////////////////////////////////////////////

					//send the info to its neighbors
					member_t.Builder entry_to_send = member_t.newBuilder();
					entry_to_send.setAddress(addr);
					entry_to_send.setApplicationProcessInstance(IPCApInstance);
					entry_to_send.setApplicationProcessName(IPCApName);

					for(int count = 0 ; count < entry.getUnderlyingDIFsCount(); count++)
					{
						entry_to_send.addUnderlyingDIFs(entry.getUnderlyingDIFs(count));
					}

					sets_to_send.addMember(entry_to_send.buildPartial());
				}
			}

			members_t send = sets_to_send.buildPartial();

			// send update to its neighbors

			if(send.getMemberCount() >=1)
			{
				this.log.debug("There exists new DIF member entry sending to neighbors");

				LinkedList<Neighbor> neighborList = this.neighbors.getNeighborList();

				for(int j = 0; j< neighborList.size(); j++ )
				{
					Neighbor neighbor = neighborList.get(j);

					String dstipcName = neighbor.getApName();
					String dstipcInstance = neighbor.getApInstance();

					int handle = this.irm.allocateFlow(this.apName, this.apInstance, "Management", "1", 
							dstipcName, dstipcInstance, "Management", "1");

					CDAP.objVal_t.Builder  objM_CREATE_directoryforwardingtableentries = CDAP.objVal_t.newBuilder();
					objM_CREATE_directoryforwardingtableentries.setByteval(ByteString.copyFrom(send.toByteArray()));

					CDAP.CDAPMessage M_CREATE = rina.message.CDAPMessageGenerator.generateM_CREATE
							(      "member set",
									"/dif/management/members",
									objM_CREATE_directoryforwardingtableentries.buildPartial(),
									56
									);

					try {
						this.irm.send(handle, M_CREATE.toByteArray());

						this.log.debug("M_CREATE(member set) updates sent over handle " + handle + " to " + dstipcName + "/" + dstipcInstance  );

					} catch (Exception e) {
						// TODO Auto-generated catch block
						this.log.error(e.getMessage());
					}
				}

			}else
			{
				this.log.debug("No new member set entry sending to neighbors");
			}





		}

	}




	private void relayCDAPMessage(CDAPMessage cdapMessage, long dstRINAAddr) {


		//here dstRINAAddr  must be casted to int, otherwise the search in forwardingtable will return null
		//as forwarding table is in format of <Integer, Integer>

		long nextHop = this.forwardingTable.getNextHop((int)dstRINAAddr);

		//this.log.debug("next hop of " +  dstRINAAddr + " is " + nextHop);


		Neighbor neighbor = this.neighbors.getBeighbor(nextHop);

		if( neighbor == null)
		{
			this.log.error("Next hop does not exsit");
		}

		int handleID = this.irm.allocateFlow(this.apName,this.apInstance, "Management", "1",
				neighbor.getApName(),neighbor.getApInstance(), "Management", "1");

		try {
			this.irm.send(handleID, cdapMessage.toByteArray());
			this.log.info( "  M_CREATE_R (flow) sent out over handleID " + handleID);

		} catch (Exception e) {

			this.log.info( "M_CREATE_R(flow) sent error");

			return;

		}

	}


	private void handle_M_CONNECT(CDAPMessage cdapMessage) {


		int result = 0;

		if(cdapMessage.getAuthMech() == CDAP.authTypes_t.AUTH_PASSWD)
		{
			this.log.debug("M_CONNECT with AUTH_PASSWD received");

			CDAP.authValue_t  authValue = cdapMessage.getAuthValue();

			String userName = authValue.getAuthName();
			String passWord = authValue.getAuthPassword();

			result = this.authenticate(userName, passWord);


			// use this underlyingDIF  info to calculate neighbors
			this.dstIPCUnderlyingDIFs = new LinkedList<String>();

			underlyingDIFs_t list;
			try {
				list = underlyingDIFs_t.parseFrom(authValue.getAuthOther());

				for(int i =0; i < list.getUnderlyingDIFsCount();i++)
				{
					this.dstIPCUnderlyingDIFs.add(list.getUnderlyingDIFs(i));
					//System.out.println("dstIPCUnderlyingDIFs " +  i  + " is " + list.getUnderlyingDIFs(i));
				}


			} catch (InvalidProtocolBufferException e) {

				this.log.error(e.getMessage());
			}


			this.log.debug("Enrollment info received in M_CONNECT - userName/passWord/underlyingDIFs: " + userName + "/" + passWord + "/" + this.dstIPCUnderlyingDIFs);

		}else if(cdapMessage.getAuthMech() == CDAP.authTypes_t.AUTH_NONE)
		{
			result = 0;//true all the time
		}else 
		{
			//Implement your own policy here
			//result = 
		}

		CDAP.CDAPMessage M_CONNECT_R = rina.message.CDAPMessageGenerator.generateM_CONNECT_R
				(       
						cdapMessage.getAbsSyntax(),
						result,
						cdapMessage.getAuthMech(), 
						"enrollment information",
						"/daf/management/enrollment",
						cdapMessage.getSrcAEInst(),
						cdapMessage.getSrcAEName(),
						cdapMessage.getSrcApInst(),
						cdapMessage.getSrcApName(),
						cdapMessage.getInvokeID(),
						cdapMessage.getDestAEInst(),
						cdapMessage.getDestAEName(),
						cdapMessage.getDestApInst(),
						cdapMessage.getDestApName(),
						cdapMessage.getVersion()
						);


		try {

			this.irm.send(this.handleID, M_CONNECT_R.toByteArray());


			this.log.debug("M_CONNECT_R sent: " +  cdapMessage.getSrcApName() + "/" + cdapMessage.getSrcApInst() + 
					"/"+cdapMessage.getSrcAEName() + "/" + cdapMessage.getSrcAEInst());

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}


	/**
	 * 
	 * @param userName
	 * @param passWord
	 * @return
	 */
	private int authenticate(String userName, String passWord) {

		if(userName.equals("BU") && passWord.equals("BU"))
		{
			return 0;//true
		}else
		{
			return -1; //false
		}

	}




	private void handle_M_START(CDAPMessage cdapMessage) {

		if(cdapMessage.getObjClass().equals("enrollment information"))
		{
			enrollmentInformation_t ei_msg= null;
			try {
				ei_msg = enrollmentInformation_t.parseFrom(cdapMessage.getObjValue().getByteval());
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			//generate the internal RINA Address for the new member
			this.dstRINAAddr = this.rib.addMember(this.he.getDstApName(),  this.he.getDstApInstance(), this.dstIPCUnderlyingDIFs) ;

			//add underlying DIF info of this ipc
			//note dstIPCUnderlyingDIFs is got in the M_CONNECT message's authenValue
			//this.rib.addUnderlyingDIFsInfo(this.dstRINAAddr, this.dstIPCUnderlyingDIFs);


			//add the new member to its direct neighbors
			Neighbor neighbor = new Neighbor(this.he.getDstApName(), this.he.getDstApInstance(),this.dstRINAAddr);

			//( (Neighbors)rib.getAttribute("neighbors") ).addNeighbor(neighbor);

			this.neighbors.addNeighbor(neighbor);

			//	System.out.println("ttttttttttttttthis.dstRINAAddr enrolled into the dif, its addr " + this.dstRINAAddr );

			//Note: subscription to the new member's routing entry is done when M_STOP_R is received


			//add direct neighbor to its forwarding table 
			//this is bootstrap
			this.forwardingTable.addNextHop((int)neighbor.getAddr(), (int) neighbor.getAddr());



			CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();
			obj.setInt64Val(this.dstRINAAddr);

			CDAP.CDAPMessage M_START_R = rina.message.CDAPMessageGenerator.generateM_START_R
					( 0,
							"address",
							"/daf/management/naming/address",
							obj.buildPartial(),
							cdapMessage.getInvokeID()
							);



			try {

				this.irm.send(this.handleID, M_START_R.toByteArray());

				this.log.debug("M_START_R(address) sent " );

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//////////////////////////////////////////////////////////////////////////////////
			//(1)send to the new member its current DIF member list

			members_t.Builder memberList_to_send = members_t.newBuilder();

			LinkedList<Member> difMemberList = this.rib.getMemberEntryList();

			for(int i = 0; i < difMemberList.size(); i++)
			{
				memberList_to_send.addMember(difMemberList.get(i).convert());
			}

			CDAP.objVal_t.Builder  objM_CREATE_Members = CDAP.objVal_t.newBuilder();

			objM_CREATE_Members.setByteval(ByteString.copyFrom(memberList_to_send.buildPartial().toByteArray()));

			CDAP.CDAPMessage M_CREATE_Members = rina.message.CDAPMessageGenerator.generateM_CREATE
					(     "member set",
							"/dif/management/members",
							objM_CREATE_Members.buildPartial(),
							56
							);


			try {

				this.irm.send(this.handleID, M_CREATE_Members.toByteArray());
				this.log.debug("M_CREATE(member set)sent" );

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				this.log.error(e1.getMessage());
				//e1.printStackTrace();
			}


			//(2)also send to its neighbors about the new member
			LinkedList<Neighbor> neighborList = this.neighbors.getNeighborList();
			members_t.Builder member_update = members_t.newBuilder();
			Member member_new = new Member(this.dstIPCName, this.dstIPCInstance, this.dstRINAAddr, this.dstIPCUnderlyingDIFs);

			member_new.print();

			member_update.addMember(member_new.convert());

			CDAP.objVal_t.Builder  objM_CREATE_directoryforwardingtableentries = CDAP.objVal_t.newBuilder();
			objM_CREATE_directoryforwardingtableentries.setByteval(ByteString.copyFrom(member_update.buildPartial().toByteArray()));

			CDAP.CDAPMessage M_CREATE_updateMember = rina.message.CDAPMessageGenerator.generateM_CREATE
					(      "member set",
							"/dif/management/members",
							objM_CREATE_directoryforwardingtableentries.buildPartial(),
							56
							);


			for(int j = 0; j< neighborList.size(); j++ )
			{
				Neighbor dstNeighbor = neighborList.get(j);

				String dstipcName = dstNeighbor.getApName();
				String dstipcInstance = dstNeighbor.getApInstance();

				if(dstipcName.equals(this.dstIPCName) && dstipcInstance.equals(this.dstIPCInstance))
				{
					continue; // no need to send to the new member, as it is also the enroller's neighbor
				}

				int handle = this.irm.allocateFlow(this.apName, this.apInstance, "Management", "1", 
						dstipcName, dstipcInstance, "Management", "1");

				try {
					this.irm.send(handle, M_CREATE_updateMember.toByteArray());

					this.log.debug("New member joined the DIF, so M_CREATE(member set) updates sent over handle " + handle + " to neighbor " + dstipcName + "/" + dstipcInstance );

				} catch (Exception e) {
					// TODO Auto-generated catch block
					this.log.error(e.getMessage());
				}
			}



			//send to the new member its the neighbors, based on its underlying DIF (or underlying Internet name) 

			neighbors_t neighbors = this.calculateNeighbors();

			CDAP.objVal_t.Builder  objM_CREATE_Neighbour = CDAP.objVal_t.newBuilder();

			objM_CREATE_Neighbour.setByteval(ByteString.copyFrom(neighbors.toByteArray()));

			CDAP.CDAPMessage M_CREATE = rina.message.CDAPMessageGenerator.generateM_CREATE
					(      "neighbor set",
							"/daf/management/neighbors",
							objM_CREATE_Neighbour.buildPartial(),
							56
							);


			try {

				this.irm.send(this.handleID, M_CREATE.toByteArray());
				this.log.debug("M_CREATE(neighbour set)sent" );

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				this.log.error(e1.getMessage());
				//e1.printStackTrace();
			}

			//send all existing reachable applications(directory forwarding table)
			//if any
			if(this.directoryForwardingTable.getSize() > 0)
			{

				CDAP.objVal_t.Builder  objM_CREATE_DFT = CDAP.objVal_t.newBuilder();

				objM_CREATE_DFT.setByteval(ByteString.copyFrom(this.directoryForwardingTable.convert().toByteArray()));


				CDAP.CDAPMessage M_CREATE_DFT = rina.message.CDAPMessageGenerator.generateM_CREATE
						(     "directoryforwardingtableentry set",
								"/dif/management/flowallocator/directoryforwardingtableentries",
								objM_CREATE_DFT.buildPartial(),
								56
								);


				try {

					this.irm.send(this.handleID, M_CREATE_DFT.toByteArray());
					this.log.debug("M_CREATE(directoryforwardingtableentry set)sent" );

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					this.log.error(e1.getMessage());
					//e1.printStackTrace();
				}


			}

			//send M_STOP enrollment
			CDAP.objVal_t.Builder  objM_STOP = CDAP.objVal_t.newBuilder();
			objM_STOP.setBoolval(true);
			CDAP.CDAPMessage M_STOP = rina.message.CDAPMessageGenerator.generateM_STOP
					(      "enrollment information",
							"/daf/management/enrollment",
							objM_STOP.buildPartial(),
							54
							);

			try {
				this.irm.send(this.handleID, M_STOP.toByteArray());
				this.log.debug("M_STOP sent " );

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				this.log.error(e1.getMessage());
				//e1.printStackTrace();
			}


		}
	}


	//calculate neighbor of the members based on its underlying DIF info

	private neighbors_t calculateNeighbors() {

		//this returns all ipc using the same underlying DIF, including the new ipc itself


		neighbors_t.Builder  neighbours_toSend = neighbors_t.newBuilder();


		int num = this.dstIPCUnderlyingDIFs.size();

		this.log.debug("calculateNeighbors(): the new IPC member has " + num + " underlying DIFs, and they are " + this.dstIPCUnderlyingDIFs);


		for(int j=0; j < num ;j++)
		{

			LinkedList<Neighbor> neighbors = this.rib.getIPCListOfUnderlyingDIFName(this.dstIPCUnderlyingDIFs.get(j));

			//		System.out.println(" nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnneighbors.size() is "  +  neighbors.size());


			for(int i = 0; i< neighbors.size();i++)
			{
				Neighbor neighbor = neighbors.get(i);

				//			System.out.println("iiiiiiiiiiiiiiiiiiiiiiii is " + i);

				//remove the new ipc itself, since it is not its own neighbor
				if(neighbor.getAddr() == this.dstRINAAddr) {continue;}


				neighbor.print();

				neighbours_toSend.addNeighbor(neighbor.convert());



				//NOTE: here we don't send M_CREATE(neighbour set), instead we use the member set update to update everyone possible direct neighbors
				// when a membe set M_CREATE is received, every ipc check the new member's underlying DIFs info, to see if it is a direct neighbor
				//then sub to it as regular
				/*
				//update everyone with this new neighbor information
				//////////////////////////////////////////////////////////////////////////////////////
				String dstipcName = neighbor.getApName();
				String dstipcInstance = neighbor.getApInstance();
				int dstAddr = (int) neighbor.getAddr();


				//this is the authenticator itself, no need to send
				if(dstipcName.equals(this.apName) && dstipcInstance.equals(this.apInstance)) {continue;}

				int handle = this.irm.allocateFlow(this.apName, this.apInstance, "Management", "1", 
						dstipcName, dstipcInstance, "Management", "1");

				CDAP.objVal_t.Builder  objM_CREATE_Neighbour = CDAP.objVal_t.newBuilder();

				neighbors_t.Builder neighbors_to_send = neighbors_t.newBuilder();

				neighbors_to_send.addNeighbor( new Neighbor(this.dstIPCName, this.dstIPCInstance, this.dstRINAAddr).convert());


				objM_CREATE_Neighbour.setByteval(ByteString.copyFrom(neighbors_to_send.buildPartial().toByteArray()));

				CDAP.CDAPMessage M_CREATE = message.generator.CDAPMessage.generateM_CREATE
						(      "neighbor set",
								"/daf/management/neighbors",
								objM_CREATE_Neighbour.buildPartial(),
								56
								);


				try {

					this.irm.send(handle, M_CREATE.toByteArray());

					this.log.debug("M_CREATE(neighbour set) update sent, due to new member joining the DIF, new member info " 
							+ this.dstIPCName + "/"  + this.dstIPCInstance + "/" + this.dstRINAAddr);

				} catch (Exception e1) {

					this.log.error(e1.getMessage());

				}
				//////////////////////////////////////////////////////////////////////////////////////
				 */



			}

		}

		return neighbours_toSend.buildPartial();

	}




	private void handle_M_STOP_R(CDAPMessage cdapMessage) {

		if(cdapMessage.getObjClass().equals("enrollment information"))
		{
			if(cdapMessage.getResult() == 0)
			{
				CDAP.CDAPMessage M_START = rina.message.CDAPMessageGenerator.generateM_START(
						"operationstatus",
						"/daf/management/operationalStatus",
						0);


				try {
					this.irm.send(this.handleID, M_START.toByteArray() );

					this.log.debug("M_START (operationstatus) sent " );

					this.log.debug("New member is enrolled into the DIF on handleID " +  this.handleID );


					//subscribe to the new member's routing entry
					if(this.routingProtocol.equals("linkState"))
					{

						this.routingDaemon.addLinkStateRoutingEntrySubEvent( Long.toString(this.dstRINAAddr) );

					}else if(this.routingProtocol.equals("distanceVector"))
					{
						//TODO
					}

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					this.log.error(e1.getMessage());
					//e1.printStackTrace();
				}

			}
		}
	}


}
