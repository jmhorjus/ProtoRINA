/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */
package rina.config.test;

import rina.config.RINAConfig;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testConfig {

	public static void main(String args[])
	{
		RINAConfig config = new RINAConfig("./experimentConfigFiles/testConfigFiles/bu_host1.properties");
		
		//double period = config.getRoutingEntrySubUpdatePeriod();
		
		String linkCostPolicy = config.getLinkCostPolity();
		
		System.out.println("linkCostPolicy is " +  linkCostPolicy);


		String a1 = config.getRinaProperties().getProperty("testtest");
		
		a1 = config.getAddressPolicy();
		//String a1 = config.getAuthenticatorApName();
		
		
		System.out.println( "a1 is " + a1); 
		
		if(a1==null)
		{
			
			System.out.println( "null"); 
			System.out.println( a1); 
		}
	}

}
