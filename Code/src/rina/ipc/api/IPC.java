
/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

/**
 * IPC
 * This defines the API that can be called by application
 * @author Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0
 *
 */


package rina.ipc.api;

import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.Flow;
import rina.util.FlowInfoQueue;
import rina.util.MessageQueue;

public interface IPC {

	// The flowing four will be call by IRM of application above.

	/**
	 * Return flow ID
	 * @param flow
	 * @return
	 */
	public int allocateFlow(Flow flow);

	/**
	 * Deallocate the flow
	 * @param portID
	 */
	public void deallocateFlow(int portID);


	public void send(int flowID, byte[] msg) throws Exception;

	public byte[] receive(int flowID);
	
	public void registerApplication(ApplicationProcessNamingInfo apInfo, FlowInfoQueue flowInfoQueue);
	
	public void deregisterApplication(ApplicationProcessNamingInfo apInfo);

}
