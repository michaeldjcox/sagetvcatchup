package uk.co.mdjcox.sagetvcatchup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import sage.SageTVPlugin;
import sage.SageTVPluginRegistry;
import uk.co.mdjcox.logger.Logger;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.logger.LoggingManager;
import uk.co.mdjcox.model.Catalog;
import uk.co.mdjcox.sagetvcatchup.plugins.PluginManager;

import java.io.File;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 01/02/13
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */

// TODO - some issues parsing content meta-data
// TODO - check video from other providers
// TODO - how to stop playback and resume - how to tidy away broken fragments
// TODO - why does sagetv giveup
// TODO - parameter for how much download before playback

public class CatchupPlugin implements SageTVPlugin {

    public static LoggerInterface logger;
    public static Injector injector;

    private SageTVPluginRegistry registry;
    private PodcastServer server;

    private ScheduledExecutorService service;
    private Publisher sagetvPublisher;

    public CatchupPlugin(sage.SageTVPluginRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void start() {

        try {
            service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "sagetvcatchup-scheduler");
                }
            });

            CatchupModule module = new CatchupModule();
            logger = module.providesLogger();

            logger.info("Starting sagetvcatchup plugin");

            registry.eventSubscribe(this, "PlaybackStopped");
            registry.eventSubscribe(this, "PlaybackStarted");
            registry.eventSubscribe(this, "PlaybackFinished");

            injector = Guice.createInjector(module);

            PluginManager pluginManager = injector.getInstance(PluginManager.class);
            final Cataloger harvester = injector.getInstance(Cataloger.class);
            server = injector.getInstance(PodcastServer.class);
            sagetvPublisher = injector.getInstance(Publisher.class);

            pluginManager.load();
            server.start();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.info("Refreshing catalog");
                        Catalog catalog = harvester.catalog();
                        server.publish(catalog);
                        sagetvPublisher.publish(catalog);
                    } catch (Exception e) {
                        logger.severe("Failed to refresh catalog", e);
                    }
                }
            } ;

            service.scheduleAtFixedRate(runnable, 1, 1200, TimeUnit.SECONDS);

        } catch (Exception e) {
            logger.severe("Failed to start sagetvcatchup plugin", e);
        }
    }

    @Override
    public void stop() {
        logger.info("Stopping sagetvcatchup plugin");

        service.shutdownNow();

        try {
            if (sagetvPublisher != null) {
                sagetvPublisher.unpublish();
            }
        } catch (Exception e) {
            logger.severe("Failed to remove online video properties", e);
        }

        try {
            registry.eventSubscribe(this, "PlaybackStopped");
            registry.eventSubscribe(this, "PlaybackStarted");
            registry.eventSubscribe(this, "PlaybackFinished");
        } catch (Exception e) {
            logger.severe("Failed to unsubscribe from events", e);
        }

        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            logger.severe("Failed to stop podcast", e);
        }
    }

    @Override
    public void destroy() {
        logger.info("Destroying sagetvcatchup plugin");
    }

    @Override
    public String[] getConfigSettings() {
        return new String[0];
    }

    @Override
    public String getConfigValue(String s) {
        return null;
    }

    @Override
    public String[] getConfigValues(String s) {
        return new String[0];
    }

    @Override
    public int getConfigType(String s) {
        return 0;
    }

    @Override
    public void setConfigValue(String s, String s1) {

    }

    @Override
    public void setConfigValues(String s, String[] strings) {

    }

    @Override
    public String[] getConfigOptions(String s) {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getConfigHelpText(String s) {
        return null;
    }

    @Override
    public String getConfigLabel(String s) {
        return null;
    }

    @Override
    public void resetConfig() {

    }

    @Override
    public void sageEvent(String s, Map map) {
        logger.info("Received event " + s + " with " + map);
    }

    public static void main(String[] args) {
        CatchupPlugin plugin = new CatchupPlugin(null);
        plugin.start();
    }
}
