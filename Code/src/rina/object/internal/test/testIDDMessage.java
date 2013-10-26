/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.object.internal.test;

import rina.object.gpb.IDDMessage_t.iddMessage_t;
import rina.object.gpb.IDDMessage_t.opCode_t;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.IDDRecord;
import rina.object.internal.IDDRecord.AppRecord;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testIDDMessage {
	
	public static void main(String args[])
	{
		iddMessage_t.Builder testIDDMsg = iddMessage_t.newBuilder();
		
		testIDDMsg.setOpCode(opCode_t.Response);
		
		testIDDMsg.setDifName("BUDIF");
		
		ApplicationProcessNamingInfo apInfo = new ApplicationProcessNamingInfo( "1", "2",  "3", "4");
		
		testIDDMsg.setResult(-1);
		
		// testIDDMsg.setApplicationNameInfo(apInfo.convert());
		
		// AppRecord appRecord = new 
		
		iddMessage_t iddMsg = testIDDMsg.buildPartial();
		
		IDDRecord testRecord = new IDDRecord(iddMsg);
		
		
	}

}
