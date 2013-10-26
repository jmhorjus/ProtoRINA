/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.routing.util;


import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Link State Routing
 */

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Dijkstra {
	/**
	 * build Forwarding Table
	 * @param map
	 * @param source
	 * @return map
	 */
	public static LinkedHashMap<Integer,Integer> buildForwardingTable(LinkedHashMap<Integer,Integer> FT, LinkedHashMap<Integer,LinkedHashMap <Integer, Double>> map, int source)
	{
		FT.clear();

		int [] all_node= LinkedHashMapKeyToArray(map); 

		int numNode = all_node.length; 

		double inf= (double) 999999999;

		int [] neighbour = LinkedHashMapKeyToArray((LinkedHashMap <Integer, Double>)map.get(source));


		double [] neighbourDis = LinkedHashMapValueToArray((LinkedHashMap <Integer, Double>)map.get(source));

		int nunNeighbour= neighbour.length;

		LinkedHashMap<Integer,Double> Dis= new LinkedHashMap<Integer,Double>();//store the distance to all other nodes in the map

		LinkedHashMap<Integer,Integer> Previous_Node = new LinkedHashMap<Integer,Integer>(); //store the previous to the source

		//// all nodes to the source is inf
		for(int i=0;i<numNode;i++)
		{
			if( all_node[i] != source)
			{
				Dis.put(all_node[i], inf );
				Previous_Node.put(all_node[i], source);

			}
		}

		//// update the neighbour of the source
		for(int i=0;i<nunNeighbour;i++)
		{
			Dis.put(neighbour[i], neighbourDis[i]);
			Previous_Node.put(all_node[i], source);
		}

		//Initialisation finished


		//now start to build the Forwarding Table 



		while(FT.size()!=(numNode-1))// not all node are in the set FT
		{
			int min = MinDisKey(Dis); // find the node with min distance in NOT FT  
			double minDis= Dis.get(min);

			int next_hop =source;

			//			System.out.println("map.get(source):" + map.get(source));
			//			System.out.println("min is " +  min);
			//			System.out.println("Previous_Node.containsKey(min): " + Previous_Node.containsKey(min));
			//			System.out.println("Previous_Node:" + Previous_Node);

			int previous_hop = Previous_Node.get(min);

			LinkedList<Integer> this_path = new LinkedList<Integer>() ;

			if( previous_hop ==source )
			{
				next_hop = min;
				this_path.add(min);
			}
			else
			{
				while(previous_hop !=  source)
				{   
					this_path.add(previous_hop);
					next_hop=previous_hop;
					previous_hop= Previous_Node.get(previous_hop);
				}


			}

			////////////////////////////////////////////////////////////////////
			//TESTME remove the islocated node
			
		//	System.out.println("pppppppppppppppppppppppppPrevious_Node" + Previous_Node ) ;
			
			//Done
			///////////////////////////////////////////////////////////////////

			//	System.out.println("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzthis_path is " +  this_path);

			FT.put(min, next_hop); 

			//////////////
			// now stat to update the Dis for all node that not in the FT

			int minNeighbourNum= ( (LinkedHashMap)map.get(min) ).size();


			int [] minNeighbour = LinkedHashMapKeyToArray((LinkedHashMap)map.get(min));
			double [] minNeighbourDis = LinkedHashMapValueToArray((LinkedHashMap)map.get(min));

			double disViaMin;

			for(int i=0;i<minNeighbourNum;i++)
			{
				if(FT.containsKey(minNeighbour[i])==false && ( minNeighbour[i] != source))
				{

					//					System.out.println("minNeighbour[i]: " + minNeighbour[i] );
					//					System.out.println("Dis.containsKey(minNeighbour[i]):" +  Dis.containsKey(minNeighbour[i]));
					//					System.out.println("map.containsKey(minNeighbour[i]):" + map.containsKey(minNeighbour[i]));


					double  current_dis = Dis.get(minNeighbour[i]);

					disViaMin = minDis + minNeighbourDis[i];

					if(disViaMin< current_dis)
					{
						Dis.put(minNeighbour[i], disViaMin);
						Previous_Node.put(minNeighbour[i],min);
					}

				}

			}

			Dis.remove(min);//remove min from the NOT FT list;

		}


		//return FT;

		
		
		/////////////////////////////////////////////////////////////////
		// neighborSet is used to clean the FT as there is outdated info,
		//so remove any dst from FT if its next hop is not direct neighbor
		//FIXME:maybe also clean the map 
		/////////////////////////////////////////////////////
		Set<Integer> neighborSet = map.get(source).keySet();
		/////////////////////////////////////////////////////
		return cleanForwardingTable(FT,neighborSet);
	}
	
	
	private static LinkedHashMap<Integer,Integer> cleanForwardingTable(LinkedHashMap<Integer,Integer> FT, Set neighborSet)
	{
		int[] keyArray = LinkedHashMapKeyToArray(FT);
		
		for(int i = 0; i< keyArray.length ; i++)
		{
			int nextHop = FT.get(keyArray[i]);
			
			if( ! neighborSet.contains(nextHop))
			{
				FT.remove(keyArray[i]);
				
				System.out.println("cleanForwardingTable, " +  keyArray[i] + " is removed, " +
						"as its next hop " + nextHop + " is not source's neighbor");
			}
		}
		
		return FT;
	}
	
	/**
	 * convert LinkedHashMap Key To Array
	 * @param map
	 * @return array
	 */
	private  static int[] LinkedHashMapKeyToArray( LinkedHashMap hp)
	{
		int num = hp.size();

		int[] keyArray= new int[num];

		Object [] array ;
		array = hp.keySet().toArray();


		for(int j =0;j< num;j++)
		{
			keyArray[j] =  Integer.parseInt(array[j].toString());
		} 
		return keyArray;


	}

	/**
	 * LinkedHashMap Value To Array
	 * @param hash map
	 * @return array of int
	 */
	private static double[] LinkedHashMapValueToArray( LinkedHashMap <Integer, Double> hp)
	{
		int [] key = LinkedHashMapKeyToArray(hp);

		int num = hp.size();

		double[] valueArray= new double[num];

		for(int j =0;j< num;j++)
		{
			valueArray[j] = hp.get(key[j]);

		}

		return valueArray;


	}
	/**
	 * Minimum Distance Key
	 * @param map
	 * @return minimum distance
	 */
	private static int MinDisKey(LinkedHashMap hp)
	{
		int min;

		int n=hp.size();

		int[] key = LinkedHashMapKeyToArray(hp);

		double[] value = LinkedHashMapValueToArray(hp);


		//bubble sort


		int minKey=key[0];

		double minValue= value[0];

		for(int j=0;j<n;j++) 
		{


			if(value[j] < minValue)
			{
				minKey = key[j];
				minValue = value[j];
			}
		}

		min=minKey;
		return min;

	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}