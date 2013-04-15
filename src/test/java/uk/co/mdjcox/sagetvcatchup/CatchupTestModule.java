package uk.co.mdjcox.sagetvcatchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.logger.LoggingManager;
import uk.co.mdjcox.sagetvcatchup.plugins.ScriptFactory;
import uk.co.mdjcox.utils.*;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public class CatchupTestModule extends AbstractModule {

    @Mock
    private PropertiesInterface properties;

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
//                .implement(ProgrammesScriptInterface.class, ProgrammesScript.class)
//                .implement(EpisodesScriptInterface.class, EpisodesScript.class)
//                .implement(EpisodeScriptInterface.class, EpisodeScript.class)
                .build(ScriptFactory.class));
        MockitoAnnotations.initMocks(this);
    }

    @Provides
    @Singleton
    public PropertiesInterface providesProperties() throws Exception {
        return properties;
    }

    @Provides
    @Singleton
    public LoggerInterface providesLogger() throws Exception {
        LoggerInterface logger = LoggingManager.getLogger(this.getClass(), "test", "logs");
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
    public OsUtilsInterface providesOsUtlis() throws Exception {
        return OsUtils.instance(providesLogger());
    }

    @Provides
    @Singleton
    public DownloadUtilsInterface providesDownloadUtils() throws Exception {
        return DownloadUtils.instance();
    }

}
