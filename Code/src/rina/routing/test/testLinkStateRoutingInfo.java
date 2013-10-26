/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.routing.test;

import java.util.LinkedHashMap;

import rina.object.gpb.LinkStateRoutingEntrySet_t.routingEntrySet_t;
import rina.object.internal.LinkStateRoutingEntry;
import rina.routing.util.LinkStateRoutingInfo;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testLinkStateRoutingInfo {

	public static void main(String args[])
	{
//		int rinaAddr = 1;
//
//		LinkedHashMap <Integer, Integer> forwardingTable = new LinkedHashMap <Integer, Integer> ();
//
//		LinkStateRoutingInfo routingInfo = new LinkStateRoutingInfo(rinaAddr,forwardingTable);
//
//		boolean rToNode2 = routingInfo.addCostToNeighbor(2, 5.5);
//
//		boolean rToNode3 =  routingInfo.addCostToNeighbor(3, 10.1);
//
//		System.out.println("rToNode2:" +  rToNode2);
//		System.out.println("rToNode3:" +  rToNode3);
//
//		//node2
//		LinkStateRoutingEntry node2Entry1 = new LinkStateRoutingEntry(2,1,5.5,System.currentTimeMillis());
//		LinkStateRoutingEntry node2Entry2 = new LinkStateRoutingEntry(2,4,5.5,System.currentTimeMillis());
//		
//		routingEntrySet_t.Builder node2_early =  routingEntrySet_t.newBuilder();
//		node2_early.setAddr(2);
//		node2_early.setTimestamp(System.currentTimeMillis()-10);
//		node2_early.addRoutingEntrySet(node2Entry1.convert());
//		node2_early.addRoutingEntrySet(node2Entry2.convert());
//
//		routingEntrySet_t.Builder node2 =  routingEntrySet_t.newBuilder();
//		node2.setAddr(2);
//		node2.setTimestamp(System.currentTimeMillis());
//		node2.addRoutingEntrySet(node2Entry1.convert());
//		node2.addRoutingEntrySet(node2Entry2.convert());
//
//		
//		
//
//		//node3
//		LinkStateRoutingEntry node3Entry1 = new LinkStateRoutingEntry(3,1,10.1,System.currentTimeMillis());
//		LinkStateRoutingEntry node3Entry2 = new LinkStateRoutingEntry(3,4,10.0,System.currentTimeMillis());
//
//		routingEntrySet_t.Builder node3 =  routingEntrySet_t.newBuilder();
//		node3.setAddr(3);
//		node3.setTimestamp(System.currentTimeMillis());
//		node3.addRoutingEntrySet(node3Entry1.convert());
//		node3.addRoutingEntrySet(node3Entry2.convert());
//
//
//		//node4
//		LinkStateRoutingEntry node4Entry1 = new LinkStateRoutingEntry(4,2,5.5,System.currentTimeMillis());
//		LinkStateRoutingEntry node4Entry2 = new LinkStateRoutingEntry(4,3,10.0,System.currentTimeMillis());
//
//		routingEntrySet_t.Builder node4 =  routingEntrySet_t.newBuilder();
//		node4.setAddr(4);
//		node4.setTimestamp(System.currentTimeMillis());
//		node4.addRoutingEntrySet(node4Entry1.convert());
//		node4.addRoutingEntrySet(node4Entry2.convert());
//
//	
//		boolean node3_r = routingInfo.addRoutingEntrySet(node3.buildPartial());
//
//		if(node3_r)
//		{
//			routingInfo.buildForwrdingTable();
//		}
//
//		
//		boolean node2_r = routingInfo.addRoutingEntrySet(node2.buildPartial());
//		
//		routingInfo.addRoutingEntrySet(node2_early.buildPartial());
//
//		if(node2_r)
//		{
//			routingInfo.buildForwrdingTable();
//		}
//		
//		System.out.println("forwardingTable:" + forwardingTable);
//		
//		boolean node4_r = routingInfo.addRoutingEntrySet(node4.buildPartial());
//
//		if(node4_r)
//		{
//			routingInfo.buildForwrdingTable();
//		}
//		
//		LinkStateRoutingEntry node2Entry1_ = new LinkStateRoutingEntry(2,1,15.5,System.currentTimeMillis());
//		LinkStateRoutingEntry node2Entry2_ = new LinkStateRoutingEntry(2,4,15.5,System.currentTimeMillis());
//		
//		routingEntrySet_t.Builder node2_new =  routingEntrySet_t.newBuilder();
//		node2_new.setAddr(2);
//		node2_new.setTimestamp(System.currentTimeMillis());
//		node2_new.addRoutingEntrySet(node2Entry1_.convert());
//		node2_new.addRoutingEntrySet(node2Entry2_.convert());
//		
//		
//		boolean node2_new_r = routingInfo.addRoutingEntrySet(node2_new.buildPartial());
//
//		if(node2_new_r)
//		{
//			routingInfo.buildForwrdingTable();
//		}
//		
//		//
//		//		System.out.println("node2_r:" + node2_r);
//		//		System.out.println("node3_r:" + node3_r);
//		//		System.out.println("node4_r:" + node4_r);
//		//
//		//		routingInfo.buildForwrdingTable();
//		//
//		
		
	}

}
