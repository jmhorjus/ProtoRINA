/**
 * 
 */
package video.clientProxy;

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
public class RtspClientHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(RtspClientHandler.class);
  protected static final AttributeKey TOKEN = new AttributeKey(RtspClientHandler.class, "token");

  private final IoSession session;
  private Long clientSessionId = -1L;
  private String rtspSessionId = null;
  private static TransportService service;
  private final Map<String, RtspMethod> cseqMap = new ConcurrentHashMap<String, RtspMethod>();
  private final Map<URI, ClientTrack> trackList = new ConcurrentHashMap<URI, ClientTrack>();

  private static Map<String, RtspClientHandler> rtspSessionIdMap = new ConcurrentHashMap<String, RtspClientHandler>();

  private static Map<Long, RtspClientHandler> clientSessionIdMap = new ConcurrentHashMap<Long, RtspClientHandler>();
  
  public static RtspClientHandler getByClientSessionId(long id) {
    LOGGER.debug("Get by client session ID=" + id);
    return clientSessionIdMap.get(Long.valueOf(id));
  }
  
  private byte[] insertClientSessionId(byte[] in) {
    byte[] byteArray = new byte[in.length + (Long.SIZE / Byte.SIZE)];
    System.arraycopy(NumberConvertor.toByteArray(session.getId()), 0, byteArray, 0, Long.SIZE / Byte.SIZE);
    System.arraycopy(in, 0, byteArray, Long.SIZE / Byte.SIZE, in.length);
    LOGGER.debug("Insert client session ID=" + session.getId());
    return byteArray;
  }
  
  public static byte[] getMessageBytes(byte[] in) {
    int length = in.length - (Long.SIZE  / Byte.SIZE);
    byte[] byteArray = new byte[length];
    System.arraycopy(in, Long.SIZE / Byte.SIZE, byteArray, 0, length);
    return byteArray;
  }
  
  public static long getClientSessionId(byte[] in) {
    byte[] longBytes = new byte[Long.SIZE / Byte.SIZE];
    System.arraycopy(in, 0, longBytes, 0, Long.SIZE / Byte.SIZE);
    return NumberConvertor.toLongValue(longBytes);
  }

  /**
   * Get handler by RTSP session ID
   * @param id
   * @return
   */
  public static RtspClientHandler getByRtspSessionId(String id) {
    return rtspSessionIdMap.get(id);
  }
  
  public static void putRtspSessionId(String id, RtspClientHandler handler) {
    rtspSessionIdMap.put(id, handler);
  }  
  
  public RtspClientHandler(IoSession session, TransportService transportService) {
    this.session = session;
    service = transportService;
    // Save session ID for de-multiplexing.
    clientSessionId = Long.valueOf(session.getId());
    clientSessionIdMap.put(clientSessionId, this);
    LOGGER.debug("Client session ID put: " + session.getId());
  }

  public void onMessageReceivedFromRemote(RtspMessage message) {
    LOGGER.debug("RtspMessage received from remote:\n"
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
      default:
        LOGGER.warn("Server shouldn't issue such a request of method " + method);
        break;
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
      case OPTIONS:
        LOGGER.debug("OPTIONS response.");
        break;
      case DESCRIBE:
        LOGGER.debug("DESCRIBE response.");
        break;
      case PLAY:
        LOGGER.debug("PLAY response.");
        break;
      case PAUSE:
        LOGGER.debug("PAUSE response.");
        break;
      case TEARDOWN:
        LOGGER.debug("TEARDOWN response.");
        processTeardownResponse(response);
        break;
      case GET_PARAMETER:
        LOGGER.debug("GET_PARAMETER response.");
        break;
      case SET_PARAMETER:
        LOGGER.debug("SET_PARAMETER response.");
        break;
      case UNKNOWN:
        LOGGER.debug("UNKNOWN response.");
        break;
      default:
        LOGGER.warn("Error since its corresponding request method " + method + " is not supported.");
        break;
      }

      break;
    // End of RESPONSE case.
    }
    sendRtspMessageToClient(message);
  }

  public void onMessageReceivedFromClient(RtspMessage message) {
    LOGGER.debug("RtspMessage received from the client:\n"
        + "--------------------------------------------------------------------------------\n"
        + message.toString()
        + "--------------------------------------------------------------------------------");
    RtspMethod method = null;
    switch (message.getType()) {
    case REQUEST:
      // It's a request from the client. We save its request method.
      RtspRequest request = (RtspRequest) message;
      method = request.getMethod();
      saveRequestMethod(request);

      // Then dispatch the response and pass it to the client.
      switch (method) {

      case SETUP:
        LOGGER.debug("SETUP request.");
        processSetupRequest(request);
        break;
      case DESCRIBE:
        LOGGER.debug("DESCRIBE request.");
        break;
      case GET_PARAMETER:
        LOGGER.debug("GET_PARAMETER request.");
        break;
      case SET_PARAMETER:
        LOGGER.debug("SET_PARAMETER request.");
        break;
      case OPTIONS:
        LOGGER.debug("OPTIONS request.");
        break;
      case PAUSE:
        LOGGER.debug("PAUSE request.");
        break;
      case PLAY:
        LOGGER.debug("PLAY request.");
        break;
      case TEARDOWN:
        LOGGER.debug("TEARDOWN request.");
        break;
      default:
        LOGGER.warn("Request method " + method + " is not supported.");
        return;

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
        LOGGER.debug("OPTIONS request.");
        break;
      case GET_PARAMETER:
        LOGGER.debug("GET_PARAMETER response.");
        break;
      case SET_PARAMETER:
        LOGGER.debug("SET_PARAMETER response.");
        break;
      case UNKNOWN:
        LOGGER.debug("UNKNOWN response.");
        break;        
      default:
        LOGGER.warn("Server shouldn't issue such a request of method " + method);
        return;

      }
      break;
    // End of RESPONSE case.
    }

    sendRtspMessageToRemote(message);
  }

  /**
   * Save the request URI specified in the SETUP request into this RTSP session 
   * @param request
   */
  private void processSetupRequest(RtspRequest request) {
    LOGGER.debug("Got SETUP request.");
    session.setAttribute("requestUri", request.getRequestUri());
  }

  /**
   * Check if the transport type is supported by this proxy. If not supported, then just pass it to player.
   * Otherwise, initialize track for this SETUP and modify the server ports and address.
   * @param response
   */
  private void processSetupResponse(RtspResponse response) {
    LOGGER.debug("Got SETUP response.");

    if (response.getStatusCode() == RtspStatusCode.NotImplemented) {
      // If the server side proxy returns a not implemented response, then just pass it the player.
      return;
    }
    
    RtspTransportHeader transportHeader = new RtspTransportHeader(response.getField("Transport"));

    // Get the transport header.
    RtspTransport rtspTransport = transportHeader.get(0);


    // Create a new Track object.
    // SSRC must be available because the server side proxy will generate a proxy SSRC.
    ClientTrack serverTrack = addClientTrack((URI) session.getAttribute("requestUri"), rtspTransport.getSsrc());

    InetAddress clientAddress = null;
    try {
        clientAddress = InetAddress.getByName(((InetSocketAddress) session.getRemoteAddress()).getHostName());
    } catch (UnknownHostException e) {
      LOGGER.warn("Unknown host");
    }


    // /////////////////////
    // Set response transport header
    // /////////////////////
    if (rtspTransport.getLowerTransport() == RtspTransportHeader.LowerTransport.UDP) {
      serverTrack.setClientSocketAddress(clientAddress, rtspTransport.getClientPort()[0], rtspTransport.getClientPort()[1]);
      
      // Get client source ports.
      // Client side proxy will send RTP/RTCP packets on these ports.
      int rtpServerPort = Configuration.getInt("client.rtp.port", Constants.CLIENT_RTP_PORT);
      int rtcpServerPort = Configuration.getInt("client.rtcp.port", Constants.CLIENT_RTCP_PORT);
      rtspTransport.setServerPort(new int[]{rtpServerPort, rtcpServerPort});
      // Client side proxy will send RTP/RTCP packets on this address.
      rtspTransport.setSource(((InetSocketAddress) session.getLocalAddress()).getAddress().getHostAddress());
    } else if (rtspTransport.getLowerTransport() == RtspTransportHeader.LowerTransport.TCP) {
      LOGGER.error("Transport is TCP based, which is not supported currently.");
    } else {
      LOGGER.error("Transport is unspecified.");
    }

    transportHeader.set(0, rtspTransport);

    //LOGGER.debug("Transport header to be sent: " + transportHeader.toString());
    response.setField("Transport", transportHeader.toString());

    rtspSessionId = response.getField("Session");
    if (rtspSessionId != null && !rtspSessionIdMap.containsKey(rtspSessionId)) {
      LOGGER.debug("Got RTSP session ID, put it into rtspSessionIDMap");
      rtspSessionIdMap.put(rtspSessionId, this);
    }
  }
  

  /**
   * Sometimes this method cannot be invoked because clients close the connection immediately
   * right after sending TEARDOWN request. This causes sessionClosed() to be invoked.
   * @param response
   */
  private void processTeardownResponse(RtspResponse response) {
    if (response.getStatusCode() == RtspStatusCode.OK) {
      close();
    }
  }


  private synchronized ClientTrack addClientTrack(URI uri, String proxySsrc) {
    LOGGER.debug("Client track added for SSRC: " + proxySsrc);
    ClientTrack clientTrack = new ClientTrack(service, uri);
    if (proxySsrc != null) {
      LOGGER.debug("proxySsrc is available: " + proxySsrc);
      clientTrack.setProxySsrc(proxySsrc);
    }
    trackList.put(uri, clientTrack);
    return clientTrack;
  }

  // ////////////////////////////

  private void saveRequestMethod(RtspRequest request) {
    cseqMap.put(request.getField("CSeq"), request.getMethod());
    LOGGER.debug("Save CSeq: " + request.getField("CSeq"));
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

  private void sendRtspMessageToClient(RtspMessage message) {
    if (message == null) {
      LOGGER.error("RtspMessage is null.");
      return;
    }
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
    LOGGER.debug("RTSP message sent to the client:\n"
        + "--------------------------------------------------------------------------------\n" 
        + message.toString()
        + "--------------------------------------------------------------------------------");
  }

  private void sendRtspMessageToRemote(RtspMessage message) {
    if (message == null) {
      LOGGER.error("RtspMessage is null.");
      return;
    }
    byte[] byteArray;
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
    for (Map.Entry<URI, ClientTrack> entry : trackList.entrySet()) {
      entry.getValue().close();
    }
    
    if (clientSessionIdMap.containsKey(session.getId())) {
      clientSessionIdMap.remove(session.getId());
    }
    
    if (rtspSessionId != null && rtspSessionIdMap.containsKey(rtspSessionId)) {
      rtspSessionIdMap.remove(rtspSessionId);
    }
  }

}
