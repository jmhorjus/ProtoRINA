
/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.idd.instance;


import rina.ipc.impl.IPCImpl;
import rina.object.gpb.IDDMessage_t.iddMessage_t;
import rina.object.gpb.IDDMessage_t.opCode_t;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.IDDRecord;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class QueryIPCProcess {


	public static void main(String args[])
	{
		String file = "./experimentConfigFiles/testConfigFiles/bu_host2.properties";	

		//DIF Manager
		IPCImpl BostonU2 = new IPCImpl(file);


		BostonU2.registerDIFToIDD();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		IDDRecord result = BostonU2.queryIDD(BostonU2.getDIFName());

		if(result == null)
		{
			System.out.println("No record found on IDD");
		}else
		{
			result.print();
		}


		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	   BostonU2.registerAppToIDD(new ApplicationProcessNamingInfo("Video", "1"));
	   

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		IDDRecord result2 = BostonU2.queryIDD(new ApplicationProcessNamingInfo("Video", "1") );

		if(result == null)
		{
			System.out.println("No record found on IDD");
		}else
		{
			result2.print();
		}

		
	}
}
