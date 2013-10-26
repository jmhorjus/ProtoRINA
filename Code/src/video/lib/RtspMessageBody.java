/**
 * 
 */
package video.lib;

import java.nio.CharBuffer;

/**
 * Message body for an RTSP message.
 * 
 * @see RFC 2326 [6]
 * @author yuezhu
 * 
 */
public class RtspMessageBody {
  private StringBuffer body;

  public RtspMessageBody() {
    body = new StringBuffer();
  }

  public void setBody(StringBuffer body) {
    this.body = body;
  }

  public StringBuffer getBody() {
    return body;
  }

  public void append(StringBuffer value) {
    body.append(value);
  }

  public void append(String value) {
    body.append(value);
  }

  public void append(CharBuffer value) {
    body.append(value);
  }

  public int length() {
    return body.length();
  }

  @Override
  public String toString() {
    return body.toString();
  }

}
