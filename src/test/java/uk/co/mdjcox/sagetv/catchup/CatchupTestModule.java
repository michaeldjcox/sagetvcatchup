package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.mockito.MockitoAnnotations;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginFactory;
import uk.co.mdjcox.sagetv.catchup.plugins.ScriptFactory;
import uk.co.mdjcox.sagetv.utils.*;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public class CatchupTestModule extends AbstractModule {

  private LoggerInterface logger;

    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
                  .build(ScriptFactory.class));
      install(new FactoryModuleBuilder()
                  .build(PluginFactory.class));
        MockitoAnnotations.initMocks(this);
    }

    @Provides
    @Singleton
    public LoggerInterface providesLogger() throws Exception {
      if (logger == null) {
        System.setProperty("logback.configurationFile", "/home/michael/Documents/Projects/sagetvcatchup/src/main/config/logback-test.xml");
        logger = new Logger(CatchupServer.class);
      }
        return logger;
    }

  @Provides
  @Singleton
  public CatchupContextInterface providesCatchupContext() {
    return new TestCatchupContext();
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

  public static class TestCatchupContext implements CatchupContextInterface {
    private final String workingDir;
    private final String tmpDir;
    private String podcastBase;
    private String pluginDir;
    private String cssDir;
    private String configDir;
    private String xsltDir;
    private String logDir;
    private String recordingDir;
    private int port;
    private int refreshRate;
    private String catalogFileName;
    private String defaultCatalogFileName;
    private String onlineVideoPropsSuffix;
    private String onlineVideoPropertiesDir;
    private PropertiesInterface properties;
    private int catchupServerRmiPort;
    private int catchupPluginRmiPort;

    public TestCatchupContext() {
      String homeDir = System.getProperty("user.dir") + File.separator;
      this.workingDir = homeDir + "test" + File.separator;
      this.tmpDir = workingDir + "tmp" + File.separator;
      defaultCatalogFileName = homeDir + "src/main/seeds/default.xml";
      catalogFileName = tmpDir + "catalog_test.xml";
      refreshRate = 2;
      port = 8083;
      podcastBase = "http://localhost:" + getPort();
      pluginDir = homeDir + "src/main/plugins";
      cssDir = homeDir + "src/main/css";
      configDir = homeDir + "src/main/config";
      xsltDir = homeDir + "src/main/xslt";
      logDir = workingDir + "logs";
      recordingDir = workingDir + "recordings";
      onlineVideoPropsSuffix = "testsagetvcatchup";
      onlineVideoPropertiesDir = tmpDir + "TestOnlineVideos";
      properties = new PropertiesFile();
      catchupPluginRmiPort = properties.getInt("catchupPluginRmiPort", 1105);
      catchupServerRmiPort = properties.getInt("catchupServerRmiPort", 1106);
    }

      @Override
      public void setProperty(String name, String value) {
          throw new UnsupportedOperationException("Cannot set properties in test context");
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
      return 10000;
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

    public void setOnlineVideoPropertiesSuffix(String onlineVideoPropsSuffix) {
      this.onlineVideoPropsSuffix = onlineVideoPropsSuffix;
    }

    public void setOnlineVideoPropertiesDir(String onlineVideoPropertiesDir) {
      this.onlineVideoPropertiesDir = onlineVideoPropertiesDir;
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
    public boolean getShowRoot(String pluginName) {
      return properties.getBoolean(pluginName + ".showRoot", true);
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
      return "TestCatchupContext{" +
              "workingDir='" + workingDir + '\'' +
              ", tmpDir='" + tmpDir + '\'' +
              ", podcastBase='" + podcastBase + '\'' +
              ", pluginDir='" + pluginDir + '\'' +
              ", cssDir='" + cssDir + '\'' +
              ", configDir='" + configDir + '\'' +
              ", xsltDir='" + xsltDir + '\'' +
              ", logDir='" + logDir + '\'' +
              ", recordingDir='" + recordingDir + '\'' +
              ", port=" + port +
              ", refreshRate=" + refreshRate +
              ", catalogFileName='" + catalogFileName + '\'' +
              ", defaultCatalogFileName='" + defaultCatalogFileName + '\'' +
              ", onlineVideoPropsSuffix='" + onlineVideoPropsSuffix + '\'' +
              ", onlineVideoPropertiesDir='" + onlineVideoPropertiesDir + '\'' +
              ", catchupServerRmiPort='" + catchupServerRmiPort + '\'' +
              ", catchupPluginRmiPort='" + catchupPluginRmiPort + '\'' +
              '}';
    }
  }
}
