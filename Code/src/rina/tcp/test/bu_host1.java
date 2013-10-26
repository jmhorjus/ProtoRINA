/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.tcp.test;

import rina.config.RINAConfig;
import rina.rib.impl.RIBImpl;
import rina.tcp.TCPFlowManager;


/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class bu_host1 {

	public static void main(String args[])
	{
		String file = "./experimentConfigFiles/testConfigFiles/bu_host1.properties";	
		RINAConfig config = new RINAConfig(file);


		String IPCName = config.getIPCName();
		String IPCInstance = config.getIPCInstance();
		String DIFName = config.getDIFName();

		RIBImpl rib = new RIBImpl();

		//put the information into the RIB
		rib.addAttribute("config",  config);
		rib.addAttribute("difName",  DIFName);
		rib.addAttribute("ipcName",  IPCName);
		rib.addAttribute("ipcInstance",  IPCInstance);
		

		TCPFlowManager tm = new TCPFlowManager(rib);
		
		int wierIDtoBUHost2 = tm.getWireID("BostonU2");
		
		System.out.println("wierIDtoBUHost2 " + wierIDtoBUHost2);
		
		try {
			tm.send(wierIDtoBUHost2, "hello".getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		


	}

}
