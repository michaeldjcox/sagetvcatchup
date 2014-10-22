package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginFactory;
import uk.co.mdjcox.sagetv.catchup.plugins.ScriptFactory;
import uk.co.mdjcox.utils.*;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public class CatchupModule extends AbstractModule {

    private PropertiesFile properties;
    private LoggerInterface logger;

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .build(ScriptFactory.class));
        install(new FactoryModuleBuilder()
                .build(PluginFactory.class));
    }

  @Provides
  @Named("PropsFile")
  public String providesPropsFileName() {
    String propFileName = "sagetvcatchup.properties";
    String base =  System.getProperty("user.dir") + File.separator;
    propFileName = base + "config" + File.separator + propFileName;
    return propFileName;
  }

    @Provides
    @Singleton
    public PropertiesInterface providesProperties(@Named("PropsFile") String propFileName ) throws Exception {
        if (properties == null) {
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
    public LoggerInterface providesLogger() throws Exception {
        if (logger == null) {
          String base =  System.getProperty("user.dir") + File.separator;
          System.setProperty("logback.configurationFile", base + "config" + File.separator + "logback.xml");
          logger = new Logger(CatchupServer.class);
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
}
