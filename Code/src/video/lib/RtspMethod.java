/**
 * 
 */
package video.lib;

/**
 * @see RFC 2326 [6.1]
 * @author yuezhu
 * 
 */
public enum RtspMethod {

  DESCRIBE,
  ANNOUNCE,
  GET_PARAMETER,
  OPTIONS,
  PAUSE,
  PLAY,
  RECORD,
  REDIRECT,
  SETUP,
  SET_PARAMETER,
  TEARDOWN,
  UNKNOWN;

  public static RtspMethod fromString(String s) {
    RtspMethod method = null;
    try {
      method = RtspMethod.valueOf(s);
    } catch (IllegalArgumentException iae) {
      method = RtspMethod.valueOf("UNKNOWN");
    }
    return method;
  }

  public static String getRegex() {
    StringBuffer sb = new StringBuffer();
    for (RtspMethod m : RtspMethod.values()) {
      sb.append(m.toString()).append("|");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

}
