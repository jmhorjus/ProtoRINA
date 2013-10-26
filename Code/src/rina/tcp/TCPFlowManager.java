/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 * @author Yuefeng Wang. Computer Science Department, Boston University 
 */

/**
 * This is a component of the IPC Process that responds to allocation Requests from Application Processes
 * @author Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0
 */

package rina.tcp;

import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.config.RINAConfig;
import rina.dns.DNSMessage;
import rina.irm.util.HandleEntry;
import rina.message.CDAP;
import rina.message.DTP;
import rina.object.gpb.DNS;
import rina.object.gpb.IDDMessage_t.iddMessage_t;
import rina.object.internal.IDDRecord;
import rina.rib.impl.RIBImpl;
import rina.tcp.util.IncomingCom;
import rina.tcp.util.IncomingComControl;
import rina.tcp.util.IncomingComData;
import rina.tcp.util.OutgoingComHandler;
import rina.tcp.util.TCPFlowAllocated;
import rina.tcp.util.WireListenerTCP;
import rina.tcp.util.WellKnownRINAAddr.DirectoryEntry;

import com.google.protobuf.InvalidProtocolBufferException;

public class TCPFlowManager {

	private Log log = LogFactory.getLog(this.getClass());

	private RIBImpl rib = null;

	private RINAConfig config = null;

	private String DIFName = null;
	
	//note for BU case, IPCName  = IPCName + IPCInstance from configuration file
	private String IPCName = null;

//	// this is only used when talking to RINA community
//	// when BU case which is using TCP as wire, IPCInstance is not used,
//	//only IPC name is used(where IPCName  = IPCName + IPCInstance from configuration file  e.g BostonU1, BostonU2 )
//	private String IPCInstance = null;
//	private IncomingComControl incomingTCPConnectionControl = null; 
//	private IncomingComData incomingTCPConnectionData = null; 
//	private TCPFlow listeningTCPFlowControl = null;
//	private TCPFlow listeningTCPFlowData = null;
//	private int controlTCPPortID = -1;
//	private int dataTCPPortID = -1;
	
	private TCPFlowAllocated flowAllocated;


	private IncomingCom incomingTCPConnection = null; // this is used when we use only one TCP flow as the wire.
	private TCPFlow listeningTCPFlow = null;// this is used when we use only one TCP flow as the wire.
	private int tcpPortID = -1;// this is used when we use only one TCP flow as the wire.



	/**
	 * The following four attributes are used for DNS process provided in the rina.dns
	 */
	private String DNSName;

	private int DNSPort;

	private TCPFlow dnsFlow = null;

	private LinkedHashMap<String, DNS.DNSRecord> dataBase = null;
	
	private TCPFlow iddFlow = null;
	private String IDDName = null;
	private int IDDPort = -1;



	/**
	 * Case 1:
	 *  this is used when we use only one TCP flow as the wire.
	 *  BU case
	 */
	public TCPFlowManager(RIBImpl rib)

	{ 
		this.rib = rib;

		this.flowAllocated = new TCPFlowAllocated(this.rib);
		
		
		this.config = (RINAConfig)this.rib.getAttribute("config");


		//here IPCName is the name of one side of the wire 
		//it is the concatenation of IPCName + IPCInstance
		this.IPCName =  this.rib.getAttribute("ipcName").toString() + this.rib.getAttribute("ipcInstance").toString();

//		this.registerApplicationOnWire(this.rib.getAttribute("ipcName").toString(), this.rib.getAttribute("ipcInstance").toString());

		if( this.rib.getAttribute("difName") != null)
		{
			this.DIFName = this.rib.getAttribute("difName").toString();
		}


		this.DNSName = this.config.getDNSName();

		this.DNSPort = this.config.getDNSPort();

		//create TCP listening  thread 
		this.tcpPortID =  this.config.getTCPPort();

		this.dataBase = new LinkedHashMap<String, DNS.DNSRecord>();

		// if the registration to DNS failed, process stopped
		if( this.registerToDNS() == false ) {System.exit(-1);}
		
		
		//init IDD flow 
		this.IDDName = this.config.getIDDName();
		this.IDDPort = this.config.getIDDPort();
		try {
			this.iddFlow = new TCPFlow(this.IDDName, this.IDDPort);
			this.log.debug("Flow to IDD created");
		} catch (Exception e1) {
			this.log.error(e1.getMessage());
			e1.printStackTrace();
		}



		try {
			this.listeningTCPFlow = new TCPFlow(this.tcpPortID);
			this.incomingTCPConnection = new IncomingCom(this.listeningTCPFlow, this.flowAllocated);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			//TESTME
			this.log.error(e.getMessage());
			System.exit(-1);
		}


		this.initWire();


	}

	/**
	 * This is to set up the wire connectivity by initializing tcp connections.
	 */
	private void initWire() {


//		// wait some time for others to bootstrap 
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


		boolean stop = true;
		int i =1;

		while(stop)
		{
			String neihbour = this.config.getNeighbour(i);
			if(neihbour == null)
			{
				stop = false;
			}else
			{
				this.allocateWire(neihbour.trim());
				i++;
			}
		}

		this.log.info("IPC Process "+this.IPCName + " : init Connection done");


	}

//	/**
//	 * This is used to debug with RINA community (Shim layer)
//	 * @param controlTCPPortID
//	 * @param dataTCPPortID
//	 */
//	public TCPFlowManager(String apName, String apInstance,int controlTCPPortID, int dataTCPPortID)
//
//	{ 
//		this.IPCName = apName;
//		this.IPCInstance = apInstance;
//
//		this.controlTCPPortID = controlTCPPortID;
//		this.dataTCPPortID = dataTCPPortID;
//
//		this.log.debug("apName:" + apName + ",apInstance:" +apInstance 
//				+ ",controlTCPPortID:" + controlTCPPortID + ",dataTCPPortID:" +dataTCPPortID);
//
//
//		this.flowAllocated = new TCPFlowAllocated(this.IPCName,this.IPCInstance);
//
//		try {
//			this.listeningTCPFlowControl = new TCPFlow(this.controlTCPPortID);
//			this.listeningTCPFlowData = new TCPFlow(this.dataTCPPortID);
//
//			this.incomingTCPConnectionControl = new IncomingComControl(this.listeningTCPFlowControl, this.flowAllocated);
//			this.incomingTCPConnectionData = new IncomingComData(this.listeningTCPFlowData, this.flowAllocated);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//	}


//	/**
//	 * this one is used to debug with RINA community
//	 * and this function will be called from outside
//	 * @param srcApName
//	 * @param srcApInstance
//	 * @param srcAeName
//	 * @param srcAeInstance
//	 * @param dstApName
//	 * @param dstApInstance
//	 * @param dstAeName
//	 * @param dstAeInstance
//	 * @return
//	 */
//	public synchronized  int allocateTCPFlow(String srcApName, String srcApInstance, String srcAeName, String srcAeInstance,
//			String dstApName, String dstApInstance, String dstAeName, String dstAeInstance)
//	{
//		int flowID = -1;
//
//		if(srcAeInstance == null) 
//		{
//			srcAeInstance = "1";
//		}
//
//		if(dstAeInstance == null)
//		{
//			dstAeInstance = "1";
//		}
//
//		DirectoryEntry directoryEntry = null;
//
//		if(dstAeName.equals("Management")||dstAeName.equals("Control"))
//		{
//			directoryEntry = this.flowAllocated.getWellKnownRINAAddr().getManagementEntry(dstApName, dstApInstance);
//		}else if (dstAeName.equals("Data Transfer") || dstAeName.equals("Data"))
//		{
//			directoryEntry = this.flowAllocated.getWellKnownRINAAddr().getDataEntry(dstApName, dstApInstance);
//		}
//
//		directoryEntry.print();
//
//		String srcName = srcApName+srcApInstance+srcAeName+srcAeInstance;
//		String dstName = dstApName+dstApInstance+dstAeName+dstAeInstance;
//
//		flowID = this.allocateTCPFlow(srcName,dstName, directoryEntry.getHostIP(), directoryEntry.getPort());
//
//		if(flowID ==-1)
//		{
//			return -1;
//		}
//
//		if(dstAeName.equals("Management") ||  dstAeName.equals("Control")) //this is a control flow
//		{
//			CDAP.CDAPMessage M_CONNECT = rina.message.CDAPMessageGenerator.generateM_CONNECT
//					(
//							dstAeInstance,//destAEInst
//							dstAeName,//destAEName
//							dstApInstance,//destApInst
//							dstApName,//destApName
//							15,
//							srcAeInstance,//srcAEInst
//							srcAeName,//srcAEName
//							srcApInstance,//srcApInst
//							srcApName//srcApName
//							);
//
//
//
//			DTP dtp_first = new DTP(M_CONNECT);
//
//			try {
//				this.send(flowID, dtp_first.toBytes());
//				this.log.info("first DTP message containing a M_CONNECT sent out over a management flow");
//
//			} catch (Exception e) {
//				this.log.error(e.getMessage());
//			}
//
//			//wait a M_CONNECT R then it is all set
//
//			byte[] msg = this.receive(flowID);
//
//			DTP dtp = new DTP(msg);
//
//			CDAP.CDAPMessage cdapMessage = null;
//
//			try {
//				cdapMessage = CDAP.CDAPMessage.parseFrom( dtp.getPayload());
//
//			} catch (InvalidProtocolBufferException e) {
//
//				this.log.error(e.getMessage());
//
//				if(flowID != -1)
//				{
//					this.flowAllocated.receive(flowID);
//					return -1;
//				}
//			}
//
//
//			if(cdapMessage.getOpCode().toString().equals("M_CONNECT_R") && cdapMessage.getResult() == 0)
//			{
//				this.log.info("M_CONNECT_R recived over flow " +  flowID);
//				this.log.info("TCP flow allocated successful between " + srcName + " and " + dstName + " with flow ID " + flowID);
//			}else
//			{
//				this.log.info("TCP flow allocated failed between " + srcName + " and " + dstName + " with flow ID " + flowID);
//
//				if(flowID != -1)
//				{
//					this.flowAllocated.receive(flowID);
//				}
//				return -1;
//			}
//
//		}else if(dstAeName.equals("Data Transfer") ||dstAeName.equals("Data") )//this.is a data flow	
//		{
//			//send a data DTP message to the other side 
//
//			int srcRINAAddr = this.flowAllocated.getWellKnownRINAAddr().getRINAAddr();
//			int dstRINAAddr = this.flowAllocated.getWellKnownRINAAddr().getRINAAddr(dstApName, dstApInstance);
//
//			this.log.debug("srcRINAAddr " + srcRINAAddr);
//			this.log.debug("dstRINAAddr " + dstRINAAddr);
//
//
//			DTP first_dtp = new DTP ( (short)srcRINAAddr, (short)srcRINAAddr, (short)0, (short)0, (byte)0xC1);
//
//
//			try {
//				this.send(flowID, first_dtp.toBytes());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				this.log.error(e.getMessage());
//			}
//		}
//
//		return flowID;
//	}



	/**
	 * this one is only called by method inside this class, not from outside
	 * @param srcName
	 * @param dstName
	 * @param ipAddr
	 * @param portID
	 * @return
	 */
	private synchronized  int allocateTCPFlow(String srcName, String dstName, String ipAddr, int portID)
	{

		TCPFlow tcpFlow = null;

		int flowID = -1;

		String ip = ipAddr;
		int tcpPort = portID;



		//attach a handler thread for this flow to receive msg and put it in the msgQueue
		try{

			tcpFlow = new TCPFlow(ip, tcpPort);

			tcpFlow.setSrcName(srcName);
			tcpFlow.setDstName(dstName);

			flowID = this.flowAllocated.addTCPFlow(srcName+dstName, tcpFlow);

			new OutgoingComHandler(tcpFlow).start();			

			this.log.info("TCP flow allocated between " + srcName + " and " + dstName + " with flow ID " + flowID 
					+ ", ipAddr is " + ipAddr + " and portID is " + portID + ". Next send M_CONNECT");


		}catch(Exception e)
		{

			this.log.error(e.getMessage());
			this.log.error("TCP flow allocated failed between " + srcName + " and " + dstName );

			if(flowID != -1)
			{
				this.flowAllocated.removeTCPFlow(flowID);
			}

			return -1;

		}


		return flowID;

	}

	public synchronized  void deallocateTCPFlow(int flowID){}


	
	/**
	 * For BU case, flowID is the wireID
	 * @param flowID
	 * @param msg
	 * @throws Exception
	 */
	public synchronized void send (int flowID, byte[]msg) throws Exception
	{
		this.flowAllocated.send(flowID, msg);
	}

	public byte[] receive(int flowID)
	{
		return this.flowAllocated.receive(flowID);
	}
	
	/**
	 * This is used for BU case where multiple flows are mapping on a wire
	 * @param flowID
	 * @return
	 */
	public byte[] receive(int wireID, int portID)
	{
		return this.flowAllocated.receive(wireID, portID);
	}


	/**
	 * Allocate a wire 
	 * Here IPCName =  apName + apInstance
	 * @param IPCName
	 * @return
	 */
	private synchronized  TCPFlow allocateWire(String IPCName)
	{

		TCPFlow tcpFlow = null;

		if(this.flowAllocated.hasTCPFlow(IPCName))
		{
			tcpFlow = this.flowAllocated.getTCPFlow(IPCName);
			this.log.info("Wire to " + IPCName + " allocated before.");
			return tcpFlow;
		}

		if(!this.dataBase.containsKey(IPCName))
		{
			this.queryDNS(IPCName);
		}


		DNS.DNSRecord dr = this.dataBase.get(IPCName);


		String ip = dr.getIp();
		int tcpPort = dr.getPort();

		if(ip.equals(" "))
		{
			this.log.error(this.IPCName + ": " + IPCName + " is not found on DNS Server" );
			return null;
		}


		try {
			tcpFlow = new TCPFlow(ip, tcpPort);
		} catch (Exception e1) {
			this.log.error(e1.getMessage());
			return null;
		}

		tcpFlow.setSrcName(this.IPCName);//this.IPCName is the name of host IPC which contains this TCP Manager
		tcpFlow.setDstName(IPCName); // IPCName is a local variable only in this method

		//here flowID is the wireID
		int flowID = -1;
		flowID = this.flowAllocated.addTCPFlow(IPCName, tcpFlow);

		//generate flow ID

		//should get a message back from the other side about his IPC Name
		// but for now we omit this part FIXME

		//send a message to the other side tells which IPC it is
		// here we just make it a byte[]

		byte [] first_msg = this.IPCName.getBytes();


		try {
			tcpFlow.send(first_msg);

		} catch (Exception e) {
			// TODO Auto-generated catch block

			//remove this flow from flowAllocated
			if(flowID != -1)
			{
				this.flowAllocated.removeTCPFlow(flowID);
			}

			this.log.error(e.getMessage());

			this.log.error(this.IPCName + ": New wire to " +  this.IPCName + " failed");

			return null;
		}


		this.log.info(this.IPCName + ": New wire is added to  " + IPCName + ", flowID is " + flowID);


		//attach a wireListener thread for this flow to receive msg and put it in the msgQueue
		try
		{

			//new OutgoingComHandler(tcpFlow).start();	
			
			this.flowAllocated.addWireListener(flowID, new WireListenerTCP(tcpFlow, this.flowAllocated));

		}catch(Exception e)
		{

			this.log.error(e.getMessage());
			return null;
		}

		return tcpFlow;


	}

	/**
	 * used for a wire in BU case
	 * @return
	 */
	private boolean registerToDNS()
	{

		try {
			this.dnsFlow = new TCPFlow(this.DNSName, this.DNSPort);
			DNS.DNSRecord register = DNSMessage.generateDNS_REG(this.IPCName,this.tcpPortID);
			this.dnsFlow.send(register.toByteArray());
		} catch (Exception e1) {
			this.log.error(e1.getMessage());
			this.log.error( this.IPCName + ":Registration to DNS failed. Process stopped");
			return false;
		}

		this.log.info( this.IPCName + ":Registration to DNS successed");
		return true;

	}

	/**
	 * used for allocating a wire in BU case
	 * @param IPCName
	 */
	private  void queryDNS(String IPCName)

	{
		DNS.DNSRecord query = DNSMessage.generateDNS_QUERY(IPCName);

		try {
			this.dnsFlow.send(query.toByteArray());
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			//e2.printStackTrace();
			this.log.error(e2.getMessage());
		}

		byte[] reply = null;
		try {
			reply = dnsFlow.receive();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			this.log.error(e1.getMessage());
		}

		DNS.DNSRecord dnsMessage = null;
		try {
			dnsMessage = DNS.DNSRecord.parseFrom(reply);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		this.dataBase.put(IPCName, dnsMessage);

		this.log.info( this.IPCName + ":DNS Query of " + IPCName +  " finished");

	}

	public synchronized int getWireID(String name)
	{
		return this.flowAllocated.getTCPFlowID(name);
	}

	public synchronized int addDIF0FlowOnWire(int wireID, HandleEntry he) 
	{

		return this.flowAllocated.addDIF0FlowOnWire(wireID, he);

	}
	
	
	public synchronized void removeDIF0FlowOnWire(int wireID, HandleEntry he) {
		
		this.flowAllocated.removeDIF0FlowOnWire(wireID, he);
		
	}

	public IDDRecord queryIDD(iddMessage_t iddRequestMsg) 
	{
		IDDRecord iddRecord = null;
		
		try {
			this.iddFlow.send(iddRequestMsg.toByteArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		byte[] reply = null;
		try {
			 reply = this.iddFlow.receive();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		iddMessage_t iddResponseMsg = null;
		
		try {
			iddResponseMsg = iddMessage_t.parseFrom(reply);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		this.log.debug("IDD Repsonse message recevied with result " + iddResponseMsg.getResult());
		
		
		if(iddResponseMsg.getResult() == 0) //true
		{
			iddRecord = new IDDRecord(iddResponseMsg);
		}else 
		{
			return null;
		}
		
	
		return iddRecord;
		
	}

	public void registerToIDD(iddMessage_t iddRegMsg) {
		
		try {
			this.iddFlow.send(iddRegMsg.toByteArray());
			this.log.debug("IDD REG message sent");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

//	/**
//	 * This is used to register application on the wire in BU case
//	 *  one for Control AE, and one for Data AE
//	 * @param ApName
//	 * @param ApInstance
//	 */
//	private  void registerApplicationOnWire(String ApName, String ApInstance) {
//
//		this.flowAllocated.registerApplicationOnWire(ApName,ApInstance);
//
//	}


}
