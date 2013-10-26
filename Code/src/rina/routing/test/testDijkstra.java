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


/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testDijkstra {
	
	public static void main(String args[])
	{
		
		LinkedHashMap<Integer, Integer> forwardingTable = new LinkedHashMap<Integer, Integer>();
		
		
		 LinkedHashMap<Integer,LinkedHashMap <Integer, Double>> map = new  LinkedHashMap<Integer,LinkedHashMap <Integer, Double>>();
		 
		 int node1 = 1;
		 int node2 = 2;
		 int node3 = 3;
		 int node4 = 4;
		 
		 LinkedHashMap <Integer, Double>  cost1 =  new LinkedHashMap <Integer, Double>(); 
		 LinkedHashMap <Integer, Double>  cost2 =  new LinkedHashMap <Integer, Double>(); 
		 LinkedHashMap <Integer, Double>  cost3 =  new LinkedHashMap <Integer, Double>(); 
		 LinkedHashMap <Integer, Double>  cost4 =  new LinkedHashMap <Integer, Double>(); 
		 
		cost1.put(2, 5.5);
	//	cost1.put(3, 10.1);
		
		cost2.put(1, 5.5);
		cost2.put(3, 5.5);
		
	//	cost3.put(2, 10.1);
		cost3.put(4, 10.0);
		
		cost4.put(3, 5.5);
		//cost4.put(3, 10.0);
		
		
		 
		 map.put(node1,cost1);
		 map.put(node2,cost2);
		 map.put(node3,cost3);
		 map.put(node4,cost4);
		 
		 System.out.println("map is " + map);
		 
		 rina.routing.util.Dijkstra.buildForwardingTable(forwardingTable, map, 4);
		  
		 System.out.println(forwardingTable); 
		 
		 
	}

	
}
