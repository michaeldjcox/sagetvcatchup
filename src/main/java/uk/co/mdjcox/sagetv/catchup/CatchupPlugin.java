package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import org.slf4j.Logger;

import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.onlinevideo.Publisher;
import uk.co.mdjcox.sagetv.onlinevideo.PublisherFactory;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtils;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

import sage.SageTVPlugin;
import sage.SageTVPluginRegistry;


/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 01/02/13
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */

// In priority order...

// TODO - deployment in windows environment
// TODO - upgrade sagetv to 7.1 and try podcast recorder with BBC iplayer
// TODO - Should cache downloaded html/xml and only update if changed

// TODO - config from plugin - operations e.g recache, data i.e. last updated, no errors
// TODO - is there any way I can incrementally update the catalog?

// TODO - check video from other providers
// TODO - Can I place fully download videos directly in recordings for later?
// TODO - Can request such downloads from the existing EPG?
// TODO - Can we use Sage Favourites to establish a favourites category?



public class CatchupPlugin implements SageTVPlugin {

    private static final String PULL_UPGRADE = "pullUpgrade";

    private static final String CATALOG_IN_PROGRESS = "catalogProgress";

    private static final String START_CATALOG = "startCatalog";

    private static final String STOP_CATALOG = "stopCatalog";


    public static Logger logger;
    public static Injector injector;

    private LinkedHashMap<String, Integer> types = new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String, String> labels = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> help = new LinkedHashMap<String, String>();

    private SageTVPluginRegistry registry;
    private PodcastServer server;

    private ScheduledExecutorService service;
    private Publisher sagetvPublisher;

    private DownloadUtilsInterface downloadUtils;
    private String pullUpgradeValue="Click here";
    private String startCatalogValue="Click here";
    private String stopCatalogValue="Click here";

    private PropertiesInterface props;
    private Cataloger cataloger;
    private boolean catalogRunning;
    private ScheduledFuture<?> future;
    private PluginManager pluginManager;

    @Inject
  public CatchupPlugin(sage.SageTVPluginRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void start() {

        try {
            AbstractModule module;
            String workingDir = System.getProperty("user.dir");
            String home = System.getProperty("user.home");
            if (workingDir.startsWith(home) && workingDir.endsWith("sagetvcatchup")) {
                System.err.println("Running in DEV");
                module = new CatchupDevModule();
            } else {
                System.err.println("Running in SageTV");
                module = new CatchupModule();
            }

            injector = Guice.createInjector(module);

            logger = injector.getInstance(Logger.class);

            logger.info("Starting catchup plugin");

            props = injector.getInstance(PropertiesInterface.class);
            logger.info(props.toString());

            this.downloadUtils = injector.getInstance(DownloadUtilsInterface.class);

            service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "catchup-scheduler");
                }
            });


            if (registry != null) {
                registry.eventSubscribe(this, "PlaybackStopped");
                registry.eventSubscribe(this, "PlaybackStarted");
                registry.eventSubscribe(this, "PlaybackFinished");
            }

            pluginManager = injector.getInstance(PluginManager.class);
            cataloger = injector.getInstance(Cataloger.class);
            server = injector.getInstance(PodcastServer.class);

            String fileName = props.getString("fileName");
            String STV = props.getString("STV");

            PublisherFactory publisherFactory =  injector.getInstance(PublisherFactory.class);
            sagetvPublisher = publisherFactory.createPublisher(fileName, STV);

            Recorder recorder = injector.getInstance(Recorder.class);

            pluginManager.load();
            server.start();

            init();

            Runnable runnable = getCatalogRunnable();

            long refreshRate = props.getInt("refreshRateHours");

            future = service.scheduleAtFixedRate(runnable, 0, refreshRate, TimeUnit.HOURS);



        } catch (Exception e) {
            if (logger == null) {
               System.err.println("Failed to start catchup plugin");
                e.printStackTrace();
            } else {
                logger.error("Failed to start catchup plugin", e);
            }
        }
    }

    private Runnable getCatalogRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    catalogRunning = true;
                    logger.info("Refreshing catalog");
                    Catalog catalog = cataloger.catalog();
                    if (catalog != null) {
                        cataloger.setProgress("Publishing catalog");
                        server.publish(catalog);
                        sagetvPublisher.publish(catalog);
                        cataloger.setProgress("Finished");
                    } 
                } catch (Exception e) {
                    logger.error("Failed to refresh catalog", e);
                    cataloger.setProgress("Failed");
                } finally {
                    catalogRunning = false;
                }
            }
        };
    }

    @Override
    public void stop() {
        logger.info("Stopping catchup plugin");

        service.shutdownNow();

        try {
            registry.eventUnsubscribe(this, "PlaybackStopped");
            registry.eventUnsubscribe(this, "PlaybackStarted");
            registry.eventUnsubscribe(this, "PlaybackFinished");
        } catch (Exception e) {
            logger.error("Failed to unsubscribe from events", e);
        }

        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            logger.error("Failed to stop podcast", e);
        }
    }

    @Override
    public void destroy() {
        logger.info("Destroying catchup plugin");
        try {
            if (sagetvPublisher != null) {
                sagetvPublisher.unpublish();
            }
        } catch (Exception e) {
            logger.error("Failed to remove online video properties", e);
        }
    }

    private void init() {
        types.put(CATALOG_IN_PROGRESS, CONFIG_TEXT);
        labels.put(CATALOG_IN_PROGRESS, "Catalog progress");
        help.put(CATALOG_IN_PROGRESS,"Show catalog progress");

        types.put(START_CATALOG, CONFIG_BUTTON);
        labels.put(START_CATALOG, "Start cataloging");
        help.put(START_CATALOG,"Force cataloging to start immediately");

        types.put(STOP_CATALOG, CONFIG_BUTTON);
        labels.put(STOP_CATALOG, "Stop cataloging");
        help.put(STOP_CATALOG,"Force cataloging to stop immediately");

        if (pluginManager != null) {
            for (Plugin plugin : pluginManager.getPlugins()) {
                String name = plugin.getSource().getId();
                String propName = name + ".maxprogrammes";
                types.put(propName, CONFIG_INTEGER);
                labels.put(propName, name + " max programmes");
                help.put(propName, name + " max programmes");
            }
        }

        types.put(PULL_UPGRADE, CONFIG_BUTTON);
        labels.put(PULL_UPGRADE, "Check for upgrade");
        help.put(PULL_UPGRADE,"Get SageTV to pull a new dev version");
    }

    @Override
    public String[] getConfigSettings() {
        return labels.keySet().toArray(new String[labels.size()]);
    }

    @Override
    public String getConfigValue(String property) {
        if (property.equals(PULL_UPGRADE)) {
            return pullUpgradeValue;
        }

        if (property.equals(CATALOG_IN_PROGRESS)) {
            String progress = cataloger.getProgress();
            if ("Finished".equals(progress) || "Failed".equals(progress) || "Waiting".equals(progress)) {
                long delay = future.getDelay(TimeUnit.MINUTES);
                progress += " - next attempt " + (delay / 60) + "hrs " + (delay % 60) + "mins";
            }
            return progress;
        }

        if (property.equals(START_CATALOG)) {
            return startCatalogValue;
        }

        if (property.equals(STOP_CATALOG)) {
            return stopCatalogValue;
        }

        if (pluginManager != null) {
            for (Plugin plugin : pluginManager.getPlugins()) {
                String name = plugin.getSource().getId();
                String propName = name + ".maxprogrammes";
                if (property.equals(propName)) {
                    return String.valueOf(props.getInt(propName, Integer.MAX_VALUE));
                }
            }
        }


        return "";
    }

    @Override
    public String[] getConfigValues(String property) {
        return new String[0];
    }

    @Override
    public int getConfigType(String property) {
        return types.get(property);
    }

    @Override
    public void setConfigValue(String property, String value) {
        if (property.equals(PULL_UPGRADE)) {
            logger.info("Checking for dev upgrade");
            try {
                String updateUrl = "http://mintpad/sagetvcatchup/download/SageTVPluginsDev.xml";
                String downloadTo = System.getProperty("user.dir")  + File.separator+ "SageTVPluginsDev.xml";
                downloadUtils.downloadFile(new URL(updateUrl), downloadTo);
                logger.info("Downloaded " + downloadTo);
                pullUpgradeValue = "Done";
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {

                        }
                        pullUpgradeValue = "Click here";
                    }
                });
                thread.start();
            } catch (Exception e) {
                pullUpgradeValue = "Failed";
                logger.info("Failed to check for upgrade", e);
            }
        }

        if (property.equals(START_CATALOG)) {
            logger.info("Force catalog start");
            try {
                if (catalogRunning) {
                    startCatalogValue = "Already running";
                } else {
                    service.schedule(getCatalogRunnable(), 0, TimeUnit.SECONDS);

                    startCatalogValue = "Started catalog";
                }
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {

                        }
                        startCatalogValue = "Click here";
                    }
                });
                thread.start();
            } catch (Exception e) {
                startCatalogValue = "Failed";
                logger.info("Failed to start catalog", e);
            }
        }

        if (property.equals(STOP_CATALOG)) {
            logger.info("Force catalog stop");
            try {
                if (!catalogRunning) {
                    stopCatalogValue = "Already stopped";
                } else {
                    stopCatalogValue = "Stopping catalog";

                    cataloger.stop();
                }
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {

                        }
                        stopCatalogValue = "Click here";
                    }
                });
                thread.start();
            } catch (Exception e) {
                stopCatalogValue = "Failed";
                logger.info("Failed to stop catalog", e);
            }
        }

        if (pluginManager != null) {
            for (Plugin plugin : pluginManager.getPlugins()) {
                String name = plugin.getSource().getId();
                String propName = name + ".maxprogrammes";
                if (property.equals(propName)) {
                    props.setProperty(propName, value);
                }
            }
        }
    }

    @Override
    public void setConfigValues(String property, String[] strings) {

    }

    @Override
    public String[] getConfigOptions(String s) {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getConfigHelpText(String property) {
        return help.get(property);
    }

    @Override
    public String getConfigLabel(String property) {
        return labels.get(property);
    }

    @Override
    public void resetConfig() {
        try {
            logger.info("Resetting config");
            init();
            logger.info("Done reseting config ");
        } catch (Throwable e) {
            logger.error("Failed to reset config", e);
        }

    }

    @Override
    public void sageEvent(String s, Map map) {

        if (s.equals("PlaybackStarted")) {
           logger.info("Playback started of " + map);
        } else
        if (s.equals("PlaybackStopped")) {
            logger.info("Playback stopped of " + map);
            stopRecording(map);
        } else
        if (s.equals("PlaybackFinished")) {
            logger.info("Playback finished of " + map);
            stopRecording(map);
        } else {
            logger.info("Received event " + s);
        }

    }

    private void stopRecording(Map map) {
    HtmlUtils htmlUtils = injector.getInstance(HtmlUtils.class);
    String episodeTitle = map.toString();
    episodeTitle = htmlUtils.moveTo("MediaFile[", episodeTitle);
    episodeTitle = htmlUtils.moveTo("\"", episodeTitle);
    episodeTitle = htmlUtils.extractTo("\"", episodeTitle);
    episodeTitle = htmlUtils.makeIdSafe(episodeTitle);

    server.stopRecording(episodeTitle);

}


    public static void main(String[] args) {
        CatchupPlugin plugin = new CatchupPlugin(null);
        plugin.start();
    }
}
