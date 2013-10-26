/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */

package video.clientProxy;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import video.lib.Configuration;
import video.transport.TransportChannel;
import video.transport.TransportCommon;
import video.transport.TransportService;

import application.impl.Application;


/**
 * 
 * @author Yue Zhu and Yuefeng Wang. Computer Science Department, Boston University
 *
 */
public class ClientProxy  extends Application implements TransportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientProxy.class);

	private TransportChannel rtspTransportChannel = null;
	private TransportChannel rtpTransportChannel = null;
	private TransportChannel rtcpTransportChannel = null;


	private String srcApName = null;
	private String srcInstance = null;
	private String dstApName = null;
	private String dstApInstance = null;
	private String rtspAE = "rtsp";
	private String rtpAE = "rtp";
	private String rtcpAE = "rtcp";
	
	private int rtspHandleID = -1;
	private int rtpHandleID = -1;
	private int rtcpHandleID = -1;

	public ClientProxy(String srcApName, String srcApInstance, String dstApName, String dstApInstance)
	{
		super(srcApName, srcApInstance);
		
		Configuration.getInstance("configuration.properties");
		
		this.srcApName = srcApName;
		this.srcInstance = srcApInstance;
		this.dstApName = dstApName;
		this.dstApInstance = dstApInstance;
		
		
		
	}
	

	
	public void init() throws IOException {
		
		//TEMP Solution (wait some time)
		//this will give time for underlying IPC's routing table converge,since it takes some time 
		//for the Pub/Sub mechannism to provide routing information for the routing daemon.
		//FIXME later, to make it more automatic
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		this.rtspHandleID = this.ipcManager.allocateFlow
				(this.srcApName,this.srcInstance, this.rtspAE, "1",
						this.dstApName,  this.dstApInstance, this.rtspAE, "1");
		
		this.LOGGER.debug("this.rtspHandleID " + this.rtspHandleID);
			
		this.rtpHandleID = this.ipcManager.allocateFlow
				(this.srcApName,this.srcInstance, this.rtpAE, "1",
						this.dstApName,  this.dstApInstance, this.rtpAE, "1");
				
		
		this.LOGGER.debug("this.rtpHandleID " + this.rtpHandleID);
		
		this.rtcpHandleID = this.ipcManager.allocateFlow
				(this.srcApName,this.srcInstance, this.rtcpAE, "1",
						this.dstApName,  this.dstApInstance, this.rtcpAE, "1");
		
		this.LOGGER.debug("this.rtcpHandleID " + this.rtcpHandleID);
		
		
		rtspTransportChannel = new TransportCommon(this.srcApName, this.rtspAE, this.dstApName, this.rtspAE, this.rtspHandleID, this.ipcManager);

		rtpTransportChannel = new TransportCommon(this.srcApName, this.rtpAE, this.dstApName, this.rtpAE,this.rtpHandleID,this.ipcManager);

		rtcpTransportChannel = new TransportCommon(this.srcApName, this.rtcpAE, this.dstApName, this.rtcpAE,this.rtcpHandleID,this.ipcManager);

		rtspTransportChannel.start();
		LOGGER.info("RTSP transport channel started.");
		rtpTransportChannel.start();
		LOGGER.info("RTP transport channel started.");
		rtcpTransportChannel.start();
		LOGGER.info("RTCP transport channel started.");
		
		
		RtspClientService rtspClientService = new RtspClientService(this);
		try {
			rtspClientService.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RtpClientService rtpClientService = new RtpClientService(this);
		try {
			rtpClientService.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	
	@Override
	public TransportChannel getRTSPTransportChannel() {
		return rtspTransportChannel;
	}

	@Override
	public TransportChannel getRTPTransportChannel() {
		return rtpTransportChannel;
	}

	@Override
	public TransportChannel getRTCPTransportChannel() {
		return rtcpTransportChannel;
	}

}
