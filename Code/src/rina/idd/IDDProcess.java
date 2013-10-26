
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.config.RINAConfig;
import rina.irm.impl.IRMImpl;
import rina.object.internal.IDDRecord;
import rina.rib.impl.RIBImpl;
import rina.tcp.TCPFlow;

/**
 * IDD Process is responsible for DIF registration, Application registration and their resolution.
 * Basically it is to resolve a DIF name to a IPC process which can enroll new members, 
 * or resolve a Application name to a DIF name which could reach that application.
 * It is similar to DNS in the current implementation. 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class IDDProcess extends Thread {

	private Log log = LogFactory.getLog(this.getClass());

	private RINAConfig  config =  null;

	private RIBImpl rib = null;

	private boolean listening = true;

	private TCPFlow listeningFlow = null;

	private String IDDName;
	private int IDDPort;
	
	//This stores all IDD record
	//For DIF, key is DIF name 
	//For APP, key is apName + apInstance
	private LinkedHashMap<String, IDDRecord> iddDatabase = null;
	
	public IDDProcess(String configurationFile)
	{
		this.config = new RINAConfig(configurationFile);

		this.rib = new RIBImpl();
		this.rib.addAttribute("config",  config);
		
		
		this.IDDPort = this.config.getIDDPort();
		this.IDDName = this.config.getIDDName();
		
		this.iddDatabase = new  LinkedHashMap<String, IDDRecord>();
	
		this.rib.addAttribute("iddDatabase", this.iddDatabase);
		
	}

	public void stopIDD()
	{
		
		this.listening = false;
	//	listeningFlow.close();
		listeningFlow = null;
	}
	
	public void run()
	{
		this.log.info("IDD Process started.");

		try {
			listeningFlow = new TCPFlow(this.IDDPort);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		while(listening)
		{
			try {
				
				TCPFlow clientFlow = listeningFlow.accept();	

				this.log.info("IDD Process: new incoming flow from "
						+ clientFlow.getSocket().getInetAddress() + " : " 
						+ clientFlow.getSocket().getPort());

				new IDDHandler(clientFlow, this.rib).start();

			} catch (Exception e) {
				//e.printStackTrace();
				this.log.info("IDD process stopped due to exception.");
				System.exit(-1); //without this line it loops forever with the error (for example if the port is already in use)
			}
		}
	}

}
