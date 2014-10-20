package uk.co.mdjcox.sagetv.catchup;

import com.google.common.io.Files;
import com.google.inject.*;
import com.google.inject.name.Names;

import sage.SageTVPlugin;
import sage.SageTVPluginRegistry;
import sagex.api.SystemMessageAPI;
import sagex.plugin.SageEvents;
import uk.co.mdjcox.logger.Logger;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.catchup.server.Server;
import uk.co.mdjcox.sagetv.onlinevideo.SageTvPublisher;
import uk.co.mdjcox.utils.*;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 01/02/13
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */

// In priority order...

// BUGS
// TODO - BUG - force refresh on online caching (set client property!)
// TODO - BUG - get_iplayer will not play some content - is this the 7 day issue - wait for 2.87
// TODO - BUG - original air date (I have) but last aired date (I don't have)
// TODO - BUG - No channel is set on TV recordings - you can create a detailed airing and link to media file
// TODO - default for windows XP
// TODO - conflicts in log framework
// TODO - conflicts in servlet API - should I separate the server from sagetv?
// TODO - ensure directories are present for logging?
// TODO - port and rec dir are not in config
// TODO - The port is in the catalog

// Refactor
// TODO - Podcast stylesheets?
// TODO - Generalise SageTV Publisher into utils
// TODO - fake programme categories are model breaking, always have to filter out source, root, category, programme

// SageTV Integration
// TODO - Can we use Sage Favourites to establish a favourites category?
// TODO - Can "watched" status extend from recordings to Online
// TODO - Can request such downloads from the existing EPG?

// WISHLIST

// TODO - Can I implement a "New" category
// TODO - Demand5 - record fails
// TODO - 4OD
// TODO - ITVPlayer
// TODO - is there any way I can incrementally update the catalog?
// TODO - STVI

public class CatchupPlugin implements SageTVPlugin {

  private static final String PULL_UPGRADE = "pullUpgrade";

  private static final String CATALOG_IN_PROGRESS = "catalogProgress";

  private static final String START_CATALOG = "startCatalog";

  private static final String STOP_CATALOG = "stopCatalog";

  private static final String RECORDINGS_IN_PROGRESS = "recordingProgress";

  private static final String RECORDINGS_PROCESSES = "recordingProcesses";

  private static final String STOP_RECORDING = "stopRecording";

  private static final String RECORDING_DIR = "recordingDir";

  private static final String PORT = "podcasterPort";

  public Logger logger;
  public static Injector injector;

  private LinkedHashMap<String, Integer> types = new LinkedHashMap<String, Integer>();
  private LinkedHashMap<String, String> labels = new LinkedHashMap<String, String>();
  private LinkedHashMap<String, String> help = new LinkedHashMap<String, String>();

  private SageTVPluginRegistry registry;
  private Server server;

  private SageTvPublisher sagetvPublisher;

  private DownloadUtilsInterface downloadUtils;
  private SageUtilsInterface sageUtils;
  private String pullUpgradeValue = "Click here";
  private String startCatalogValue = "Click here";
  private String stopCatalogValue = "Click here";
  private String stopRecordingValue = "Click here";

  private PropertiesInterface props;
  private Cataloger cataloger;
  private PluginManager pluginManager;
  private Recorder recorder;

  private String propFileName;
  private String backupFileName;
  private CatchupContextInterface context;
  private AbstractModule module;

  @Inject
  public CatchupPlugin(sage.SageTVPluginRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void start() {

    try {
      PersistentRollingFileAppender.stopped = false;

      if (CatchupContext.isRunningInSageTV()) {
        SageUtils.debug("Starting SageTV Catchup plugin");
        module = new CatchupModule();
      } else {
        module = new CatchupDevModule();
      }

      injector = Guice.createInjector(module);
      logger = injector.getInstance(Logger.class);

      logger.info("#######################");
      logger.info("Starting catchup plugin");
      logger.info("#######################");

      props = injector.getInstance(PropertiesInterface.class);
      context = injector.getInstance(CatchupContextInterface.class);

      logger.info("Properties: " + props.toString());
      logger.info("Context:    " + context.toString());


      this.downloadUtils = injector.getInstance(DownloadUtilsInterface.class);

      this.sageUtils = injector.getInstance(SageUtilsInterface.class);

      this.propFileName = injector.getInstance(Key.get(String.class, Names.named("PropsFile")));
      this.backupFileName = injector.getInstance(Key.get(String.class, Names.named("BackupPropsFile")));

      pluginManager = injector.getInstance(PluginManager.class);
      server = injector.getInstance(Server.class);
      cataloger = injector.getInstance(Cataloger.class);
      sagetvPublisher = injector.getInstance(SageTvPublisher.class);
      recorder = injector.getInstance(Recorder.class);
      CatalogPersister persister = injector.getInstance(CatalogPersister.class);

      pluginManager.start();

      List<CatalogPublisher> publishers = new ArrayList<CatalogPublisher>();
      publishers.add(sagetvPublisher);
      publishers.add(server);
      publishers.add(persister);

      recorder.start();
      server.start();
      cataloger.start(publishers, persister);

      registerForEvents();

      initConfig();
    } catch (Throwable e) {
      try {
        if (CatchupContext.isRunningInSageTV()) {
          SageUtils.debug("Failed to start catchup plugin");
          SageUtils.debug(Arrays.toString(e.getStackTrace()));
        }
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      if (logger == null) {
        System.err.println("Failed to start catchup plugin");
        e.printStackTrace();
      } else {
        logger.error("Failed to start catchup plugin", e);
      }
    }
  }

  private void registerForEvents() {
    if (registry != null) {
      registry.eventSubscribe(this, SageEvents.PlaybackStarted);
      registry.eventSubscribe(this, SageEvents.PlaybackStopped);
      registry.eventSubscribe(this, SageEvents.PlaybackFinished);
      registry.eventSubscribe(this, SageEvents.MediaFileImported);
      registry.eventSubscribe(this, SageEvents.MediaFileRemoved);
    }
  }

  @Override
  public void stop() {
    try {
      logger.info("Stopping catchup plugin");

      try {
        logger.info("Backing up properties");
        props.commit(backupFileName, new CatchupPropertiesFileLayout());
      } catch (Exception e) {
        logger.error("Unable to save property backup", e);
      }


      unregisterForEvents();

      if (recorder != null) {
        recorder.shutdown();
      }

      if (cataloger != null) {
        cataloger.shutdown();
      }


      try {
        if (server != null) {
          server.shutdown();
        }
      } catch (Exception e) {
        logger.error("Failed to stop podcast", e);
      }
    } catch (Throwable ex) {
      logger.error("Failed to stop catchup plugin");
    }
    finally
     {
       logger.info("#######################");
       logger.info("Stopped catchup plugin");
       logger.info("#######################");

       logger.flush();

     }
  }

  private void unregisterForEvents() {
    try {
      if (registry != null) {
        registry.eventUnsubscribe(this, SageEvents.PlaybackStarted);
        registry.eventUnsubscribe(this, SageEvents.PlaybackStopped);
        registry.eventUnsubscribe(this, SageEvents.PlaybackFinished);
        registry.eventUnsubscribe(this, SageEvents.MediaFileImported);
        registry.eventUnsubscribe(this, SageEvents.MediaFileRemoved);
      }
    } catch (Exception e) {
      logger.error("Failed to unsubscribe from events", e);
    }
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

    File recordings = new File(context.getRecordingDir());
    File logs = new File(context.getLogDir());

    logger.info("###########################");
    logger.info("Uninstalled catchup plugin");
    logger.info("###########################");

    PersistentRollingFileAppender.stopped = true;

    logger.flush();

    // SageTV should take care of the root
    deleteFileOrDir(recordings, false);
    deleteFileOrDir(logs, false);

    File props = new File(propFileName);
    if (props.exists()) {
      deleteFileOrDir(props, true);
    }

  }

  private boolean deleteFileOrDir(File fileOrDir, boolean deleteRoot) {
    if (fileOrDir.isDirectory()) {
      String[] children = fileOrDir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteFileOrDir(new File(fileOrDir, children[i]), true);
        if (!success) {
          logger.info("FAILED deleting " + fileOrDir);
          return false;
        } else {
          logger.info("Deleted " + fileOrDir);
        }
      }
    }

    if (deleteRoot) {
      return fileOrDir.delete();
    } else {
      return false;
    }
  }

  @Override
  public void destroy() {
    try {
      logger.info("Destroying catchup plugin");

      String enabled = sageUtils.getSageTVProperty("sagetv_core_plugins/sagetvcatchup/enabled", "blah");
      logger.info("Destroying catchup plugin enabled = " + enabled);

      // This will occur if its an upgrade or an uninstall
      // Theres no way to tell the difference
      if (enabled.equalsIgnoreCase("false")) {
        uninstall();
      }
    } catch (Throwable e) {
      logger.error("Failed to destroy plugin", e);
    } finally {
      logger.flush();
    }
  }

  private void initConfig() {

    try {
    types.clear();
    labels.clear();
    help.clear();

    sageUtils.setClientProperty("online_video/cache_time_limit", "10000");

    types.put(CATALOG_IN_PROGRESS, CONFIG_TEXT);
    labels.put(CATALOG_IN_PROGRESS, "Catalog progress");
    help.put(CATALOG_IN_PROGRESS, "Show catalog progress");

    types.put(START_CATALOG, CONFIG_BUTTON);
    labels.put(START_CATALOG, "Start cataloging");
    help.put(START_CATALOG, "Force cataloging to start immediately");

    types.put(STOP_CATALOG, CONFIG_BUTTON);
    labels.put(STOP_CATALOG, "Stop cataloging");
    help.put(STOP_CATALOG, "Force cataloging to stop immediately");

    types.put(RECORDINGS_IN_PROGRESS, CONFIG_TEXT);
    labels.put(RECORDINGS_IN_PROGRESS, "Recordings in progress");
    help.put(RECORDINGS_IN_PROGRESS, "Show number of recordings progress");

    types.put(RECORDINGS_PROCESSES, CONFIG_TEXT);
    labels.put(RECORDINGS_PROCESSES, "Recording processes");
    help.put(RECORDINGS_PROCESSES, "Show number of recording processes");

    types.put(STOP_RECORDING, CONFIG_BUTTON);
    labels.put(STOP_RECORDING, "Stop recording");
    help.put(STOP_RECORDING, "Force all recording to stop immediately");

      types.put(RECORDING_DIR, CONFIG_TEXT);
      labels.put(RECORDING_DIR, "Temporary recording dir");
      help.put(RECORDING_DIR, "Change temporary recording dir");

      types.put(PORT, CONFIG_INTEGER);
      labels.put(PORT, "Server port");
      help.put(PORT, "Change the server port if it conflicts with one in use");

      for (String name : pluginManager.getPluginNames()) {
        String propName = name + ".skip";
        types.put(propName, CONFIG_BOOL);
        labels.put(propName, name + " enabled");
        help.put(propName, name + " enabled");
      }

      File sageTvDevPlugins = context.getSageTVPluginsDevFile();
      if (sageTvDevPlugins.exists()) {
        List<String> lines = Files.readLines(sageTvDevPlugins, Charset.defaultCharset());
        boolean isDevSite = false;
        for (String line : lines) {
          if (line.contains("sagetvcatchup")) {
            isDevSite = true;
          }
        }
        if (isDevSite) {
          logger.info("This is a sagetv developer site");

          if (pluginManager != null) {
            for (String name : pluginManager.getPluginNames()) {
              String propName = name + ".maxprogrammes";
              types.put(propName, CONFIG_INTEGER);
              labels.put(propName, name + " max programmes");
              help.put(propName, name + " max programmes");
            }
          }


          types.put(PULL_UPGRADE, CONFIG_BUTTON);
          labels.put(PULL_UPGRADE, "Check for upgrade");
          help.put(PULL_UPGRADE, "Get SageTV to pull a new dev version");



        } else {
          logger.info("This is not a sagetv developer site");
        }
      }
    } catch (Throwable e) {
      logger.warn("Failed to setup developer config controls", e);
    }
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

    if (property.equals(RECORDING_DIR)) {
      return props.getString(RECORDING_DIR);
    }

    if (property.equals(PORT)) {
      return props.getString(PORT);
    }

    if (property.equals(RECORDINGS_IN_PROGRESS)) {
      return String.valueOf(recorder.getRecordingCount());
    }

    if (property.equals(RECORDINGS_PROCESSES)) {
      return String.valueOf(recorder.getProcessCount());
    }

    if (pluginManager != null) {
      try {
        for (String name : pluginManager.getPluginNames()) {
          String propName = name + ".maxprogrammes";
          if (property.equals(propName)) {
            return String.valueOf(props.getInt(propName, Integer.MAX_VALUE));
          }

          String propNameSkip = name + ".skip";
          if (property.equals(propNameSkip)) {
            return String.valueOf(!props.getBoolean(propNameSkip, Boolean.FALSE));
          }
        }
      } catch (Exception e) {
        logger.error("Failed to add plugin properties", e);
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
        String updateUrl = context.getSageTVPluginsURL();
        File sageTvDevPluginsFile = context.getSageTVPluginsDevFile();
        downloadUtils.downloadFile(new URL(updateUrl), sageTvDevPluginsFile.getAbsolutePath());
        logger.info("Downloaded " + sageTvDevPluginsFile);
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

    if (property.equals(RECORDING_DIR)) {
      setCatchupProperty(RECORDING_DIR, value);
      context.setRecordingDir(value);
    }

    if (property.equals(PORT)) {
      setCatchupProperty(PORT, value);
      final Integer newPort = Integer.valueOf(value);
      context.setPort(newPort);
      server.shutdown();

      server.setPort(newPort);

      try {
        server.start();
      } catch (Exception e) {
        logger.error("Failed to restart server");
      }

    }

    if (pluginManager != null) {
      for (String name : pluginManager.getPluginNames()) {
        String propName = name + ".maxprogrammes";
        if (property.equals(propName)) {
          setCatchupProperty(propName, value);
        }
        String propNameSkip = name + ".skip";
        if (property.equals(propNameSkip)) {

          setCatchupProperty(propNameSkip, (value.equals("true") ? "false" : "true"));
        }
      }
    }
  }

  private void setCatchupProperty(String propName, String value) {
    props.setProperty(propName, value);
    try {
      props.commit(propFileName, new CatchupPropertiesFileLayout());
    } catch (Exception e) {
      logger.warn("Failed to persist property change", e);
    }
  }

  private void forceStopRecording() {
    logger.info("Force recording stop");
    try {
      stopRecordingValue = recorder.requestStopAll();

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
      stopCatalogValue = cataloger.stopCataloging();
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
      startCatalogValue = cataloger.startCataloging();
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
      initConfig();
      logger.info("Done reseting config ");
    } catch (Throwable e) {
      logger.error("Failed to reset config", e);
    }

  }

  @Override
  public void sageEvent(String s, Map map) {

    logger.info("SageEvent: " + s);

    if (s.equals("PlaybackStarted")) {
      logger.info("Playback started of " + map);
    } else if (s.equals("PlaybackStopped")) {
      logger.info("Playback stopped of " + map);
    } else if (s.equals("PlaybackFinished")) {
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
