/**
 * 
 */
package video.transport;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuezhu
 * 
 */
public class MessageQueue<T> {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageQueue.class);

  private BlockingQueue<T> inQueue = null;
  private BlockingQueue<T> outQueue = null;

  public MessageQueue() {
    inQueue = new LinkedBlockingQueue<T>();
    outQueue = new LinkedBlockingQueue<T>();
  }

  public void outQueuePut(T item) {
    try {
      outQueue.put(item);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  public void inQueuePut(T item) {
    try {
      inQueue.put(item);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  public T outQueueTake() {
    T rtn;
    try {
      rtn = outQueue.take();
    } catch (InterruptedException e) {
      rtn = null;
      e.printStackTrace();
    }
    return rtn;
  }

  public T inQueueTake() {
    T rtn;
    try {
      rtn = inQueue.take();
    } catch (InterruptedException e) {
      rtn = null;
      e.printStackTrace();
    }
    return rtn;
  }
}
