/**
 * 
 */
package video.lib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuezhu
 * 
 */
public class Configuration {
  private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

  private static Properties cfgProp = new Properties();

  private static String pwd = null;
  private static String fileSeparator = null;
  private static String lineSeparator = null;
  private static String cfgFilename = null;
  private static String cfgPathname = null;
  private static String logFilename = null;
  private static String logPathname = null;
  private static String logSize = null;
  private static String logBakIndex = "1";
  private static String appName = null;
  private static String appVersion = null;
  private static String osVersion = null;
  private static String osArch = null;
  private static String osName = null;
  private static String productTokens = null;

  private Configuration(String filename) {
    configureEnv();
    configureApp(filename);
    //configureLog();
    LOGGER.info("Configuration file succcessfully loaded from " + cfgPathname);
  }
  
  // Singleton
  private static Configuration instance = null;
  private static Object classLock = Configuration.class;

  public static Configuration getInstance(String filename) {
    synchronized (classLock) {
      if (instance == null)
        instance = new Configuration(filename);
      return instance;
    }
  }

  /**
   * @return the pwd
   */
  public static String getPwd() {
    return pwd;
  }
  
  /**
   * @return the fileSeparator
   */
  public static String getFileSeparator() {
    return fileSeparator;
  }
  
  /**
   * @return the lineSeparator
   */
  public static String getLineSeparator() {
    return lineSeparator;
  }

  public static void configureEnv() {
    pwd = System.getProperty("user.dir");
    fileSeparator = System.getProperty("file.separator");
    lineSeparator = System.getProperty("line.separator");
    osVersion = System.getProperty("os.version");
    osArch = System.getProperty("os.arch");
    osName = System.getProperty("os.name");
  }

  public static void configureApp(String filename) {
		cfgFilename = filename;
		cfgPathname = pwd + fileSeparator + cfgFilename;

		// Open properties file.
		try {
			FileInputStream fis = new FileInputStream(cfgPathname);
			cfgProp.load(fis);
			fis.close();
		} catch (Exception e) {
			cfgProp.setProperty("application.name", "RINA Media Streaming");
			cfgProp.setProperty("application.version", "v0.1");
			cfgProp.setProperty("log.level", "info");
			cfgProp.setProperty("log.to.file", "enable");
			cfgProp.setProperty("log.to.console", "enable");
			cfgProp.setProperty("log.filename", "video_streaming_log");
			cfgProp.setProperty("log.size", "512KB");
			cfgProp.setProperty("log.backup.index", "10");
			cfgProp.setProperty("streaming.server.hostname", "localhost");
			cfgProp.setProperty("streaming.server.port", "8554");
			cfgProp.setProperty("client.listening.port", "15000");
			cfgProp.setProperty("client.listening.interface", "");
			cfgProp.setProperty("client.rtp.port", "16970");
			cfgProp.setProperty("client.rtcp.port", "16971");
			cfgProp.setProperty("server.rtp.port", "61000");
			cfgProp.setProperty("server.rtcp.port", "61001");
			cfgProp.setProperty("server.rtsp.listening.port", "51234");
			cfgProp.setProperty("server.rtp.listening.port", "51235");
			cfgProp.setProperty("server.rtcp.listening.port", "51236");
			cfgProp.setProperty("server.hostname", "localhost");
			try {
				FileOutputStream fos = new FileOutputStream("configuration.properties");
				cfgProp.store(fos, "RINA Media Streaming auto generated configuration file");
				fos.close();
			} catch (FileNotFoundException e1) {
				LOGGER.error("Configuration file error: " + e1.getMessage());
			} catch (IOException e1) {
				LOGGER.error("Configuration file error: " + e1.getMessage());
			}
		}

		appName = getString("application.name", "RINA Media Streaming");
		appVersion = getString("application.version", "v0.1");
		productTokens = appName + " " + appVersion + " (" + osName + " " + osVersion + " " + osArch + ")";
	}

  public static void configureLog() {
		// Log properties
		Properties logProp = new Properties();

		String logLevel = getString("log.level", "info");

		// Configure log to console.
		if (getBoolean("log.to.console", true) && getBoolean("log.to.file", true)) {
			if (logLevel.equalsIgnoreCase("debug")) {
				logProp.setProperty("log4j.rootLogger", "DEBUG, A1, A2");
			} else if (logLevel.equalsIgnoreCase("info")) {
				logProp.setProperty("log4j.rootLogger", "INFO, A1, A2");
			}
		} else if (getBoolean("log.to.console", true)) {
			if (logLevel.equalsIgnoreCase("debug")) {
				logProp.setProperty("log4j.rootLogger", "DEBUG, A1");
			} else if (logLevel.equalsIgnoreCase("info")) {
				logProp.setProperty("log4j.rootLogger", "INFO, A1");
			}
		} else if (getBoolean("log.to.file", true)) {
			if (logLevel.equalsIgnoreCase("debug")) {
				logProp.setProperty("log4j.rootLogger", "DEBUG, A2");
			} else if (logLevel.equalsIgnoreCase("info")) {
				logProp.setProperty("log4j.rootLogger", "INFO, A2");
			}
		}

		logProp.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
		logProp.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
		logProp.setProperty("log4j.appender.A1.layout.ConversionPattern", "%d{MM/dd/yy HH:mm:ss,SSS} [%c::%M]-[%p] %m%n");

		// Set default log filename.
		logFilename = getString("log.filename", "fitbit_fetch_log");
		// Make default log pathname.
		logPathname = pwd + fileSeparator + logFilename;
		// Get log size.
		logSize = getString("log.size", "512KB");
		// Get log backup index.
		logBakIndex = getString("log.backup.index", "1");

		logProp.setProperty("log4j.appender.A2", "org.apache.log4j.RollingFileAppender");
		logProp.setProperty("log4j.appender.A2.File", logPathname);
		logProp.setProperty("log4j.appender.A2.MaxFileSize", logSize);
		logProp.setProperty("log4j.appender.A2.MaxBackupIndex", logBakIndex);
		logProp.setProperty("log4j.appender.A2.layout", "org.apache.log4j.PatternLayout");
		logProp.setProperty("log4j.appender.A2.layout.ConversionPattern", "%d{MM/dd/yy HH:mm:ss,SSS} [%c::%M]-[%p] %m%n");

		PropertyConfigurator.configure(logProp);
	}  
  
  public static String getProductTokens() {
    return productTokens;
  }

  public static boolean getBoolean(String key, boolean defaultValue) {
    String val = null;

    val = cfgProp.getProperty(key, "false");

    if (val.equalsIgnoreCase("enable") ||
        val.equalsIgnoreCase("yes") ||
        val.equalsIgnoreCase("true") ||
        val.equalsIgnoreCase("1")) {
      return true;
    } else if (val.equalsIgnoreCase("disable") ||
        val.equalsIgnoreCase("no") ||
        val.equalsIgnoreCase("false") ||
        val.equalsIgnoreCase("0")) {
      return false;
    } else {
      return defaultValue;
    }
  }

  public static void setBoolean(String key, boolean value) {
    String s = null;
    if (value) {
      s = "enable";
    } else {
      s = "disable";
    }
    cfgProp.setProperty(key, s);
  }

  public static String getString(String key, String defaultValue) {
		return cfgProp.getProperty(key, defaultValue);
	}

	public static void setString(String key, String value) {
		cfgProp.setProperty(key, value);
	}

	public static int getInt(String key, int defaultValue) {
		return Integer.parseInt(cfgProp.getProperty(key, Integer.valueOf(defaultValue).toString()).trim());
	}

	public static void setInt(String key, int value) {
		cfgProp.setProperty(key, Integer.valueOf(value).toString());
	}

	public static short getShort(String key, short defaultValue) {
		return Short.parseShort(cfgProp.getProperty(key, Short.valueOf(defaultValue).toString()).trim());
	}

	public static void setShort(String key, short value) {
		cfgProp.setProperty(key, Short.valueOf(value).toString());
	}

	public static double getDouble(String key, double defaultValue) {
		return Double.parseDouble(cfgProp.getProperty(key, Double.valueOf(defaultValue).toString()).trim());
	}

	public static void setDouble(String key, double value) {
		cfgProp.setProperty(key, Double.valueOf(value).toString());
	}

}
