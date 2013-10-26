/**
 * 
 */
package video.transport;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author yuezhu
 *
 */
public class TcpServerTransport extends TcpTransport {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpServerTransport.class);

	private int port = 0;
	private ServerSocket serverSocket;
	
	public TcpServerTransport(int port) throws IOException {
		this.port = port;
	}
	
	public void start() throws IOException {
		serverSocket = new ServerSocket(port);
		LOGGER.debug("Server socket created.");
	}
	
	public void listen() throws IOException {
	  LOGGER.debug("Listening at " + port);
	  socket = serverSocket.accept();
		LOGGER.debug("Connection accepted.");
		if (serverSocket != null) {
			serverSocket.close();
			LOGGER.debug("Listening socket closed.");
		}
		openStream();
	}

	public void stop() throws IOException {
	  closeStream();
	  closeSocket();
	}

}
