/**
 * 
 */
package video.serverProxy;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import video.lib.RtspMessage;



/**
 * @author yuezhu
 *
 */
public class MinaServerHandler extends IoHandlerAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(MinaServerHandler.class);

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) {
    LOGGER.debug("exceptionCaught() is invoked.");
    cause.printStackTrace();
    RtspServerHandler rtspServerHandler = (RtspServerHandler) (session.getAttribute(RtspServerHandler.TOKEN));
    if (rtspServerHandler != null) {
      rtspServerHandler.close();
      LOGGER.info("Server session closed.");
    }
  }
  
  @Override
  public void sessionCreated(IoSession session) {
    LOGGER.debug("sessionCreated() is invoked.");
    LOGGER.info("New session: " + session.getRemoteAddress());
  }
  
  @Override
  public void sessionClosed(IoSession session) {
//    System.err.println("sessionClosed() is invoked.");
    LOGGER.debug("sessionClosed() is invoked.");
    RtspServerHandler rtspServerHandler = (RtspServerHandler) (session.getAttribute(RtspServerHandler.TOKEN));
    if (rtspServerHandler != null) {
      rtspServerHandler.close();
    }
  }

  @Override
  public void messageReceived(IoSession session, Object message) {
    LOGGER.debug("messageReceived() is invoked.");
    RtspServerHandler rtspServerHandler = (RtspServerHandler) (session.getAttribute(RtspServerHandler.TOKEN));
    rtspServerHandler.onMessageReceivedFromServer((RtspMessage) message);
  }

}
