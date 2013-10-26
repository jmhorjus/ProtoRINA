/**
 * 
 */
package video.lib;

import java.io.IOException;
import java.util.TimerTask;


/**
 * @author yuezhu
 * 
 */
public class OverallStatistic extends Statistic {
  
  public OverallStatistic(String name) {
    super(name);
  }

  public void doStatistic(RtpPacket packet) {
    totalSize += packet.size();
    if (!started) {
      started = true;
    }
  }

  @Override
  public void start() throws IOException {
    if (started) {
      return;
    }
    openFile(name, true);
    int delay = 1;     // delay for 1 second.
    int period = 1000; // repeat every second.
    timer.scheduleAtFixedRate(new TimerTask() {
      public void run() {
//        if (!started) {
//          return;
//        }
        currentTime = System.currentTimeMillis();
        timeElapsed++;
        avgThroughput = totalSize / (double)timeElapsed;
        throughput = totalSize - lastSize;
        lastSize = totalSize;
        StringBuilder line = new StringBuilder("Time=")
        .append(String.valueOf(currentTime))
        .append("\t\tThru=")
        .append(dt.format(throughput / 1000))
        .append(" KB/s")
        .append("\t\tAvg thru=")
        .append(dt.format(avgThroughput / 1000))
        .append(" KB/s")
        .append("\t\tTotal=")
        .append(String.valueOf(totalSize / 1000))
        .append(" KB")
        .append(Configuration.getLineSeparator());
        try {
          writeToFile(line.toString());
        } catch (IOException e) {
          e.printStackTrace();
        }
        System.err.print(line);
      }
    }, delay, period);
  }

  @Override
  public void stop() throws IOException {
    if (!started) {
      return;
    }
    timer.cancel();
    closeFile();
    started = false;
  }
}
