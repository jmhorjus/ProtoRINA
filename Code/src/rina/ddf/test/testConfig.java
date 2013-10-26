/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */
package rina.ddf.test;

import java.util.LinkedList;

import rina.config.RINAConfig;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testConfig {

	
	public static void main(String args[])
	{
		RINAConfig ipcConfig = new RINAConfig();
		
		ipcConfig.setProperty("rina.ipc.name", "ipctesttt");
		
		System.out.println(ipcConfig.getIPCName());
		
		
		LinkedList<String> underlyingDIFs = new LinkedList<String>();
		underlyingDIFs.add("DIF1");
		underlyingDIFs.add("DIF2");
		underlyingDIFs.add("DIF3");
		
		
		ipcConfig.setUnderlyingDIFs(underlyingDIFs);
		
		LinkedList<String> underlyingDIFRead = ipcConfig.getUnderlyingDIFs();
		
		
		System.out.println(underlyingDIFRead);
	}
}
