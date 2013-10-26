/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *   
 */
package rina.irm.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.object.gpb.IDDMessage_t.iddMessage_t;
import rina.object.internal.IDDRecord;
import rina.rib.impl.RIBImpl;
import rina.tcp.TCPFlowManager;

/**
 * This is used by IRM of DIF0 IPC process to manage the wire.
 * The wire can be a real wire or an emulated wire(can be a TCP Connection)
 * In current implementation the wire is emulated by the TCP connection. All method calls from IRM are directed to TCP flow Manager.
 * More implementations (Ethernet,etc) can be added later to this WireManager container by adding more if else statement
 * in each method.
 */
public class WireManager {

	private Log log = LogFactory.getLog(this.getClass());

	private TCPFlowManager tcpManager = null;
	
	private RIBImpl rib = null;

	public WireManager(RIBImpl rib)
	{
		this.log.info("Wire Manager is inited.");
		this.rib = rib;
		//right now, the wire manager has only one case: wire is emulated by the TCP connection
		this.tcpManager = new TCPFlowManager(this.rib);
		
	}

	public int addDIF0FlowOnWire(int wireID, HandleEntry he) {
		return this.tcpManager.addDIF0FlowOnWire(wireID,he);
	}


	public void removeDIF0FlowOnWire(int wireID, HandleEntry he) {

		this.tcpManager.removeDIF0FlowOnWire(wireID,he);
	}


	public int getWireID(String string) {
		return this.tcpManager.getWireID(string);
	}

	
	public void send(int wireID, byte[] bytes) throws Exception {

		this.tcpManager.send(wireID, bytes);
	}

	public byte[] receive(int wireID, int portID) {

		return this.tcpManager.receive(wireID, portID);
	}

	public IDDRecord queryIDD(iddMessage_t iddRequestMsg) {
		
		return  this.tcpManager.queryIDD(iddRequestMsg);
	}

	public void registerToIDD(iddMessage_t iddRegMsg) {
		
		this.tcpManager.registerToIDD(iddRegMsg);
		
	}






}
