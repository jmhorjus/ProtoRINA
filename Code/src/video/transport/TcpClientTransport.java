/**
 * 
 */
package video.transport;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuezhu
 * 
 */
public class TcpClientTransport extends TcpTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(TcpClientTransport.class);

  private String hostname;
  private int port = 0;

  public TcpClientTransport(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;
  }

  public void start() throws IOException {
    LOGGER.debug("Connecting " + hostname + ":" + port);
    socket = new Socket(this.hostname, this.port);
    openStream();
  }

  public void stop() throws IOException {
    closeStream();
    closeSocket();
  }

}
