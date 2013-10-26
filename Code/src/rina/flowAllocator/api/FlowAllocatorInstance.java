
/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */



package rina.flowAllocator.api;

import rina.object.internal.Flow;

/**
 * Flow Allocator Instance API
 * @author Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0
 *
 */
public interface FlowAllocatorInstance {

	public void submitAllocationRequest(Flow flowRequest);


	public void receiveAllocationRequest(Flow flowRequest);
	
	
	

}
