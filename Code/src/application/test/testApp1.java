package application.test;

import rina.ipc.impl.IPCImpl;
import rina.object.internal.*;
import rina.rib.impl.RIBImpl;
import application.component.api.IPCResourceManager;
import application.component.impl.IPCResourceManagerImpl;
import application.impl.Application;

public class testApp1 {


	public static void main(String args[])
	{


		String file = "./experimentConfigFiles/testConfigFiles/bu_host1.properties";	

		IPCImpl BostonU1 = new IPCImpl(file);



		String  apName = "app1";
		Application app1 = new Application(apName, null);
		app1.addIPC(BostonU1);


		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		System.out.println("Starting to allocate flow between applicaitons");

		IPCResourceManagerImpl ipcManager = app1.getIpcManager();

		int hanldeID = ipcManager.allocateFlow("app1", "app3");


//		System.out.println("hanldeID is " + hanldeID);


//		int i = 0;
//
//		while(true)
//		{
//			String msg = "hello " + i;
//
//			try {
//				ipcManager.send(hanldeID,msg.getBytes() );
//				System.out.println("mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmsg number:  " + i + " sent");
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			i++;
//
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}


	}
}
