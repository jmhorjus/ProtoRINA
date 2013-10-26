/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */

package rina.idd;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.object.gpb.IDDMessage_t.iddMessage_t;
import rina.object.gpb.IDDMessage_t.opCode_t;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.IDDRecord;
import rina.object.internal.IDDRecord.AppRecord;
import rina.rib.impl.RIBImpl;
import rina.tcp.TCPFlow;

/**
 * IDD process creates an IDDHanlder for each process communicating with it
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class IDDHandler extends Thread {


	private Log log = LogFactory.getLog(this.getClass());

	private RIBImpl rib = null;
	private TCPFlow clientFlow = null;

	private boolean stop = false;

	//For DIF, key is DIF name 
	//For APP, key is apName + apInstance
	private LinkedHashMap<String, IDDRecord> iddDatabase = null;


	public IDDHandler(TCPFlow clientFlow, RIBImpl rib) 
	{

		this.clientFlow = clientFlow;
		this.rib = rib;	
		
		this.iddDatabase = (LinkedHashMap<String, IDDRecord>) this.rib.getAttribute("iddDatabase");

	}


	public void run()
	{
		byte[] msg = null;

		while(!stop)
		{
			try {
				msg = this.clientFlow.receive();
				
				
			} catch (Exception e) {
				//this.log.error(e.getMessage());
				this.log.info("IDD handler stopped due to exception.");
				return;
			}
			
			this.handleReceivedMessage(msg);
			
		}

	}


	private void handleReceivedMessage(byte[] msg) {


		iddMessage_t IDDMsg = null;
		try {
			IDDMsg = iddMessage_t.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		this.log.debug("IDD message received with opcode " +  IDDMsg.getOpCode());

		if(IDDMsg.getOpCode().toString().equals("Request"))
		{
			this.handleRequest(IDDMsg);

		}else if (IDDMsg.getOpCode().toString().equals("Register"))
		{
			this.handleRegister(IDDMsg);
		}else
		{
			this.log.error("IDD Message cannot be handled");
		}




	}

	//handle register message	
	private void handleRegister(iddMessage_t iddMsg) {

		IDDRecord iddRecord = null;
		
		if(iddMsg.hasDifName()) //DIF reg
		{

			//For DIF, key is DIFName
			String DIFName = iddMsg.getDifName();


			this.log.debug("DIFName is " +  DIFName);
			
			if(!this.iddDatabase.containsKey(DIFName)) //New Entry
			{
				iddRecord =  new IDDRecord(iddMsg);
				this.iddDatabase.put(DIFName,iddRecord);
			}else
			{
				//remember one DIF may have multiple authenticator, which means IDD may receive REG message from different 
				//IPC process for same DIF

				 iddRecord = this.iddDatabase.get(DIFName);

				//update timestamp, basically it is the last time the record is modified

				if(iddRecord.getTimeStamp() <= iddMsg.getTimeStamp())
				{
					iddRecord.setTimeStamp(iddMsg.getTimeStamp());
				}

				LinkedList<ApplicationProcessNamingInfo> authNameList = iddRecord.getAuthenticatorNameInfoList();

				//FIXME: Here we don't remove the duplicate record, 
				//so the queryer later have to deal with this once they got an response
				for(int i = 0; i < iddMsg.getAuthenticatorNameInfoCount(); i++)
				{
					authNameList.add(new ApplicationProcessNamingInfo ( iddMsg.getAuthenticatorNameInfo(i) )  );
				}


			}

		}else //APP reg
		{

			//this can be application name or service name
			//For service name, such as "relay" service
			ApplicationProcessNamingInfo appInfo = new ApplicationProcessNamingInfo ( iddMsg.getApplicationNameInfo() );

			String apName = appInfo.getApName();
			String apInstance = appInfo.getApInstance();

			//For app, key is apName + apInstance 
			String key  = apName + apInstance;


			if(!iddDatabase.containsKey(key)) //New Entry
			{
				iddRecord = new IDDRecord(iddMsg);
				
				this.iddDatabase.put(key, iddRecord);
			}else 
			{
				 iddRecord = this.iddDatabase.get(key);

				//update timestamp, basically it is the last time the record is modified

				if(iddRecord.getTimeStamp() <= iddMsg.getTimeStamp())
				{
					iddRecord.setTimeStamp(iddMsg.getTimeStamp());
				}


				LinkedList<AppRecord> appRecordList = iddRecord.getAppRecordList();

				for(int i = 0 ; i < iddMsg.getIddResponseCount(); i++)
				{
					iddRecord.addAppRecord(iddMsg.getIddResponse(i)) ;
				}


			}

		}

		
		this.log.debug("REG new IDD entry added");
		iddRecord.print();


	}

	//handle IDD  Request (query) message and send back Response message
	private void handleRequest(iddMessage_t iddMsg) {

		String key = null;

		if(iddMsg.hasDifName()) //DIF Name query
		{
			key = iddMsg.getDifName();

		}else // APP query
		{
			key = iddMsg.getApplicationNameInfo().getApplicationProcessName() 
					+ iddMsg.getApplicationNameInfo().getApplicationProcessInstance(); 
		}



		int result = -1;
		// 0 true
		// 1 false

		iddMessage_t.Builder responseMsg = iddMessage_t.newBuilder();
		responseMsg.setOpCode(opCode_t.Response);

		if(this.iddDatabase.containsKey(key))
		{
			result = 0;
			responseMsg.setResult(result);

			IDDRecord iddRecord = this.iddDatabase.get(key);

			if(iddRecord.getType().toString().equals("DIF")) //DIF response
			{

				responseMsg.setDifName(iddRecord.getDIFName());

				//POLICY HOLDER
				//return all authenticators
				//YOU can implement your own policy

				for(int i = 0; i < iddRecord.getAuthenticatorNameInfoList().size(); i++)
				{
					responseMsg.addAuthenticatorNameInfo(iddRecord.getAuthenticatorNameInfoList().get(i).convert());
				}

			}else //APP response
			{
				responseMsg.setApplicationNameInfo(iddRecord.getApplicationProcessInfo().convert());

				for(int i = 0; i < iddRecord.getAppRecordList().size(); i++)
				{
					responseMsg.addIddResponse(iddRecord.getAppRecordList().get(i).convert());
				}

			}
			
			responseMsg.setTimeStamp(iddRecord.getTimeStamp());



		}else
		{
			result = 1; // no such key exist
			responseMsg.setResult(result);

		}


		try {
			this.clientFlow.send(responseMsg.buildPartial().toByteArray());
			this.log.info("IDD reponse sent");
		} catch (Exception e) {
			this.log.error(e.getMessage());
			e.printStackTrace();
		}
	}



}
