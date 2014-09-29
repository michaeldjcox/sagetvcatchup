package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger logger;

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .build(ScriptFactory.class));
        install(new FactoryModuleBuilder()
                .build(PluginFactory.class));
    }

    @Provides
    @Singleton
    public PropertiesInterface providesProperties(OsUtilsInterface osUtils) throws Exception {
        if (properties == null) {
          String propFileName = "sagetvcatchup.unix.properties";
          if (osUtils.isWindows()) {
            propFileName = "sagetvcatchup.windows.properties";
          } else {
            propFileName = "sagetvcatchup.unix.properties";
          }
          String base =  System.getProperty("user.dir") + File.separator + "sagetvcatchup" + File.separator;
            String props = base + "config" + File.separator + propFileName;
            properties = new PropertiesFile(props, true);
            String workingDir = System.getProperty("user.dir");
            properties.setProperty("logDir", workingDir + File.separator + "sagetvcatchup" + File.separator + "logs");
        }
        return properties;
    }

    @Provides
    @Singleton
    public Logger providesLogger() throws Exception {
        if (logger == null) {
          String base =  System.getProperty("user.dir") + File.separator + "sagetvcatchup" + File.separator;
          System.setProperty("logback.configurationFile", base + "config" + File.separator + "logback.xml");
          logger = LoggerFactory.getLogger(CatchupPlugin.class);
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
