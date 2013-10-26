/**
 * 
 */
package video.transport;


/**
 * @author yuezhu
 *
 */
public interface TransportService {
  
  TransportChannel getRTSPTransportChannel();
  TransportChannel getRTPTransportChannel();
  TransportChannel getRTCPTransportChannel();
  
 
}
