package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import uk.co.mdjcox.logger.Logger;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginFactory;
import uk.co.mdjcox.sagetv.catchup.plugins.ScriptFactory;
import uk.co.mdjcox.utils.*;

import java.io.File;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public class CatchupModule extends AbstractModule {

    private PropertiesFile properties;
    private Logger logger;

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .build(ScriptFactory.class));
        install(new FactoryModuleBuilder()
                .build(PluginFactory.class));
    }

  @Provides
  @Named("BackupPropsFile")
  public String providesBackupPropsFileName() {
    return System.getProperty("java.io.tmpdir", ".") + File.separator + "sagetvcatchup.props.backup";
  }

    @Provides
    @Named("SeedPropsFile")
    public String providesSeedPropsFileName(OsUtilsInterface osUtils) {
      String base =  System.getProperty("user.dir") + File.separator + "sagetvcatchup" + File.separator;
      String propFileName = "sagetvcatchup.unix.properties";
      if (osUtils.isWindows()) {
        propFileName = "sagetvcatchup.windows.properties";
      } else {
        propFileName = "sagetvcatchup.unix.properties";
      }
      propFileName = base + "seeds" + File.separator + propFileName;
      return propFileName;
    }

  @Provides
  @Named("PropsFile")
  public String providesPropsFileName() {
    String propFileName = "sagetvcatchup.properties";
    String base =  System.getProperty("user.dir") + File.separator + "sagetvcatchup" + File.separator;
    propFileName = base + "config" + File.separator + propFileName;
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
  public CatchupContextInterface providesCatchupContext(PropertiesInterface properties) {
    return new CatchupContext(properties);
  }

    @Provides
    @Singleton
    public Logger providesLogger() throws Exception {
        if (logger == null) {
          String base =  System.getProperty("user.dir") + File.separator + "sagetvcatchup" + File.separator;
          System.setProperty("logback.configurationFile", base + "config" + File.separator + "logback.xml");
          logger = new Logger(CatchupPlugin.class);
        }
        return logger;
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

}
