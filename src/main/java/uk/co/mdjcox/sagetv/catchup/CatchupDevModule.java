package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        workingDir = System.getProperty("user.dir");
        install(new FactoryModuleBuilder()
                .build(ScriptFactory.class));
        install(new FactoryModuleBuilder()
                .build(PluginFactory.class));
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
  @Named("BackupPropsFile")
  public String providesBackupPropsFileName() {
    return System.getProperty("java.io.tmpdir", ".") + File.separator + "sagetvcatchup-dev.props.backup";
  }

  @Provides
  @Named("SeedPropsFile")
  public String providesSeedPropsFileName(OsUtilsInterface osUtils) {
    String propFileName = "sagetvcatchup.unix.properties";
    if (osUtils.isWindows()) {
      propFileName = "sagetvcatchup.windows.properties";
    } else {
      propFileName = "sagetvcatchup.unix.properties";
    }
    propFileName = "src/main/config/" + propFileName;
    return propFileName;
  }

  @Provides
  @Named("PropsFile")
  public String providesPropsFileName() {
    String propFileName = "sagetvcatchup.properties";
    propFileName = "config" + File.separator + propFileName;
    return propFileName;
  }

  @Provides
  @Named("PropsFile")
  public String providesPropsFileName(OsUtilsInterface osUtils) {
    String propFileName = "sagetvcatchup.unix.properties";
    if (osUtils.isWindows()) {
      propFileName = "sagetvcatchup.windows.properties";
    } else {
      propFileName = "sagetvcatchup.unix.properties";
    }
    String base = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "config" + File.separator;
    propFileName = base + propFileName;

    return propFileName;
  }

    @Provides
    @Singleton
    public PropertiesInterface providesProperties(@Named("SeedPropsFile") String seedFileName, @Named("BackupPropsFile") String backupFileName, @Named("PropsFile") String propFileName ) throws Exception {
      if (properties == null) {
        File backupFile = new File(backupFileName);
        File runFile = new File(propFileName);

        if (runFile.exists()) {
          // We have a file
          logger.info("Properties - reusing existing " + propFileName);
          return new PropertiesFile(propFileName, true);
        } else {
          logger.info("Properties - loading seed properties from " + seedFileName);

          // We have no file
          PropertiesFile seed = new PropertiesFile(seedFileName, true);

          String workingDir = System.getProperty("user.dir");
          String recordingDir = workingDir + File.separator + "recordings";
          seed.put("recordingDir", recordingDir);

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

        properties = new PropertiesFile(propFileName, true);
      }
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
    private final PropertiesInterface properties;

    public DevCatchupContext(PropertiesInterface properties) {
      tmpDir = System.getProperty("java.io.tmpdir");
      workingDir = System.getProperty("user.dir");
      this.properties = properties;
    }

    @Override
    public String getDefaultCatalogFileName() {
      return workingDir + "src/main/config/default.xml";
    }

    @Override
    public String getCatalogFileName() {
      return tmpDir + File.separator + "catalog_dev.xml";
    }

    @Override
    public int getRefreshRate() {
      return 2;
    }

    @Override
    public int getPort() {
      return 8082;
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
      return "sagetvcatchup";
    }

    @Override
    public String getOnlineVideoPropertiesDir() {
      return "/opt/sagetv/server/STVs/SageTV7/OnlineVideos";
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
      return new File(workingDir, "SageTVPluginsDev.xml");
    }

    @Override
    public String getSageTVPluginsURL() {
      return "http://mintpad/sagetvcatchup/download/SageTVPluginsDev.xml";
    }
  }
}
