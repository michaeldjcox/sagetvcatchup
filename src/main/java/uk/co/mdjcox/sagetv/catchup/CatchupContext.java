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

  private static boolean runningInSageTV;

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
  private File sageTvPluginsFile;
  private String sageTVPluginsURL;
  private PropertiesInterface properties;

  public CatchupContext(final PropertiesInterface properties) {
    this.properties = properties;

    xsltDir = workingDir + File.separator + "sagetvcatchup" + File.separator + "xslt";
    cssDir = workingDir + File.separator + "sagetvcatchup" + File.separator + "css";
    logDir = workingDir + File.separator + "sagetvcatchup" + File.separator + "logs";
    pluginDir = workingDir + File.separator + "sagetvcatchup" + File.separator + "plugins";
    configDir = workingDir + File.separator + "sagetvcatchup" + File.separator + "config";

    recordingDir = properties.getString("recordingDir");

    refreshRate = properties.getInt("refreshRateHours");
    port = properties.getInt("podcasterPort");

    String defaultFileName = System.getProperty("java.io.tmpdir", ".") + File.separator + "sagetvcatchup.xml";
    catalogFileName = properties.getString("catalogFileName", defaultFileName);
    defaultCatalogFileName = configDir + File.separator + "default.xml";
    onlineVideoPropsSuffix = properties.getString("onlineVideoPropsSuffix");
    onlineVideoPropertiesDir = properties.getString("onlineVideoPropertiesDir");
    sageTvPluginsFile = new File(workingDir, "SageTVPluginsDev.xml");
    sageTVPluginsURL = properties.getString("sageTvPluginsURL", "http://mintpad/sagetvcatchup/download/SageTVPluginsDev.xml");
  }

  public static boolean isRunningInSageTV() {
    return !(workingDir.startsWith(userHome) && workingDir.endsWith("sagetvcatchup"));
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
  public int getPort() {
    return port;
  }

  @Override
  public String getPodcastBase() {
    return "http://localhost:" + port + "/";
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
  public String getOnlineVideoPropsSuffix() {
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
    return properties.getInt(pluginName + ".maxprogrammes", Integer.MAX_VALUE);
  }

  @Override
  public PropertiesInterface getProperties() {
    return properties;
  }

  @Override
  public boolean skipPlugin(String sourceId) {
    return properties.getBoolean(sourceId + ".skip");
  }

  @Override
  public File getSageTVPluginsDevFile() {
    return sageTvPluginsFile;
  }

  @Override
  public String getSageTVPluginsURL() {
    return sageTVPluginsURL;
  }
}
