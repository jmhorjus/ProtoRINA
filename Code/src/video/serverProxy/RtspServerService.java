/**
 * 
 */
package video.serverProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import video.lib.Configuration;
import video.lib.Constants;
import video.lib.RtspCodecFactory;
import video.lib.RtspMessage;
import video.lib.RtspResponse;
import video.lib.RtspStatusCode;
import video.lib.ServiceInterface;
import video.transport.TransportService;


/**
 * @author yuezhu
 * 
 */
public class RtspServerService implements ServiceInterface {

  private static final Logger LOGGER = LoggerFactory.getLogger(RtspServerService.class);

  private static TransportService service;

  private static Thread rtspDispatcherThread;
  
  private static boolean running = false;

  public RtspServerService(TransportService transportService) {
    service = transportService;
  }

  /**
   * Setup a RTSP connection with the streaming server
   * @param hostname
   * @param port
   * @return
   */
  private static IoSession connectToServer(String hostname, int port) {
    NioSocketConnector connector = new NioSocketConnector();
    connector.setConnectTimeoutMillis(Constants.CONNECT_TIMEOUT);
    connector.setHandler(new MinaServerHandler());
    connector.getFilterChain().addLast(RtspCodecFactory.TOKEN.toString(), new ProtocolCodecFilter(new RtspCodecFactory(Charset.forName(Constants.CHARSET))));
    LOGGER.info("About to connect " + hostname + ":" + port);
    ConnectFuture connFuture = connector.connect(new InetSocketAddress(hostname, port));
    connFuture.awaitUninterruptibly();
    if (connFuture.isConnected()) {
      LOGGER.info("Connected to " + hostname + ":" + port);
      return connFuture.getSession();
    } else {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see demo.video.common.ServiceInterface#start()
   */
  @Override
  public void start() throws IOException {
    running = true;
    rtspDispatcherThread = new Thread() {
      @Override
      public void run() {
        while (running) {
          rtspMessageDispatcher();
        }
      }
    };
    
    rtspDispatcherThread.start();
    
    LOGGER.debug("Rtsp server service started.");
  }

  /* (non-Javadoc)
   * @see demo.video.common.ServiceInterface#stop()
   */
  @Override
  public void stop() throws IOException {
    running = false;
  }

  /**
   * Receive RTSP messages from the client side proxy.
   * @return
   * @throws IOException
   */
  private static byte[] recvMessageBytesFromRemote() throws IOException {
    return service.getRTSPTransportChannel().recv();
  }

  /**
   * Dispatcher RTSP messages received from the client side proxy.
   */
  private static void rtspMessageDispatcher() {
    byte[] bytes = null;
    
    try {
      bytes = recvMessageBytesFromRemote();
    } catch (IOException e) {
      LOGGER.error("Failed to recv RTSP message via transport service: " + e.toString());
      return;
    }
    
    // Get client session ID
    long clientSessionId = RtspServerHandler.getClientSessionId(bytes);
    // Get the message bytes
    byte[] messageBytes = RtspServerHandler.getMessageBytes(bytes);
    
    LOGGER.debug("Message received for client session ID=" + clientSessionId);
    
    // Construct a RTSP message from bytes received from the client side proxy.
    RtspMessage message = RtspMessage.parse(messageBytes);
    //message.setProxy();
    
    RtspServerHandler rtspServerHandler = null;
    
    String rtspSessionId = message.getField("Session");
    if (rtspSessionId != null) {
      LOGGER.debug("Get RtspServerHandler by RTSP session ID");
      rtspServerHandler = RtspServerHandler.getByRtspSessionId(rtspSessionId);
      if (rtspServerHandler == null) {
        rtspServerHandler = RtspServerHandler.getByClientSessionId(clientSessionId);
        RtspServerHandler.putRtspSessionId(rtspSessionId, rtspServerHandler);
      }
    } else {
      rtspServerHandler = RtspServerHandler.getByClientSessionId(clientSessionId);
    }
    
    if (rtspServerHandler == null) {
      // This is a new client, allocate a new handler for it.
      LOGGER.info("A message from New client (id=" + clientSessionId + ")");
      int port = Configuration.getInt("streaming.server.port", Constants.STREAMING_SERVER_PORT);
      String hostname = Configuration.getString("streaming.server.hostname", Constants.STREAMING_SERVER_HOSTNAME);
      IoSession session = connectToServer(hostname, port);
      rtspServerHandler = new RtspServerHandler(session, service, clientSessionId);
      if (session != null) {
        // Save the handler to its session
        session.setAttribute(RtspServerHandler.TOKEN, rtspServerHandler);
      } else {
        // The streaming server is unreachable
        LOGGER.info("Streaming server " + hostname + ":" + port + " is not reachable.");
        rtspServerHandler.sendRtspMessageToRemote(RtspResponse.newInstance(RtspStatusCode.DestinationUnreachable, message.getField("CSeq")));
        rtspServerHandler.close();
        return;
      }
    }
    
    // Pass the message received from the client side proxy to the corresponding handler
    rtspServerHandler.onMessageReceivedFromRemote(message);
  }

}
