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
 * Flow Allocator API
 * @author Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0
 *
 */
public interface FlowAllocator {
	
	/**
	 * This is the one side sending request
	 * Return the portID 
	 * Create the Flow Allocator Instance (FAI) to for each flow request
	 * @param flowRequest : specify the parameters of the flow requested
	 * @return
	 */
	public int submitAllocationRequest(Flow flowRequest );
	
	/**
	 * This is the other side receiving request
	 * Create the Flow Allocator Instance (FAI) upon receive the flow create request
	 * Return the portID if accepted 
	 * @param flowResponse
	 */
	public int receiveAllocationRequest(Flow flowRequest);

	
	/**
	 * Remove the FAI for portID
	 * @param portID
	 */
	public void deallocateFlow(int portID);

}
 