package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.mdjcox.sagetv.catchup.plugins.PluginFactory;
import uk.co.mdjcox.sagetv.catchup.plugins.ScriptFactory;
import uk.co.mdjcox.sagetv.onlinevideo.PublisherFactory;
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
    private Logger logger;
    private String workingDir;

    @Override
    protected void configure() {
        workingDir = System.getProperty("user.dir");
        install(new FactoryModuleBuilder()
                .build(ScriptFactory.class));
        install(new FactoryModuleBuilder()
                .build(PluginFactory.class));
      install(new FactoryModuleBuilder()
                  .build(PublisherFactory.class));
    }

    @Provides
    @Singleton
    public Logger providesLogger() throws Exception {
        if (logger == null) {
          System.setProperty("logback.configurationFile", workingDir + "/src/main/config/logback-test.xml");
            logger = LoggerFactory.getLogger(CatchupPlugin.class);
        }
        return logger;
    }

    @Provides
    @Singleton
    public PropertiesInterface providesProperties() throws Exception {
        String base = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "config" + File.separator + "catchup.properties";
        if (properties == null) {
            properties =  new PropertiesFile(base, true);
            properties.setProperty("recordingDir", workingDir + "/recordings");
            properties.setProperty("pluginDir", workingDir + "/src/main/plugins");
            properties.setProperty("podcasterPort", "8082");
        }
        return properties;
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