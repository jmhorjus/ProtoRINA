/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */
package node.test;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testConstructor {
	
	String arg1 = null;


	public testConstructor(String arg1)
	{
		this.arg1 = arg1;
	}
	
	public testConstructor(String arg1, String arg2)
	{
		String newArg = arg1+ arg2;
		new testConstructor(arg1+arg2);
	}


	public void print()
	{
		System.out.println("arg1:" + this.arg1);
	}
	
	
	public static void main(String args[])
	{
		testConstructor test = new testConstructor("a", "b");
		
		test.print();
	}
}
