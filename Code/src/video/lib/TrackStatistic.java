/**
 * 
 */
package video.lib;

import java.io.IOException;
//import java.util.TimerTask;


/**
 * @author yuezhu
 *
 */
public class TrackStatistic extends Statistic {
  
  public TrackStatistic(String name) {
    super(name);
  }

  /* (non-Javadoc)
   * @see demo.video.common.ServiceInterface#start()
   */
  @Override
  public void start() throws IOException {
    if (started) {
      return;
    }
    openFile(name, true);
  }

  /* (non-Javadoc)
   * @see demo.video.common.ServiceInterface#stop()
   */
  @Override
  public void stop() throws IOException {
    if (!started) {
      return;
    }
    closeFile();
    started = false;

  }

  /* (non-Javadoc)
   * @see demo.video.common.Statistic#doStatistic(demo.video.rtp.RtpPacket)
   */
  @Override
  public void doStatistic(RtpPacket packet) {
    currentTime = System.currentTimeMillis();
    StringBuilder line = new StringBuilder()
      .append(packet.getSequence())
      .append(" ")
      .append(String.valueOf(currentTime))
      .append(" ")
      .append(packet.size())
      .append(Configuration.getLineSeparator());
      try {
        writeToFile(line.toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

}
