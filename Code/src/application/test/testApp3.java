package application.test;

import rina.ipc.impl.IPCImpl;
import application.impl.Application;

public class testApp3 {
	
	
	public static void main(String args[])
	{
		
	
		String file = "./experimentConfigFiles/testConfigFiles/bu_host3.properties";	

		IPCImpl BostonU3 = new IPCImpl(file);
		

//		String apName = "app3";
//		Application app2 = new Application(apName);
//		
//	
//	    app2.addIPC(BostonU3);
	   
	   
		
	}

}
