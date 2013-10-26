/**
 * 
 */
package video.clientProxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
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
 * @author yuezhu
 *
 */
public class ClientTrack {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientTrack.class);
  
  private static TransportService service;

  /** Maps a proxy SSRC id to a Track */
  private static Map<UnsignedInt, ClientTrack> proxySsrcMap = new ConcurrentHashMap<UnsignedInt, ClientTrack>();

  /** Maps a client address to a Track */
  private static Map<InetSocketAddress, ClientTrack> clientAddressMap = new ConcurrentHashMap<InetSocketAddress, ClientTrack>();

  private Statistic trackStatistic;
  /**
   * Control Url of the track. This is the url handle given by the server to
   * control different tracks in a RTSP session.
   */
  private URI uri;

  /** SSRC id given by the server side proxy*/
  private UnsignedInt proxySsrc = new UnsignedInt(0);

  /**
   * Cached references to IoSession objects used to send packets to the client.
   */
  private IoSession rtpClientSession = null;
  private IoSession rtcpClientSession = null;

  /**
   * IP address and RTP/RTCP ports for the client.
   * <p>
   * TODO: When using reflection, there will be more than one connected client
   * at a time to the same Track. So the track should keep a list of connected
   * clients and forward packets to each of them.
   */
  private InetAddress clientAddress;
  private int clientRtpPort;
  private int clientRtcpPort;

  /**
   * Construct a new Track.
   * 
   * @param uri
   *          the control name for this track.
   */
  public ClientTrack(TransportService transportService, URI uri) {
    service = transportService;
    this.uri = uri;
    trackStatistic = new TrackStatistic("[client]" + uri.toString().replaceAll("\\/", "-"));
    try {
      trackStatistic.start();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Get the track by looking at client socket address.
   * 
   * @return a Track instance if a matching pair is found or null
   */
  public static ClientTrack getByClientSocketAddress(InetSocketAddress clientSocketAddress) {
    return clientAddressMap.get(clientSocketAddress);
  }

  /**
   * Get the track by looking at proxy SSRC id.
   * 
   * @return a Track instance if a matching SSRC is found or null
   */
  public static ClientTrack getByProxySsrc(UnsignedInt proxySsrc) {
    return proxySsrcMap.get(proxySsrc);
  }

  // /// Member methods

  /**
   * @return the proxy SSRC id
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
    this.proxySsrc = UnsignedInt.fromString(proxySsrc, 16);
    proxySsrcMap.put(this.proxySsrc, this);
  }

  /**
   * Sets the proxy SSRC id.
   * 
   * @param proxySsrc
   */
  public void setProxySsrc(UnsignedInt proxySsrc) {
    this.proxySsrc = proxySsrc;
    proxySsrcMap.put(this.proxySsrc, this);
  }

  public URI getUri() {
    return uri;
  }

  public void setUrl(URI uri) {
    this.uri = uri;
  }

  public void setRtcpClientSession(IoSession rtcpClientSession) {
    this.rtcpClientSession = rtcpClientSession;
  }

  public void setRtpClientSession(IoSession rtpClientSession) {
    this.rtpClientSession = rtpClientSession;
  }


  /**
   * Forwards a RTP packet to server. The packet will be set to the address
   * indicated by the server at RTP (eve  n) port.
   * 
   * @param packet
   *          a RTP packet
   */
  public void forwardRtpToServer(RtpPacket packet) {
    try {
      service.getRTPTransportChannel().send(packet.toIoBuffer().array());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
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
		packet.setSsrc( proxySsrc );
    try {
      service.getRTCPTransportChannel().send(packet.toIoBuffer().array());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
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
    if (rtpClientSession == null) {
      rtpClientSession = RtpClientService.newRtpSession(new InetSocketAddress(clientAddress, clientRtpPort));
    }
    rtpClientSession.write(packet.toIoBuffer());
    
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
    if (rtcpClientSession == null) {
      rtcpClientSession = RtpClientService.newRtcpSession(new InetSocketAddress(clientAddress, clientRtcpPort));
    }
    rtcpClientSession.write(packet.toIoBuffer());
  }

  /**
   * Set the address of the server associated with this track.
   * <p>
   * TODO: This will be changed to support multiple clients connected to the
   * same (live) track.
   * 
   * @param clientAddress
   *          The client address to set.
   * @param rtpPort
   *          the port number used for RTP packets
   * @param rtcpPort
   *          the port number used for RTCP packets
   */
  public void setClientSocketAddress(InetAddress clientAddress, int rtpPort, int rtcpPort) {
    this.clientAddress = clientAddress;
    this.clientRtpPort = rtpPort;
    this.clientRtcpPort = rtcpPort;

    clientAddressMap.put(new InetSocketAddress(clientAddress, rtpPort), this);
    clientAddressMap.put(new InetSocketAddress(clientAddress, rtcpPort), this);
  }

  public synchronized void close() {
    if (proxySsrc != null) {
      proxySsrcMap.remove(proxySsrc);
    }

    clientAddressMap.remove(new InetSocketAddress(clientAddress, clientRtpPort));
    clientAddressMap.remove(new InetSocketAddress(clientAddress, clientRtcpPort));
    
    try {
      trackStatistic.stop();
    } catch (IOException e) {
      e.printStackTrace();
    }

    LOGGER.debug("Closed track " + uri);
  }

  public String toString() {
    return "Track(url=\"" + uri + "\"";
  }


}
