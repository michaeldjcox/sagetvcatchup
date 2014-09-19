package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import sage.SageTV;
import sage.SageTVPlugin;
import sage.SageTVPluginRegistry;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.onlinevideo.SageTvPublisher;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 01/02/13
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */

// In priority order...

// TODO - TEST - plugin config - how many recorders? stop all
// TODO - TEST - on start up existing online files get deleted - need to make this happen on uninstall only

// TODO - make the stop and play rock solid
// TODO - home page

// TODO - why does desktop PC not always see laptop by name mintpad



// TODO - can resume be made to work
// TODO - is there any way I can incrementally update the catalog?

// TODO - check video from other providers
// TODO - Can I place fully download videos directly in recordings for later?
// TODO - Can I implement my own record button and loose podcast recorder
// TODO - Can request such downloads from the existing EPG?
// TODO - Can we use Sage Favourites to establish a favourites category?



public class CatchupPlugin implements SageTVPlugin {

    private static final String PULL_UPGRADE = "pullUpgrade";

    private static final String CATALOG_IN_PROGRESS = "catalogProgress";

    private static final String START_CATALOG = "startCatalog";

    private static final String STOP_CATALOG = "stopCatalog";

    private static final String RECORDINGS_IN_PROGRESS = "recordingProgress";

    private static final String STOP_RECORDING = "stopRecording";

    public static Logger logger;
    public static Injector injector;

    private LinkedHashMap<String, Integer> types = new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String, String> labels = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> help = new LinkedHashMap<String, String>();

    private SageTVPluginRegistry registry;
    private PodcastServer server;

    private SageTvPublisher sagetvPublisher;

    private DownloadUtilsInterface downloadUtils;
    private String pullUpgradeValue="Click here";
    private String startCatalogValue="Click here";
    private String stopCatalogValue="Click here";
    private String stopRecordingValue="Click here";

    private PropertiesInterface props;
    private Cataloger cataloger;
    private PluginManager pluginManager;
    private Recorder recorder;

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


            if (registry != null) {
                registry.eventSubscribe(this, "PlaybackStopped");
                registry.eventSubscribe(this, "PlaybackStarted");
                registry.eventSubscribe(this, "PlaybackFinished");
            }


            pluginManager = injector.getInstance(PluginManager.class);
            server = injector.getInstance(PodcastServer.class);
            cataloger = injector.getInstance(Cataloger.class);
            sagetvPublisher = injector.getInstance(SageTvPublisher.class);
            recorder = injector.getInstance(Recorder.class);

            Recorder recorder = injector.getInstance(Recorder.class);

            pluginManager.load();
            server.start();

            List<CatalogPublisher> publishers = new ArrayList<CatalogPublisher>();
            publishers.add(sagetvPublisher);
            publishers.add(server);

            cataloger.init(publishers);

            init();
        } catch (Exception e) {
            if (logger == null) {
               System.err.println("Failed to start catchup plugin");
                e.printStackTrace();
            } else {
                logger.error("Failed to start catchup plugin", e);
            }
        }
    }


    @Override
    public void stop() {
        logger.info("Stopping catchup plugin");

        if (cataloger != null) {
            cataloger.shutdown();
        }

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

        try {
            boolean enabled = getSageTVProperty(
                    "sagetv_core_plugins/sagetvcatchup/enabled", "false").equals("true");

            if (!enabled) {
                uninstall();
            }
        } catch (Exception e) {
            logger.error("Failed to check for uninstall", e);
        }

    }

    private static String getSageTVProperty(String property, String defaultValue) throws Exception {
        return (String) SageTV.api("GetServerProperty", new Object[]{property, defaultValue});
    }

    private void uninstall() {
        logger.info("Uninstalling catchup plugin");
        try {
            if (sagetvPublisher != null) {
                sagetvPublisher.unpublish();
            }
        } catch (Exception e) {
            logger.error("Failed to remove online video properties", e);
        }
    }

    @Override
    public void destroy() {
        logger.info("Destroying catchup plugin");
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

        types.put(RECORDINGS_IN_PROGRESS, CONFIG_TEXT);
        labels.put(RECORDINGS_IN_PROGRESS, "Recordings in progress");
        help.put(RECORDINGS_IN_PROGRESS,"Show number of recordings progress");

        types.put(STOP_RECORDING, CONFIG_BUTTON);
        labels.put(STOP_RECORDING, "Stop recording");
        help.put(STOP_RECORDING,"Force all recording to stop immediately");

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
            return cataloger.getProgress();
        }

        if (property.equals(START_CATALOG)) {
            return startCatalogValue;
        }

        if (property.equals(STOP_CATALOG)) {
            return stopCatalogValue;
        }

        if (property.equals(STOP_RECORDING)) {
            return stopRecordingValue;
        }

        if (property.equals(RECORDINGS_IN_PROGRESS)) {
            return String.valueOf(recorder.getRecordingCount());
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
            forceCatalogStart();
        }

        if (property.equals(STOP_CATALOG)) {
            forceCatalogStop();
        }

        if (property.equals(STOP_RECORDING)) {
            forceStopRecording();
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

    private void forceStopRecording() {
        logger.info("Force recording stop");
        try {
            stopRecordingValue = recorder.requestStopAll();
            ;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {

                    }
                    stopRecordingValue = "Click here";
                }
            });
            thread.start();
        } catch (Exception e) {
            stopRecordingValue = "Failed";
            logger.info("Failed to stop recording", e);
        }
    }

    private void forceCatalogStop() {
        logger.info("Force catalog stop");
        try {
            stopCatalogValue = cataloger.stop();
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

    private void forceCatalogStart() {
        logger.info("Force catalog start");
        try {
            startCatalogValue = cataloger.start();
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
        } else
        if (s.equals("PlaybackFinished")) {
            logger.info("Playback finished of " + map);
        } else {
            logger.info("Received event " + s);
        }

    }

//    private void stopRecordingFromSage(Map map) {
//        HtmlUtils htmlUtils = injector.getInstance(HtmlUtils.class);
//        String episodeTitle = map.toString();
//        episodeTitle = htmlUtils.moveTo("MediaFile[", episodeTitle);
//        episodeTitle = htmlUtils.moveTo("\"", episodeTitle);
//        episodeTitle = htmlUtils.extractTo("\"", episodeTitle);
//        episodeTitle = htmlUtils.makeIdSafe(episodeTitle);
//
//        server.stopRecordingByName(episodeTitle);
//    }

    public static void main(String[] args) {
        CatchupPlugin plugin = new CatchupPlugin(null);
        plugin.start();
    }
}
