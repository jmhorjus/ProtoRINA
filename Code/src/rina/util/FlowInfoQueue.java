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

package rina.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.object.internal.Flow;

/**
 * This one is used in the communication between application and its underlying IPC for creating flow object
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class FlowInfoQueue {

	private Log log = LogFactory.getLog(this.getClass());

	private BlockingQueue<Flow> flowInfoQueue = null;

	public FlowInfoQueue()
	{
		this.flowInfoQueue = new LinkedBlockingQueue<Flow>(); 
	}
	public  void addFlowInfo(Flow flow)
	{
		this.flowInfoQueue.offer(flow);
	}

	public Flow getFlowInfo() 
	{
		Flow flow = null;
		try {
			flow =  this.flowInfoQueue.take();
		} catch (InterruptedException e) {
			this.log.error(e.getMessage());
		}
		return flow;
	}



}
