package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by michael on 10/10/14.
 */
public class CatchupContext implements CatchupContextInterface {

  private final static String workingDir = System.getProperty("user.dir");
  private final static String userHome = System.getProperty("user.home");

  private String podcastBase;
  private int refreshStartHour;
  private String tmpDir;
  private String pluginDir;
  private String cssDir;
  private String configDir;
  private String xsltDir;
  private String logDir;
  private String recordingDir;
  private String onlineVideoPropertiesDir;
  private int port;
  private int refreshRate;
  private String catalogFileName;
  private String defaultCatalogFileName;
  private String onlineVideoPropsSuffix;
  private PropertiesInterface properties;
  private int refreshStartNowProgrammeThreshold;
  private int catchupServerRmiPort;
  private int catchupPluginRmiPort;

  public CatchupContext(final PropertiesInterface properties) {
      this.properties = properties;
      init(properties);
  }

    private void init(PropertiesInterface properties) {
        xsltDir = workingDir + File.separator + "xslt";
        cssDir = workingDir + File.separator + "css";
        logDir = workingDir + File.separator + "logs";
        pluginDir = workingDir + File.separator + "plugins";
        configDir = workingDir + File.separator + "config";

        recordingDir = properties.getString("recordingDir");
        refreshRate = properties.getInt("refreshRateHours");
        refreshStartHour = properties.getInt("refreshStartHour");
        refreshStartNowProgrammeThreshold = properties.getInt("refreshStartNowProgrammeThreshold");
        port = properties.getInt("podcasterPort");

        tmpDir = System.getProperty("java.io.tmpdir", ".");
        if (!tmpDir.endsWith(File.separator)) {
          tmpDir += File.separator;
        }
        tmpDir += "sagetvcatchup";
        tmpDir += File.separator;

        File tmpDirFle = new File(tmpDir);
        tmpDirFle.mkdirs();

        String defaultFileName = tmpDir + "sagetvcatchup.xml";
        catalogFileName = properties.getString("catalogFileName", defaultFileName);
        String seedDir = workingDir + File.separator + "seeds";
        defaultCatalogFileName = seedDir + File.separator + "default.xml";
        onlineVideoPropsSuffix = properties.getString("onlineVideoPropsSuffix");
        onlineVideoPropertiesDir = properties.getString("onlineVideoPropertiesDir");
        podcastBase = "http://localhost:" + port;
        catchupPluginRmiPort = properties.getInt("catchupPluginRmiPort", 1105);
        catchupServerRmiPort = properties.getInt("catchupServerRmiPort", 1106);
    }

    public static boolean isRunningInDev() {
    File src = new File("src");
    return src.exists();
  }

  @Override
  public String getDefaultCatalogFileName() {
    return defaultCatalogFileName;
  }

  @Override
  public String getCatalogFileName() {
    return catalogFileName;
  }

  @Override
  public int getRefreshRate() {
    return refreshRate;
  }

  @Override
  public int getRefreshStartHour() {
    return refreshStartHour;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public String getPodcastBase() {
    return podcastBase;
  }

  @Override
  public String getPluginDir() {
    return pluginDir;
  }

  @Override
  public String getCssDir() {
    return cssDir;
  }

  @Override
  public String getConfigDir() {
    return configDir;
  }

  @Override
  public String getTmpDir() {
    return tmpDir;
  }

  @Override
  public String getXsltDir() {
    return xsltDir;
  }

  @Override
  public String getLogDir() {
    return logDir;
  }

  @Override
  public String getRecordingDir() {
    return recordingDir;
  }

  @Override
  public String getOnlineVideoPropertiesSuffix() {
    return onlineVideoPropsSuffix;
  }

  @Override
  public String getOnlineVideoPropertiesDir() {
    return onlineVideoPropertiesDir;
  }

  @Override
  public ArrayList<String> getTestProgrammes(String pluginName) {
    return properties.getPropertySequence(pluginName + ".programmes");
  }

  @Override
  public int getMaxProgrammes(String pluginName) {
    return properties.getInt(pluginName + ".maxprogrammes", 0);
  }

  @Override
  public PropertiesInterface getProperties() {
    return properties;
  }

  @Override
  public boolean skipPlugin(String sourceId) {
    return properties.getBoolean(sourceId + ".skip");
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setRecordingDir(String recordingDir) {
    this.recordingDir = recordingDir;
  }

  @Override
  public int getRefreshStartNowProgrammeThreshold() {
    return refreshStartNowProgrammeThreshold;
  }

  @Override
  public int getCatchupServerRmiPort() {
    return catchupServerRmiPort;
  }

  @Override
  public int getCatchupPluginRmiPort() {
    return catchupPluginRmiPort;
  }

  public void setProperty(String name, String value) {
    properties.setProperty(name, value);
    init(properties);
  }

  @Override
  public String toString() {
    return "CatchupContext{" + "\n" +
            "podcastBase='" + podcastBase + '\'' + "\n" +
            "pluginDir='" + pluginDir + '\'' +"\n" +
            "cssDir='" + cssDir + '\'' +"\n" +
            "configDir='" + configDir + '\'' +"\n" +
            "xsltDir='" + xsltDir + '\'' +"\n" +
            "logDir='" + logDir + '\'' +"\n" +
            "recordingDir='" + recordingDir + '\'' +"\n" +
            "onlineVideoPropertiesDir='" + onlineVideoPropertiesDir + '\'' +"\n" +
            "port=" + port +"\n" +
            "refreshRate=" + refreshRate +"\n" +
            "catalogFileName='" + catalogFileName + '\'' +"\n" +
            "defaultCatalogFileName='" + defaultCatalogFileName + '\'' +"\n" +
            "onlineVideoPropsSuffix='" + onlineVideoPropsSuffix + '\'' +"\n" +
            "catchupServerRmiPort='" + catchupServerRmiPort + '\'' + "\n" +
            "catchupPluginRmiPort='" + catchupPluginRmiPort + '\'' + "\n" +
            "properties=" + properties +"\n" +
            '}';
  }
}
