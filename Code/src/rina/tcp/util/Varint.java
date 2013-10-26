/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 * 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 *   
 */

package rina.tcp.util;

public class Varint {
	
	
	public static  byte[] writeVarint(int value) {
		int numberOfBytes = 0;
		byte[] encodedLength = null;
		byte[] buffer = new byte[5];

		while (true) {
			if ((value & ~0x7F) == 0) {
				buffer[numberOfBytes] = (byte)value;
				numberOfBytes ++;
				encodedLength = new byte[numberOfBytes];
				for(int i=0; i<encodedLength.length; i++){
					encodedLength[i] = buffer[i];
				}
		
				return encodedLength;
			} else {
				buffer[numberOfBytes] = (byte)((value & 0x7F) | 0x80);
				numberOfBytes++;
				value >>>= 7;
			}
		}
	}
	
	
	public static void main(String args[])
	{
		int len = 1000;
		
		System.out.println("orignal varint is  "  + len);
		
		byte[] varByteArray = writeVarint(len);
		
		printVarint(len);
		
		int back = readVarint(varByteArray);
		
		System.out.println("the read varint is "  + back);
		
	}
	
	public static int readVarint(byte[] buffer)
	{
		int index = 0;
		int value = 0;
		int b;
		int i = 0;
		
		while (((b = buffer[index]) & 0x80) != 0) 
		{
			 value |= (b & 0x7F) << i;
		     i+= 7;	 
			index++;
		}
		
		return value | (b << i);
	
		
	}
	
	
	public static void printVarint(int len)
	{
		byte[] encodedLength = writeVarint(len);
		
		for(int i =0; i < encodedLength.length;i++)
		{
		   int value;
		   
		   if(  encodedLength[i] <128 && encodedLength[i] >=0 )
		   {
			   value = encodedLength[i];
			   System.out.println(" byte " + i + ": the last byte ");
		   }else
		   {
			   value = 256 + encodedLength[i];
			   System.out.println(" byte " + i + ": not the last byte ");
		   }
			
		   
		   System.out.println(" byte " + i + " is " + value);
		   

		   System.out.println(" byte " + i + " binary " + Integer.toBinaryString( value & 0x00FF ) );
		}
		
	
	}

}


