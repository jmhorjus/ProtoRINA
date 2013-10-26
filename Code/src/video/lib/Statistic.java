/**
 * 
 */
package video.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;


/**
 * @author yuezhu
 *
 */
public abstract class Statistic implements ServiceInterface {
  public static DecimalFormat dt = (DecimalFormat) DecimalFormat.getInstance();
  
  protected String name;
  protected double totalSize;
  protected double lastSize;
  protected long timeElapsed;
  protected double avgThroughput;
  protected double throughput;
  protected boolean started;
  protected Timer timer;
  protected FileWriter writer;
  protected long currentTime;
  
  public Statistic(String name) {
    totalSize = 0;
    lastSize = 0;
    timeElapsed = 0;
    avgThroughput = 0;
    throughput = 0;
    started = false;
    timer = new Timer();
    dt.applyPattern("0.000");
    this.name = name;
  }
  
  abstract public void doStatistic(RtpPacket packet);

  public void openFile(String filename, boolean append) throws IOException {
    File file = new File(filename);
    if (file.exists()) {
      file.delete();
    }
    writer = new FileWriter(filename, append);
    // writeToFile("---------------------------------------------------------" + Configuration.getLineSeparator());
  }
  
  public void writeToFile(String content) throws IOException {
    writer.write(content);
    writer.flush();
  }
  
  public void closeFile() throws IOException {
    // writeToFile("---------------------------------------------------------" + Configuration.getLineSeparator());
    writer.close();
  }

}
