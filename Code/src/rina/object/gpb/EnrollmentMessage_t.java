/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */

package rina.object.gpb;

import rina.object.gpb.EnrollmentInformation_t.enrollmentInformation_t;

public class EnrollmentMessage_t {
	
	public static enrollmentInformation_t generate(long address, String operationalStatus)
	{
		enrollmentInformation_t.Builder  enrollmentMessage = enrollmentInformation_t.newBuilder();
		enrollmentMessage.setAddress(address);
		enrollmentMessage.setOperationalStatus(operationalStatus);
		return enrollmentMessage.build();
	}
	
	public static enrollmentInformation_t generate( String operationalStatus)
	{
		enrollmentInformation_t.Builder  enrollmentMessage = enrollmentInformation_t.newBuilder();
		enrollmentMessage.setOperationalStatus(operationalStatus);
		return enrollmentMessage.buildPartial();
	}
	

}
