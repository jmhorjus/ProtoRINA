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

import video.lib.RtcpPacket;


/**
 * @author yuezhu
 * 
 */
public class ServerRtcpPacketHandler extends IoHandlerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerRtcpPacketHandler.class);

  @Override
  public void messageReceived(IoSession session, Object buffer) throws Exception {
    RtcpPacket packet = new RtcpPacket((IoBuffer) buffer);
//    LOGGER.debug( "Receive RTCP packet: " + packet.getType() );
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

    serverTrack.setRtcpServerSession(session);
    serverTrack.forwardRtcpToClient(packet);
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
    LOGGER.debug("Exception: " + cause);
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
    LOGGER.debug("ServerRtcpPacketHandler sessionClosed() is invoked.");
  }


}
