package uk.co.mdjcox.catchup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import sage.SageTVPlugin;
import uk.co.mdjcox.model.Catalog;
import uk.co.mdjcox.catchup.plugins.PluginManager;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 01/02/13
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */
public class CatchupPlugin implements SageTVPlugin {

    public static Injector injector;

//    private Publisher sagetvPublisher;
//    private PodcastServer server;
//    private Catalog catalog;
//    private Cataloger harvester;
//    private PluginManager pluginManager;

    public CatchupPlugin(sage.SageTVPluginRegistry registry) {
    }

    @Override
    public void start() {

        try {
            CatchupModule module = new CatchupModule();
            injector = Guice.createInjector(module);

            PluginManager pluginManager = injector.getInstance(PluginManager.class);
            Cataloger harvester = injector.getInstance(Cataloger.class);
            PodcastServer server = injector.getInstance(PodcastServer.class); //   (logger, props, HtmlUtils.instance(), OsUtils.instance(logger));
            Publisher sagetvPublisher = injector.getInstance(Publisher.class); // (logger, props, HtmlUtils.instance());

            pluginManager.load();
            Catalog catalog = harvester.catalog();
            server.publish(catalog);
            server.start();
            sagetvPublisher.publish(catalog);

        } catch (Exception e) {
            System.err.println("Failed to start plugin");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String[] getConfigSettings() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getConfigValue(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String[] getConfigValues(String s) {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getConfigType(String s) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setConfigValue(String s, String s1) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setConfigValues(String s, String[] strings) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String[] getConfigOptions(String s) {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getConfigHelpText(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getConfigLabel(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resetConfig() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sageEvent(String s, Map map) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void main(String[] args) {
        CatchupPlugin plugin = new CatchupPlugin(null);
        plugin.start();
    }
}
