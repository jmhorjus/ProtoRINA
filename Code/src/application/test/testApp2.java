package application.test;

import rina.ipc.impl.IPCImpl;
import application.impl.Application;

public class testApp2 {

	public static void main(String args[])
	{


		String file = "./experimentConfigFiles/testConfigFiles/bu_host2.properties";	

		//DIF Manager
		IPCImpl BostonU2 = new IPCImpl(file);
		
		
	}

}
