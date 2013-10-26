/**
 * 
 */
package video.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author yuezhu
 * 
 */
public abstract class TcpTransport {
  private static final Logger LOGGER = LoggerFactory.getLogger(TcpTransport.class);

  Socket socket = null;
  DataOutputStream dos = null;
  DataInputStream dis = null;

  protected void openStream() throws IOException {
    dos = new DataOutputStream(socket.getOutputStream());
    dis = new DataInputStream(socket.getInputStream());
    LOGGER.debug("Data Streams opened.");
  }

  protected void closeStream() throws IOException {
    if (dos != null) {
      dos.close();
      LOGGER.debug("Output Streams closed.");
    }
    if (dis != null) {
      dis.close();
      LOGGER.debug("Input Streams closed.");
    }
    
  }
  
  protected void closeSocket() throws IOException {
    if (socket != null) {
      if (!socket.isClosed()) {
        socket.close();
        LOGGER.debug("Socket closed.");
      }
    }
  }

  private void writeBytes(byte[] bytes, int start, int len) throws IOException {
    if (len < 0) {
      throw new IllegalArgumentException("Negative length not allowed");
    }
    if (start < 0 || start >= bytes.length) {
      throw new IndexOutOfBoundsException("Out of bounds: " + start);
    }

    dos.writeInt(len);
    if (len > 0) {
      dos.write(bytes, start, len);
    }
  }

  public void send(byte[] bytes) throws IOException {
    writeBytes(bytes, 0, bytes.length);
  }

  public byte[] recv() throws IOException {
    // Receive length
    int len = dis.readInt();
    byte[] data = new byte[len];
    if (len > 0) {
      dis.readFully(data);
    }
    return data;
  }
  
  abstract public void start() throws IOException;
  abstract public void stop() throws IOException;


}
