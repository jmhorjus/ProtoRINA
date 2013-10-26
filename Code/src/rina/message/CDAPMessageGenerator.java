/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.message;




/**
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 *   
 */

public class CDAPMessageGenerator {

	/**
	 * version of the abstract syntax
	 */
	private static final int ABSTRACT_SYNTAX_VERSION = 0x0073;

	/**
	 * version of the CDAP protocol
	 */

	private static final int CDAP_VERSION = 0x0001;
	/**
	 * dummy Constructor
	 */
	public CDAPMessageGenerator(){}



	/**
	 * generate M_CONNECT
	 * @param authMech
	 * @param authValue
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_CONNECT(
			CDAP.authTypes_t authMech,
			CDAP.authValue_t authValue, 
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName, 
			int invokeID, 
			String srcAEInst, 
			String srcAEName, 
			String srcApInst,
			String srcApName
			)
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setAuthMech(authMech);
		cdapMessage.setAuthValue(authValue);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CONNECT);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}

	/**
	 * 
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_CONNECT(

			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName, 
			int invokeID, 
			String srcAEInst, 
			String srcAEName, 
			String srcApInst,
			String srcApName

			)
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setAbsSyntax(0x0072);
		cdapMessage.setAuthMech(CDAP.authTypes_t.AUTH_NONE);

		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);

		cdapMessage.setInvokeID(invokeID);

		cdapMessage.setOpCode(CDAP.opCode_t.M_CONNECT);

		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(0x0001);


		return  cdapMessage.buildPartial();
	}
	
	


	/**
	 * generate M_CONNECT_R
	 * @param resultValue
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_CONNECT_R(
			int resultValue,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName, 
			int invokeID, 
			String srcAEInst, 
			String srcAEName, 
			String srcApInst,
			String srcApName
			)
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setResult(resultValue);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CONNECT_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.build();
	}

	public static CDAP.CDAPMessage generateM_CONNECT_R(
			int absSyntax,
			int resultValue,
			CDAP.authTypes_t authMech,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName, 
			int invokeID, 
			String srcAEInst, 
			String srcAEName, 
			String srcApInst,
			String srcApName,
			long version
			)
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setAbsSyntax(absSyntax);
		cdapMessage.setResult(resultValue);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CONNECT_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(version);

		return  cdapMessage.build();
	}

	/**
	 * 
	 * @param ObjClass
	 * @param ObjName
	 * @param objValue
	 * @param invokeID
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_CREATE(
			String ObjClass,
			String ObjName,
			CDAP.objVal_t objValue,
			int invokeID
			)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setObjValue(objValue);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE);
		cdapMessage.setInvokeID(invokeID);

		return  cdapMessage.buildPartial();

	}
	
	/**
	 * This one might be used for DDF
	 * @param ObjClass
	 * @param ObjName
	 * @param invokeID
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_CREATE(
			String ObjClass,
			String ObjName,
			int invokeID
			)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE);
		cdapMessage.setInvokeID(invokeID);

		return  cdapMessage.buildPartial();

	}

	
	public static CDAP.CDAPMessage generateM_CREATE_R(
			int result,
			String ObjClass,
			String ObjName,
			CDAP.objVal_t objValue,
			int invokeID
			)    {
		
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result);
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setObjValue(objValue);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE_R);
		cdapMessage.setInvokeID(invokeID);

		return  cdapMessage.buildPartial();

	}

	
	public static CDAP.CDAPMessage generateM_CREATE_R(
			int result,
			String ObjClass,
			String ObjName,
			int invokeID
			)    {
		
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result);
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE_R);
		cdapMessage.setInvokeID(invokeID);

		return  cdapMessage.buildPartial();

	}
	
	public static CDAP.CDAPMessage generateM_START(
			String objClass,
			String objName,
			CDAP.objVal_t objValue,
			int invokeID
			)    
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 
		cdapMessage.setObjValue(objValue);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_START);
		cdapMessage.setVersion(0x0001);

		return  cdapMessage.buildPartial();
	}

	
	public static CDAP.CDAPMessage generateM_START(
			String objClass,
			String objName,
			int invokeID
			)    
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_START);

		return  cdapMessage.buildPartial();
	}
	
	public static CDAP.CDAPMessage generateM_STOP_R(    
			int result,
			String objClass,
			int invokeID) {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2

		cdapMessage.setResult(result);

		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_STOP_R);

		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial();


	}
	
	public static CDAP.CDAPMessage generateM_READ_R(
			String ObjClass,
			String ObjName,
			int invokeID
			)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_READ_R); //Mandatory

		return  cdapMessage.buildPartial();
	}


	public static CDAP.CDAPMessage generateM_CONNECT_R(
			int absSyntax,
			int resultValue,
			CDAP.authTypes_t authMech,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName, 
			int invokeID, 
			String srcAEInst, 
			String srcAEName, 
			String srcApInst,
			String srcApName,
			long version
			)
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setAbsSyntax(absSyntax);
		cdapMessage.setResult(resultValue);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CONNECT_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(version);

		return  cdapMessage.build();
	}

	public static CDAP.CDAPMessage generateM_START_R(
			int result,
			String objClass,
			String objName,
			CDAP.objVal_t objValue,
			int invokeID)
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result);
		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 
		cdapMessage.setObjValue(objValue);	
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_START_R);

		return  cdapMessage.buildPartial();

	}

	public static CDAP.CDAPMessage generateM_STOP(

			String objClass,
			String objName,
			CDAP.objVal_t objValue,
			int invokeID

			)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 
		cdapMessage.setObjValue(objValue);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_STOP);



		return  cdapMessage.buildPartial();

	}
	
	
	/**
	 * encode CDAPMessage to byte array
	 * @param msg
	 * @return data
	 */
	public static byte[] encodeCDAPMessage(CDAP.CDAPMessage msg)
	{
		byte[] data = msg.toByteArray();
		return data;
	}

}