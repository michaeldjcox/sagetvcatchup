package uk.co.mdjcox.sagetv.catchup;

import sage.SageTVPlugin;
import sage.SageTVPluginRegistry;
import uk.co.mdjcox.utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;


//TODO multithreaded plugin can get stuck on socket reads downloading web pages
//TODO cataloging times
//TODO data leakage between episodes?
//TODO heap size of catchup server

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 01/02/13
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */
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

  private String pullUpgradeValue = "Click here";
  private String startCatalogValue = "Click here";
  private String stopCatalogValue = "Click here";
  private String stopRecordingValue = "Click here";

  private LinkedHashMap<String, Integer> types = new LinkedHashMap<String, Integer>();
  private LinkedHashMap<String, String> labels = new LinkedHashMap<String, String>();
  private LinkedHashMap<String, String> help = new LinkedHashMap<String, String>();

  private SageTVPluginRegistry registry;

  private SageUtilsInterface sageUtils;

  private OsUtilsInterface osUtils;


  private String tmpDir = System.getProperty("java.io.tmpdir", ".") + File.separator + "sagetvcatchup" + File.separator;
  private String sageHomeDir = System.getProperty("user.dir") + File.separator;
  private String catchupDir = sageHomeDir + "sagetvcatchup" + File.separator;
  private String propFileName = catchupDir + "config" + File.separator + "sagetvcatchup.properties";
  private String backupFileName = tmpDir + "sagetvcatchup.properties.backup";
  private String seedFileName;
  private String sageTvDevPluginsFile = sageHomeDir +  "SageTVPluginsDev.xml";
  private String devDownloadUrl = "http://mintpad/sagetvcatchup/download/SageTVPluginsDev.xml";
  private PropertiesInterface props;
  private CatchupPluginService rmiService;

  public CatchupPlugin(sage.SageTVPluginRegistry registry) {
    this.registry = registry;
    if (File.separator.equals("\\")) {
      if (sageHomeDir.contains(File.separator + "Program Files" + File.separator)) {
          seedFileName = "sagetvcatchup.windows32.properties";
      } else {
          seedFileName = "sagetvcatchup.windows.properties";
      }
    } else {
      seedFileName = "sagetvcatchup.unix.properties";
    }
    seedFileName = catchupDir + "seeds" + File.separator + seedFileName;
  }

  private void startRmiServer() {
    try {
      rmiService = new CatchupPluginService(sageUtils);

      int rmiRegistryPort = props.getInt("catchupPluginRmiPort", 1105);
      sageUtils.info("Offer remote access to plugin");
      RmiHelper.startupLocalRmiRegistry(rmiRegistryPort);
      String name =RmiHelper.rebind("localhost", rmiRegistryPort, "CatchupPlugin", rmiService);
      sageUtils.info("Bound name >" + name +"<");

      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

        public void run() {
          try {
            sageUtils.info("Stopping catchup plugin rmi server");
            stopRmiServer();
            sageUtils.info("Stopped catchup plugin rmi server");
          } catch (Exception e) {
            sageUtils.warn("Failed to stop catchup plugin rmi server", e);
          }
        }
      }));
    } catch (Exception e) {
      sageUtils.error("Cannot start server ", e);
    }
  }

  private void stopRmiServer() throws Exception {
    try {
      sageUtils.info("Discontinue rmi access to catchup plugin");
      int rmiRegistryPort = props.getInt("catchupPluginRmiPort", 1105);
      RmiHelper.unbind("localhost", rmiRegistryPort, "CatchupPlugin");
    } catch (NotBoundException nb) {
      // Ignore
    } catch (Exception e) {
      sageUtils.error("Cannot stop server ", e);
    }
  }


  @Override
  public void start() {
    try {
      sageUtils = SageUtils.instance();

      sageUtils.info("Starting catchup plugin");

      getProperties();

      osUtils = OsUtils.instance(sageUtils);

      startRmiServer();

      startCatchupServer();

      initConfig();

      sageUtils.info("Started catchup plugin");

    } catch (Throwable e) {
      sageUtils.error("Failed to start catchup plugin", e);
    }
  }

  private CatchupServerRemote getCatchupServerRemote() throws Exception {
    int rmiRegistryPort = props.getInt("catchupServerRmiPort", 1106);

    return (CatchupServerRemote)RmiHelper.lookup("localhost", rmiRegistryPort, "CatchupServer");
  }

  @Override
  public void stop() {
    try {
      sageUtils.info("Stopping catchup plugin");

      stopRmiServer();

      stopCatchupServer();

      try {
        sageUtils.info("Backing up properties");
        props.commit(backupFileName, new CatchupPropertiesFileLayout());
      } catch (Exception e) {
        sageUtils.error("Unable to save property backup", e);
      }


      sageUtils.info("Stopped catchup plugin");

    } catch (Throwable e) {
      sageUtils.error("Failed to stop catchup plugin", e);
    }
  }

  private void startCatchupServer()  {
    try {
        String javaCmd = "";
        if (osUtils.isWindows()) {
            javaCmd += "\"";
            javaCmd += System.getProperty("java.home");
            if (!javaCmd.endsWith(File.separator)) {
                javaCmd += File.separator;
            }
            javaCmd += "bin";
            javaCmd += File.separator;
            javaCmd += "java\" \"-Xmx500m\" \"-jar\" \"" + catchupDir + "libs" + File.separator + "sagetvcatchup.jar\"";
        } else {
            javaCmd = System.getProperty("java.home");
            if (!javaCmd.endsWith(File.separator)) {
                javaCmd += File.separator;
            }
            javaCmd += "bin";
            javaCmd += File.separator;
            javaCmd += "java -Xmx500m -jar " + catchupDir + "libs" + File.separator + "sagetvcatchup.jar";
        }

      osUtils.spawnProcess(javaCmd, "catchupserver", false, new File(catchupDir));
    } catch (Exception e) {
      sageUtils.error("Failed to start catchup server plugin", e);
    }

  }

  private void stopCatchupServer()  {
    try {
      if (isCatchupServerRunning()) {
          sageUtils.info("Requesting catchup server shutdown");
          stopServer();
      } else {
          sageUtils.info("Catchup Server is not running");
      }

      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        // Ignore
      }

      if (isCatchupServerRunning()) {
          sageUtils.info("Server is still alive so killing catchup server");
          if (osUtils.isWindows()) {
              osUtils.killProcessesContaining("\"-jar\" \"" + catchupDir + "libs" + File.separator + "sagetvcatchup.jar\"");
          } else {
              osUtils.killProcessesContaining("-jar " + catchupDir + "libs" + File.separator + "sagetvcatchup.jar");
          }
      } else {
          sageUtils.info("Catchup Server is not running");
      }

      if (isCatchupServerRunning()) {
        sageUtils.error("Failed to stop catchup server");
      }

    } catch (Exception e) {
      sageUtils.error("Failed to stop catchup server", e);
    }
  }

  private boolean isCatchupServerRunning() {
    sageUtils.info("Is catchup server running?");
    Map<String, String> processes = new HashMap<String, String>();
      if (osUtils.isWindows()) {
          processes = osUtils.findProcessesContaining("\"-jar\" \"" + catchupDir + "libs" + File.separator + "sagetvcatchup.jar\"");
      } else {
          processes = osUtils.findProcessesContaining("-jar " + catchupDir + "libs" + File.separator + "sagetvcatchup.jar");
          }

    sageUtils.info("Found running catchup servers: " + processes.keySet());
    return !processes.isEmpty();
  }

  @Override
  public void destroy() {
    try {

      sageUtils.info("Destroying catchup plugin");

      stopCatchupServer();

      String enabled = sageUtils.getSageTVProperty("sagetv_core_plugins/sagetvcatchup/enabled", "blah");

      if (enabled.equalsIgnoreCase("false")) {
        sageUtils.info("Destroying as part of uninstallation - tidying up");

        String recordingDir = catchupDir + "recordings";
        File recordings = new File(recordingDir);

        // SageTV should take care of the root
        deleteFileOrDir(recordings, false);

        File props = new File(propFileName);
        if (props.exists()) {
          deleteFileOrDir(props, true);
        }

        String logDir = catchupDir + "logs";
        File logs = new File(logDir);
        deleteFileOrDir(logs, false);
      } else {
        sageUtils.info("Destroying as part of an upgrade - leaving files in place");
      }
      sageUtils.info("Destroying catchup plugin enabled = " + enabled);
    } catch (Throwable e) {
      sageUtils.error("Failed to destroy plugin", e);
    }
  }

  private boolean deleteFileOrDir(File fileOrDir, boolean deleteRoot) {
    if (fileOrDir.isDirectory()) {
      String[] children = fileOrDir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteFileOrDir(new File(fileOrDir, children[i]), true);
        if (!success) {
          sageUtils.warn("FAILED deleting " + fileOrDir);
          return false;
        } else {
          sageUtils.info("Deleted " + fileOrDir);
        }
      }
    }

    if (deleteRoot) {
      return fileOrDir.delete();
    } else {
      return false;
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

      for (String name : getPluginNames()) {
        String propName = name + ".skip";
        types.put(propName, CONFIG_BOOL);
        labels.put(propName, name + " enabled");
        help.put(propName, name + " enabled");
      }

      File sageTvDevPlugins = new File(sageHomeDir, "SageTVPluginsDev.xml");
      if (sageTvDevPlugins.exists()) {
        boolean isDevSite = fileContainsLine(sageTvDevPlugins, "sagetvcatchup");
        if (isDevSite) {
          sageUtils.info("This is a sagetv developer site");

          for (String name : getPluginNames()) {
            String propName = name + ".maxprogrammes";
            types.put(propName, CONFIG_INTEGER);
            labels.put(propName, name + " max programmes");
            help.put(propName, name + " max programmes. 0 = no maximum");
          }

          types.put(PULL_UPGRADE, CONFIG_BUTTON);
          labels.put(PULL_UPGRADE, "Check for upgrade");
          help.put(PULL_UPGRADE, "Get SageTV to pull a new dev version");



        } else {
          sageUtils.info("This is not a sagetv developer site");
        }
      }
    } catch (Throwable e) {
      sageUtils.error("Failed to setup developer config controls", e);
    }
  }

  private boolean fileContainsLine(File sageTvDevPlugins, String text) {
    FileReader fr = null;
    BufferedReader br = null;
    String line="";
    try {
      fr = new FileReader(sageTvDevPlugins);
      br = new BufferedReader(fr);
      while ((line = br.readLine()) != null) {
        // print the line.
        if (line.contains(text)) {
          return true;
        }
      }
    } catch (Exception e) {
      // Ignore
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (Exception e) {
          // Ignore
        }
      }
      if (fr != null) {
        try {
          fr.close();
        } catch (Exception e) {
          // Ignore
        }
      }

    }
    return false;
  }

  @Override
  public String[] getConfigSettings() {
    return labels.keySet().toArray(new String[labels.size()]);
  }

  @Override
  public String getConfigValue(String property) {

    Map<String, String> statii = getServerStatus();

    if (property.equals(PULL_UPGRADE)) {
      return pullUpgradeValue;
    }

    if (property.equals(CATALOG_IN_PROGRESS)) {
      String status = statii.get("Catalog Progress");
      if (status == null) {
        return "Server not running";
      } else {
        return status;
      }
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
      String status = statii.get("Recording Progress");
      if (status == null) {
        return "Server not running";
      } else {
        return status;
      }
    }

    if (property.equals(RECORDINGS_PROCESSES)) {
      String status = statii.get("Recording Processes");
      if (status == null) {
        return "Server not running";
      } else {
        return status;
      }
    }

    for (String name : getPluginNames()) {
      String propName = name + ".maxprogrammes";
      if (property.equals(propName)) {
        final int maxProgrammes = props.getInt(propName, 0);
        return String.valueOf(maxProgrammes);
      }

      String propNameSkip = name + ".skip";
      if (property.equals(propNameSkip)) {
        return String.valueOf(!props.getBoolean(propNameSkip, Boolean.FALSE));
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
      sageUtils.info("Checking for dev upgrade");
      try {
        DownloadUtils.instance().downloadFile(new URL(devDownloadUrl), sageTvDevPluginsFile);
        sageUtils.info("Downloaded " + sageTvDevPluginsFile);
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
        sageUtils.error("Failed to check for upgrade", e);
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
    }

    if (property.equals(PORT)) {
      setCatchupProperty(PORT, value);
      restartCatchupServer();
    }

    for (String name : getPluginNames()) {
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

  private void restartCatchupServer() {
    try {
      stopCatchupServer();
    } catch (Exception e) {
      sageUtils.error("Failed to stop catchup server", e);
    }
    try {
      startCatchupServer();
    } catch (Exception e) {
      sageUtils.error("Failed to restart catchup server", e);
    }
  }

  private void setCatchupProperty(String propName, String value) {
    props.setProperty(propName, value);
    try {
      props.commit(propFileName, new CatchupPropertiesFileLayout());
    } catch (Exception e) {
      sageUtils.error("Failed to persist property change", e);
    }

      try {
          getCatchupServerRemote().setProperty(propName, value);
      } catch (Exception e) {
          sageUtils.error("Failed to set property on catchup server", e);
      }
  }

  private void forceStopRecording() {
    sageUtils.info("Force recording stop");
    try {
      stopRecordingValue = stopAllRecording();

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
      sageUtils.error("Failed to stop recording", e);
    }
  }

  private void forceCatalogStop() {
    sageUtils.info("Force catalog stop");
    try {
      stopCatalogValue = stopCataloging();
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
      sageUtils.error("Failed to stop catalog", e);
    }
  }

  private void forceCatalogStart() {
    sageUtils.info("Force catalog start");
    try {
      startCatalogValue = startCataloging();
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            // Ignore
          }
          startCatalogValue = "Click here";
        }
      });
      thread.start();
    } catch (Exception e) {
      startCatalogValue = "Failed";
      sageUtils.error("Failed to start catalog", e);
    }
  }

  private String startCataloging() {
    String result = "Server not running";
    try {
      return getCatchupServerRemote().startCataloging();
    } catch (Exception e) {
      sageUtils.error("Failed to request start cataloging", e);
    }
    return result;
  }

  private String stopCataloging() {
    String result = "Server not running";
    try {
      return getCatchupServerRemote().stopCataloging();
    } catch (Exception e) {
      sageUtils.error("Failed to request stop cataloging", e);
    }
    return result;
  }

  private String stopAllRecording() {
    String result = "Server not running";
    try {
      return getCatchupServerRemote().stopAllRecording();
    } catch (Exception e) {
      sageUtils.error("Failed to request stop all recording", e);
    }
    return result;
  }

  private String stopServer() {
    String result = "Server not running";
    try {
      return getCatchupServerRemote().shutdown();
    } catch (Exception e) {
      sageUtils.error("Failed to request catchup server shutdown", e);
    }
    return result;
  }

  private Map<String,String> getServerStatus() {
    Map<String, String> results = new HashMap<String,String>();
    try {
      results = getCatchupServerRemote().getStatus();
    } catch (Exception e) {
      sageUtils.error("Failed to perform server operation status", e);
    }
    return results;
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
      sageUtils.info("Resetting config of catchup plugin");
      initConfig();
      sageUtils.info("Done reseting config of catchup plugin");
    } catch (Throwable e) {
      sageUtils.error("Failed to reset config of catchup plugin", e);
    }
  }

  @Override
  public void sageEvent(String s, Map map) {
  }

  private List<String> getPluginNames() {
    ArrayList<String> pluginNames = new ArrayList<String>();
    File dir = new File(catchupDir, "plugins");

    File[] pluginDirs = dir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });

    for (File pluginDir : pluginDirs) {
      String sourceId = pluginDir.getName();
      pluginNames.add(sourceId);

    }
    return pluginNames;
  }

  private void getProperties() throws Exception {
      File backupFile = new File(backupFileName);
      File runFile = new File(propFileName);

      if (runFile.exists()) {
        // We have a file
        sageUtils.info("Properties - reusing existing " + propFileName);
      } else {
        sageUtils.info("Properties - loading seed properties from " + seedFileName);

        // We have no file
        PropertiesFile seed = new PropertiesFile(seedFileName, true);

        if (backupFile.exists()) {
          sageUtils.info("Properties - applying backup properties " + backupFileName);

          // We can reuse an old one
          PropertiesFile backup = new PropertiesFile(backupFileName, true);
          for (Map.Entry<Object, Object> entry : backup.entrySet()) {
            seed.put(entry.getKey(), entry.getValue());
          }
        }

        seed.commit(propFileName, new CatchupPropertiesFileLayout());
      }

      props = new PropertiesFile(propFileName, true);

  }

}
