package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginFactory;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginUpnpFactory;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginUpnpLiteFactory;
import uk.co.mdjcox.sagetv.catchup.plugins.ScriptFactory;
import uk.co.mdjcox.sagetv.utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 11/04/13
 * Time: 08:09
 * To change this template use File | Settings | File Templates.
 */
public class CatchupDevModule extends AbstractModule {

  private PropertiesFile properties;
    private LoggerInterface logger;
    private String workingDir;

    @Override
    protected void configure() {
      workingDir = System.getProperty("user.dir") + File.separator;
      install(new FactoryModuleBuilder()
              .build(ScriptFactory.class));
      install(new FactoryModuleBuilder()
              .build(PluginFactory.class));
      install(new FactoryModuleBuilder()
              .build(PluginUpnpLiteFactory.class));
    }

    @Provides
    @Singleton
    public LoggerInterface providesLogger() throws Exception {
      if (logger == null) {
        System.setProperty("logback.configurationFile", workingDir + "src/main/config/logback-test.xml");
        logger = new Logger(CatchupServer.class);
      }
      return logger;
    }

  @Provides
  @Named("BackupPropsFile")
  public String providesBackupPropsFileName() {
    return workingDir + "test/tmp/sagetvcatchup-dev.properties.backup";
  }

  @Provides
  @Named("SeedPropsFile")
  public String providesSeedPropsFileName(OsUtilsInterface osUtils) {
    String propFileName = "src/main/seeds/sagetvcatchup.unix.properties";
    if (osUtils.isWindows()) {
      propFileName = "src/main/seeds/sagetvcatchup.windows.properties";
    } else {
      propFileName = "src/main/seeds/sagetvcatchup.unix.properties";
    }
    return propFileName;
  }

  @Provides
  @Named("PropsFile")
  public String providesPropsFileName() {
    String propFileName = "sagetvcatchup.properties";
    propFileName = workingDir + "test" + File.separator + "config" + File.separator + propFileName;
    return propFileName;
  }


    @Provides
    @Singleton
    public PropertiesInterface providesProperties(@Named("SeedPropsFile") String seedFileName, @Named("BackupPropsFile") String backupFileName, @Named("PropsFile") String propFileName ) throws Exception {
      if (properties == null) {
        File backupFile = new File(backupFileName);
        File runFile = new File(propFileName);

        if (!runFile.exists()) {
          logger.info("Properties - loading seed properties from " + seedFileName);

          // We have no file
          PropertiesFile seed = new PropertiesFile(seedFileName, true);

          if (backupFile.exists()) {
            logger.info("Properties - applying backup properties " + seedFileName);

            // We can reuse an old one
            PropertiesFile backup = new PropertiesFile(backupFileName, true);
            for (Map.Entry<Object, Object> entry : backup.entrySet()) {
              seed.put(entry.getKey(), entry.getValue());
            }
          }

          seed.commit(propFileName, new CatchupPropertiesFileLayout());
        }

      } else {
        // We have a file
        logger.info("Properties - reusing existing " + propFileName);
      }

      String recordingDir = workingDir + File.separator + "test" + File.separator + "recordings";

      properties = new PropertiesFile(propFileName, true);
      properties.put("recordingDir", recordingDir);
      properties.put("podcasterPort", "8082");

      return properties;
    }

  @Provides
  @Singleton
  public CatchupContextInterface providesCatchupContext(final PropertiesInterface properties) {
    return new DevCatchupContext(properties);
  }

  @Provides
    @Singleton
    public HtmlUtilsInterface providesHtmlUtils() throws Exception {
        return HtmlUtils.instance();
    }

    @Provides
    @Singleton
    public OsUtilsInterface providesOsUtils() throws Exception {
        return OsUtils.instance(providesLogger());
    }

    @Provides
    @Singleton
    public DownloadUtilsInterface providesDownloadUtils() throws Exception {
        return DownloadUtils.instance();
    }

  public static class DevCatchupContext implements CatchupContextInterface {
    private final String tmpDir;
    private final String workingDir;
    private String podcastBase;
    private String pluginDir;
    private String cssDir;
    private String configDir;
    private String xsltDir;
    private String logDir;
    private String recordingDir;
    private String onlineVideoPropertiesDir;
    private String imageDir;
    private int port;
    private int refreshRate;
    private String catalogFileName;
    private String defaultCatalogFileName;
    private String onlineVideoPropsSuffix;
    private PropertiesInterface properties;
    private int catchupServerRmiPort;
    private int catchupPluginRmiPort;

    public DevCatchupContext(PropertiesInterface properties) {
      String homeDir = System.getProperty("user.dir") + File.separator;
      this.workingDir = homeDir + "test" + File.separator;
      this.tmpDir = workingDir + "tmp" + File.separator;
      defaultCatalogFileName = homeDir + "src" +File.separator+"main"+File.separator+"seeds"+File.separator+"default.xml";
      catalogFileName = tmpDir + "catalog_dev.xml";
      refreshRate = 2;
      port = properties.getInt("podcasterPort", 8082);
      podcastBase = "http://localhost:" + getPort();
      pluginDir = homeDir + "src"+File.separator+"main"+File.separator+"plugins";
      cssDir = homeDir + "src"+File.separator+"main"+File.separator+"css";
      xsltDir = homeDir + "src"+File.separator+"main"+File.separator+"xslt";
      imageDir = homeDir + "src"+File.separator+"main"+File.separator+"images";
      logDir = workingDir + "logs";
      recordingDir = workingDir + "recordings";
      onlineVideoPropsSuffix = "sagetvcatchup";
      onlineVideoPropertiesDir = "/opt/sagetv/server/STVs/SageTV7/OnlineVideos";
      this.properties = properties;
      configDir = workingDir + "config";
      catchupPluginRmiPort = properties.getInt("catchupPluginRmiPort", 1105);
      catchupServerRmiPort = properties.getInt("catchupServerRmiPort", 1106);

    }

      @Override
      public void setProperty(String name, String value) {
          throw new UnsupportedOperationException("Cannot set properties in dev context");
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
      return 2;
    }

    @Override
    public int getRefreshStartNowProgrammeThreshold() {
      return 1;
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
    public String getImageDir() {
      return imageDir;
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

    public boolean isStreamOnly(String sourceId) {
      return properties.getBoolean(sourceId+".streamonly", false);
    }

    @Override
    public boolean isStreamable(String sourceId) {
      return properties.getBoolean(sourceId+".streamble", false);
    }

    @Override
    public PropertiesInterface getProperties() {
      return properties;
    }

    @Override
    public boolean skipPlugin(String sourceId) {
      return properties.getBoolean(sourceId + ".skip");
    }

    public void setRecordingDir(String recordingDir) {
      this.recordingDir = recordingDir;
    }

    public void setPort(int port) {
      this.port = port;
    }

    @Override
    public int getCatchupServerRmiPort() {
      return catchupServerRmiPort;
    }

    @Override
    public int getCatchupPluginRmiPort() {
      return catchupPluginRmiPort;
    }

    @Override
    public long getPartialSizeForStreamingTimeout() {
      return 60000;
    }

    @Override
    public long getPartialSizeForStreaming() {
      return 1024000;
    }

    @Override
    public long getPartialFileNameConfirmationTimeout() {
      return 60000;
    }


    @Override
    public long getStreamingTimeout() {
      return 30000;
    }

    @Override
    public long getRecordingTimeout() {
      return 30000;
    }

    @Override
    public String toString() {
      return "DevCatchupContext{" +"\n" +
              "tmpDir='" + tmpDir + '\'' + "\n" +
              "workingDir='" + workingDir + '\'' + "\n" +
              "podcastBase='" + podcastBase + '\'' + "\n" +
              "pluginDir='" + pluginDir + '\'' + "\n" +
              "cssDir='" + cssDir + '\'' + "\n" +
              "configDir='" + configDir + '\'' + "\n" +
              "xsltDir='" + xsltDir + '\'' + "\n" +
              "logDir='" + logDir + '\'' + "\n" +
              "recordingDir='" + recordingDir + '\'' + "\n" +
              "onlineVideoPropertiesDir='" + onlineVideoPropertiesDir + '\'' + "\n" +
              "port=" + port + "\n" +
              "refreshRate=" + refreshRate + "\n" +
              "catalogFileName='" + catalogFileName + '\'' + "\n" +
              "defaultCatalogFileName='" + defaultCatalogFileName + '\'' + "\n" +
              "onlineVideoPropsSuffix='" + onlineVideoPropsSuffix + '\'' + "\n" +
              "catchupServerRmiPort='" + catchupServerRmiPort + '\'' + "\n" +
              "catchupPluginRmiPort='" + catchupPluginRmiPort + '\'' + "\n" +
              '}';
    }
  }
}
