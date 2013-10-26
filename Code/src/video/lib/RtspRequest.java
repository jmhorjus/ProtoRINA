/**
 * 
 */
package video.lib;

import java.net.URI;
import java.util.Map;

/**
 * @author yuezhu
 * 
 */
public class RtspRequest extends RtspMessage {
  private RtspMethod method;
  private URI requestUri;

  public RtspRequest() {
    setType(Type.REQUEST);
  }

  public RtspMethod getMethod() {
    return method;
  }

  public void setMethod(RtspMethod method) {
    this.method = method;
  }

  public URI getRequestUri() {
    return requestUri;
  }

  public void setRequestUri(URI requestUri) {
    this.requestUri = requestUri;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    // Request line.
    String uri;
    if (requestUri == null) {
      uri = "*";
    } else {
      uri = requestUri.toString();
    }
    sb.append(method.toString());
    sb.append(SP);
    sb.append(uri);
    sb.append(SP);
    sb.append(VERSION);
    sb.append(CRLF);
    // Header and body
    // Header
    for (Map.Entry<String, String> e : getHeader().entrySet()) {
      sb.append(e.getKey() + ": " + e.getValue() + CRLF);
    }
    sb.append(CRLF);
    // Body
    if (body.length() != 0) {
      sb.append(getBody());
      sb.append(CRLF);

    }
    return sb.toString();
  }

}
