/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */

package video.serverProxy;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rina.irm.util.HandleEntry;
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
public class ServerProxy extends Application implements TransportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerProxy.class);

	private TransportChannel rtspTransportChannel = null;
	private TransportChannel rtpTransportChannel = null;
	private TransportChannel rtcpTransportChannel = null;


	private String rtspAE = "rtsp";
	private String rtpAE = "rtp";
	private String rtcpAE = "rtcp";

	public ServerProxy(String serverName, String serverInstance) {
		super(serverName, serverInstance);
		Configuration.getInstance("configuration.properties");

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

	@Override
	public void attachHandler(int handleID, HandleEntry he ) {

		//if(flow.getsrcflow.getSrcApInfo().getAeName().equals("rtsp"))
		if(he.getSrcAeName().equals("rtsp"))
		{
			rtspTransportChannel = new TransportCommon(this.apName, "rtsp", handleID, this.ipcManager);
			rtspTransportChannel.start();
			LOGGER.info("RTSP transport channel started.");

			RtspServerService rtspServerService = new RtspServerService(this);
			try {
				rtspServerService.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else if(he.getSrcAeName().equals("rtp"))
		{
			rtpTransportChannel = new TransportCommon(this.apName, "rtp", handleID, this.ipcManager);
			rtpTransportChannel.start();
			LOGGER.info("RTP transport channel started.");

			if(rtpTransportChannel != null && rtcpTransportChannel != null )	
			{
				RtpServerService rtpServerService = new RtpServerService(this);
				try {
					rtpServerService.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}else if(he.getSrcAeName().equals("rtcp"))
		{
			rtcpTransportChannel = new TransportCommon(this.apName, "rtcp", handleID, this.ipcManager);
			rtcpTransportChannel.start();
			LOGGER.info("RTCP transport channel started.");

			if(rtpTransportChannel != null && rtcpTransportChannel != null )	
			{
				RtpServerService rtpServerService = new RtpServerService(this);
				try {
					rtpServerService.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}



}
