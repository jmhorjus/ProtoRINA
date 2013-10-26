/**
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

package rina.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.tcp.util.Varint;
import rina.util.MessageQueue;



/**
 * TCP flow 
 * @author Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0
 */

public class TCPFlow {

	private Log log = LogFactory.getLog(TCPFlow.class);

	/**
	 * source address
	 */
	private byte[] addr;
	/**
	 * Internet address
	 */
	private InetAddress inetAddr;
	/**
	 * destination port
	 */
	private int dstPort;
	/**
	 * destination address
	 */
	private byte[] dnsAddr;
	/**
	 * TCP socket
	 */
	private Socket socket;
	/**
	 * TCP server socket
	 */
	private ServerSocket serverSocket;
	/**
	 * local port 
	 */
	private int localPort;
	/**
	 * URL or IP address of dst
	 */
	private String dstURL;


	/**
	 * This one is used for possible use for flow allocator (TCP Manager)
	 */
	private int flowID;
	private String srcName;
	private String dstName;


	private MessageQueue  msgQueue = null;

	/**
	 * Dummy Constructor
	 */
	public TCPFlow()
	{
		this.msgQueue = new MessageQueue();
	}





	/**
	 * Constructor
	 * create a local tcp flow listening to  a certain port 
	 * @param local Port
	 * @throws Exception 
	 */
	public TCPFlow(int localPort) throws Exception
	{
		this.localPort = localPort;
		this.msgQueue = new MessageQueue();
		this.serverSocket = new ServerSocket(this.localPort);
		
	}

	/**
	 * Constructor
	 * @param dstURL
	 * @param port
	 * @throws Exception 
	 * @throws Exception 
	 */
	public TCPFlow(String dstURL, int dstPort) throws Exception 
	{
		this.dstURL = dstURL;
		this.dstPort = dstPort;
		this.msgQueue = new MessageQueue();

		inetAddr = InetAddress.getByName(this.dstURL);
		socket = new Socket(inetAddr, this.dstPort);

	}




	public synchronized void send( byte[] message) throws Exception
	{   

		OutputStream  out =  socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		int len =  message.length;

		byte[] varByteArray = Varint.writeVarint(len);

		if(len <= Integer.MAX_VALUE)
		{	
			for(int i = 0; i< varByteArray.length; i++)
			{
				dos.writeByte(varByteArray[i]);
			}

			dos.write(message, 0, len);

		}else
		{
			this.log.error("Message too large, fragment it first");
		}


	//	this.log.debug(" Msg sent out with length " + len + " to dstName "  + this.dstName+ ", IP address " + this.dstURL );
	}




	/**
	 * receive a message from the tcp socket 
	 * @return the array of byte received over the TCP socket
	 * @throws Exception 
	 * @throws Exception 
	 */
	public  byte[] receive() throws Exception 
	{   
		byte[] data = null;

		InputStream in = socket.getInputStream();
		DataInputStream dis = new DataInputStream(in);

		int length = 0 ;

		int value = 0;
		int b;
		int i = 0;
		while (((b = dis.readByte()) & 0x80) != 0) 
		{
			value |= (b & 0x7F) << i;
			i+= 7;	 
		}

		length =  value | (b << i);

	//	this.log.debug("msg received  length is " + length);


		data  = new byte[length];
		dis.readFully(data, 0 , length);


		return data;
	}

	/**
	 * accept an incoming client socket
	 * @return a listening TCP flow
	 */
	public TCPFlow accept() {

		TCPFlow ListeningTcpFlow = new TCPFlow();
		try {
			ListeningTcpFlow.socket = serverSocket.accept();
		} catch (IOException e) {
			this.log.warn("Exception: " +e);
			e.printStackTrace();
		}
		return ListeningTcpFlow;
	}

	public void close() {
		if(serverSocket != null){
			try {
				serverSocket.close();
			} catch (IOException e) {
				this.log.warn("Exception: " + e);
				e.printStackTrace();
			}
		}
		if(socket != null){
			try {
				socket .close();
			} catch (IOException e) {
				this.log.warn("Exception: " + e);
				e.printStackTrace();
			}
		}
	}


	/**
	 * 
	 * @return destination port
	 */
	public int getDstport() {
		return dstPort;
	}
	/**
	 * 
	 * @param destination port
	 */
	public void setDstport(int dstPort) {
		this.dstPort = dstPort;
	}
	/**
	 * 
	 * @param localPort
	 */
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	/**
	 * getUrl
	 * @return dstURL
	 */
	public String getUrl() {
		return dstURL;
	}
	/**
	 * setUrl
	 * @param dstURL
	 */
	public void setUrl(String dstURL) {
		this.dstURL = dstURL;
	}
	/**
	 * getAddr
	 * @return addr
	 */
	public byte[] getAddr() {
		return addr;
	}
	/**
	 * setAddr
	 * @param addr
	 */
	public void setAddr(byte[] addr) {
		this.addr = addr;
	}
	/**
	 * getInetAddr
	 * @return inetAddr
	 */
	public InetAddress getInetAddr() {
		return inetAddr;
	}
	/**
	 * setInetAddr
	 * @param inetAddr
	 */
	public void setInetAddr(InetAddress inetAddr) {
		this.inetAddr = inetAddr;
	}
	/**
	 * getPort
	 * @return port
	 */
	public int getDstPort() {
		return dstPort;
	}
	/**
	 * setPort
	 * @param port
	 */
	public void setDstPort(int port) {
		this.dstPort = port;
	}
	/**
	 * getDnsAddr
	 * @return dnsAddr
	 */
	public byte[] getDnsAddr() {
		return dnsAddr;
	}
	/**
	 * setDnsAddr
	 * @param dnsAddr
	 */
	public void setDnsAddr(byte[] dnsAddr) {
		this.dnsAddr = dnsAddr;
	}
	/**
	 * getSocket
	 * @return socket
	 */
	public Socket getSocket() {
		return socket;
	}
	/**
	 * setSocket
	 * @param socket
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	/**
	 * getServerSocket
	 * @return serverSocket
	 */
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	/**
	 * setServerSocket
	 * @param ServerSocket serverSocket
	 */
	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	/**
	 * getlocalPort
	 * @return  localPort
	 */
	public int getLocalPort() {
		return localPort;	
	}
	/**
	 * setlocalPort
	 * @param localPort
	 */
	public void setlocalPort(int localPort) {
		this.localPort = localPort;
	}





	public synchronized String getDstURL() {
		return dstURL;
	}





	public synchronized void setDstURL(String dstURL) {
		this.dstURL = dstURL;
	}





	public synchronized int getFlowID() {
		return flowID;
	}




	/**
	 * Set the flow ID for the TCP flow, also attach the msg queue with this flow ID
	 * @param flowID
	 */
	public synchronized void setFlowID(int flowID) {
		this.flowID = flowID;
		this.msgQueue.setFlowID(flowID);
	}





	public synchronized String getSrcName() {
		return srcName;
	}





	public synchronized void setSrcName(String srcName) {
		this.srcName = srcName;
	}





	public synchronized String getDstName() {
		return dstName;
	}





	public synchronized void setDstName(String dstName) {
		this.dstName = dstName;
	}





	public synchronized MessageQueue getMsgQueue() {
		return msgQueue;
	}





	public synchronized void setMsgQueue(MessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}




} //end of class
