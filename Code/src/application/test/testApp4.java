package application.test;

import rina.ipc.impl.IPCImpl;
import application.impl.Application;

public class testApp4 {
	
	
	public static void main(String args[])
	{
		
	
		String file = "./experimentConfigFiles/testConfigFiles/bu_host4.properties";	

		IPCImpl BostonU4 = new IPCImpl(file);
		
//		
//		String apName = "app4";
//		Application app4 = new Application(apName);
//		
//	
//	    app4.addIPC(BostonU4);
//	   
	   
		
	}

}
