/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose.
 */

package rina.object.internal;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.object.gpb.Neighbour_t.neighbor_t;
import rina.object.gpb.Neighbour_t.neighbors_t;

/**
 * This corresponds with the Neighbors in rina.object.gpb 
 * This contains all the direct neighbors of an IPC process.
 * Note: This is not DIF member. DIF member is another attributed in the RIB.
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class Neighbors {


	private Log log = LogFactory.getLog(this.getClass());

	private LinkedHashMap<Long, Neighbor>  addrToNeighbor = null;
	private LinkedHashMap<String, Neighbor>  nameToNeighbor = null;

	private LinkedList<Neighbor> neighborList = null; 


	public Neighbors()
	{
		this.addrToNeighbor = new LinkedHashMap<Long, Neighbor> ();
		this.nameToNeighbor = new LinkedHashMap<String, Neighbor> ();
		this.neighborList = new LinkedList<Neighbor>();


	}

	/**
	 * Return true if new neighbor added
	 * false if neighbor existed before
	 * @param neighbor
	 * @return
	 */
	public synchronized boolean addNeighbor(Neighbor neighbor)
	{

		neighbor.print();

		if(this.addrToNeighbor.containsKey(neighbor.getAddr()))
		{
			this.log.debug("neighbor exists before" );
			return true;
		}

		this.addrToNeighbor.put(neighbor.getAddr(), neighbor);

		String name = neighbor.getApName() + neighbor.getApInstance();

		this.nameToNeighbor.put(name, neighbor);

		this.neighborList.add(neighbor);

		return false;


	}

	public synchronized boolean containNeighbor(int addr)
	{
		return this.addrToNeighbor.containsKey((long)addr);
	}

	public synchronized boolean containNeighbor(long addr)
	{
		return this.addrToNeighbor.containsKey(addr);
	}


	public synchronized void removeNeighbor(String ipcName, String ipcInstance)
	{
		String name = ipcName + ipcInstance;

		if(!this.nameToNeighbor.containsKey(name)) {return;} // does not exist

		Neighbor neighbor = this.nameToNeighbor.get(name);

		long addr = neighbor.getAddr();

		this.nameToNeighbor.remove(name);
		this.addrToNeighbor.remove(addr);
		this.neighborList.remove(neighbor);

	}

	public synchronized void removeNeighbor(long addr)
	{
		if(!this.addrToNeighbor.containsKey(addr))// does not exist
		{	
			this.log.debug("The neighbor with addr "  +  addr + " does not exist, thus it cannot be removed from Neighbor info");
			return;
		}

		Neighbor neighbor = this.addrToNeighbor.get(addr);

		String apName = neighbor.getApName();
		String apInstance = neighbor.getApInstance();

		this.addrToNeighbor.remove(addr);
		this.nameToNeighbor.remove(apName + apInstance);
		this.neighborList.remove(neighbor);

		this.log.debug("Neighbor(" + apName + "/" +  apInstance + "/" + addr + ") removed");


	}

	
	

	public synchronized Neighbor getBeighbor(long addr)
	{
		return this.addrToNeighbor.get(addr);
	}




	public synchronized LinkedList<Neighbor> getNeighborList()
	{
		return this.neighborList;
	}


	public synchronized neighbors_t convert()
	{
		rina.object.gpb.Neighbour_t.neighbors_t.Builder  neighbours =  rina.object.gpb.Neighbour_t.neighbors_t.newBuilder();


		for(int i = 0; i< this.neighborList.size();i++)
		{

			neighbor_t.Builder  neighbour =  neighbor_t.newBuilder();

			Neighbor neighbor = this.neighborList.get(i);

			neighbour.setApplicationProcessName(neighbor.getApName());
			neighbour.setApplicationProcessInstance(neighbor.getApInstance());
			neighbour.setAddress(neighbor.getAddr()); 

			neighbours.addNeighbor(neighbour.buildPartial());
		}

		return neighbours.buildPartial();

	}


}


