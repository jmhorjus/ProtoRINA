/**
 * 
 */
package video.clientProxy;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import video.lib.RtspMessage;
import video.transport.TransportService;


/**
 * @author yuezhu
 * 
 */
public class MinaClientHandler extends IoHandlerAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(MinaClientHandler.class);

  private static TransportService service;
  
  public MinaClientHandler(TransportService transportService) {
    service = transportService;
  }
  

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) {
    // Close connection when unexpected exception is caught.
    LOGGER.debug("exceptionCaught() is invoked.");
    cause.printStackTrace();
    RtspClientHandler rtspClientHandler = (RtspClientHandler) (session.getAttribute(RtspClientHandler.TOKEN));
    if (rtspClientHandler != null) {
      rtspClientHandler.close();
      LOGGER.info("Client session closed.");
    }
  }
  
  @Override
  public void sessionCreated(IoSession session) {
//    System.err.println("sessionCreated() is invoked.");
    LOGGER.debug("sessionCreated() is invoked.");
    LOGGER.info("New client " + session.getRemoteAddress().toString() + " is connected.");
  }

  @SuppressWarnings("static-access")
  @Override
  public void sessionOpened(IoSession session) {
    LOGGER.debug("sessionOpened() is invoked.");

    RtspClientHandler rtspClientHandler = new RtspClientHandler(session, service);

    session.setAttribute(rtspClientHandler.TOKEN, rtspClientHandler);

  }

  @Override
  public void sessionClosed(IoSession session) {
    System.err.println("sessionClosed() is invoked.");
    LOGGER.debug("sessionClosed() is invoked.");
    RtspClientHandler rtspClientHandler = (RtspClientHandler) (session.getAttribute(RtspClientHandler.TOKEN));
    if (rtspClientHandler != null) {
      rtspClientHandler.close();
    }

  }

  @Override
  public void messageReceived(IoSession session, Object message) {
    LOGGER.debug("messageReceived() is invoked.");
    RtspClientHandler rtspClientHandler = (RtspClientHandler) (session.getAttribute(RtspClientHandler.TOKEN));
    rtspClientHandler.onMessageReceivedFromClient((RtspMessage)message);

  }
}
