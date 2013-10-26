/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */
package application.impl;

import rina.irm.util.HandleEntry;
import rina.object.internal.Flow;
import application.component.util.DummyHandler;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class DummyApplication extends Application {

	public DummyApplication(String configurationFile) {
		super(configurationFile);
		// TODO Auto-generated constructor stub
	}
	public DummyApplication(String apName, String apInstance) {
		super(apName, apInstance);
		// TODO Auto-generated constructor stub
	}

	//overload this method in extended class
	public void attachHandler(int handleID, HandleEntry he) {

		new DummyHandler(handleID, this.ipcManager);

	}
}
