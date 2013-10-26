/**
 * 
 */
package video.clientProxy;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rina.ipc.impl.IPCImpl;
import video.lib.Configuration;
import video.lib.Constants;
import video.transport.TransportService;


/**
 * @author yuezhu
 * 
 */
public class OldClientProxy {
	private static final Logger LOGGER = LoggerFactory.getLogger(OldClientProxy.class);

	private static ClientProxy clientTransport = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		Configuration.getInstance("configuration.properties");

		String clientName = "client";

		String clientInstance = "1";

		String serverName = "server";

		String serverInstance = "1";

		String file = "./experimentConfigFiles/videoDemo/ipcBostonU2.properties";	

		IPCImpl ipc = new IPCImpl(file);

		
		clientTransport = new ClientProxy(clientName, clientInstance,serverName,serverInstance );

		clientTransport.addIPC(ipc);


		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			clientTransport.init();
		} catch (IOException e) {
			LOGGER.error("Failed to initialize transport service.");
			System.exit(-1);
		}

		RtspClientService rtspClientService = new RtspClientService(clientTransport);
		try {
			rtspClientService.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RtpClientService rtpClientService = new RtpClientService(clientTransport);
		try {
			rtpClientService.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

	}
}
