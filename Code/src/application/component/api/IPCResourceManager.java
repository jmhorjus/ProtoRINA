package application.component.api;

/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */


import rina.ipc.impl.IPCImpl;

/**
 * IPC Resource Manager (IRM)
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public interface IPCResourceManager {
	
	public void addIPC(IPCImpl ipc);
	
	public void removeIPC(String IPCName, String IPCInstance);
	
	public int allocateFlow(String srcApName, String srcApInstance, String srcAeName, String srcAeInstance, 
			String dstApName, String dstApInstance, String dstAeName, String dstAeInstance);
	
	public void deallocate(int handleID);

	public void send(int handleID,  byte[] msg) throws Exception;

	public byte[] receive(int handleID);

}
