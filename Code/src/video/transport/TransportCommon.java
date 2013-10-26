/**
 * 
 */
package video.transport;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import application.component.impl.IPCResourceManagerImpl;



/**
 * @author yuezhu
 *
 */
public class TransportCommon implements TransportChannel {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransportCommon.class);

	private MessageQueue<byte[]> queue = null;

	private IPCResourceManagerImpl ipcManager = null;

	private String srcApName = null;
	private String dstApName = null;
	private String srcAeName = null;
	private String dstAeName = null;

	private Thread sendThread = null;
	private Thread recvThread = null;

	private int handleID;

	volatile private boolean running = false;

	/**
	 * Client 
	 * @param srcApName
	 * @param srcAeName
	 * @param dstApName
	 * @param dstAeName
	 * @param handleID
	 * @param ipcManager
	 */
	public TransportCommon(String srcApName, String srcAeName, String dstApName, String dstAeName,  int handleID, IPCResourceManagerImpl ipcManager ) {

		this.srcApName = srcApName;
		this.srcAeName = srcAeName;
		this.dstApName = dstApName;
		this.dstAeName = dstAeName;

		this.ipcManager = ipcManager;

		this.handleID = handleID;

		queue = new MessageQueue<byte[]>();

	}

	public TransportCommon(String srcApName, String srcAeName, int handleID, IPCResourceManagerImpl ipcManager ) {

		this.srcApName = srcApName;
		this.srcAeName = srcAeName;

		this.ipcManager = ipcManager;

		this.handleID = handleID;

		queue = new MessageQueue<byte[]>();

	}


	/**
	 * Take from out queue and send to network.
	 * @throws IOException
	 */
	private void doSend() throws Exception {
		byte[] bytes = queue.outQueueTake();

		this.ipcManager.send(this.handleID,bytes);
	}

	/**
	 * Receive from network and put into in queue.
	 * @throws IOException
	 */
	private void doRecv() throws Exception {
		byte[] bytes = this.ipcManager.receive(this.handleID);
		queue.inQueuePut(bytes);
	}

	@Override
	public void send(byte[] bytes) {
		queue.outQueuePut(bytes);
	}

	@Override
	public byte[] recv() {
		return queue.inQueueTake();
	}

	public void start() {
		running = true;
		sendThread = new Thread() {
			public void run() {

				while (running)
				{
					try {

						doSend();
					} catch (Exception e) {
						LOGGER.error("Transport service stopped at send thread due to underlying transport service down: " + e.toString());
					    running = false;
					} 
				}
			}
		};

		recvThread = new Thread() {
			public void run() {

				while (running)
				{
					try {

						doRecv();
					} catch (Exception e) {
						LOGGER.error("Transport service stopped at recv thread due to underlying transport service down: " + e.toString());
						running = false;
					} 
				}
			}
		};

		sendThread.start();
		recvThread.start();
	}

	public void stop() {
		running = false;
	}

}
