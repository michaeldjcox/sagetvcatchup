package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginFactory;
import uk.co.mdjcox.sagetv.catchup.plugins.ScriptFactory;
import uk.co.mdjcox.sagetv.catchup.server.Server;
import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.onlinevideo.SageTvPublisher;
import uk.co.mdjcox.utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public class CatchupTestModule extends AbstractModule {

  private Logger logger;

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
    public Logger providesLogger() throws Exception {
      if (logger == null) {
        System.setProperty("logback.configurationFile", "/home/michael/Documents/Projects/sagetvcatchup/src/main/config/logback-test.xml");
        logger = LoggerFactory.getLogger(CatchupPlugin.class);
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

    @Provides
    @Singleton
    public SageUtilsInterface providesSageUtils() throws Exception {
        return SageUtils.instance(providesLogger());
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
    private File sageTvPluginsFile;
    private String sageTVPluginsURL;
    private PropertiesInterface properties;

    public TestCatchupContext() {
      this.tmpDir = System.getProperty("java.io.tmpdir");
      this.workingDir = System.getProperty("user.dir");
      defaultCatalogFileName = workingDir + "src/main/seeds/default.xml";
      catalogFileName = tmpDir + File.separator + "catalog_test.xml";
      refreshRate = 2;
      port = 8083;
      podcastBase = "http://localhost:" + getPort() + "/";
      pluginDir = workingDir + "/src/main/plugins";
      cssDir = workingDir + "/src/main/css";
      configDir = workingDir + "/src/main/config";
      xsltDir = workingDir + "/src/main/xslt";
      logDir = workingDir + "/logs";
      recordingDir = workingDir + "/recordings";
      onlineVideoPropsSuffix = "testsagetvcatchup";
      onlineVideoPropertiesDir = tmpDir + "/TestOnlineVideos";
      sageTvPluginsFile = new File(workingDir, "SageTVPluginsDev.xml");
      sageTVPluginsURL = "http://mintpad/sagetvcatchup/download/SageTVPluginsDev.xml";
      properties = new PropertiesFile();
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
              ", sageTvPluginsFile=" + sageTvPluginsFile +
              ", sageTVPluginsURL='" + sageTVPluginsURL + '\'' +
              ", properties=" + properties +
              '}';
    }
  }
}
