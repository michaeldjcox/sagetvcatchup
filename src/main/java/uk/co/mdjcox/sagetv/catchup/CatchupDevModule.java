package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import uk.co.mdjcox.utils.Logger;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginFactory;
import uk.co.mdjcox.sagetv.catchup.plugins.ScriptFactory;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.*;

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
    private Logger logger;
    private String workingDir;

    @Override
    protected void configure() {
        workingDir = System.getProperty("user.dir") + File.separator;
        install(new FactoryModuleBuilder()
                .build(ScriptFactory.class));
        install(new FactoryModuleBuilder()
                .build(PluginFactory.class));
    }

    @Provides
    @Singleton
    public Logger providesLogger() throws Exception {
      if (logger == null) {
        System.setProperty("logback.configurationFile", workingDir + "src/main/config/logback-test.xml");
        logger = new Logger(CatchupPlugin.class);
      }
      return logger;
    }

  @Provides
  @Named("BackupPropsFile")
  public String providesBackupPropsFileName() {
    return workingDir + "test/tmp/sagetvcatchup-dev.props.backup";
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

    @Provides
    @Singleton
    public SageUtilsInterface providesSageUtils() throws Exception {
        return new SageUtilsInterface() {
            @Override
            public String getSageTVProperty(String property, String defaultValue) throws Exception {
                return "";
            }

            @Override
            public String[] findTitlesWithName(String regex) {
                return new String[0];
            }

            @Override
            public Object[] findAiringsByText(String name) {
                return new Object[0];
            }

            @Override
            public String printAiring(Object airing) {
                return "";
            }

            @Override
            public Object findShowForAiring(Object airing) {
                return null;
            }

            @Override
            public String printShow(Object show) {
                return "";
            }

            @Override
            public Object addAiringToSageTV(Recording recording) {
                return null;
            }

            @Override
            public File[] getRecordingDirectories() {
                return new File[0];
            }

            @Override
            public void setClientProperty(String name, String value) {

            }
        };


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
    private int port;
    private int refreshRate;
    private String catalogFileName;
    private String defaultCatalogFileName;
    private String onlineVideoPropsSuffix;
    private File sageTvPluginsFile;
    private String sageTVPluginsURL;
    private PropertiesInterface properties;

    public DevCatchupContext(PropertiesInterface properties) {
      String homeDir = System.getProperty("user.dir") + File.separator;
      this.workingDir = homeDir + "test" + File.separator;
      this.tmpDir = workingDir + "tmp" + File.separator;
      defaultCatalogFileName = homeDir + "src/main/seeds/default.xml";
      catalogFileName = tmpDir + "catalog_dev.xml";
      refreshRate = 2;
      port = properties.getInt("podcasterPort", 8082);
      podcastBase = "http://localhost:" + getPort();
      pluginDir = homeDir + "src/main/plugins";
      cssDir = homeDir + "src/main/css";
      xsltDir = homeDir + "src/main/xslt";
      logDir = workingDir + "logs";
      recordingDir = workingDir + "recordings";
      onlineVideoPropsSuffix = "sagetvcatchup";
      onlineVideoPropertiesDir = "/opt/sagetv/server/STVs/SageTV7/OnlineVideos";
      sageTvPluginsFile = new File(workingDir, "SageTVPluginsDev.xml");
      sageTVPluginsURL = "http://mintpad/sagetvcatchup/download/SageTVPluginsDev.xml";
      this.properties = properties;
      configDir = workingDir + "config";
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

    public void setRecordingDir(String recordingDir) {
      this.recordingDir = recordingDir;
    }

    public void setPort(int port) {
      this.port = port;
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
              "sageTvPluginsFile=" + sageTvPluginsFile + "\n" +
              "sageTVPluginsURL='" + sageTVPluginsURL + '\'' + "\n" +
              "properties=" + properties + "\n" +
              '}';
    }
  }
}
