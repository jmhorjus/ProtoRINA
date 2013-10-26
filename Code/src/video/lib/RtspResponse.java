/**
 * 
 */
package video.lib;

import java.util.Map;

/**
 * @author yuezhu
 * 
 */
public class RtspResponse extends RtspMessage {
  private RtspStatusCode statusCode;

  public RtspResponse() {
    setType(Type.RESPONSE);
  }
  
  public RtspStatusCode getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(RtspStatusCode statusCode) {
    this.statusCode = statusCode;
  }

  public static RtspResponse newInstance(RtspStatusCode rtspStatusCode, String CSeq) {
    RtspResponse rtspResponse = new RtspResponse();
    rtspResponse.setStatusCode(rtspStatusCode);
    rtspResponse.setField("CSeq", CSeq);
    return rtspResponse;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    // Status line.
    sb.append(VERSION);
    sb.append(SP);
    sb.append(getStatusCode().toCode());
    sb.append(SP);
    sb.append(getStatusCode().toReason());
    sb.append(CRLF);
    // Header and body.
    // Header
    for (Map.Entry<String, String> e : getHeader().entrySet()) {
      sb.append(e.getKey() + ": " + e.getValue() + CRLF);
    }
    // CRLF
    sb.append(CRLF);
    // Body
    if (body.length() != 0) {
      sb.append(getBody());
      // Note: Never append an ending CRLF for the body.
      // sb.append(CRLF);
    }

    return sb.toString();

  }

}
