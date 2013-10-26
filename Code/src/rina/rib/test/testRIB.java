/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.rib.test;

import java.util.LinkedHashMap;

import rina.config.RINAConfig;
import rina.rib.impl.RIBImpl;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testRIB {

	public static void main(String args[])
	{

//		RINAConfig config = new RINAConfig("./experimentConfigFiles/testConfigFiles/bu_host2.properties");
//
//
//		String a1 = config.getAuthenticatorApName();
//		String a2 = config.getAuthenticatorApInstance();
//
//
//		
//		RIBImpl rib = new RIBImpl();
//		
//		rib.addAttribute("authenticatorApName", config.getAuthenticatorApName() );
//		rib.addAttribute("authenticatorApInstance", config.getAuthenticatorApInstance());
//	
//
//		try{
//			String authenticatorApName = rib.getAttribute("authenticatorApName").toString();
//
//			String authenticatorApInstance  = rib.getAttribute("authenticatorApInstance").toString();
//
//			System.out.println(authenticatorApName + "/" + authenticatorApInstance );
//		
//		}catch(Exception e)
//		{
//			e.printStackTrace();	
//		}
//		
		
//		 LinkedHashMap<String, Integer > nameToAddr = new   LinkedHashMap<String, Integer > ();
//		 
//		 System.out.println(" try " + nameToAddr.get("11111"));
		 
		 RIBImpl rib = new RIBImpl();
		 
		Object  test =  rib.getAttribute("ddddd");
		
		if(test == null)
		{
			System.out.println("dddd");
		}
	}

}
