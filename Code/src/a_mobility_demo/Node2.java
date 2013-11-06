/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */
package a_mobility_demo;


import application.impl.*;
import rina.ipc.impl.IPCImpl;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Node2 {

	public  static void main(String args[])
	{
		//ipc1
		String ipcFile1 = "./experimentConfigFiles/mobility/ipcDIFA2.properties";	

		IPCImpl ipcDIFA2 = new IPCImpl(ipcFile1);

		//ipc2
		String ipcFile2 = "./experimentConfigFiles/mobility/ipcDIFB2.properties";	

		IPCImpl ipcDIFB2 = new IPCImpl(ipcFile2);

		
		//application
		String  apName = "server";
		String apInstance = "1";

		Application app = new Application(apName, apInstance);

		//application "server/1" has two PoAs, on in each of the two DIFs (DIFA and DIFB)
		app.addIPC(ipcDIFA2);

		app.addIPC(ipcDIFB2);
		
		

	}

}
