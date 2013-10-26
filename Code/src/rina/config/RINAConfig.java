/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 *   
 */

package rina.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Read or set the configuration parameters common to every IPC
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 *   
 *
 */
public class RINAConfig {

	/**
	 * configuration file
	 */
	private String configFile = null;
	/**
	 * auxiliar property object
	 */
	private Properties rinaProperties = null;

	/**
	 * auxiliar input stream to read from configuration file
	 */
	private InputStream inputStream = null;
	/**
	 * logger
	 */
	private Log log = LogFactory.getLog(RINAConfig.class);


	/**
	 * 
	 * @param configFile
	 */
	public RINAConfig(String configFile){

		this.configFile = configFile;
		this.loadRinaProperties();

	}


	/**
	 * config manullly instead of reading from a configration
	 */
	public RINAConfig()
	{
		this.rinaProperties = new Properties();
	}



	/**
	 *  Reads and loads properties from the "rina.properties" file
	 */
	public void loadRinaProperties() {

		this.rinaProperties = new Properties();
		try{
			InputStream inputStream = new FileInputStream(this.configFile);
			this.rinaProperties.load(inputStream);

			this.log.info("Configuration file: "+this.configFile+" loaded");

		}catch(IOException e){
			e.printStackTrace();
		}
		finally {
			if( null != inputStream ) 
				try { 
					inputStream.close(); 
				} catch( IOException e ) 
				{ 
					e.printStackTrace();
				}
		}

	}


	public String getIPCName()
	{
		String IPCName = (String) this.rinaProperties.getProperty("rina.ipc.name");
		this.log.info("IPCName: "+ IPCName ) ;
		return IPCName;
	}


	public String getIPCInstance()
	{
		String IPCInstance = (String) this.rinaProperties.getProperty("rina.ipc.instance");
		this.log.info("IPCInstance: "+ IPCInstance ) ;
		return IPCInstance;
	}

	public int getTCPPort()
	{
		int TCPPort = Integer.parseInt((String) this.rinaProperties.getProperty("TCPPort"));
		this.log.info("TCPPort: "+TCPPort );
		return TCPPort;

	}


	public int getDNSPort()
	{

		int DNSPort = Integer.parseInt((String) this.rinaProperties.getProperty("rina.dns.port"));
		this.log.info("DNSPort: "+ DNSPort );
		return DNSPort;

	}


	public String getDNSName()
	{
		String DNSName = (String) this.rinaProperties.getProperty("rina.dns.name");
		this.log.info("DNSName: "+ DNSName ) ;
		return DNSName;
	}



	public int getIDDPort()
	{
		int IDD_PORT = Integer.parseInt((String) this.rinaProperties.getProperty("rina.idd.port"));
		this.log.info("IDD local port is: "+IDD_PORT );
		return IDD_PORT;
	}


	/**
	 * get IDD Name
	 */
	public String getIDDName()
	{

		String IDD_NAME = this.rinaProperties.getProperty("rina.idd.name");
		this.log.info("IDD name is: "+IDD_NAME);
		return IDD_NAME;

	}


	public String getUserName()
	{

		String userName = this.rinaProperties.getProperty("rina.ipc.userName");
		this.log.info("User name is: "+userName);
		return userName;

	}

	public String getPassWord()
	{

		String passWord = this.rinaProperties.getProperty("rina.ipc.passWord");
		this.log.info("Password is: "+passWord);
		return passWord;

	}

	public String getDIFName()
	{

		String difName = this.rinaProperties.getProperty("rina.dif.name");
		this.log.info("The name of the DIF that is going to join  is: "+ difName);
		return difName;

	}

	public String getNeighbour(int i)
	{
		String neighbour = this.rinaProperties.getProperty("neighbour." + i);
		if(neighbour != null )
		{
			this.log.info("Name of neihbour " + i +" is " + neighbour);
		}
		return neighbour;

	}




	/**
	 * set a new property 
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value){
		//TODO: store the properties in the RIB
		this.rinaProperties.setProperty(key, value);
	}


	/**
	 * get a property from the configuration file
	 * @param key
	 * @return value
	 */
	public synchronized  String getProperty(String key){

		return this.rinaProperties.get(key).toString();

	}
	
	/**
	 * This is used to replace "rina.ipc.flag" in the first place in the configuration file 
	 * "rina.ipc.flag" has three cases: two for DIF0(1.BU 2. RINA community), one for non-DIF0.
	 * Right now we ignore RINA community case.
	 * So use level is enough, but ipc_flag is kept in the IRM implementation
	 * @return
	 */
	public synchronized  String getIPCLevel()
	{
		return this.rinaProperties.getProperty("rina.ipc.level").toString();
	}
	

	/**
	 * @return the configFile
	 */
	public synchronized String getConfigFile() {
		return configFile;
	}

	/**
	 * @param configFile the configFile to set
	 */
	public synchronized void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	/**
	 * @return the rinaProperties
	 */
	public synchronized Properties getRinaProperties() {
		return rinaProperties;
	}

	/**
	 * @param rinaProperties the rinaProperties to set
	 */
	public synchronized void setRinaProperties(Properties rinaProperties) {
		this.rinaProperties = rinaProperties;
	}



	public boolean enrollmentFlag() {

		String flag = this.rinaProperties.getProperty("rina.enrollment.flag");

		boolean flag_B = false;

		if(flag != null)
		{
			flag_B = Boolean.parseBoolean(flag);

		}

		return flag_B;



	}



	public boolean getEnrolledState() {

		String enrolled = this.rinaProperties.getProperty("rina.dif.enrolled").trim();

		if(enrolled != null && (enrolled.equals("true") || enrolled.equals("false")))
		{
			return Boolean.parseBoolean(enrolled);
		}

		return false;


	}



	public String getAuthenticatorApName() {
		return this.rinaProperties.getProperty("rina.authenticator.apName");
	}



	public String getAuthenticatorApInstance() {
		return this.rinaProperties.getProperty("rina.authenticator.apInstance");
	}



	public int getRINAAddr() {

		return Integer.parseInt(this.rinaProperties.getProperty("rina.address"));
	}


	public LinkedList<String> getUnderlyingDIFs() {

		LinkedList<String> underlyingDIFList = new LinkedList<String>();

		boolean stop = true;
		int i = 1; 

		while(stop)
		{
			String underlyingDIF = this.rinaProperties.getProperty("rina.underlyingDIF.name."+ i);
			if(underlyingDIF == null)
			{
				stop = false;
			}else
			{
				underlyingDIFList.add(underlyingDIF);
				i++;
			}
		}

		return underlyingDIFList;

	}



	public String  getAuthenPolicy() {
		return this.rinaProperties.getProperty("rina.enrollment.authenPolicy");
	}

	public Double  getRoutingEntrySubUpdatePeriod() {
		return  Double.parseDouble(  this.rinaProperties.getProperty("rina.routingEntrySubUpdatePeriod") );
	}


	public String  getRoutingProtocol() {
		return this.rinaProperties.getProperty("rina.routing.protocol");
	}


	/**
	 * The frequency to check if a neighbor is up or down
	 * @return
	 */

	public Double getCheckNeighborPeriod() {

		return  Double.parseDouble(  this.rinaProperties.getProperty("rina.checkNeighborPeriod") );
	}



	public String getLinkCostPolity() {

		return this.rinaProperties.getProperty("rina.linkCost.policy");
	}


	//manually set the underlyingDIFs 
	public void setUnderlyingDIFs(LinkedList<String> underlyingDIFs) {

		for(int i = 0; i < underlyingDIFs.size();i++)
		{
			this.setProperty("rina.underlyingDIF.name."+ (i+1) , underlyingDIFs.get(i));
		}

	}

	//Application related 

	//required
	public String getApplicationName ()
	{
		return this.rinaProperties.getProperty("application.name");
	}
	
	public String getRelayedApName() {
		
		return this.rinaProperties.getProperty("relayed.apName");
	}

	public String getRelayedApInstance() {
		
		return this.rinaProperties.getProperty("relayed.apInstance");
	}



	
	//optional
	public String getApplicationInstance ()
	{
		String apInstance =  this.rinaProperties.getProperty("application.instance");
		if(apInstance == null)
		{
			apInstance = "";
		}
		return apInstance;
	}

	
	//optional
	public String getServiceName() {
		
		String serviceName = this.rinaProperties.getProperty("service.name");
		if(serviceName == null)
		{
			serviceName = "";
		}
		return serviceName;
	}
		
	
	


	public String getAddressPolicy() {

		String policy = this.rinaProperties.getProperty("rina.address.policy");

		//default is random 
		if(policy == null)
		{
			policy = "DEFAULT";
		}

		return policy;

	}


	//used for RINA node configuration
	public String getNodeName() {

		return this.rinaProperties.getProperty("node.name");
	}


	public String getUnderlyIPCConfigFileName(int i)
	{

		String fileName =  this.rinaProperties.getProperty("underlyingIPC." + i + ".configurationFile");

		if(fileName != null)
		{
			this.log.info( "underlyingIPC." + i + ".configurationFile is " + fileName);
		}

		return fileName;
	}




	public String getOnNodeIPCName(int i) {

		String ipcName =  this.rinaProperties.getProperty("IPC." + i + ".name");

		if(ipcName != null)
		{
			this.log.info( "IPC." + i + ".ipcName is " + ipcName);
		}

		return ipcName;

	}


	public String getOnNodeIPCInstance(int i) {

		String ipcInstance =  this.rinaProperties.getProperty("IPC." + i + ".instance");

		if(ipcInstance != null)
		{
			this.log.info( "IPC." + i + ".instance is " + ipcInstance);
		}

		return ipcInstance;


	}

	public String getOnNodeIPCDIF(int i) {

		String ipcDIF =  this.rinaProperties.getProperty("IPC." + i + ".DIF");

		if(ipcDIF != null)
		{
			this.log.info( "IPC." + i + ".DIF is " + ipcDIF);
		}

		return ipcDIF;
	}

	public String getOnNodeIPCConfigFileName(int i) {

		String fileName =  this.rinaProperties.getProperty("IPC." + i + ".configurationFile");

		if(fileName != null)
		{
			this.log.info( "IPC." + i + ".configurationFile is " + fileName);
		}

		return fileName;
	}


	public String getUnderlyingIPCKey(int i) {

		String key =  this.rinaProperties.getProperty("underlyingIPC." + i );

		if(key != null)
		{
			this.log.info( "underlyingIPC." + i + "  is " + key);
		}

		return key;
	}






}
