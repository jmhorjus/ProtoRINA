/**
 * 
 */
package video.serverProxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.ExpiringSessionRecycler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import video.lib.Configuration;
import video.lib.Constants;
import video.lib.RtcpPacket;
import video.lib.RtpPacket;
import video.lib.ServiceInterface;
import video.transport.TransportService;


/**
 * @author yuezhu
 * 
 */
public class RtpServerService implements ServiceInterface {

  private static final Logger LOGGER = LoggerFactory.getLogger(RtpServerService.class);
  private static TransportService service;
  
  private static NioDatagramAcceptor rtpAcceptor;
  private static NioDatagramAcceptor rtcpAcceptor;
  private static InetSocketAddress rtpAddress = null;
  private static InetSocketAddress rtcpAddress = null;
  
  private static Thread rtpDispatcherThread;
  private static Thread rtcpDispatcherThread;
  
  private static boolean running = false;
  
  public RtpServerService(TransportService transportService) {
    service = transportService;
  }
  

  public void setupDatagramAcceptor() throws IOException {
    int rtpPort = Configuration.getInt("server.rtp.port", Constants.SERVER_RTP_PORT);
    int rtcpPort = Configuration.getInt("server.rtcp.port", Constants.SERVER_RTCP_PORT);
    rtpAddress = new InetSocketAddress(rtpPort);    
    rtpAcceptor = new NioDatagramAcceptor();
    rtpAcceptor.setHandler(new ServerRtpPacketHandler());
    rtpAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 0);
    rtpAcceptor.setSessionRecycler(new ExpiringSessionRecycler(0));
    rtpAcceptor.getSessionConfig().setReuseAddress(true);
    rtpAcceptor.bind(rtpAddress);
    LOGGER.info("RTP server service is listening on " + rtpPort);
    rtcpAddress = new InetSocketAddress(rtcpPort);
    rtcpAcceptor = new NioDatagramAcceptor();
    rtcpAcceptor.setHandler(new ServerRtcpPacketHandler());
    rtcpAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 0);
    rtcpAcceptor.setSessionRecycler(new ExpiringSessionRecycler(0));
    rtcpAcceptor.getSessionConfig().setReuseAddress(true);
    rtcpAcceptor.bind(rtcpAddress);
    LOGGER.info("RTCP server service is listening on " + rtcpPort);

  }
  
  private static RtpPacket recvRtpPacketFromRemote() {
    byte[] bytes = null;
    try {
      bytes = service.getRTPTransportChannel().recv();
    } catch (IOException e) {
      LOGGER.error("Failed to recv RTP packer via transport service: " + e.toString());
    }
    return new RtpPacket(IoBuffer.wrap(bytes));
  }
  
  private static RtcpPacket recvRtcpPacketFromRemote() {
    byte[] bytes = null;
    try {
      bytes = service.getRTCPTransportChannel().recv();
    } catch (IOException e) {
      LOGGER.error("Failed to recv RTCP packer via transport service: " + e.toString());
    }
    return new RtcpPacket(IoBuffer.wrap(bytes));
  }
  
  private static void rtpPacketDispatcher() {
    RtpPacket packet = recvRtpPacketFromRemote();
    ServerTrack serverTrack = ServerTrack.getByServerSsrc(packet.getSsrc());
    if (serverTrack != null) {
      serverTrack.forwardRtpToServer(packet);
    }
  }
  
  private static void rtcpPacketDispatcher() {
    RtcpPacket packet = recvRtcpPacketFromRemote();
    ServerTrack serverTrack = ServerTrack.getByProxySsrc(packet.getSsrc());
    if (serverTrack != null) {
      serverTrack.forwardRtcpToServer(packet);
    }
  }


  /*
   * (non-Javadoc)
   * 
   * @see demo.video.common.ServiceInterface#start()
   */
  @Override
  public void start() throws IOException {
    running = true;
    
    rtpDispatcherThread = new Thread() {
      @Override
      public void run() {
        while (running) {
          rtpPacketDispatcher();
        }
      }
    };
    
    rtcpDispatcherThread = new Thread() {
      @Override
      public void run() {
        while (running) {
          rtcpPacketDispatcher();
        }
      }
    };
    
    rtpDispatcherThread.start();
    rtcpDispatcherThread.start();
    
    setupDatagramAcceptor();

  }

  /*
   * (non-Javadoc)
   * 
   * @see demo.video.common.ServiceInterface#stop()
   */
  @Override
  public void stop() throws IOException {
    running = false;
  }
  
  public static IoSession newRtpSession(SocketAddress remoteAddress) {
    IoSession session = rtpAcceptor.newSession(remoteAddress, rtpAddress);
    session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 0);
    return session;
  }

  public static IoSession newRtcpSession(SocketAddress remoteAddress) {
    IoSession session = rtcpAcceptor.newSession(remoteAddress, rtcpAddress);
    session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 0);
    return session;
  }

  public static InetSocketAddress getRtpAddress() {
    return rtpAddress;
  }

  public static InetSocketAddress getRtcpAddress() {
    return rtcpAddress;
  }

  public static InetAddress getHostAddress() {
    /*
     * The InetAddress (IP) is the same for both RTP and RTCP.
     */
    return rtpAddress.getAddress();
  }

  public static int getRtpPort() {
    return rtpAddress.getPort();
  }

  public static int getRtcpPort() {
    return rtcpAddress.getPort();
  }


}
