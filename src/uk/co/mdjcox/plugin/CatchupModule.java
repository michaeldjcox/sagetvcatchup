package uk.co.mdjcox.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.logger.LoggingManager;
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

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public PropertiesInterface providesProperties() throws Exception {
        return new PropertiesFile("config" + File.separator + "catchup.properties", true);
    }

    @Provides
    @Singleton
    public LoggerInterface providesLogger() throws Exception {
        LoggerInterface logger = LoggingManager.getLogger(PodcastServer.class, "Catchup", "logs");
        LoggingManager.addConsole(logger);
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
