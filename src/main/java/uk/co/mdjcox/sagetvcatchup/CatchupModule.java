package uk.co.mdjcox.sagetvcatchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.logger.LoggingManager;
import uk.co.mdjcox.sagetvcatchup.plugins.PluginFactory;
import uk.co.mdjcox.sagetvcatchup.plugins.ScriptFactory;
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
    @Singleton
    public PropertiesInterface providesProperties() throws Exception {
        if (properties == null) {
            String base =  System.getProperty("user.dir") + File.separator + "sagetvcatchup" + File.separator + "config" + File.separator + "catchup.properties";
            properties = new PropertiesFile(base, true);
        }
        return properties;
    }

    @Provides
    @Singleton
    public LoggerInterface providesLogger() throws Exception {
        PropertiesInterface props = providesProperties();
        if (logger == null) {
            logger = LoggingManager.getLogger(CatchupPlugin.class, "sagetvcatchup", props.getProperty("logDir", "/tmp/logs"));
        }
        return logger;
    }

    @Provides
    @Singleton
    public HtmlUtilsInterface providesHtmlUtls() throws Exception {
        return HtmlUtils.instance();
    }

    @Provides
    @Singleton
    public OsUtils providesOsUtlis() throws Exception {
        return OsUtils.instance(providesLogger());
    }

    @Provides
    @Singleton
    public DownloadUtilsInterface providesDownloadUtils() throws Exception {
        return DownloadUtils.instance();
    }

}
