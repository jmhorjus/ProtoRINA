/**
 * 
 */
package video.lib;

import java.util.LinkedList;
import java.util.List;

/**
 * @author yuezhu
 * 
 */
public class RtspTransportHeader {

  private List<RtspTransport> transportList = null;

  public RtspTransportHeader(String transport) {
    transportList = new LinkedList<RtspTransport>();
    for (String item : transport.split(",")) {
      transportList.add(new RtspTransport(item));
    }
  }

  public int size() {
    return transportList.size();
  }

  public RtspTransport get(int i) {
    return transportList.get(i);
  }
  
  public RtspTransport remove(int i) {
    return transportList.remove(i);
  }
  
  public RtspTransport removeAllExcept(int i) {
    RtspTransport transport = transportList.get(i);
    transportList.removeAll(transportList);
    transportList.add(transport);
    return transport;
  }

  public List<RtspTransport> getAll() {
    return transportList;
  }

  public void set(int i, RtspTransport transport) {
    transportList.set(i, transport);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (RtspTransport item : transportList) {
      sb.append(item.toString()).append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  private static final int UNSPECIFIED = -1;

  // Cast type. The default is multicast.
  public enum Cast {
    UNSPECIFIED, unicast, multicast;
  }

  // Transport protocol.
  public enum Transport {
    UNSPECIFIED, RTP, RDT;
  }

  // Lower transport.
  public enum LowerTransport {
    OTHER, TCP, UDP;
  }

  // The mode parameter indicates the methods to be supported for this
  // session.
  // The default is PLAY.
  public enum Mode {
    UNSPECIFIED, PLAY, RECORD;
  }

  private static int getIntValue(String s) {
    String[] sp = s.split("=");
    return Integer.parseInt(sp[1].trim());
  }

  public static String getStringValue(String s) {
    String[] sp = s.split("=");
    return sp[1].trim();
  }

  public static int[] getPairValue(String s) {
    String[] v = s.split("=");
    if (v.length < 2) {
      return null;
    }
    String[] sp = v[1].split("-");
    int[] rtn = new int[2];
    rtn[0] = Integer.parseInt(sp[0].trim());
    rtn[1] = Integer.parseInt(sp[1].trim());
    return rtn;
  }

  public static String pairToString(int[] value) {
    if (value.length < 2) {
      return "";
    }
    StringBuffer sb = new StringBuffer();
    sb.append(value[0]).append("-").append(value[1]);
    return sb.toString();

  }

  public static String intToString(int value) {
    return String.valueOf(value);
  }

  public class RtspTransport {

    // General parameters:
    private Cast cast = Cast.UNSPECIFIED;

    // The address to which a stream will be sent.
    private String destination = null;

    // The address from which a stream will be sent.
    private String source = null;

    // The number of multicast layers to be used in this media stream.
    private int layers = UNSPECIFIED;

    private Mode mode = Mode.UNSPECIFIED;

    // If the mode parameter includes RECORD, the append parameter indicates
    // that the media data should append to the existing resource rather
    // than overwrite it.
    private boolean append = false;

    // The channels number of mixed media streams.
    private int[] interleaved = null;

    // Multicast specific:

    // multicast time-to-live
    private int ttl = UNSPECIFIED;

    // RTP Specific:

    // RTP/RTCP port pair for a mulitcast session.
    private int[] port = null;

    // The unicast RTP/RTCP port pair on which the client has chosen to
    // receive media data and control information.
    private int[] clientPort = null;

    // The unicast RTP/RTCP port pair on which the server has chosen to
    // receive media data and control information.
    private int[] serverPort = null;

    // RTP SSRC.
    private String ssrc = null;

    // Transport
    Transport transport = Transport.UNSPECIFIED;

    // Profile.
    private String profile = null;

    // lower transport
    LowerTransport lowerTransport = LowerTransport.UDP;

    public RtspTransport(String line) {
      initAllFields();
      setAllFields(line);
    }

    private void initAllFields() {
      cast = Cast.UNSPECIFIED;
      destination = null;
      source = null;
      layers = UNSPECIFIED;
      mode = Mode.UNSPECIFIED;
      append = false;
      interleaved = null;
      ttl = UNSPECIFIED;
      port = null;
      clientPort = null;
      serverPort = null;
      ssrc = null;
      transport = Transport.UNSPECIFIED;
      profile = null;
      lowerTransport = null;
    }

    private void setAllFields(String line) {
      for (String s : line.split(";")) {
        if (s.startsWith("RTP") || s.startsWith("RDT")) {
          if (s.startsWith("RTP")) {
            transport = Transport.RTP;
          } else if (s.startsWith("RDT")) {
            transport = Transport.RDT;
          }
          String[] sp = s.split("/");
          if (sp.length > 2) {
            // e.g. RTP/AVP/TCP
            profile = sp[1];
            lowerTransport = LowerTransport.valueOf(sp[2]);
          } else if (sp.length > 1) {
            // e.g. RTP/AVP
            profile = sp[1];
            lowerTransport = LowerTransport.UDP;
          } else {
            profile = null;
            lowerTransport = LowerTransport.OTHER;
          }
        } else if (s.equals("unicast") || s.equals("multicast")) {
          cast = Cast.valueOf(s);
        } else if (s.startsWith("destination")) {
          destination = getStringValue(s);
        } else if (s.startsWith("source")) {
          source = getStringValue(s);
        } else if (s.startsWith("interleaved")) {
          interleaved = getPairValue(s);
        } else if (s.startsWith("append")) {
          append = true;
        } else if (s.startsWith("ttl")) {
          ttl = getIntValue(s);
        } else if (s.startsWith("layers")) {
          layers = getIntValue(s);
        } else if (s.startsWith("port")) {
          port = getPairValue(s);
        } else if (s.startsWith("client_port")) {
          clientPort = getPairValue(s);
        } else if (s.startsWith("server_port")) {
          serverPort = getPairValue(s);
        } else if (s.startsWith("ssrc")) {
          ssrc = getStringValue(s);
        } else if (s.startsWith("mode")) {
          String m = getStringValue(s);
          mode = Mode.valueOf(m.substring(1, m.length() - 1));
        }
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();

      // Transport specifier
      // It's in the form of transport/profile/lower-transport
      if (transport != Transport.UNSPECIFIED) {
        sb.append(transport.toString());
        if (profile != null) {
          sb.append("/").append(profile);
          if (lowerTransport != LowerTransport.UDP) {
            // UDP is the default lower transport type, and we don't need to explicitly specify it.
            sb.append("/").append(lowerTransport.toString());
          }
        }

      }

      // unicast or multicast
      if (cast != Cast.UNSPECIFIED) {
        sb.append(";").append(cast.toString());
      }

      // destination
      if (destination != null) {
        sb.append(";").append("destination=").append(destination);
      }

      // source
      if (source != null) {
        sb.append(";").append("source=").append(source);
      }

      // interleaved
      if (interleaved != null) {
        sb.append(";").append("interleaved=").append(pairToString(interleaved));
      }

      // append
      if (append == true) {
        sb.append(";").append("append");
      }

      // ttl
      if (ttl != UNSPECIFIED) {
        sb.append(";").append("ttl=").append(intToString(ttl));
      }

      // layers
      if (layers != UNSPECIFIED) {
        sb.append(";").append("layers=").append(intToString(layers));
      }

      // port
      if (port != null) {
        sb.append(";").append(pairToString(port));
      }

      // client_port
      if (clientPort != null) {
        sb.append(";").append("client_port=").append(pairToString(clientPort));
      }

      // server_port
      if (serverPort != null) {
        sb.append(";").append("server_port=").append(pairToString(serverPort));
      }

      // ssrc
      if (ssrc != null) {
        sb.append(";").append("ssrc=").append(ssrc);
      }

      // mode
      if (mode != Mode.UNSPECIFIED) {
        sb.append(";").append("mode=").append("\"").append(mode.toString()).append("\"");
      }

      return sb.toString();
    }

    /**
     * @return the cast
     */
    public Cast getCast() {
      return cast;
    }

    /**
     * @param cast
     *          the cast to set
     */
    public void setCast(Cast cast) {
      this.cast = cast;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
      return destination;
    }

    /**
     * @param destination
     *          the destination to set
     */
    public void setDestination(String destination) {
      this.destination = destination;
    }

    /**
     * @return the source
     */
    public String getSource() {
      return source;
    }

    /**
     * @param source
     *          the source to set
     */
    public void setSource(String source) {
      this.source = source;
    }

    /**
     * @return the layers
     */
    public int getLayers() {
      return layers;
    }

    /**
     * @param layers
     *          the layers to set
     */
    public void setLayers(int layers) {
      this.layers = layers;
    }

    /**
     * @return the mode
     */
    public Mode getMode() {
      return mode;
    }

    /**
     * @param mode
     *          the mode to set
     */
    public void setMode(Mode mode) {
      this.mode = mode;
    }

    /**
     * @return the append
     */
    public boolean isAppend() {
      return append;
    }

    /**
     * @param append
     *          the append to set
     */
    public void setAppend(boolean append) {
      this.append = append;
    }

    /**
     * @return the interleaved
     */
    public int[] getInterleaved() {
      return interleaved;
    }

    /**
     * @param interleaved
     *          the interleaved to set
     */
    public void setInterleaved(int[] interleaved) {
      this.interleaved = interleaved;
    }

    /**
     * @return the ttl
     */
    public int getTtl() {
      return ttl;
    }

    /**
     * @param ttl
     *          the ttl to set
     */
    public void setTtl(int ttl) {
      this.ttl = ttl;
    }

    /**
     * @return the port
     */
    public int[] getPort() {
      return port;
    }

    /**
     * @param port
     *          the port to set
     */
    public void setPort(int[] port) {
      this.port = port;
    }

    /**
     * @return the clientPort
     */
    public int[] getClientPort() {
      return clientPort;
    }

    /**
     * @param clientPort
     *          the clientPort to set
     */
    public void setClientPort(int[] clientPort) {
      this.clientPort = clientPort;
    }

    /**
     * @return the serverPort
     */
    public int[] getServerPort() {
      return serverPort;
    }

    /**
     * @param serverPort
     *          the serverPort to set
     */
    public void setServerPort(int[] serverPort) {
      this.serverPort = serverPort;
    }

    /**
     * @return the ssrc
     */
    public String getSsrc() {
      return ssrc;
    }

    /**
     * @param ssrc
     *          the ssrc to set
     */
    public void setSsrc(String ssrc) {
      this.ssrc = ssrc;
    }

    /**
     * @return the transport
     */
    public Transport getTransport() {
      return transport;
    }

    /**
     * @param transport
     *          the transport to set
     */
    public void setTransport(Transport transport) {
      this.transport = transport;
    }

    /**
     * @return the profile
     */
    public String getProfile() {
      return profile;
    }

    /**
     * @param profile
     *          the profile to set
     */
    public void setProfile(String profile) {
      this.profile = profile;
    }

    /**
     * @return the lowerTransport
     */
    public LowerTransport getLowerTransport() {
      return lowerTransport;
    }

    /**
     * @param lowerTransport
     *          the lowerTransport to set
     */
    public void setLowerTransport(LowerTransport lowerTransport) {
      this.lowerTransport = lowerTransport;
    }

  }
/*
  public static void main(String[] args) {
    String test = "RTP/AVP;multicast;ttl=127;mode=\"PLAY\",RTP/AVP;unicast;client_port=3456-3457;mode=\"PLAY\"";
    String test1 = "RTP/AVP;unicast;client_port=60190-60191,RTP/AVP;unicast;destination=127.0.0.1;source=127.0.0.1;client_port=60190-60191;server_port=6970-6971";
    RtspTransportHeader header = new RtspTransportHeader(test1);
    System.err.print(header.toString());

  }
*/
}
