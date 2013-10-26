/**
 * 
 */
package video.clientProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import video.lib.Configuration;
import video.lib.Constants;
import video.lib.RtspCodecFactory;
import video.lib.RtspMessage;
import video.lib.ServiceInterface;
import video.transport.TransportService;


/**
 * @author yuezhu
 * 
 */
public class RtspClientService implements ServiceInterface {

  private static final Logger LOGGER = LoggerFactory.getLogger(RtspClientService.class);
  
  private static TransportService service;
  
  private static Thread rtspDispatcherThread;
  
  private static boolean running = false;
  
  public RtspClientService(TransportService transportService) {
    service = transportService;
  }
  
  private static byte[] recvMessageBytesFromRemote() throws IOException {
    return service.getRTSPTransportChannel().recv();
  }

  private static void rtspMessageDispatcher() {
    byte[] bytes = null; 

    try {
      bytes = recvMessageBytesFromRemote();
    } catch (IOException e) {
      LOGGER.error("Failed to recv RTSP message via transport service: " + e.toString());
      return;
    }
    
    long clientSessionId = RtspClientHandler.getClientSessionId(bytes);
    byte[] messageBytes = RtspClientHandler.getMessageBytes(bytes);
    
    LOGGER.debug("Message received for client session ID=" + clientSessionId);
    
    RtspMessage message = RtspMessage.parse(messageBytes);

    RtspClientHandler rtspClientHandler = null;
    
    String rtspSessionId = message.getField("Session");
    if (rtspSessionId != null) {
      LOGGER.debug("Get RtspClientHandler by RTSP session ID");
      rtspClientHandler = RtspClientHandler.getByRtspSessionId(rtspSessionId);
      if (rtspClientHandler == null) {
        rtspClientHandler = RtspClientHandler.getByClientSessionId(clientSessionId);
        RtspClientHandler.putRtspSessionId(rtspSessionId, rtspClientHandler);
      }
    } else {
      rtspClientHandler = RtspClientHandler.getByClientSessionId(clientSessionId);
    }    

    if (rtspClientHandler == null) {
      LOGGER.debug("The client session ID=" + clientSessionId + " is invalid.");
      return;
    }
    
    rtspClientHandler.onMessageReceivedFromRemote(message);
  }

  

  /*
   * (non-Javadoc)
   * 
   * @see rina.demo.video.proxy.ProxyService#start()
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


    int listeningPort = Configuration.getInt("client.listening.port", Constants.CLIENT_LISTENING_PORT);
    String listeningInterface = Configuration.getString("client.listening.interface", Constants.CLIENT_LISTENING_INTERFACE);
    NioSocketAcceptor acceptor = new NioSocketAcceptor();
    acceptor.setHandler(new MinaClientHandler(service));
    acceptor.setReuseAddress(true);
    acceptor.getFilterChain().addLast(RtspCodecFactory.TOKEN.toString(), new ProtocolCodecFilter(new RtspCodecFactory(Charset.forName(Constants.CHARSET))));
    acceptor.getSessionConfig().setReadBufferSize(Constants.BUFFER_SIZE);
    if (listeningInterface.equalsIgnoreCase("")) {
      acceptor.bind(new InetSocketAddress(listeningPort));
    } else {
      acceptor.bind(new InetSocketAddress(listeningInterface, listeningPort));
    }
    
    LOGGER.debug("RTSP service started at " + acceptor.getLocalAddress());

  }

  /*
   * (non-Javadoc)
   * 
   * @see rina.demo.video.proxy.ProxyService#stop()
   */
  @Override
  public void stop() {
    running = false;
  }
  


}
