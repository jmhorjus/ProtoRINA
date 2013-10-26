
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

public class bu_host1_irm {

	
	
	public static void main(String args[])
	{
		String file = "./experimentConfigFiles/testConfigFiles/bu_host1.properties";	
		RINAConfig config = new RINAConfig(file);

		RIBImpl rib = new RIBImpl();
		
		

		//put the information into the RIB
		rib.addAttribute("config",  config);
		
		
		IRMImpl irm = new IRMImpl(rib);
		
	
		
		
		

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
		
		
		 String srcApName = "BostonU";
		 String srcApInstance = "1";
		 String srcAeName = "Management";
		 String srcAeInstance = "1";
		 

		 String dstApName = "BostonU";
		 String dstApInstance = "2";
		 String dstAeName = "Management";
		 String dstAeInstance = "1";
		 
		 String srcApName1 = "BostonU";
		 String srcApInstance1 = "1";
		 String srcAeName1 = "Data Transfer";
		 String srcAeInstance1 = "1";
		 
		 String dstApName1 = "BostonU";
		 String dstApInstance1 = "2";
		 String dstAeName1 = "Data Transfer";
		 String dstAeInstance1 = "1";
		 
		 int dataFlowID = irm.allocateFlow(srcApName1, srcApInstance1, srcAeName1, srcAeInstance1,
				 dstApName1, dstApInstance1, dstAeName1, dstAeInstance1);
		 
		 System.out.println("dataFlowID is " + dataFlowID);
		 
		 
//			try {
//				tm.send(dataFlowID, "hello".getBytes());
//				tm.send(dataFlowID, "hi".getBytes());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		 
		
	}

}
