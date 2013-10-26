/**
 * 
 */
package video.clientProxy;

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
import video.lib.OverallStatistic;
import video.lib.RtcpPacket;
import video.lib.RtpPacket;
import video.lib.ServiceInterface;
import video.lib.Statistic;
import video.transport.TransportService;


/**
 * @author yuezhu
 * 
 */
public class RtpClientService implements ServiceInterface {
  private static final Logger LOGGER = LoggerFactory.getLogger(RtpClientService.class);
  private static TransportService service;

  private static NioDatagramAcceptor rtpAcceptor;
  private static NioDatagramAcceptor rtcpAcceptor;
  private static InetSocketAddress rtpAddress = null;
  private static InetSocketAddress rtcpAddress = null;
  
  private static Thread rtpDispatcherThread;
  private static Thread rtcpDispatcherThread;
  
  private static boolean running = false;
  
  private static Statistic overallStatistic = new OverallStatistic("client_overall");
  
  public RtpClientService(TransportService transportService) {
    service = transportService;
  }

  public static void setupDatagramConnector() throws IOException {
    int rtpPort = Configuration.getInt("client.rtp.port", Constants.CLIENT_RTP_PORT);
    int rtcpPort = Configuration.getInt("client.rtcp.port", Constants.CLIENT_RTCP_PORT);
    rtpAddress = new InetSocketAddress(rtpPort);
    rtpAcceptor = new NioDatagramAcceptor();
    rtpAcceptor.setHandler(new ClientRtpPacketHandler());
    rtpAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 0);
    rtpAcceptor.setSessionRecycler(new ExpiringSessionRecycler(0));
    rtpAcceptor.getSessionConfig().setReuseAddress(true);
    rtpAcceptor.bind(rtpAddress);
    LOGGER.info("RTP client service is listening on " + rtpPort);
    rtcpAddress = new InetSocketAddress(rtcpPort);
    rtcpAcceptor = new NioDatagramAcceptor();
    rtcpAcceptor.setHandler(new ClientRtcpPacketHandler());
    rtcpAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 0);
    rtcpAcceptor.setSessionRecycler(new ExpiringSessionRecycler(0));
    rtcpAcceptor.getSessionConfig().setReuseAddress(true);
    rtcpAcceptor.bind(rtcpAddress);
    LOGGER.info("RTCP client service is listening on " + rtcpPort);

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
    overallStatistic.doStatistic(packet);
    ClientTrack clientTrack = ClientTrack.getByProxySsrc(packet.getSsrc());
    if (clientTrack != null) {
      clientTrack.forwardRtpToClient(packet);
    }
  }
  
  private static void rtcpPacketDispatcher() {
    RtcpPacket packet = recvRtcpPacketFromRemote();
    ClientTrack clientTrack = ClientTrack.getByProxySsrc(packet.getSsrc());
    if (clientTrack != null) {
      clientTrack.forwardRtcpToClient(packet);
    }
  }

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
    
    setupDatagramConnector();
    
    overallStatistic.start();

  }

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
