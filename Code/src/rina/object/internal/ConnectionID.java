
/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */

package rina.object.internal;

/**
 * This corresponds with the ConnectionID in Flow object
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class ConnectionID {
	
	private int qosID;
	private int sourceCEPID;
	private int destinationCEPID;
	
	public ConnectionID(){}
	
	public ConnectionID( int qosID, int sourceCEPID, int destinationCEPID)
	{
		this.qosID = qosID;
		this.sourceCEPID = sourceCEPID;
		this.destinationCEPID = destinationCEPID;
	}

	public synchronized int getQosID() {
		return qosID;
	}

	public synchronized void setQosID(int qosID) {
		this.qosID = qosID;
	}

	public synchronized int getSourceCEPID() {
		return sourceCEPID;
	}

	public synchronized void setSourceCEPID(int sourceCEPID) {
		this.sourceCEPID = sourceCEPID;
	}

	public synchronized int getDestinationCEPID() {
		return destinationCEPID;
	}

	public synchronized void setDestinationCEPID(int destinationCEPID) {
		this.destinationCEPID = destinationCEPID;
	}

}
