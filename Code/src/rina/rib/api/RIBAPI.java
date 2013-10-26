/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.rib.api;

import java.util.LinkedHashMap;
import java.util.LinkedList;


/**
 * Resource Information Base (RIB) API
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 *
 */
public interface RIBAPI {


	/**
	 * 
	 * @param attribute to be read
	 */
	public  Object getAttribute(String attribute);
	/**
	 * 
	 * @param attribute to remove
	 */
	public void removeAttribute(String attribute);
	/**
	 * 
	 * @param attributeName
	 * @param attribute to add
	 */
	public  void  addAttribute(String attributeName, Object attribute);

	/**
	 * 
	 * @param ipcName
	 * @param ipcInstance
	 * @param underlyingDIFs
	 * @return
	 */
	public int addMember(String ipcName, String ipcInstance, LinkedList<String> underlyingDIFs);
	/**
	 * 
	 * @param name
	 */
	public void removeMember(String name);

}
