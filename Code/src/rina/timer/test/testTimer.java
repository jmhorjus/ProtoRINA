/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.timer.test;


import java.util.Timer;
import java.util.TimerTask;


/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testTimer {


	Timer timer;

	public testTimer(int seconds) {
		timer = new Timer();
		timer.schedule(new testTimerTask(), 2000, seconds*1000);
		timer.schedule(new testTimerTask2(), 3000, seconds*2000);
	}
	
	public void run()
	{
	
		while(true)
		{
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("main run()");
		}
	}

	class testTimerTask extends TimerTask 
	{
		public void run() {
			System.out.format("Time's up!%n");
			//timer.cancel(); //Terminate the timer thread
		}
	}
	
	class testTimerTask2 extends TimerTask 
	{
		public void run() {
			System.out.format("1111 Time's up!%n");
			timer.cancel(); //Terminate the timer thread
		}
	}


	public static void main(String args[]) {
		testTimer time = new testTimer(1);
		time.run();
		
		System.out.format("Task scheduled.%n");
	}


}
