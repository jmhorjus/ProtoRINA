/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.flowAllocator.test;

import rina.flowAllocator.impl.FlowAllocatorImpl;
import rina.ipc.impl.IPCImpl;
import rina.object.internal.ApplicationProcessNamingInfo;
import rina.object.internal.DirectoryForwardingTable;
import rina.object.internal.DirectoryForwardingTableEntry;
import rina.object.internal.Flow;
import rina.object.internal.Neighbor;
import rina.object.internal.Neighbors;
import rina.rib.impl.RIBImpl;

public class fl_host1 {


	public static void main(String args[])
	{
		String file = "./experimentConfigFiles/testConfigFiles/bu_host1.properties";	

		IPCImpl BostonU1 = new IPCImpl(file);

		RIBImpl rib = BostonU1.getRib();

		ApplicationProcessNamingInfo apInfo = new ApplicationProcessNamingInfo("App2");

		DirectoryForwardingTableEntry directoryForwardingTableEntry =
				new  DirectoryForwardingTableEntry(apInfo, 1, 1);

		( (DirectoryForwardingTable)rib.getAttribute("directoryForwardingTable") ).addEntry(directoryForwardingTableEntry);


		Neighbor neighbor = new Neighbor("BostonU","2", 1);

		( (Neighbors)rib.getAttribute("neighbors") ).addNeighbor(neighbor);


		FlowAllocatorImpl  flowAllocator = BostonU1.getFlowAllocator();




		Flow flowRequest  = new Flow("App1", "App2");

		//flowAllocator.submitAllocationRequest(flowRequest);


		int portID = BostonU1.allocateFlow(flowRequest);
		
		System.out.println("portID allocated in test is " + portID);
		
		byte[] dummyMsg = "hello".getBytes();
		
		try {
			BostonU1.send(portID, dummyMsg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}
