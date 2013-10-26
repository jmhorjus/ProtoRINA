/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */

package rina.tcp.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class is used together with DirectorEntry.java to for talking with RINA community.
 * @author Yuefeng Wang. 
 *
 */
public class WellKnownRINAAddr {

	private Log log = LogFactory.getLog(WellKnownRINAAddr.class);

	private String apName = null;
	private String apInstance = null;
	private int RINAAddr = -1;

	LinkedHashMap<Integer, LinkedList<DirectoryEntry>> dataBase = null;
	LinkedHashMap<Integer, DirectoryEntry> addrDataBase = null;

	//Here name is a concatenation of ApName + ApInstance
	LinkedHashMap<String, Integer> nameToAddr = null;

	

	public WellKnownRINAAddr(String apName, String apInstance)
	{
		this.apName = apName;
		this.apInstance = apInstance;
		
	     this.dataBase = new LinkedHashMap<Integer, LinkedList<DirectoryEntry>>();
		this.nameToAddr = new LinkedHashMap<String, Integer> ();

		this.addrDataBase = new LinkedHashMap<Integer, DirectoryEntry> (); 



		this.init();
	}

	private void init() 
	{


		this.addrDataBase.put(64, new DirectoryEntry("Barcelona.i2CAT", "1"));
		this.nameToAddr.put("Barcelona.i2CAT"+  "1", 64);

		this.addrDataBase.put(65, new DirectoryEntry("Castefa.i2CAT", "1"));
		this.nameToAddr.put("Castefa.i2CAT"+  "1", 65);

		this.addrDataBase.put(33, new DirectoryEntry("cthulhu.TRIA-Bos", "1"));
		this.nameToAddr.put("cthulhu.TRIA-Bos"+  "1", 33);


		this.addrDataBase.put(20, new DirectoryEntry("bigslug.TRIA-Fl", "1"));
		this.nameToAddr.put("bigslug.TRIA-Fl"+  "1", 20);

		this.addrDataBase.put(16, new DirectoryEntry("Snowman.TRIA-Fl", "1"));
		this.nameToAddr.put("Snowman.TRIA-Fl"+  "1", 16);

		this.addrDataBase.put(48, new DirectoryEntry("BostonU", "1"));
		this.nameToAddr.put("BostonU"+  "1", 48);

		this.addrDataBase.put(49, new DirectoryEntry("BostonU", "2"));
		this.nameToAddr.put("BostonU"+  "2", 49);

		this.dataBase.put(64, new LinkedList<DirectoryEntry>() );
		this.dataBase.put(65, new LinkedList<DirectoryEntry>());
		this.dataBase.put(33, new LinkedList<DirectoryEntry>());
		this.dataBase.put(20, new LinkedList<DirectoryEntry>());
		this.dataBase.put(16, new LinkedList<DirectoryEntry>());
		this.dataBase.put(48, new LinkedList<DirectoryEntry>());
		this.dataBase.put(49, new LinkedList<DirectoryEntry>());



		this.dataBase.get(64).add(new DirectoryEntry("Barcelona.i2CAT", "1", "Management","84.88.40.70", 32769, 64 ));
		this.dataBase.get(64).add(new DirectoryEntry("Barcelona.i2CAT", "1", "Data Transfer","84.88.40.70", 32770, 64 ));

		this.dataBase.get(65).add(new DirectoryEntry("Castefa.i2CAT", "1", "Management","84.88.40.71", 32769, 65 ));
		this.dataBase.get(65).add(new DirectoryEntry("Castefa.i2CAT", "1", "Data Transfer","84.88.40.71", 32769, 65 ));

		this.dataBase.get(33).add(new DirectoryEntry("cthulhu.TRIA-Bos", "1", "Management","24.147.10.210", 32769, 33 ));
		this.dataBase.get(33).add(new DirectoryEntry("cthulhu.TRIA-Bos", "1", "Data Transfer","24.147.10.210", 32770, 33 ));

		this.dataBase.get(20).add(new DirectoryEntry("bigslug.TRIA-Fl", "1", "Management","tria-fl.dyndns.org", 32769, 20 ));
		this.dataBase.get(20).add(new DirectoryEntry("bigslug.TRIA-Fl", "1", "Data Transfer","tria-fl.dyndns.org", 32770, 20 ));

		this.dataBase.get(16).add(new DirectoryEntry("Snowman.TRIA-Fl", "1", "Management","tria-fl.dyndns.org", 32790, 16 ));
		this.dataBase.get(16).add(new DirectoryEntry("Snowman.TRIA-Fl", "1", "Data Transfer","tria-fl.dyndns.org", 32791, 16 ));

		this.dataBase.get(48).add(new DirectoryEntry("BostonU", "1", "Management","localhost", 32781, 48 ));
		this.dataBase.get(48).add(new DirectoryEntry("BostonU", "1", "Data Transfer","localhost", 32782, 48 ));

		this.dataBase.get(49).add(new DirectoryEntry("BostonU", "2", "Management","localhost", 32791, 49 ));
		this.dataBase.get(49).add(new DirectoryEntry("BostonU", "2", "Data Transfer","localhost", 32792, 49 ));

		this.RINAAddr = this.getLocalRINAAddr();
		
	}


	public synchronized DirectoryEntry getManagementEntry(String apName, String apInstance)
	{
		DirectoryEntry directoryEntry = null;
		if(apInstance == null)
		{
			apInstance= "1";
		}

		if(!this.nameToAddr.containsKey(apName + apInstance))
		{
			return null;
		}

		directoryEntry = this.getManagementEntry(this.nameToAddr.get(apName + apInstance));

		return directoryEntry;

	}


	public synchronized DirectoryEntry getDataEntry(String apName, String apInstance)
	{
		DirectoryEntry directoryEntry = null;
		if(apInstance == null)
		{
			apInstance= "1";
		}

		if(!this.nameToAddr.containsKey(apName + apInstance))
		{
			return null;
		}

		directoryEntry = this.getDataEntry(this.nameToAddr.get(apName + apInstance));

		return directoryEntry;

	}

	private synchronized int getLocalRINAAddr()
	{
		return this.getManagementEntry(this.apName, this.apInstance).getRINAAddr();
	}
	
	public synchronized  int getRINAAddr(String apName, String apInstance) {
		return this.getManagementEntry(apName, apInstance).getRINAAddr();
	}
	
	public  synchronized DirectoryEntry getManagementEntry(int RINAAddr)
	{
		if(this.dataBase.containsKey(RINAAddr))
		{
			return this.dataBase.get(RINAAddr).get(0);
		}else
		{
			return null;
		}
	}

	public  synchronized DirectoryEntry getDataEntry(int RINAAddr)
	{
		if(this.dataBase.containsKey(RINAAddr))
		{
			return this.dataBase.get(RINAAddr).get(1);
		}else
		{
			return null;
		}
	}



	public synchronized String getApName() {
		return apName;
	}

	public synchronized void setApName(String apName) {
		this.apName = apName;
	}

	public synchronized String getApInstance() {
		return apInstance;
	}

	public synchronized void setApInstance(String apInstance) {
		this.apInstance = apInstance;
	}

	public synchronized int getRINAAddr() {
		return RINAAddr;
	}

	public synchronized void setRINAAddr(int rINAAddr) {
		RINAAddr = rINAAddr;
	}



	public class DirectoryEntry {

		private Log log = LogFactory.getLog(DirectoryEntry.class);

		private String ApName = null;
		private String ApInstance = "1"; // by defualt
		private String AeName = null;
		private String AeInstance = "1" ;//by default
		private String hostIP = null;
		private int port = -1;
		private int RINAAddr = -1;

		public  DirectoryEntry( String ApName, String ApInstance, String AeName, String hostIP, int port, int RINAAddr)
		{
			this.ApName = ApName;
			this.ApInstance = ApInstance;
			this.AeName = AeName;
			this.hostIP = hostIP;
			this.port = port;
			this.RINAAddr = RINAAddr;
		}

		public  DirectoryEntry( String ApName, String ApInstance, String AeName, String AeInstance, String hostIP, int port, int RINAAddr)
		{
			this.ApName = ApName;
			this.ApInstance = ApInstance;
			this.AeName = AeName;
			this.AeInstance = AeInstance;
			this.hostIP = hostIP;
			this.port = port;
			this.RINAAddr = RINAAddr;
		}


		public  DirectoryEntry( String ApName, String ApInstance)
		{
			this.ApName = ApName;
			this.ApInstance = ApInstance;
		}

		public void print()
		{
			this.log.info( "ApName:" + this.ApName + ", ApInstance:" + this.ApInstance + ", AeName:" + this.AeName
					+ ",hostIP:" + this.hostIP  + ",port:" + this.port + ",RINAAddr:" +  this.RINAAddr);
		}

		public synchronized String getApName() {
			return ApName;
		}

		public synchronized void setApName(String apName) {
			ApName = apName;
		}

		public synchronized String getApInstance() {
			return ApInstance;
		}

		public synchronized void setApInstance(String apInstance) {
			ApInstance = apInstance;
		}

		public synchronized String getAeName() {
			return AeName;
		}

		public synchronized void setAeName(String aeName) {
			AeName = aeName;
		}

		public synchronized String getAeInstance() {
			return AeInstance;
		}

		public synchronized void setAeInstance(String aeInstance) {
			AeInstance = aeInstance;
		}

		public synchronized String getHostIP() {
			return hostIP;
		}

		public synchronized void setHostIP(String hostIP) {
			this.hostIP = hostIP;
		}

		public synchronized int getPort() {
			return port;
		}

		public synchronized void setPort(int port) {
			this.port = port;
		}

		public synchronized int getRINAAddr() {
			return RINAAddr;
		}

		public synchronized void setRINAAddr(int rINAAddr) {
			RINAAddr = rINAAddr;
		}

	}

}


