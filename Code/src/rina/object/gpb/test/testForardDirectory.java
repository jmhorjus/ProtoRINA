/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.object.gpb.test;

import java.util.LinkedHashMap;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.object.gpb.ApplicationProcessNamingInfoMessage_t.applicationProcessNamingInfo_t;
import rina.object.gpb.DirectoryForwardingTableEntry_t.directoryForwardingTableEntry_t;
import rina.object.gpb.DirectoryForwardingTableEntry_t.directoryForwardingTableEntry_t.Builder;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testForardDirectory {


	public static void main(String args[])
	{
//		//directoryForwardingTableEntry_t.Builder entry = directoryForwardingTableEntry_t.newBuilder();
//
//		applicationProcessNamingInfo_t.Builder apInfo = applicationProcessNamingInfo_t.newBuilder();
//
//		apInfo.setApplicationProcessName("BostonU");
//
//		byte[] msg = apInfo.buildPartial().toByteArray();
//
//		applicationProcessNamingInfo_t apInfo_c = null;
//		try {
//			apInfo_c = applicationProcessNamingInfo_t.parseFrom(msg);
//		} catch (InvalidProtocolBufferException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		String  apName = apInfo_c.getApplicationProcessName();
//		String apInstance = apInfo_c.getApplicationProcessInstance();
//
//		System.out.println("apName" + apName);
//		
//		System.out.println("apInstance" + apInstance);
//
//		if(apInstance == "")
//		{
//			System.out.println("apInstance is empty" );
//		}
		
		
		String s1 = "aaa";
		String s2 ="";
		
		String s3 = s1 + s2;
		if(s3.endsWith(s1))
		{
			System.out.println("same " + s3);
		}
		
		LinkedHashMap <String, String > test =  new LinkedHashMap <String, String >();
		
		test.put("a", "a");
		
		test.remove("b");
				

	}

}
