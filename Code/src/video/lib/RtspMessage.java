/**
 * 
 */
package video.lib;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * All heads for an RTSP message, including general and entity headers. For RTSP
 * request message, this also includes request header. For RTSP response
 * message, this also includes response header.
 * 
 * @see RFC 2326 [6] and RFC 2616 [4.2].
 * @author yuezhu
 * 
 */
public abstract class RtspMessage {
  protected static final String CRLF = "\r\n";
  protected static final String SP = " ";
  protected static final String VERSION = "RTSP/1.0";

  public enum Type {
    REQUEST, RESPONSE, NONE;
  }

  public RtspMessage() {
    this.headers = new LinkedHashMap<String, String>();
    this.body = new StringBuilder();
  }

  // RTSP headers.
  private Map<String, String> headers = null;

  // RTSP body.
  StringBuilder body = null;

  // RTSP message type.
  private Type type = Type.NONE;

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Map<String, String> getHeader() {
    return headers;
  }

  /**
   * @see RFC 2616 [14.45]
   */
  public void setProxy() {
    String s = Configuration.getProductTokens();
    if (headers.get("Server") != null) {
      headers.put("Via", s);
    } else {
      headers.put("Server", s);
    }
//    headers.put("Via", s);
  }

  public String getField(String name) {
    return headers.get(name);
  }

  public void setField(String name, String value) {
    headers.put(name, value);
  }

  public boolean containsField(String name) {
    return headers.containsKey(name);
  }

  public StringBuilder getBody() {
    return body;
  }

  public void setBody(StringBuilder body) {
    this.body = body;
  }

  public void appendBody(StringBuilder value) {
    body.append(value);
  }

  public void appendBody(String value) {
    body.append(value);
  }

  public void appendBody(CharBuffer value) {
    body.append(value);
  }

  public int getBodyLength() {
    return body.length();
  }

  @Override
  abstract public String toString();
  
  ////////
  
  // RTSP request pattern.
  protected static final Pattern RtspRequestPattern = Pattern.compile(new StringBuilder()
    .append("(")
    .append(RtspMethod.getRegex())
    .append(")")
    .append(" ")
    .append("(\\*|[^\\s]+)")
    .append(" ")
    .append("(RTSP/1.0)")
    .toString());

  // RTSP response pattern.
  protected static final Pattern RtspResponsePattern = Pattern.compile(new StringBuilder()
    .append("(RTSP/1.0)")
    .append(" ")
    .append("([1-5][\\d]*)")
    .append(" ")
    .append("(.*)")
    .toString());

  // RTSP message header pattern.
  protected static final Pattern RtspHeaderPattern = Pattern.compile(new StringBuilder()
    .append("([\\w]+[\\-\\w]*)")
    .append(":\\s?")
    .append("(.+)")
    .toString());

  public static RtspMessage parse(CharBuffer in) {
    if (in == null) {
      return null;
    }
    BufferedReader rdr = new BufferedReader(new StringReader(in.toString()));
    return parse(rdr);
  }

  public static RtspMessage parse(String in) {
    if (in == null) {
      return null;
    }
    BufferedReader rdr = new BufferedReader(new StringReader(in));
    return parse(rdr);
  }

  public static RtspMessage parse(byte[] in) {
    if (in == null) {
      return null;
    }
    InputStream is = new ByteArrayInputStream(in);
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is, Charset.forName(Constants.CHARSET)));
    return parse(rdr);
  }

  public static RtspMessage parse(BufferedReader rdr) {
    RtspMessage message = null;
    try {

      String line = null;
      Matcher matcher = null;
      // first line.
      line = rdr.readLine();

      if (line.startsWith(RtspMessage.VERSION)) {
        // It is an RTSP response.
        message = new RtspResponse();
        matcher = RtspResponsePattern.matcher(line);
        if (!matcher.matches()) {
          // Request line is ill-formatted.
          return null;
        }
        ((RtspResponse) message).setStatusCode(RtspStatusCode.fromCode(matcher.group(2)));
      } else {
        // It is an RTSP request.
        message = new RtspRequest();
        matcher = RtspRequestPattern.matcher(line);
        if (!matcher.matches()) {
          // Status line is ill-formatted.
          return null;
        }
        ((RtspRequest) message).setMethod(RtspMethod.fromString(matcher.group(1)));
        String uriString = matcher.group(2);
        if (uriString.equals("*")) {
          ((RtspRequest) message).setRequestUri(null);
        } else {
          URI uri = new URI(uriString);
          ((RtspRequest) message).setRequestUri(uri);
        }
      }

      while ((line = rdr.readLine()) != null && (!line.equals(""))) {
        matcher = RtspHeaderPattern.matcher(line);
        if (!matcher.matches()) {
          // Header line is ill-formatted.
          return null;
        }
        // Set each parsed field into message header.
        message.setField(matcher.group(1), matcher.group(2));
      }

      while ((line = rdr.readLine()) != null && (!line.equals(""))) {
        message.appendBody(line);
        message.appendBody(RtspMessage.CRLF);
      }

      rdr.close();

      return message;
    } catch (URISyntaxException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return null;
  }

}
