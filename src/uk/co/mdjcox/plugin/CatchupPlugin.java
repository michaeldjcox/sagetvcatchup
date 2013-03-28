package uk.co.mdjcox.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import sage.SageTVPlugin;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Catalog;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 01/02/13
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */
public class CatchupPlugin implements SageTVPlugin {

    private OnlineVideoPublisher sagetvPublisher;
    private PodcastServer server;
    private Catalog catalog;
    private Harvester harvester;

    public CatchupPlugin(sage.SageTVPluginRegistry registry) {
    }

    @Override
    public void start() {

        try {
            CatchupModule module = new CatchupModule();
            Injector injector = Guice.createInjector(module);

            harvester =  injector.getInstance(Harvester.class);
            server = injector.getInstance(PodcastServer.class); //   (logger, props, HtmlUtils.instance(), OsUtils.instance(logger));
            sagetvPublisher = injector.getInstance(OnlineVideoPublisher.class); // (logger, props, HtmlUtils.instance());

            catalog = harvester.refresh();
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
