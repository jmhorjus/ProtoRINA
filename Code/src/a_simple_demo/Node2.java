/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */
package a_simple_demo;


import application.component.impl.IPCResourceManagerImpl;
import application.impl.Application;
import node.impl.Node;


//simple demo
/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Node2 {

	public static void main(String args[])
	{
		String file = "./experimentConfigFiles/simpleDemo/demo_node2.properties";
	

		Node node2 = new Node(file);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Application app = node2.getApp();

		IPCResourceManagerImpl irm = app.getIpcManager();

		int handle = irm.allocateFlow("dummyApp", "2", "dummyApp", "1");

		System.out.println("the handle from dummyApp1 to dummyApp2 is " + handle);

		String content  = "Hello, world";

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
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


	}

}
