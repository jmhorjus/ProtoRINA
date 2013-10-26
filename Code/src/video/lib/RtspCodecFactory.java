/**
 * 
 */
package video.lib;

import java.nio.charset.Charset;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

/**
 * @author yuezhu
 * 
 */
public class RtspCodecFactory extends DemuxingProtocolCodecFactory {
  
  public static final AttributeKey TOKEN = new AttributeKey(RtspCodecFactory.class, "token");

  public RtspCodecFactory(Charset charset) {
    super.addMessageEncoder(RtspMessage.class, new RtspEncoder(charset));
    super.addMessageDecoder(new RtspDecoder(charset));
  }

}
