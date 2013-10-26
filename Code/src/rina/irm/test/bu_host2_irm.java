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

package rina.irm.test;

import rina.config.RINAConfig;
import rina.irm.impl.IRMImpl;
import rina.rib.impl.RIBImpl;
import rina.tcp.TCPFlowManager;

public class bu_host2_irm {

	public static void main(String args[])
	{
		String file = "./experimentConfigFiles/testConfigFiles/bu_host2.properties";	
		RINAConfig config = new RINAConfig(file);

		RIBImpl rib = new RIBImpl();



		//put the information into the RIB
		rib.addAttribute("config",  config);


		IRMImpl irm = new IRMImpl(rib);


		//TCPFlowManager tm = irm.getTcpManager();

		//
		//		int wierIDtoBUHost2 = tm.getWireID("BostonU2");
		//
		//		System.out.println("wierIDtoBUHost2 " + wierIDtoBUHost2);
		//
		//		try {
		//			tm.send(wierIDtoBUHost2, "hello".getBytes());
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
	}

}
