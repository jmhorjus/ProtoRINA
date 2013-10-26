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
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.object.gpb.LinkStateRoutingEntrySet_t.routingEntrySetForwardedByNeighbor_t;
import rina.object.gpb.LinkStateRoutingEntrySet_t.routingEntrySet_t;
import rina.object.gpb.LinkStateRoutingEntrySet_t.routingEntry_t;
import rina.object.internal.ForwardingTable;
import rina.object.internal.LinkStateRoutingEntry;
import rina.routing.RoutingDaemon;


/**
 * This is a component of RoutingDaemon, which contains all the info related to Link State routing 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class LinkStateRoutingInfo {

	private Log log = LogFactory.getLog(this.getClass());

	private double inf = RoutingDaemon.inf;

	//rina address of this IPC process
	private int rinaAddr = -1;

	/**
	 * RoutingEntry for all neighbours
	 * key is a neighbour, value is a routing entry
	 */
	private LinkedHashMap<Integer, LinkStateRoutingEntry> neighbourCost = new LinkedHashMap <Integer, LinkStateRoutingEntry>();
	// just to make the code efficient
	private LinkedList<LinkStateRoutingEntry> neighbourCostList = new LinkedList<LinkStateRoutingEntry>();


	//this stores the timestamp of the latest routing entry set from a certain address
	//<addr, timestamp>
	private LinkedHashMap<Integer,Long> routingEntrySetTimestamp = null;



	/**
	 * information about the whole DIF containing the network topology
	 * This one is used to calculate the forwarding table
	 * < addr, < addr,  cost > >
	 */

	private LinkedHashMap <Integer,LinkedHashMap <Integer, Double>> map = null;

	private ForwardingTable forwardingTable = null;  

	//this is the routing entry set received from its neighbor, and need forward to its other neighbor, bacially flooding
	private LinkedHashMap<Integer,routingEntrySet_t> routingEntrySetForward = null;
	private LinkedList<routingEntrySet_t>  routingEntrySetForwardList = null;




	public LinkStateRoutingInfo(int rinaAddr, ForwardingTable forwardingTable)
	{
		this.rinaAddr = rinaAddr;

		this.neighbourCost = new LinkedHashMap <Integer, LinkStateRoutingEntry>();
		this.neighbourCostList = new LinkedList<LinkStateRoutingEntry>();

		this.routingEntrySetTimestamp = new LinkedHashMap<Integer,Long>();

		this.map =  new LinkedHashMap<Integer, LinkedHashMap <Integer, Double> >();
		this.map.put(this.rinaAddr, new LinkedHashMap <Integer, Double>());

		this.forwardingTable = forwardingTable;


		this.routingEntrySetForward = new LinkedHashMap<Integer,routingEntrySet_t>();
		this.routingEntrySetForwardList = new LinkedList<routingEntrySet_t>();



	}




	/**
	 * Return true if old entry changes
	 * Return false if nothing changes
	 * @param neighborAddr
	 * @param cost
	 * @return
	 */
	public synchronized boolean  addCostToNeighbor(int neighborAddr, double cost)
	{

		long currentTimeStamp =  System.currentTimeMillis();

		LinkStateRoutingEntry  entry = new LinkStateRoutingEntry(this.rinaAddr, neighborAddr, cost,currentTimeStamp);

		//this.log.debug(entry.getPrint());

		if(this.neighbourCost.containsKey(neighborAddr))
		{
			//		this.log.debug("Old entry exist, now update");

			LinkStateRoutingEntry oldEntry = this.neighbourCost.get(neighborAddr);

			if(oldEntry.getCost() != entry.getCost() && oldEntry.getTimeStamp() < entry.getTimeStamp())
			{

				this.neighbourCostList.remove(oldEntry);

				this.neighbourCost.put(neighborAddr, entry);

				this.neighbourCostList.add(entry);

				//this.map.get(this.rinaAddr).put((int)entry.getDstAddr(), entry.getCost());

				this.map.get(this.rinaAddr).put(neighborAddr, cost);

				//this is added to make the forwardingTable build algorithm work
				if( !this.map.containsKey(neighborAddr) )
				{
					this.map.put(neighborAddr, new LinkedHashMap <Integer, Double>());
					this.routingEntrySetTimestamp.put(neighborAddr, (long) 0); 
				}

				//				System.out.println("Symmetric added into map:" 
				//				+ neighborAddr+ "/" +  this.rinaAddr + "/" + entry.getCost());
				////////////////////////////////////////////////////////////////////////////

				//update forwarding table

				this.log.debug("addCostToNeighbor, leads to update FT");
				this.buildForwrdingTable();

				return true;
			}else
			{
				//		this.log.debug("Old entry exist, but nothing changed");

				return false; //nothing changes
			}

		}else
		{
			//		this.log.debug("New  entry add");

			this.neighbourCost.put(neighborAddr, entry);
			this.neighbourCostList.add(entry);

			this.map.get(this.rinaAddr).put((int)entry.getDstAddr(), entry.getCost());

			//this is added to make the forwardingTable build algorithm work
			if( !this.map.containsKey(neighborAddr) )
			{
				this.map.put(neighborAddr, new LinkedHashMap <Integer, Double>());
				this.routingEntrySetTimestamp.put(neighborAddr, (long) 0); 
			}

			//			System.out.println("Symmetric added into map:" 
			//			+ neighborAddr+ "/" +  this.rinaAddr + "/" + entry.getCost());
			////////////////////////////////////////////////////////////////////////////

			//update forwarding table
			this.log.debug("addCostToNeighbor, leads to update FT");
			this.buildForwrdingTable();

			return true;
		}
	}


	/**
	 * update when receiving a routingEntrySet object from it neighbors
	 * Note: it might be not the neighbor's routing entry set, as each node forwards
	 * the routing entry it received to its neighbors, basically  the routing entry set is broadcasting 
	 * 
	 * Return true if old entry changes
	 * Return false if nothing changes
	 * @param routingEntrySet
	 * @return
	 */
	public synchronized boolean addRoutingEntrySet( routingEntrySet_t routingEntrySet)
	{

		int addr = (int) routingEntrySet.getAddr();
		long timestamp = routingEntrySet.getTimestamp();


		//this.log.debug("Routing entry set received about " + addr +  ",  and time stamp is  " +  timestamp);

		if(addr == this.rinaAddr)
		{
			//this.log.debug("RoutingEntrySet received about itself, no need to call addRoutingEntrySet()");
			return false;
		}

		if(this.map.containsKey(addr) && this.routingEntrySetTimestamp.get(addr) !=  (long)0 ) // 
		{
			if(this.routingEntrySetTimestamp.get(addr) >= timestamp)
			{

			//	this.log.debug("New routing entry set received about " + addr + ", but discarded as the timestamp is old");

				return false;

			}else
			{
				this.routingEntrySetTimestamp.put(addr, timestamp);

				//TESTME

				boolean changed = false;

				LinkedHashMap <Integer, Double> oldEntrySet = new LinkedHashMap( this.map.get(addr));

				this.map.get(addr).clear();

				//	this.log.debug("OldEntrySet is " +  oldEntrySet);

				for(int i = 0; i< routingEntrySet.getRoutingEntrySetCount(); i++)
				{
					routingEntry_t entry = routingEntrySet.getRoutingEntrySet(i);


					double oldCost;

					if(oldEntrySet.containsKey((int)entry.getDstAddr()))
					{
						oldCost = oldEntrySet.get((int)entry.getDstAddr() );
					}else
					{
						oldCost = -1; // first time received, and initialized with -1
					}

					//add to the map, as we cleared the map entry before the for loop
					this.map.get(addr).put((int)entry.getDstAddr(), entry.getCost());

					if(oldCost != entry.getCost())
					{
						//		this.log.debug("oldCost is " +  oldCost + ", and new old is " + entry.getCost() + ", so  changed for " + entry.getDstAddr());

						changed = true;

					}else
					{
						//		this.log.debug("oldCost is " +  oldCost + ", and new old is " + entry.getCost() + ", so not changed for " + entry.getDstAddr());
					}

					//this is added to make the forwardingTable build algorithm work
					if( !this.map.containsKey((int)entry.getDstAddr()) )
					{
						this.map.put((int)entry.getDstAddr(), new LinkedHashMap <Integer, Double>());
						this.routingEntrySetTimestamp.put((int)entry.getDstAddr(), (long)0);
					}

					//					System.out.println("Symmetric added into map:" 
					//							+ (int)entry.getDstAddr() + "/" +  addr + "/" + entry.getCost());


					////////////////////////////////////////////////////////////////////////////

				}

				//	this.log.debug("Routing entry set updated for " + addr + " and map is " + this.map + ", and the value of changed is " +  changed);

				
				//compare the old set received and the new one, if the new one has less entry, then means the map is change, need return true to trigger updateFT
				LinkedHashMap <Integer, Double> newEntrySet = this.map.get(addr);
				if(newEntrySet.size() != oldEntrySet.size())
				{
					changed = true;
					this.log.debug("the new one has less entries that the old one");
					this.log.debug("oldEntrySet in the map is " + oldEntrySet);
					this.log.debug("new EntrySet in the map  is " + newEntrySet);
				}
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				return changed;			
			}

		}else // first time got routing entry set about this addr
		{

			System.out.print(" first time got routing entry set about this addr");

			if(!this.map.containsKey(addr))
			{
				this.map.put(addr, new LinkedHashMap <Integer, Double>() );
			}

			this.routingEntrySetTimestamp.put(addr, timestamp);

			for(int i = 0; i< routingEntrySet.getRoutingEntrySetCount(); i++)
			{
				routingEntry_t entry = routingEntrySet.getRoutingEntrySet(i);

				this.map.get(addr).put((int)entry.getDstAddr(), entry.getCost());


				//this is added to make the forwardingTable build algorithm work
				if( !this.map.containsKey((int)entry.getDstAddr()) )
				{
					this.map.put((int)entry.getDstAddr(), new LinkedHashMap <Integer, Double>());
					this.routingEntrySetTimestamp.put((int)entry.getDstAddr(), (long)0);
				}

				//				System.out.println("Symmetric added into map:" 
				//						+ (int)entry.getDstAddr() + "/" +  addr + "/" + entry.getCost());


				////////////////////////////////////////////////////////////////////////////
			}

			//			this.log.debug("Routing entry set updated for " + addr + "and map is " + this.map);

			return true;

		}


	}


	/**
	 * This is used when broadcast local routing entry set to its neighbors
	 * @return
	 */
	public synchronized routingEntrySet_t getRoutingEntrySet()
	{

		routingEntrySet_t.Builder  entrySet = routingEntrySet_t.newBuilder();

		entrySet.setAddr(this.rinaAddr);
		entrySet.setTimestamp(System.currentTimeMillis());


		for(int i =0; i< this.neighbourCostList.size(); i++)
		{
			//this.log.debug( "getRoutingEntrySet(): " + this.neighbourCostList.get(i).getPrint());

			entrySet.addRoutingEntrySet(this.neighbourCostList.get(i).convert());

		}

		return entrySet.buildPartial();
	}


	/**
	 * 
	 * @return
	 */
	public synchronized routingEntrySetForwardedByNeighbor_t getRoutingEntrySetForwardToNeighbor()
	{
		routingEntrySetForwardedByNeighbor_t.Builder routingEntrySetForwardedToNeighbor = 
			routingEntrySetForwardedByNeighbor_t.newBuilder();

		int num = this.routingEntrySetForwardList.size();

		//this.log.debug("rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrroutingEntrySetForwardList.size is " + num);

		for(int i = 0; i < num; i++)
		{
			routingEntrySetForwardedToNeighbor.addRoutingEntrySetForwardedByNeighbor(this.routingEntrySetForwardList.get(i));
		}

		this.printRoutingEntrySetForwardToNeighbor(routingEntrySetForwardedToNeighbor.buildPartial());

		return routingEntrySetForwardedToNeighbor.buildPartial();
	}

	private void printRoutingEntrySetForwardToNeighbor(routingEntrySetForwardedByNeighbor_t forwardedSets) 
	{

		int num = forwardedSets.getRoutingEntrySetForwardedByNeighborCount();

		//	this.log.debug("printRoutingEntrySetForwardToNeighbor() is called, and num to sent is " + num);

		for(int i = 0; i< num; i++)
		{
			routingEntrySet_t entrySet = forwardedSets.getRoutingEntrySetForwardedByNeighbor(i);
			int n =entrySet.getRoutingEntrySetCount();

			//			this.log.debug("Now print entrySet: origin's addr/timestamp:" +  entrySet.getAddr() + "/" 
			//			+  entrySet.getTimestamp() + ", and number of the entries inside it is  " + n );

			for(int j = 0; j < n; j++)
			{
				LinkStateRoutingEntry entry = new LinkStateRoutingEntry(entrySet.getRoutingEntrySet(j));

				//this.log.debug(entry.getPrint());
			}
		}


	}


	/**
	 * 
	 * @param routingEntrySet
	 */
	public synchronized void addForwardedRoutingEntrySet(routingEntrySet_t routingEntrySet)
	{
		//this.log.debug("current this.routingEntrySetForwardList.size() " + this.routingEntrySetForwardList.size() );

		long originalSender = routingEntrySet.getAddr();
		long sendTimeStamp = routingEntrySet.getTimestamp();


		if((int)originalSender == this.rinaAddr)
		{
			//	this.log.debug("RoutingEntrySet forwarded received about itself, discard");
			return;
		}

		if(this.routingEntrySetForward.containsKey((int)originalSender))
		{

			routingEntrySet_t routingEntrySetOld = this.routingEntrySetForward.get((int)originalSender);

			long sendTimeStampLast = routingEntrySetOld.getTimestamp();

			if(sendTimeStamp <= sendTimeStampLast)
			{
				//		this.log.debug("Old routingEntrySet forwarded by its neighbors, discarded ");
			}else
			{
				boolean remove_result = this.routingEntrySetForwardList.remove(routingEntrySetOld); //remove the old one
			//	this.log.debug("remove_result  is " +  remove_result);
				
				this.routingEntrySetForward.put((int)originalSender, routingEntrySet);
				this.routingEntrySetForwardList.add(routingEntrySet); //add the new one

				//			this.log.debug("New routingEntrySet forwarded updated");
				
				
//				//compare the old entry set received and the new one, remove the one that not in the new set
//				for(int i = 0; i< routingEntrySetOld.getRoutingEntrySetCount(); i ++)
//				{
//					boolean exist = false;
//					
//					routingEntry_t oldEntry = routingEntrySetOld.getRoutingEntrySet(i);  
//					
//					for(int j = 0; j < routingEntrySet.getRoutingEntrySetCount(); j ++)
//					{
//						routingEntry_t entry = routingEntrySet.getRoutingEntrySet(j);
//						
//						if(entry.getDstAddr() == oldEntry.getDstAddr())
//						{
//							exist = true;
//						}
//					}
//					
//					if(exist == false) // in the new set no such entry, remove from the list
//					{
//						boolean result  =  this.routingEntrySetForwardList.remove(oldEntry);
//						
//						this.log.debug( result + ":old Entry removed(src/dst/cost.timestamp) " + oldEntry.getSrcAddr() + "/" +  oldEntry.getDstAddr()
//								+ "/" +  oldEntry.getCost() + "/" + oldEntry.getTimestamp());
//					}
//					
//				}
//				///////////////////////////////////////////////////////////////////////////////////////////////////////
				
			}

		}else
		{
			this.routingEntrySetForward.put((int)originalSender, routingEntrySet);
			this.routingEntrySetForwardList.add(routingEntrySet);

			//		this.log.debug("New routingEntrySet forwarded added");	
		}

		//	this.log.debug("current this.routingEntrySetForwardList.size() " + this.routingEntrySetForwardList.size() );

	}


	public synchronized void buildForwrdingTable()
	{
		this.log.debug("this.map: " + this.map  );

		//pass the LinkedHashMap<Integer, Integer> to the algorithm, not the forwardingTable object, but
		//the forwardingTable object in the object
		//Note: routing update sync problem
		Dijkstra.buildForwardingTable(this.forwardingTable.getForwardingTable(),this.map,this.rinaAddr); 

		this.log.debug("ForwardingTable is " + this.forwardingTable.getForwardingTable());

	}



	public synchronized LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> getMap() {
		return map;
	}



	/**
	 * This will be used when a neighbor is found to be down
	 * @param neighborAddr
	 */
	public synchronized void removeNeighbor(int neighborAddr) {

		LinkStateRoutingEntry oldEntry = this.neighbourCost.get(neighborAddr);

		this.neighbourCostList.remove(oldEntry);

		this.neighbourCost.remove(neighborAddr);

		this.map.get(this.rinaAddr).remove(neighborAddr);

		this.map.get(neighborAddr).clear();

		//this.map.remove(neighborAddr);

		//this.routingEntrySetTimestamp.remove(neighborAddr);


		// remove the routing entry received from this neighbor, and no need to send this info to its neighbor any more

		if(this.routingEntrySetForward.containsKey(neighborAddr))
		{
			routingEntrySet_t routingEntrySetOld = this.routingEntrySetForward.get((int)neighborAddr);
			this.routingEntrySetForwardList.remove(routingEntrySetOld); //remove the old one
			this.routingEntrySetForward.remove((int)neighborAddr);
			this.log.debug("routingEntrySetForward is modified to clear info about " + neighborAddr);
		}

		this.log.debug("neighbor removed, leads to update FT");

		this.buildForwrdingTable();
	}





}
