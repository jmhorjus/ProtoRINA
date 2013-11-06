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


import application.component.impl.IPCResourceManagerImpl;
import application.component.util.RegularHandler;
import application.impl.*;
import rina.ipc.impl.IPCImpl;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Node1 {

	public  static void main(String args[])
	{
		String ipcFile1 = "./experimentConfigFiles/mobility/ipcDIFA1.properties";	

		IPCImpl ipcDIFA1 = new IPCImpl(ipcFile1);

		String  apName = "client";
		String apInstance = "1";

		Application app = new Application(apName, apInstance);

		//at the beginning application "client/1" uses "ipcDIFA1" as the PoA (Point of Attachment)
		app.addIPC(ipcDIFA1);

		//allocate a flow to "server/1", and send messages
		IPCResourceManagerImpl irm = app.getIpcManager();

		int handle = irm.allocateFlow("client", "1", "server", "1");

		//attach a handle to printer whatever received
		new RegularHandler(handle,irm);

		System.out.println("the handle from client/1 to server/1 is " + handle);

		String content  = "Hello, world";

		//send only once
		for(int i = 0; i < 10; i++)
		{

			String msg = content + ":" + i;

			try {
				irm.send(handle, msg.getBytes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}




		//then application "client/1" leaves DIFA, unregister the previous IPCDIFA1
		app.removeIPC(ipcDIFA1);

		//and the node moves to a new location and use "ipcDIFB1" as PoA in DIFB to talk to "server/1"
		String ipcFile2 = "./experimentConfigFiles/mobility/ipcDIFB1.properties";	

		IPCImpl ipcDIFB1 = new IPCImpl(ipcFile2);

		app.addIPC(ipcDIFB1);

		int newHandle = irm.allocateFlow("client", "1", "server", "1");

		System.out.println("new  handle from client/1 to server/1 is " + newHandle);

		content  = "Hello, new world";

		for(int i = 0; i < 10; i++)
		{

			String msg = content + ":" + i;

			try {
				irm.send(newHandle, msg.getBytes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
