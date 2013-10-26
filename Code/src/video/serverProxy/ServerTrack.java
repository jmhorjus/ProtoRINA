/**
 * 
 */
package video.serverProxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import video.lib.RtcpPacket;
import video.lib.RtpPacket;
import video.lib.Statistic;
import video.lib.TrackStatistic;
import video.lib.UnsignedInt;
import video.transport.TransportService;


/**
 * A Track is a part of a RTSP session. A typical RTSP session for a video
 * stream transmission is composed of 2 tracks: a track for video data and
 * another track for audio data.
 * <p>
 * These two stream are independent and usually are activated by the same
 * <code>PLAY</code> and <code>TEARDOWN</code> requests.
 * 
 * @author Matteo Merli
 */
public class ServerTrack {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerTrack.class);

  private static TransportService service;

  /** Maps a server SSRC id to a Track */
  private static Map<UnsignedInt, ServerTrack> serverSsrcMap = new ConcurrentHashMap<UnsignedInt, ServerTrack>();
  
  /** Maps a proxy SSRC id to a Track */
  private static Map<UnsignedInt, ServerTrack> proxySsrcMap = new ConcurrentHashMap<UnsignedInt, ServerTrack>();
  
  /** Maps a server address to a Track */
  private static Map<InetSocketAddress, ServerTrack> serverAddressMap = new ConcurrentHashMap<InetSocketAddress, ServerTrack>();

  /** Keeps track of the SSRC IDs used by the proxy, to avoid collisions. */
  private static Set<UnsignedInt> proxySsrcSet = Collections.synchronizedSet(new HashSet<UnsignedInt>());

  private Statistic trackStatistic;
  /**
   * Control Url of the track. This is the url handle given by the server to
   * control different tracks in a RTSP session.
   */
  private URI uri;

  /** SSRC id given by the server */
  private UnsignedInt serverSsrc = new UnsignedInt(0);
  /** SSRC id selected by the proxy */
  private UnsignedInt proxySsrc = new UnsignedInt(0);

  /**
   * Cached references to IoSession objects used to send packets to server and
   * client.
   */
  private IoSession rtpServerSession = null;
  private IoSession rtcpServerSession = null;

  private InetAddress serverAddress;
  private int serverRtpPort;
  private int serverRtcpPort;

  /**
   * Construct a new Track.
   * Proxy SSRC is automatically assigned.
   * 
   * @param url
   *          the control name for this track.
   */
  public ServerTrack(TransportService transportService, URI uri) {
    service = transportService;
    this.uri = uri;
    setProxySsrc(newSsrc());
    proxySsrcMap.put(this.proxySsrc, this);
    trackStatistic = new TrackStatistic("[server]" + uri.toString().replaceAll("\\/", "-"));
    try {
      trackStatistic.start();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Get the track by looking at server socket address.
   * <p>
   * Used as a workaround for streaming servers which do not hand out a ssrc in
   * the setup handshake.
   * 
   * @return a Track instance if a matching pair is found or null
   */
  public static ServerTrack getByServerSocketAddress(InetSocketAddress serverSocketAddress) {
    return serverAddressMap.get(serverSocketAddress);
  }

  /**
   * Get the track by looking at server SSRC id.
   * 
   * @return a Track instance if a matching SSRC is found or null
   */
  public static ServerTrack getByServerSsrc(UnsignedInt serverSsrc) {
    return serverSsrcMap.get(serverSsrc);
  }

  public static ServerTrack getByProxySsrc(UnsignedInt proxySsrc) {
    return proxySsrcMap.get(proxySsrc);
  }

  
  
  
  
  // /// Member methods

  /**
   * @return the SSRC id used byt the proxy
   */
  public UnsignedInt getProxySsrc() {
    return proxySsrc;
  }

  /**
   * Sets the proxy SSRC id.
   * 
   * @param proxySsrc
   */
  public void setProxySsrc(String proxySsrc) {
    try {
      this.proxySsrc = UnsignedInt.fromString(proxySsrc, 16);
      proxySsrcSet.add(this.proxySsrc);
    } catch (NumberFormatException nfe) {
      LOGGER.debug("Cannot convert " + proxySsrc + " to integer.");
      throw nfe;
    }
  }

  /**
   * @return the server SSRC id
   */
  public UnsignedInt getServerSsrc() {
    return serverSsrc;
  }

  /**
   * Sets the server SSRC id.
   * 
   * @param serverSsrc
   */
  public void setServerSsrc(String serverSsrc) {
    this.serverSsrc = UnsignedInt.fromString(serverSsrc, 16);
    serverSsrcMap.put(this.serverSsrc, this);
  }

  /**
   * Sets the server SSRC id.
   * 
   * @param serverSsrc
   */
  public void setServerSsrc(UnsignedInt serverSsrc) {
    this.serverSsrc = serverSsrc;
    serverSsrcMap.put(this.serverSsrc, this);
    LOGGER.debug("ServerTrack.setServerSsrc done: " + serverSsrc.toHexString());
  }

  public URI getUri() {
    return uri;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  public void setRtcpServerSession(IoSession rtcpServerSession) {
    this.rtcpServerSession = rtcpServerSession;
  }

  public void setRtpServerSession(IoSession rtpServerSession) {
    this.rtpServerSession = rtpServerSession;
  }

  /**
   * Forwards a RTP packet to server. The packet will be set to the address
   * indicated by the server at RTP (even) port.
   * 
   * @param packet
   *          a RTP packet
   */
  public void forwardRtpToServer(RtpPacket packet) {
    // modify the SSRC for the server
    packet.setSsrc(proxySsrc);

    if (rtpServerSession == null)
      rtpServerSession = RtpServerService.newRtpSession(new InetSocketAddress(serverAddress, serverRtpPort));

    rtpServerSession.write(packet.toIoBuffer());
  }

  /**
   * Forwards a RTCP packet to server. The packet will be set to the address
   * indicated by the server at RTCP (odd) port.
   * 
   * @param packet
   *          a RTCP packet
   */
  public void forwardRtcpToServer(RtcpPacket packet) {
    // modify the SSRC for the server
    packet.setSsrc(proxySsrc);

    if (rtcpServerSession == null)
      rtcpServerSession = RtpServerService.newRtcpSession(new InetSocketAddress(serverAddress, serverRtcpPort));

    rtcpServerSession.write(packet.toIoBuffer());
  }

  /**
   * Forwards a RTP packet to client. The packet will be set to the address
   * indicated by the client at RTP (even) port.
   * <p>
   * TODO: This will be changed to support multiple clients connected to the
   * same (live) track.
   * 
   * @param packet
   *          a RTP packet
   */
  public void forwardRtpToClient(RtpPacket packet) {
    // modify the SSRC for the client
    packet.setSsrc(proxySsrc);

    try {
      service.getRTPTransportChannel().send(packet.toIoBuffer().array());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    trackStatistic.doStatistic(packet);
  }

  /**
   * Forwards a RTCP packet to client. The packet will be set to the address
   * indicated by the client at RTCP (odd) port.
   * <p>
   * TODO: This will be changed to support multiple clients connected to the
   * same (live) track.
   * 
   * @param packet
   *          a RTCP packet
   */
  public void forwardRtcpToClient(RtcpPacket packet) {
    // modify the SSRC for the client
    packet.setSsrc(proxySsrc);
    try {
      service.getRTCPTransportChannel().send(packet.toIoBuffer().array());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * Set the address of the server associated with this track.
   * 
   * @param serverHost
   *          The serverHost to set.
   * @param rtpPort
   *          the port number used for RTP packets
   * @param rtcpPort
   *          the port number used for RTCP packets
   */
  public void setServerSocketAddress(InetAddress serverAddress, int rtpPort, int rtcpPort) {
    this.serverAddress = serverAddress;
    this.serverRtpPort = rtpPort;
    this.serverRtcpPort = rtcpPort;

    serverAddressMap.put(new InetSocketAddress(serverAddress, rtpPort), this);
    serverAddressMap.put(new InetSocketAddress(serverAddress, rtcpPort), this);
  }

  public synchronized void close() {
    if (serverSsrc != null) {
      serverSsrcMap.remove(serverSsrc);
    }
    
    if (proxySsrc != null) {
      proxySsrcMap.remove(proxySsrc);
    }
    
    serverAddressMap.remove(new InetSocketAddress(serverAddress, serverRtpPort));
    serverAddressMap.remove(new InetSocketAddress(serverAddress, serverRtcpPort));

    if (proxySsrc != null) {
      proxySsrcSet.remove(proxySsrc);
    }
    
    LOGGER.debug("Closed track " + uri);
  }

  public String toString() {
    return "Track(url=\"" + uri + "\"";
  }

  // ////////////////

  /** Used in SSRC id generation */
  private static Random random = new Random();

  /**
   * Creates a new SSRC id that is unique in the proxy.
   * 
   * @return the session ID
   */
  private static String newSsrc() {
    long id;
    while (true) {
      id = random.nextLong() & 0xFFFFFFFFL;

      if (!proxySsrcSet.contains(id)) {
        // Ok, the id is unique
        String ids = Long.toString(id, 16);
        return ids;
      }
      // try with another id
    }
  }

}
