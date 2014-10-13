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
import uk.co.mdjcox.utils.*;

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

  private static class TestCatchupContext implements CatchupContextInterface {
    private final String workingDir;
    private final String tmpDir;

    public TestCatchupContext() {
      this.tmpDir = System.getProperty("java.io.tmpdir");
      this.workingDir = System.getProperty("user.dir");
    }

    @Override
    public String getDefaultCatalogFileName() {
      return workingDir + "src/main/config/default.xml";
    }

    @Override
    public String getCatalogFileName() {
      return tmpDir + File.separator + "catalog_test.xml";
    }

    @Override
    public int getRefreshRate() {
      return 2;
    }

    @Override
    public int getPort() {
      return 8083;
    }

    @Override
    public String getPodcastBase() {
      return "http://localhost:" + getPort() + "/";
    }

    @Override
    public String getPluginDir() {
      return workingDir + "/src/main/plugins";
    }

    @Override
    public String getCssDir() {
      return workingDir + "/src/main/css";
    }

    @Override
    public String getConfigDir() {
      return workingDir + "/src/main/config";
    }

    @Override
    public String getXsltDir() {
      return workingDir + "/src/main/xslt";
    }

    @Override
    public String getLogDir() {
      return workingDir + "/logs";
    }

    @Override
    public String getRecordingDir() {
      return workingDir + "/recordings";
    }

    @Override
    public String getOnlineVideoPropsSuffix() {
      return "testsagetvcatchup";
    }

    @Override
    public String getOnlineVideoPropertiesDir() {
      return tmpDir + "/TestOnlineVideos";
    }

    @Override
    public ArrayList<String> getTestProgrammes(String pluginName) {
      return new ArrayList<String>();
    }

    @Override
    public int getMaxProgrammes(String pluginName) {
      return Integer.MAX_VALUE;
    }

    @Override
    public PropertiesInterface getProperties() {
      return new PropertiesFile();
    }

    @Override
    public boolean skipPlugin(String sourceId) {
      return false;
    }

    @Override
    public File getSageTVPluginsDevFile() {
      return new File(workingDir, "SageTVPluginsDev.xml");
    }

    @Override
    public String getSageTVPluginsURL() {
      return "http://mintpad/sagetvcatchup/download/SageTVPluginsDev.xml";
    }
  }
}
