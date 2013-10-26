/**
 * 
 */
package video.serverProxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import video.lib.Configuration;
import video.lib.Constants;
import video.lib.NumberConvertor;
import video.lib.RtspMessage;
import video.lib.RtspMethod;
import video.lib.RtspRequest;
import video.lib.RtspResponse;
import video.lib.RtspStatusCode;
import video.lib.RtspTransportHeader;
import video.lib.RtspTransportHeader.RtspTransport;
import video.transport.TransportService;


/**
 * @author yuezhu
 * 
 */
public class RtspServerHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(RtspServerHandler.class);
  
  protected static final AttributeKey TOKEN = new AttributeKey(RtspServerHandler.class, "token");

  private static TransportService service;

  private final IoSession session;

  private Long clientSessionId = -1L;
  
  private String rtspSessionId = null;

  // Map MINA session ID to handler
  private static Map<Long, RtspServerHandler> clientSessionIdMap = new ConcurrentHashMap<Long, RtspServerHandler>();
  
  // Map RTSP session ID to handler
  private static Map<String, RtspServerHandler> rtspSessionIdMap = new ConcurrentHashMap<String, RtspServerHandler>();

  // Map cseq to RTSP method
  private final Map<String, RtspMethod> cseqMap = new ConcurrentHashMap<String, RtspMethod>();

  // Map URI to track
  private final Map<URI, ServerTrack> trackList = new ConcurrentHashMap<URI, ServerTrack>();

  /**
   * Get handler by client session ID
   * @param id
   * @return
   */
  public static RtspServerHandler getByClientSessionId(long id) {
    return clientSessionIdMap.get(Long.valueOf(id));
  }
  
  /**
   * Get handler by RTSP session ID
   * @param id
   * @return
   */
  public static RtspServerHandler getByRtspSessionId(String id) {
    return rtspSessionIdMap.get(id);
  }
  
  public static void putRtspSessionId(String id, RtspServerHandler handler) {
    rtspSessionIdMap.put(id, handler);
  }

  /**
   * Insert client session ID into the front of the message raw byte
   * @param in
   * @return
   */
  private byte[] insertClientSessionId(byte[] in) {
    byte[] byteArray = new byte[in.length + (Long.SIZE / Byte.SIZE)];
    System.arraycopy(NumberConvertor.toByteArray(clientSessionId), 0, byteArray, 0, Long.SIZE / Byte.SIZE);
    System.arraycopy(in, 0, byteArray, Long.SIZE / Byte.SIZE, in.length);
    LOGGER.debug("Insert client session ID=" + clientSessionId);
    return byteArray;
  }

  /**
   * Get message raw bytes
   * @param in
   * @return
   */
  public static byte[] getMessageBytes(byte[] in) {
    int length = in.length - (Long.SIZE / Byte.SIZE);
    byte[] byteArray = new byte[length];
    System.arraycopy(in, Long.SIZE / Byte.SIZE, byteArray, 0, length);
    return byteArray;
  }

  /**
   * Get client session ID
   * @param in
   * @return
   */
  public static long getClientSessionId(byte[] in) {
    byte[] longBytes = new byte[Long.SIZE / Byte.SIZE];
    System.arraycopy(in, 0, longBytes, 0, Long.SIZE / Byte.SIZE);
    // byte[] longBytes = Arrays.copyOf(in, Long.SIZE / Byte.SIZE);
    return NumberConvertor.toLongValue(longBytes);
  }

  public RtspServerHandler(IoSession session, TransportService transportService, Long clientSessionId) {
    this.session = session;
    service = transportService;
    this.clientSessionId = clientSessionId;
    // Save session ID for de-multiplexing.
    clientSessionIdMap.put(clientSessionId, this);
    LOGGER.debug("Client session ID put: " + clientSessionId);
  }

  /**
   * This method will be invoked when a RTSP message received from the client side proxy.
   * @param message
   */
  public void onMessageReceivedFromRemote(RtspMessage message) {
    LOGGER.debug("RtspMessage received from remote:\n"
        + "--------------------------------------------------------------------------------\n" 
        + message.toString()
        + "--------------------------------------------------------------------------------");
    RtspMethod method = null;
    switch (message.getType()) {
    case REQUEST:
      // It's a request from remote. We save its request method.
      RtspRequest request = (RtspRequest) message;
      
      method = request.getMethod();
      saveRequestMethod(request);

      // Then dispatch the response and pass it to the server.
      switch (method) {
      case SETUP:
        LOGGER.debug("SETUP request.");
        boolean isSupported = processSetupRequest(request);
        if (!isSupported) {
          String cseq = request.getField("CSeq");
          RtspResponse r = RtspResponse.newInstance(RtspStatusCode.NotImplemented, cseq);
          retrieveRequestMethod(r);
          sendRtspMessageToRemote(r);
          return;
        }
        break;
      case OPTIONS:
        LOGGER.debug("OPTIONS request.");
        break;
      case DESCRIBE:
        LOGGER.debug("DESCRIBE request.");
        break;
      case PLAY:
        LOGGER.debug("PLAY request.");
        break;
      case PAUSE:
        LOGGER.debug("PAUSE request.");
        break;
      case TEARDOWN:
        LOGGER.debug("TEARDOWN request.");
        break;
      case GET_PARAMETER:
        LOGGER.debug("GET_PARAMETER request.");
        break;
      case SET_PARAMETER:
        LOGGER.debug("SET_PARAMETER request.");
        break;
      default:
        LOGGER.warn("Request method " + method + " is not supported.");
        break;
      }

      break;
    // End of REQUEST case.

    case RESPONSE:
      // It's a response from the server. We find the method of its
      // corresponding request.
      RtspResponse response = (RtspResponse) message;
      method = retrieveRequestMethod(response);
      // Then dispatch the response and pass it to remote.
      switch (method) {
      case OPTIONS:
        LOGGER.debug("OPTIONS response.");
        break;
      case GET_PARAMETER:
        LOGGER.debug("GET_PARAMETER response.");
        break;
      case SET_PARAMETER:
        LOGGER.debug("SET_PARAMETER response.");
        break;
      default:
        LOGGER.warn("Server shouldn't issue such a request of method " + method);
        return;
      }

      break;
    // End of RESPONSE case.
    }

    sendRtspMessageToServer(message);
  }

  /**
   * This method will be invoked when RTSP message received from the living streaming server.
   * @param message
   */
  public void onMessageReceivedFromServer(RtspMessage message) {
    LOGGER.debug("RtspMessage received from the server:\n"
        + "--------------------------------------------------------------------------------\n" 
        + message.toString()
        + "--------------------------------------------------------------------------------");
    RtspMethod method = null;
    switch (message.getType()) {
    case REQUEST:
      // It's a request from the server. We save its request method.
      RtspRequest request = (RtspRequest) message;
      
      method = request.getMethod();
      saveRequestMethod(request);

      // Then dispatch the response and pass it to remote.
      switch (method) {
      case OPTIONS:
        LOGGER.debug("OPTIONS request.");
        break;
      case GET_PARAMETER:
        LOGGER.debug("GET_PARAMETER request.");
        break;
      case SET_PARAMETER:
        LOGGER.debug("SET_PARAMETER request.");
        break;
      case UNKNOWN:
        LOGGER.debug("UNKNOWN request.");
        break;
      default:
        LOGGER.warn("Server shouldn't issue such a request of method " + method);
        return;
      }
      break;
    // End of REQUEST case.

    case RESPONSE:
      // It's a response from the server. We find the method of its
      // corresponding request.
      RtspResponse response = (RtspResponse) message;
      method = retrieveRequestMethod(response);
      if (method == null) {
        LOGGER.error("Cannot retrieve corresponding RTSP method with CSeq " + response.getField("CSeq"));
        return;
      }
      // Then dispatch the response and pass it to remote.
      switch (method) {
      case SETUP:
        LOGGER.debug("SETUP response.");
        processSetupResponse(response);
        break;
      case DESCRIBE:
        LOGGER.debug("DESCRIBE response.");
        break;
      case GET_PARAMETER:
        LOGGER.debug("GET_PARAMETER response.");
        break;
      case SET_PARAMETER:
        LOGGER.debug("SET_PARAMETER response.");
        break;
      case OPTIONS:
        LOGGER.debug("OPTIONS response.");
        break;
      case PAUSE:
        LOGGER.debug("PAUSE response.");
        break;
      case PLAY:
        LOGGER.debug("PLAY response.");
        break;
      case TEARDOWN:
        LOGGER.debug("TEARDOWN response.");
        processTeardownResponse(response);
        break;
      case UNKNOWN:
        LOGGER.debug("UNKNOWN response.");
        break;
      default:
        LOGGER.warn("Error since its corresponding request method " + method + " is not supported.");
        return;
      }
      
      if (response.getStatusCode() == RtspStatusCode.NotFound) {
        close();
      }
      break;
    // End of RESPONSE case.
    }

    sendRtspMessageToRemote(message);
    
    LOGGER.debug("RtspMessage sent to the remote:\n"
        + "--------------------------------------------------------------------------------\n" 
        + message.toString()
        + "--------------------------------------------------------------------------------");
  }

  /**
   * Process SETUP request received from the client
   * @param request
   */
  private boolean processSetupRequest(RtspRequest request) {
    LOGGER.debug("Got SETUP request.");

    RtspTransportHeader transportHeader = new RtspTransportHeader(request.getField("Transport"));

    // FIXME: Here this is a hack. We save the request URI with its client ports
    // into the session.
    // This is problematic in the case that two successive SETUP requests arrive
    // at the proxy, and the former will be replaced.
    // Select the first option. Here we assume that the server will choose this
    // one.
    RtspTransport rtspTransport = null;
    for (int i = 0; i < transportHeader.size(); i++) {
      rtspTransport = transportHeader.get(i);
      // Modify all UDP client ports
      if (rtspTransport.getLowerTransport() == RtspTransportHeader.LowerTransport.UDP) {
        session.setAttribute("clientPort", rtspTransport.getClientPort());
        // Set new client ports so that the server will send RTP/RTCP packets to the new ports.
        int[] newClientPort = {
            Configuration.getInt("server.rtp.port", Constants.SERVER_RTP_PORT), 
            Configuration.getInt("server.rtcp.port", Constants.SERVER_RTCP_PORT)
            };

        LOGGER.debug("Before modifying client port: " + transportHeader.toString());

        // Modify transport headers.
        rtspTransport.setClientPort(newClientPort);
        transportHeader.set(i, rtspTransport);

        LOGGER.debug("After modifying client port: " + transportHeader.toString());
        
        // Proxy selects this transport method and discard others.
        transportHeader.removeAllExcept(i);
        
        break;

      } else if (rtspTransport.getLowerTransport() == RtspTransportHeader.LowerTransport.TCP) {
        // UDP based transport must be supported.
        // TCP based transport is not supported. Just discard it.
        transportHeader.remove(i);
        LOGGER.warn("Transport # " + i + " is TCP based, which is not supported currently.");
      } else {
        LOGGER.error("Transport is unspecified.");
      }
    }
    
    if (transportHeader.size() == 0) {
      // TODO:
      // No available transport option
      // This is actually an error case because RTSP must provide an UDP transport option.
      LOGGER.error("No UDP transport option");
      return false;
    }
    
    // Set modified transport headers into request message.
    request.setField("Transport", transportHeader.toString());
    
    // Save its request URI into session in order to assign a track for this URI when getting this SETUP response.
    session.setAttribute("requestUri", request.getRequestUri());

    return true;
  }

  private void processSetupResponse(RtspResponse response) {
    LOGGER.debug("Got SETUP response.");

    RtspTransportHeader transportHeader = new RtspTransportHeader(response.getField("Transport"));

    // Get the transport header.
    RtspTransport rtspTransport = transportHeader.get(0);
    // Create a new Track object.
    ServerTrack serverTrack = addServerTrack((URI) session.getAttribute("requestUri"), rtspTransport.getSsrc());

    // Save server socket address into track.
    // This is a workaround in the case that the server doesn't include a SSRC in the transport response.
    // When we get an RTP packet in the RtpServerPacketHandler, we will get the SSRC, and
    // we are able to match the SSRC with the server socket address.
    InetAddress serverAddress = null;
    if (rtspTransport.getSource() != null) {
      // The server specified the source.
      try {
        serverAddress = InetAddress.getByName(rtspTransport.getSource());
      } catch (UnknownHostException e) {
        LOGGER.warn("Unknown host: " + rtspTransport.getSource());
      }
    } else {
      // The server didn't specify the source. We still can get the server address from the session.
      serverAddress = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
    }

    // /////////////////////
    // Set response transport header
    // At this time the transport type should be UDP based because we have selected an UDP transport type
    // when sending the SETUP request.
    // /////////////////////
    if (rtspTransport.getLowerTransport() == RtspTransportHeader.LowerTransport.UDP) {

      // Get server ports. The media server will send RTP/RTCP packets from
      // these ports. Now we are ready to set server socket address.
      serverTrack.setServerSocketAddress(serverAddress, rtspTransport.getServerPort()[0], rtspTransport.getServerPort()[1]);
      // Retrieve the client ports from the session.
      rtspTransport.setClientPort((int[]) session.getAttribute("clientPort"));

    } else if (rtspTransport.getLowerTransport() == RtspTransportHeader.LowerTransport.TCP) {
      LOGGER.error("Transport is TCP based, which is not supported currently.");
    } else {
      LOGGER.error("Transport is unspecified.");
    }

    // Assign a SSRC for this SETUP message. This SSRC is proxy specified and
    // will be included in the RTP packets in the future.
    rtspTransport.setSsrc(serverTrack.getProxySsrc().toHexString());
    transportHeader.set(0, rtspTransport);
      
    //LOGGER.debug("Transport header to be sent: " + transportHeader.toString());

    response.setField("Transport", transportHeader.toString());
    
    rtspSessionId = response.getField("Session");
    if (rtspSessionId != null && !rtspSessionIdMap.containsKey(rtspSessionId)) {
      LOGGER.debug("Got RTSP session ID, put it into rtspSessionIDMap");
      rtspSessionIdMap.put(rtspSessionId, this);
    }

  }

  // FIXME: this.close() will block when executing session.close(false). It's
  // weird.
  private void processTeardownResponse(RtspResponse response) {
    if (response.getStatusCode() == RtspStatusCode.OK) {
      close();
    }
  }

  /**
   * Adds a new Track associated with this ProxySession.
   * 
   * @param url
   *          The URL used as a control reference for the Track
   * @param serverSsrc
   *          the SSRC id given by the server or null if not provided
   * @return a reference to the newly created Track
   */
  private synchronized ServerTrack addServerTrack(URI uri, String serverSsrc) {
    ServerTrack serverTrack = new ServerTrack(service, uri);
    if (serverSsrc != null) {
      LOGGER.debug("serverSsrc is available: " + serverSsrc);
      serverTrack.setServerSsrc(serverSsrc);
    }
    trackList.put(uri, serverTrack);
    return serverTrack;
  }

  // ////////////////////////////

  private void saveRequestMethod(RtspRequest request) {
    cseqMap.put(request.getField("CSeq"), request.getMethod());
  }

  private synchronized RtspMethod retrieveRequestMethod(RtspResponse response) {
    RtspMethod rtn = null;
    String cseq = response.getField("CSeq");
    if (cseq != null) {
      if (cseqMap.containsKey(cseq)) {
        rtn = cseqMap.get(cseq);
        cseqMap.remove(cseq);
        return rtn;
      } else {
        LOGGER.error("CSeq is illegal.");
        return null;
      }
    } else {
      LOGGER.debug("CSeq is not specified.");
      return RtspMethod.UNKNOWN;
    }
  }

  // ////////////////////////////

  private void sendRtspMessageToServer(RtspMessage message) {
    if (message == null) {
      LOGGER.error("RtspMessage is null.");
      return;
    }
    //message.setProxy();
    switch (message.getType()) {

    case REQUEST:
      RtspRequest request = (RtspRequest) message;
      session.write(request);
      break;

    case RESPONSE:
      RtspResponse response = (RtspResponse) message;
      session.write(response);
      break;
    }
    LOGGER.debug("RTSP message sent to the server.");
  }

  public void sendRtspMessageToRemote(RtspMessage message) {
    if (message == null) {
      LOGGER.error("RtspMessage is null.");
      return;
    }
    byte[] byteArray;
    //message.setProxy();
    switch (message.getType()) {

    case REQUEST:
      RtspRequest request = (RtspRequest) message;
      try {
        byteArray = insertClientSessionId(request.toString().getBytes());
        service.getRTSPTransportChannel().send(byteArray);
      } catch (IOException e) {
        LOGGER.error("Failed to send RTSP request via transport service: " + e.toString());
      }
      break;

    case RESPONSE:
      RtspResponse response = (RtspResponse) message;
      try {
        byteArray = insertClientSessionId(response.toString().getBytes());
        service.getRTSPTransportChannel().send(byteArray);
      } catch (IOException e) {
        LOGGER.error("Failed to send RTSP response via transport service: " + e.toString());
      }
      break;
    }
  }

  /**
   * Synchronized close.
   */
  public synchronized void close() {
    if ((session != null) && session.isConnected()) {
      session.close(true);
//      session.getService().dispose();
//      CloseFuture future = session.close(true);
//      future.awaitUninterruptibly();
    }

    // close all associated tracks
    for (Map.Entry<URI, ServerTrack> entry : trackList.entrySet()) {
      entry.getValue().close();
    }

    if (clientSessionIdMap.containsKey(clientSessionId)) {
      clientSessionIdMap.remove(clientSessionId);
    }
    
    if (rtspSessionId != null && rtspSessionIdMap.containsKey(rtspSessionId)) {
      rtspSessionIdMap.remove(rtspSessionId);
    }
  }
}
