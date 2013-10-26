/**
 * 
 */
package video.lib;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;


/**
 * Encode a RTSP message into a buffer for sending.
 */
public class RtspEncoder implements MessageEncoder<Object> {
  private final CharsetEncoder encoder;

  public RtspEncoder(Charset charset) {
    encoder = charset.newEncoder();
  }

  @Override
  public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
    String strVal;

    switch (((RtspMessage) message).getType()) {
    case REQUEST:
      strVal = ((RtspRequest) message).toString();
      break;
    case RESPONSE:
      strVal = ((RtspResponse) message).toString();
      break;
    default:
      strVal = "";
    }

    IoBuffer buf = IoBuffer.allocate(Constants.BUFFER_SIZE).setAutoExpand(true);
    buf.putString(strVal, encoder);
    buf.flip();
    out.write(buf);
  }

}
