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

import video.lib.RtpPacket;


/**
 * @author yuezhu
 * 
 */
public class ClientRtpPacketHandler extends IoHandlerAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientRtpPacketHandler.class);

  @Override
  public void sessionCreated(IoSession session) throws Exception {
  }

  @Override
  public void messageReceived(IoSession session, Object message) throws Exception {
    IoBuffer buffer = (IoBuffer) message;
    if (buffer.remaining() < 12) {
      LOGGER.debug("Discard a RTP packet due to header size < 12 bytes");
      return;
    }
    
    RtpPacket packet = new RtpPacket(buffer);

    LOGGER.debug("Received RTP packet: " + packet.getSequence());

    ClientTrack clientTrack = ClientTrack.getByClientSocketAddress((InetSocketAddress) session.getRemoteAddress());

    if (clientTrack == null) {
      // drop packet
      LOGGER.debug("Packet received from unknown client: " + session.getRemoteAddress());
      return;
    }

    clientTrack.forwardRtpToServer(packet);
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
    LOGGER.debug("ClientRtpPacketHandler sessionClosed() is invoked.");
  }

}
