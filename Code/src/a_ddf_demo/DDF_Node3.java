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

import node.impl.Node;

//Dynamic DIF Formation demo
/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class DDF_Node3 {

	public static void main(String args[])
	{
		String file = "./experimentConfigFiles/ddfDemo/ddf_node3.properties";

		//dst app resides
		Node node = new Node(file);
	}
	
}
