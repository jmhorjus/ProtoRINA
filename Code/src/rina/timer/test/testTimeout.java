/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.timer.test;



import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class testTimeout {
	public static void main(String[] args) throws Exception {
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> future = executor.submit(new Task());

		try {
			System.out.println("Started..");
			System.out.println(future.get(3, TimeUnit.SECONDS));
			System.out.println("Finished!");
		} catch (TimeoutException e) {
			System.out.println("Terminated!");
		}

		executor.shutdownNow();
	}
}

class Task implements Callable<String> {
	@Override
	public String call() throws Exception {
		Thread.sleep(2000); // Just to demo a long running task of 4 seconds.
		return "Ready!";
	}
}


