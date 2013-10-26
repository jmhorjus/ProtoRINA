/**
 * 
 */
package video.transport;

import java.io.IOException;

/**
 * @author yuezhu
 *
 */
public interface TransportChannel {
  void send(byte[] bytes) throws IOException;
	byte[] recv() throws IOException;
	
	void start();
  void stop();
}
