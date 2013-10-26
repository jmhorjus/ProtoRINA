/**
 * 
 */
package video.serverProxy;

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
public class ServerRtpPacketHandler extends IoHandlerAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerRtpPacketHandler.class);

  @Override
  public void messageReceived(IoSession session, Object message) throws Exception {
    IoBuffer buffer = (IoBuffer) message;
    if (buffer.remaining() < 12) {
      LOGGER.debug("Discard a RTP packet due to header size < 12 bytes");
      return;
    }

    RtpPacket packet = new RtpPacket(buffer);
//    LOGGER.debug("Received RTP packet: " + packet.getSequence());
    ServerTrack serverTrack = ServerTrack.getByServerSsrc(packet.getSsrc());

    if (serverTrack == null) {
      serverTrack = ServerTrack.getByServerSocketAddress((InetSocketAddress) session.getRemoteAddress());
      if (serverTrack == null) {
        // drop packet
        LOGGER.debug("Invalid SSRC identifier: " + packet.getSsrc().toHexString());
        return;
      } else {
        // hot-wire the ssrc into the track
        LOGGER.debug("Adding SSRC identifier: " + packet.getSsrc().toHexString());
        serverTrack.setServerSsrc(packet.getSsrc());
      }

    }

    serverTrack.setRtpServerSession(session);
    serverTrack.forwardRtpToClient(packet);
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
  public void sessionCreated(IoSession session) throws Exception {
  }
  
  @Override
  public void sessionClosed(IoSession session) {
    /*
     * Invoked when a connection is closed.
     */
    LOGGER.debug("ServerRtpPacketHandler sessionClosed() is invoked.");
  }


}
