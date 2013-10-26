/**
 * 
 */
package video.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderAdapter;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class RtspDecoder extends MessageDecoderAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RtspDecoder.class);

  private final CharsetDecoder decoder;
    
  private static final byte[] CONTENT_LENGTH = new String("Content-Length:").getBytes();

  public RtspDecoder(Charset charset) {
    decoder = charset.newDecoder();
  }

  /**
   * Check to see if the message received so far is complete. The IoBuffer will
   * not change.
   * 
   * @param in
   * @return
   * @throws Exception
   */
  private boolean messageComplete(IoBuffer in) throws Exception {
    int last = in.remaining() - 1;
    if (in.remaining() < 4) {
      return false;
    }

    // first the position of the 0x0D 0x0A 0x0D 0x0A bytes
    // 0x0D 0x0A 0x0D 0x0A is ACSII code for \r\n.
    int eoh = -1; // End of header
    for (int i = last; i > 2; i--) {
      if (in.get(i) == (byte) 0x0A && in.get(i - 1) == (byte) 0x0D && in.get(i - 2) == (byte) 0x0A
          && in.get(i - 3) == (byte) 0x0D) {
        eoh = i + 1;
        break;
      }
    }

    if (eoh == -1) {
      // There is no CRLF in the message.
      return false;
    }

    // Search for Content-Length.
    // If the Content-Length is present, the length of the body must equal to
    // the value specified in the Content-Length.
    // Else the Content-Length is 0, and the message is considered compete.
    for (int i = 0; i < last; i++) {
      boolean found = false;
      for (int j = 0; j < CONTENT_LENGTH.length; j++) {
        if (in.get(i + j) != CONTENT_LENGTH[j]) {
          found = false;
          break;
        }
        found = true;
      }

      // If Content-Length is present, we check if the length of body equals to
      // the value.
      if (found) {
        // retrieve value from this position till next 0x0D 0x0A, which will be
        // the content length of body.
        StringBuilder contentLength = new StringBuilder();
        for (int j = i + CONTENT_LENGTH.length; j < last; j++) {
          if (in.get(j) == 0x0D) {
            break;
          }
          contentLength.append(new String(new byte[] { in.get(j) }));
        }
        // if content-length worth of data has been received then the message is complete
        return Integer.parseInt(contentLength.toString().trim()) + eoh == in.remaining();
      }
    }

    // No Content-Length is present, we will assume the length of body is 0 and
    // simple return true.
    return true;
  }

  /**
   * Once a message is complete and decodable, then parse RTSP message and save
   * it into an RTSP object.
   * 
   * @param in
   * @return
   */
  private RtspMessage doParse(IoBuffer in) {
    RtspMessage message = null;
    try {

      BufferedReader rdr = new BufferedReader(new StringReader(in.getString(decoder)));
      String line = null;
      Matcher matcher = null;
      // first line.
      line = rdr.readLine();

//      LOGGER.debug("Parsing first line.");
      if (line.startsWith(RtspMessage.VERSION)) {
        // It is an RTSP response.
//        LOGGER.debug("RTSP response.");
        message = new RtspResponse();
        matcher = RtspMessage.RtspResponsePattern.matcher(line);
        if (!matcher.matches()) {
          // Request line is ill-formatted.
          LOGGER.debug("Request line is mal-formatted: " + line);
          return null;
        }
        ((RtspResponse) message).setStatusCode(RtspStatusCode.fromCode(matcher.group(2)));
      } else {
        // It is an RTSP request.
//        LOGGER.debug("RTSP request.");
        message = new RtspRequest();
        matcher = RtspMessage.RtspRequestPattern.matcher(line);
        if (!matcher.matches()) {
          // Status line is ill-formatted.
          LOGGER.debug("Status line is mal-formatted: " + line);
          return null;
        }
        ((RtspRequest) message).setMethod(RtspMethod.fromString(matcher.group(1)));
        String uriString = matcher.group(2);
        // ((RtspRequest)message).setRtspVersion(matcher.group(3));
        // logger.debug("uri: " + uriString);
        if (uriString.equals("*")) {
          ((RtspRequest) message).setRequestUri(null);
        } else {
          URI uri = new URI(uriString);
          ((RtspRequest) message).setRequestUri(uri);
        }

      }

//      LOGGER.debug("Parsing header lines.");
      while ((line = rdr.readLine()) != null && (!line.equals(""))) {
        matcher = RtspMessage.RtspHeaderPattern.matcher(line);
        if (!matcher.matches()) {
          // Header line is ill-formatted.
          LOGGER.debug("Header line is mal-formatted: " + line);
          return null;
        }
        // Set each parsed field into message header.
        message.setField(matcher.group(1), matcher.group(2));
        // logger.debug("header group1: " + matcher.group(1));
        // logger.debug("header group2: " + matcher.group(2));
      }

//      LOGGER.debug("Parsing body lines.");
      while ((line = rdr.readLine()) != null && (!line.equals(""))) {
        message.appendBody(line);
        message.appendBody(RtspMessage.CRLF);
      }

      rdr.close();

      LOGGER.debug("RTSP message parse done.");
      return message;
    } catch (URISyntaxException ex) {
      LOGGER.error("RTSP message parsing error: " + ex.toString());
    } catch (IOException ex) {
      LOGGER.error("RTSP message parsing error: " + ex.toString());
    }
    return null;
  }

  @Override
  public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
    // Return NEED_DATA if the whole header is not read yet.
    try {
      return messageComplete(in) ? MessageDecoderResult.OK : MessageDecoderResult.NEED_DATA;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return MessageDecoderResult.NOT_OK;
  }

  @Override
  public MessageDecoderResult decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
    LOGGER.debug("RTSP message is complete. Start parsing.");
    RtspMessage m = doParse(in);
    // Return NEED_DATA if the body is not fully read.
    if (m == null) {
      return MessageDecoderResult.NEED_DATA;
    }
    out.write(m);
    LOGGER.debug("Write done.");
    return MessageDecoderResult.OK;

  }

}
