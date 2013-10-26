/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */

/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.object.internal;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.object.gpb.Member_t.member_t;



/**
 * This corresponds to the member in rina.object.gpb
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Member {
	
	
	private  Log log = LogFactory.getLog(this.getClass());
	private String apName = null;
	private String apInstance = null;
	private LinkedList<String> underlyingDIFs = null;
	
	private long addr = -1;
	
	public Member(String apName, String apInstance, long addr, LinkedList<String> underlyingDIFs)
	{
		this.apName  = apName;
		this.apInstance = apInstance;
		this.addr = addr;
		this.underlyingDIFs = underlyingDIFs;
	}

	public void print()
	{
		this.log.debug("Member(apName/apInstance/Addr): " +  this.apName + "/" +  this.apInstance + "/" + this.addr + "/" + this.underlyingDIFs);
	}
	
	public  member_t convert()
	{
		member_t.Builder  member =  member_t.newBuilder();
		member.setApplicationProcessName(this.apName);
		member.setApplicationProcessInstance(this.apInstance);
		member.setAddress(this.addr);
		
		for(int i = 0 ; i < this.underlyingDIFs.size(); i++)
		{
			member.addUnderlyingDIFs(this.underlyingDIFs.get(i));
		}
		
		return member.buildPartial();
	}

	/**
	 * @return the apName
	 */
	public synchronized String getApName() {
		return apName;
	}

	/**
	 * @param apName the apName to set
	 */
	public synchronized void setApName(String apName) {
		this.apName = apName;
	}

	/**
	 * @return the apInstance
	 */
	public synchronized String getApInstance() {
		return apInstance;
	}

	/**
	 * @param apInstance the apInstance to set
	 */
	public synchronized void setApInstance(String apInstance) {
		this.apInstance = apInstance;
	}

	/**
	 * @return the addr
	 */
	public synchronized long getAddr() {
		return addr;
	}

	/**
	 * @param addr the addr to set
	 */
	public synchronized void setAddr(long addr) {
		this.addr = addr;
	}

	/**
	 * @return the underlyingDIFs
	 */
	public synchronized LinkedList<String> getUnderlyingDIFs() {
		return underlyingDIFs;
	}

	/**
	 * @param underlyingDIFs the underlyingDIFs to set
	 */
	public synchronized void setUnderlyingDIFs(LinkedList<String> underlyingDIFs) {
		this.underlyingDIFs = underlyingDIFs;
	}
	
	

}
