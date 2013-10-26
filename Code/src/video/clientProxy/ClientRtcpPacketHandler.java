/**
 *
 */
package video.clientProxy;

import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import video.lib.RtcpPacket;


/**
 * @author yuezhu
 * 
 */
public class ClientRtcpPacketHandler extends IoHandlerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientRtcpPacketHandler.class);

  @Override
  public void sessionCreated(IoSession session) throws Exception {
  }

  @Override
  public void messageReceived(IoSession session, Object buffer) throws Exception {
    RtcpPacket packet = new RtcpPacket((IoBuffer) buffer);
//    LOGGER.debug( "Received RTCP packet: " + packet.getType() );
    
    ClientTrack clientTrack = ClientTrack.getByClientSocketAddress((InetSocketAddress) session.getRemoteAddress());

    if (clientTrack == null) {
      // drop packet
      LOGGER.debug("Invalid address: " + (InetSocketAddress) session.getRemoteAddress() + " - Class: "
          + ((InetSocketAddress) session.getRemoteAddress()).getAddress().getClass());
      return;
    }

    clientTrack.forwardRtcpToServer(packet);
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
    LOGGER.error(cause.toString());
    cause.printStackTrace();
//    CloseFuture future = session.close(false);
//    future.awaitUninterruptibly();
//    session.close(true);
//    session.getService().dispose();
  }
  
  @Override
  public void sessionClosed(IoSession session) {
    /*
     * Invoked when a connection is closed.
     */
    LOGGER.debug("ClientRtcpPacketHandler sessionClosed() is invoked.");
  }

}
