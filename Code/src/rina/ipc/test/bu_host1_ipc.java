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

package rina.ipc.test;

import rina.ipc.impl.IPCImpl;
import rina.irm.impl.IRMImpl;



public class bu_host1_ipc {

	public static void main(String args[])
	{
		String file = "./experimentConfigFiles/testConfigFiles/bu_host1.properties";	


		IPCImpl bu_host1 = new IPCImpl(file);


//		IRMImpl irm = bu_host1.getIrm();

//		String srcApName = "BostonU";
//		String srcApInstance = "1";
//		String srcAeName = "Management";
//		String srcAeInstance = "1";
//
//
//		String dstApName = "BostonU";
//		String dstApInstance = "2";
//		String dstAeName = "Management";
//		String dstAeInstance = "1";
//
//		
//
//		int handleID = irm.allocateFlow(srcApName, srcApInstance, srcAeName, srcAeInstance,
//				dstApName, dstApInstance, dstAeName, dstAeInstance);
//		
//		System.out.println("handleID is " + handleID);
//		
//
//		byte[] msg = "hello".getBytes();
//
//		try {
//			irm.send(handleID, msg);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		int handleID2 = irm.allocateFlow(srcApName, srcApInstance, srcAeName, srcAeInstance,
//				dstApName, dstApInstance, dstAeName, dstAeInstance);
//
//		byte[] msg2 = "hello, world".getBytes();
//
//		try {
//			irm.send(handleID2, msg);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		System.out.println("handleID  is " + handleID2);
//		
//		
//		String srcApName1 = "BostonU";
//		String srcApInstance1 = "1";
//		String srcAeName1 = "Data Transfer";
//		String srcAeInstance1 = "1";
//
//		String dstApName1 = "BostonU";
//		String dstApInstance1 = "3";
//		String dstAeName1 = "Data Transfer";
//		String dstAeInstance1 = "1";
//
//		 int handleID3 = irm.allocateFlow(srcApName1, srcApInstance1, srcAeName1, srcAeInstance1,
//				 dstApName1, dstApInstance1, dstAeName1, dstAeInstance1);
	}

}
