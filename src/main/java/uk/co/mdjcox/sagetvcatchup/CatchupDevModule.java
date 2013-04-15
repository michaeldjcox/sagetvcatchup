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
 * Date: 11/04/13
 * Time: 08:09
 * To change this template use File | Settings | File Templates.
 */
public class CatchupDevModule extends AbstractModule {

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
    public LoggerInterface providesLogger() throws Exception {
        PropertiesInterface props = providesProperties();
        if (logger == null) {
            logger = LoggingManager.getLogger(CatchupPlugin.class, "sagetvcatchup", props.getProperty("logDir", "/tmp/logs"));
            LoggingManager.addConsole(logger);
        }
        return logger;
    }

    @Provides
    @Singleton
    public PropertiesInterface providesProperties() throws Exception {
        String base = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "config" + File.separator + "catchup.properties";
        if (properties == null) {
            properties =  new PropertiesFile(base, true);
            properties.setProperty("recordingDir", "/home/michael/Documents/sagetvcatchup/recordings");
            properties.setProperty("pluginDir", "/home/michael/Documents/sagetvcatchup/src/main/plugins");
            properties.setProperty("logDir", "/home/michael/Documents/sagetvcatchup/logs");
            properties.setProperty("podcasterPort", "8082");
        }
        return properties;
    }

    @Provides
    @Singleton
    public HtmlUtilsInterface providesHtmlUtls() throws Exception {
        return HtmlUtils.instance();
    }

    @Provides
    @Singleton
    public OsUtilsInterface providesOsUtlis() throws Exception {
        return OsUtils.instance(providesLogger());
    }

    @Provides
    @Singleton
    public DownloadUtilsInterface providesDownloadUtils() throws Exception {
        return DownloadUtils.instance();
    }

}
