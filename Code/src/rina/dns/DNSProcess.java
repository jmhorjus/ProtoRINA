package rina.dns;

/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */


import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.config.RINAConfig;
import rina.object.gpb.DNS;
import rina.tcp.TCPFlow;



/**
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 *   
 */

public class DNSProcess  extends Thread{


	private Log log = LogFactory.getLog(this.getClass());
	private LinkedHashMap<String, DNS.DNSRecord> dataBase = null;
	
	

	private String DNSName;

	private String DNSIP;

	private int DNSPort;
	
	private RINAConfig config;

	private boolean running = true;
	
	private TCPFlow listeningFlow = null;

	public DNSProcess(RINAConfig config)
	{
		this.dataBase = new LinkedHashMap<String, DNS.DNSRecord>();
		this.config = config;
		this.DNSPort = Integer.parseInt(this.config.getProperty("rina.dns.port"));
		this.DNSName = this.config.getProperty("rina.dns.name");

		
	}


	public DNSProcess(String DNSName, String DNSIP, int DNSPort)
	{
		this.dataBase = new LinkedHashMap<String, DNS.DNSRecord>();
		this.DNSName = DNSName;
		this.DNSIP = DNSIP;
		this.DNSPort = DNSPort;

	}

	public void stopDNS()
	{
		
		this.running = false;
	//	listeningFlow.close();
		listeningFlow = null;
	}
	
	public void run()
	{
		
		this.log.info("DNS Process started.");

		try {
			listeningFlow = new TCPFlow(this.DNSPort);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		
		while(running)
		{
			try {
				TCPFlow clientFlow = listeningFlow.accept();	
				
				this.log.info("DNS Process: new request received from "
						+ clientFlow.getSocket().getInetAddress() + " : " 
						+ clientFlow.getSocket().getPort());
				
				new DNSHandler(clientFlow, this.dataBase).start();

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1); //without this line it loops forever with the error (for example if the port is already in use)
			}
		}
	}


}
