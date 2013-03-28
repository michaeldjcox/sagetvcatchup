package uk.co.mdjcox.plugin;

import sage.SageTVPlugin;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.logger.LoggingManager;
import uk.co.mdjcox.model.Catalog;
import uk.co.mdjcox.utils.HtmlUtils;
import uk.co.mdjcox.utils.OsUtils;
import uk.co.mdjcox.utils.PropertiesFile;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 01/02/13
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */
public class CatchupPlugin implements SageTVPlugin {

    private Logger logger;
    private PropertiesFile props;
    private OnlineVideoPublisher sagetvPublisher;
    private PodcastServer server;
    private Catalog catalog;

    public CatchupPlugin(sage.SageTVPluginRegistry registry) {
    }

    @Override
    public void start() {
        LoggerInterface logger = LoggingManager.getLogger(PodcastServer.class, "Catchup", "logs");
        LoggingManager.addConsole(logger);

        try {
            props = new PropertiesFile("config" + File.separator + "catchup.properties", true);

            Harvester harvester = new Harvester(logger, props);
            catalog = harvester.refresh();

            server = new PodcastServer(logger, props, HtmlUtils.instance(), OsUtils.instance(logger));
            server.publish(catalog);
            server.start();

            sagetvPublisher = new OnlineVideoPublisher(logger, props, HtmlUtils.instance());
            sagetvPublisher.publish(catalog);

        } catch (Exception e) {
            logger.severe("Failed to start plugin", e);
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
