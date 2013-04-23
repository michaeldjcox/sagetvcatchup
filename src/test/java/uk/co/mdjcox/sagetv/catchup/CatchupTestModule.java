package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.mdjcox.sagetv.catchup.plugins.ScriptFactory;
import uk.co.mdjcox.utils.*;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public class CatchupTestModule extends AbstractModule {

  private Logger logger;

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
    public Logger providesLogger() throws Exception {
      if (logger == null) {
        System.setProperty("logback.configurationFile", "/home/michael/Documents/catchup/src/main/config/logback-test.xml");
        logger = LoggerFactory.getLogger(this.getClass());
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
    public OsUtilsInterface providesOsUtlis() throws Exception {
        return OsUtils.instance(providesLogger());
    }

    @Provides
    @Singleton
    public DownloadUtilsInterface providesDownloadUtils() throws Exception {
        return DownloadUtils.instance();
    }

}
