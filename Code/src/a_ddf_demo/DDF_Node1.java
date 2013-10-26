/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */
package a_ddf_demo;

import application.component.impl.IPCResourceManagerImpl;
import application.impl.Application;
import node.impl.Node;

//Dynamic DIF Formation demo
/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class DDF_Node1 {

	public static void main(String args[])
	{
		String file = "./experimentConfigFiles/ddfDemo/ddf_node1.properties";
		//src app resids
		Node node = new Node(file);
		
		
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Application srcApp = node.getApp();
		
		System.err.println("Starting to allocate flow between applicaitons");

		IPCResourceManagerImpl ipcManager = srcApp.getIpcManager();

		int hanldeID = ipcManager.allocateFlow("app","1", "app","3");
		
		System.err.println("wow, the final in the DDF experiment is " + hanldeID);
		
		int i = 0;

		while(true)
		{
			String msg = "hello " + i;

			try {
				ipcManager.send(hanldeID,msg.getBytes() );
				System.out.println("mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmsg number:  " + i + " sent");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			i++;

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
}
