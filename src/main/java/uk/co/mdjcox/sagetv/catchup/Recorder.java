package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.sagetv.utils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 18/03/13
 * Time: 07:51
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class Recorder {

  private final LoggerInterface logger;
  private final OsUtilsInterface osUtils;
  private final CatchupContextInterface context;
  private final PluginManager pluginManager;

  private ConcurrentHashMap<String, Recording> currentRecordings = new ConcurrentHashMap<String, Recording>();
  private ConcurrentHashMap<String, Recording> failedRecordings = new ConcurrentHashMap<String, Recording>();
  private ConcurrentHashMap<String, Recording> completedRecordings = new ConcurrentHashMap<String, Recording>();

  private ExecutorService recordToKeepService;
  private ExecutorService recordToWatchService;

  private AtomicInteger processCount = new AtomicInteger(0);
  private long lastChecked = 0;

  @Inject
  private Recorder(LoggerInterface theLogger, PluginManager pluginManager, CatchupContextInterface context,
                   OsUtilsInterface osUtils) {
    this.logger = theLogger;
    this.pluginManager = pluginManager;
    this.osUtils = osUtils;
    this.context = context;
  }

  /*********************************************************************************************************************
   * SERVICE CONTROL
   ********************************************************************************************************************/

  /**
   * Starts the recording service
   */
  public void start() {
    logger.info("Starting recorder service");
    try {
      String recordingDir = context.getRecordingDir();
      logger.info("Recording to " + recordingDir);
      File dir = new File(recordingDir);
      if (!dir.exists()) {
        logger.warn("Recording directory does not exist - creating...");
        Files.createDirectories(dir.toPath());
      }

      recordToKeepService = Executors.newSingleThreadExecutor(new NamedThreadFactory("catchup-keep-recorder"));
      recordToWatchService = Executors.newFixedThreadPool(2, new NumberedThreadFactory("catchup-watch-recorder"));

      logger.info("Started recorder service");
    } catch (Exception ex) {
      logger.error("Failed to start the recorder service", ex);
    }

  }

  /**
   * Shuts down recording service
   */
  public void shutdown() {
    logger.info("Stopping the recorder service");
    try {
      requestStopAll();
      recordToKeepService.shutdownNow();
      recordToWatchService.shutdownNow();
      logger.info("Stopped the recorder service");
    } catch (Exception ex) {
      logger.error("Failed to stop the recorder service");
    }
  }


  /*********************************************************************************************************************
   * RECORDING METHODS
   ********************************************************************************************************************/

  /**
   * Schedules a recording of the specified episode
   *
   * @param episode the episode to record
   * @param toWatch <code>true</code> if the recording is being streamed
   * @param toKeep <code>true</code> if the recording is being saved
   * @return the associated recording object
   * @throws Exception if the recording cannot be started
   */
  public Recording record(final Episode episode, final boolean toWatch, boolean toKeep) throws Exception {
    Recording existingRecording = checkForExistingRecording(episode.getId());

    if (existingRecording == null) {

      final Recording recording = createNewRecording(episode, toWatch, toKeep);

      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          try {

            recording.setProgress("In progress");
            File partialFile = callPlayScript(recording);

            logger.info("Wait for recording "+ recording+ " to stop or complete");

            long lastChecked = 0;
            while (partialFile.exists() && !(recording.isStopped() || recording.isFailed() || recording.isComplete())) {
              osUtils.waitFor(1000);
              if (partialFile.exists()) {
                long currentSize = partialFile.length();
                if (currentSize < recording.getLastSize()) {
                  // length() may return zero if the file is not there
                  logger.info("Current size "+ currentSize+ "seems to be less than last size " + recording.getLastSize() + " assuming resume");
                  recording.setLastSize(currentSize);
                  lastChecked = System.currentTimeMillis();
                } else
                if (currentSize > recording.getLastSize()) {
                  recording.setLastSize(currentSize);
                  lastChecked = System.currentTimeMillis();
                } else {
                  if ((System.currentTimeMillis() - lastChecked) > context.getRecordingTimeout()) {
                    if (!(recording.isComplete() || recording.isStopped() || recording.isFailed())) {
                      recording.setStalled();
                      logger.info("Recording of " + episode + " stalled for " + context.getRecordingTimeout() + "ms after recording " + currentSize);
                      throw new Exception("Recording of " + episode + " stalled");
                    }
                  }
                }
              }
            }

            logger.info("Done recording " + recording + " isStopped=" + recording.isStopped() + " isFailed=" + recording.isFailed() + " isComplete=" + recording.isComplete());

            File completedFile = recording.getCompletedFile();

            long timeout = System.currentTimeMillis() + context.getRecordingTimeout();

            if (!recording.isFailed() && !recording.isStopped()) {

              while (!completedFile.exists() && System.currentTimeMillis() < timeout) {
                logger.info("Waiting for completed file for " + recording + " to appear");
                osUtils.waitFor(1000);
              }

            }

            logger.info("Done recording " + recording+ " File " + completedFile + " exists=" + completedFile.exists());

            if (completedFile.exists()) {
              setCompleted(recording);
              try {
                recording.setLastSize(completedFile.length());
              } catch (Exception e) {
                // Ignore
              }
            } else {
              if (!(recording.isToWatch() && !recording.isToKeep())) {
                logger.error("No completed recording file found for " + episode);
                throw new Exception("No completed recording file found for " + episode);
              }
            }
          } catch (Throwable e) {
              setFailed(recording, e);
          } finally {
              if (recording != null) {
                  if (!recording.isToWatch() && recording.isToKeep()) {
                      logger.info("Recording of " + recording + " tidying up");
                      finalizeRecording(recording);
                  } else
                  if (recording.isToWatch()) {
                      if (recording.hasFinishedStreaming()) {
                          logger.info("Recording of " + recording + " tidying up");
                          finalizeRecording(recording);
                      }  else {
                          logger.info("Recorder is letting streaming tidy up when done");
                          recording.setFinishedRecording();
                      }
                  }
              }
          }
        }
      };
      if (toWatch) {
        recordToWatchService.submit(runnable);
      } else {
        recordToKeepService.submit(runnable);
      }

      return recording;
    }

    return existingRecording;
  }

  /**
   * Schedules a recording and immediately starts streaming it back to the user
   * @param out the output stream to feed the content back on
   * @param episode the episode to record
   * @param toKeep <code>true</code> if the recording is being saved
   * @throws Exception if the recording cannot be started
   */
  public void watch(final OutputStream out, final Episode episode, final boolean toKeep) throws Exception {

    Recording recording = null;
    FileInputStream in = null;
    String id = episode.getId();
    try {
      recording = record(episode, true, toKeep);

      if (recording.isStopped()) {
        logger.info("SageTV is asking for the content again because we did not not the content size");
        return;
      }

      File file = recording.hasCompletedFile() ? recording.getCompletedFile() : recording.getPartialFile();

      if (file == null || !file.exists()) {
        synchronized (recording) {
          try {
            recording.wait(context.getStreamingTimeout());
          } catch (InterruptedException e) {
            // Ignore
          }
        }
      }

      file = recording.hasCompletedFile() ? recording.getCompletedFile() : recording.getPartialFile();

      if (file == null || !file.exists()) {
        throw new Exception("Failed to stream episode " + episode);
      }

        logger.info("Streaming " + file + " exists=" + file.exists());

        in = new FileInputStream(file);

        // Copy the contents of the file to the output stream

        byte[] buf = new byte[100];
        long streamed = 0;
        long lastStreamed = 0;
        long lastChecked = 0;
        try {
          logger.info("Streaming of " + id + " inProgress=" + isInProgress(id) + " isCompleted=" + isCompleted(id) + " isStopped=" + isStopped(id) + " isStalled=" + isStalled(id) + " isFailed=" + isFailed(id));
          int count = 0;
          while (!isStopped(id) && !isFailed(id) && !isStalled(id)) {
            while ((count = in.read(buf)) >= 0 && !isStopped(id) && !isFailed(id) && !isStalled(id)) {
              out.write(buf, 0, count);
              streamed += count;
                try {
                    out.flush();
                } catch (IOException e) {
                    // Ignore
                }
            }
            osUtils.waitFor(1000);

            if (streamed >= recording.getLastSize() && isCompleted(id)) {
              logger.info("Streaming of " + id + " done after serving " + streamed + "/" + recording.getLastSize());
              break;
            } else
            if (streamed > lastStreamed) {
              logger.info("Streaming of " + id + " continues after serving " + streamed + "/" + recording.getLastSize());
              lastStreamed = streamed;
              lastChecked = System.currentTimeMillis();
            } else {
              if ((System.currentTimeMillis() - lastChecked) > context.getStreamingTimeout()) {
                logger.info("Streaming of " + id + " stalled for " + context.getStreamingTimeout() + " after serving " + streamed + "/" + recording.getLastSize());
              }
            }
          }
        } finally {
          logger.info("Streaming of " + id + " done inProgress=" + isInProgress(id) + " isCompleted=" + isCompleted(id) + " isStopped=" + isStopped(id) + " isStalled=" + isStalled(id) + " isFailed=" + isFailed(id));
        }
      } catch (Exception e) {
        if (e.getCause() != null && e.getCause() instanceof SocketException) {
          logger.warn("Streaming of " + id + " stopped due to client termination");
        } else {
          logger.warn("Streaming of " + id + " stopped due to exception ", e);
          throw e;
        }
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException e) {
        // Ignore
      }
      if (recording != null) {
          if (recording.isToWatch() && !recording.isToKeep()) {
              logger.info("Streaming of " + id + " tidying up");
              finalizeRecording(recording);
          } else
          if (recording.isToWatch()) {
              if (recording.hasFinishedRecording()) {
                  logger.info("Streaming of " + id + " tidying up");
                  finalizeRecording(recording);
              } else {
                  logger.info("Streamer is letting recording tidy up when done");
                  recording.setFinishedStreaming();
              }
          }
        }
      }
  }

    private void finalizeRecording(Recording recording) {
        if (!recording.isStopped()) {
          stop(recording);
        }
      // TODO take this out!
      String hostname = "NONE";
      try {
        hostname = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {

      }

      // Only on mintpad dev machine will we use a substitute recording
      if (hostname.equals("mintpad")) {
        if (!recording.hasCompletedFile()) {
          File completedFile = recording.getCompletedFile();
          if (completedFile == null) {
            completedFile = new File(context.getRecordingDir() + File.separator + recording.getEpisode().getId() + ".mp4" );
            recording.setCompletedFile(completedFile);
          }
          File testFile = new File(context.getPluginDir() + File.separator + "Test" + File.separator + "TestEpisode.mp4");
          try {
            logger.info("Copy test programme in from " + testFile + " to " + completedFile);
            Files.copy(testFile.toPath(), completedFile.toPath());
          } catch (Exception e) {
            logger.error("Failed to copy in substitute completed file", e);
            e.printStackTrace();
          }
        }
      }
        if (recording.isToKeep() && recording.hasCompletedFile()) {
                uploadToSageTv(recording);
        }
        houseKeepFiles(recording);
        removeRecording(recording);
        updateProcessCount();
    }

    private boolean isFailed(String id) {
    Recording recording = currentRecordings.get(id);
    if (recording == null) {
      return false;
    } else {
      return recording.isFailed();
    }
  }

  private Recording checkForExistingRecording(String id) throws Exception {
    logger.info("Looking for recording of " + id);
    Recording recording = currentRecordings.get(id);
    if ((recording != null) && recording.isStopped()) {
      return recording;
    }
    if ((recording != null) && recording.isInProgress()) {
      logger.info("Recording in progress for " + id);
      if (recording.getPartialFile() == null) {
        synchronized (recording) {
          recording.wait();
        }
      }
      logger.info("Returning file " + recording.getPartialFile() + " for " + id);
      return recording;
    }

    return null;
  }

  private Recording createNewRecording(Episode episode, boolean toWatch, boolean toKeep) throws Exception {
    logger.info("Starting new recording of " + episode + (toWatch ? " to watch" : "" ) + (toKeep ? " to keep" : "") );
    String recordingDir = context.getRecordingDir();
    Recording recording = new Recording(episode, recordingDir, toWatch, toKeep);
    currentRecordings.put(episode.getId(), recording);
    return recording;
  }

  private File callPlayScript(Recording recording) throws Exception {
    Plugin plugin = pluginManager.getPlugin(recording.getSourceId());
    plugin.playEpisode(recording);

    synchronized (recording) {
      recording.notifyAll();
    }

    logger.info("Returning file " + recording.getPartialFile() + " for " + recording.getUrl());

    File file = recording.getPartialFile();

    if (file == null) {
      throw new Exception("No partial recording file found");
    }

    return file;
  }

  /*********************************************************************************************************************
   * RECORDING TERMINATION METHODS
   ********************************************************************************************************************/

  public String requestStop(String id) {
    final Recording recording = currentRecordings.get(id);
    if (recording != null) {
      return stop(recording);
    } else {
      logger.info("Cannot find recording of " + id);
      return "Recording " + id + " already stopped";
    }
  }

  public String requestStopAll() {
    if (!isRecording()) {
      return "All recordings already stopped";
    } else {
      ArrayList<Recording> recordings = new ArrayList<Recording>(currentRecordings.values());
      for (Recording recording : recordings) {
        stop(recording);
      }
      return "All recordings stopping";
    }
  }

  private String stop(final Recording recording) {
    try {
      logger.info("Going to stop recording of " + recording);
      recording.setStopped();
      Plugin plugin = pluginManager.getPlugin(recording.getSourceId());
      plugin.stopEpisode(recording);
      return "Recording " + recording + " stopping";
    } catch (Exception e1) {
      logger.error("Failed to stop recording " + recording, e1);
      return "Failed to stop recording " + recording;
    }
  }

  private void houseKeepFiles(Recording recording) {
    logger.info("Deleting files for recording " + recording);
    try {
      File partialFile = recording.getPartialFile();
      if (partialFile != null && partialFile.exists()) {
        logger.info("Deleting partial recording " + partialFile);
        partialFile.delete();
      }

      File completedFile = recording.getCompletedFile();
      if (completedFile != null && completedFile.exists()) {
          logger.info("Deleting completed recording " + completedFile);
        completedFile.delete();
      }
    } catch (Exception e) {
      logger.error("Failed deleting files for recording " + recording, e);
    }
  }

  private void removeRecording(final Recording recording) {
    logger.info("Removing recording " + recording);
    try {
      currentRecordings.remove(recording.getId());
    } catch (Exception e) {
      logger.error("Failed removing recording " + recording, e);
    }
  }

/**********************************************************************************************
 * SAVE RECORDING TO SAGE TV
 **********************************************************************************************/

  /**
   * Imports recording into the Sage database as an Airing.
   * <p/>
   *
   * @return The Airing if success, null otherwise.
   */
  public void uploadToSageTv(Recording recording) {

    try {
      checkNotNull(recording);

      logger.info("Uploading " + recording + " to SageTV");

      File recordingFile = recording.getCompletedFile();
      String id = recording.getId();
      Episode episode = recording.getEpisode();
      String sourceId = recording.getSourceId();
      String programmeTitle = episode.getProgrammeTitle();
      String episodeTitle = episode.getEpisodeTitle();
      String description = episode.getDescription();
      String[] categories = episode.getGenres().toArray(new String[episode.getGenres().size()]);
      String origAirDate = episode.getOrigAirDate();
      String origAirTime = episode.getOrigAirTime();
      String airDate = episode.getAirDate();
      String airTime = episode.getAirTime();
      String icon = episode.getIconUrl();
      String durationStr = episode.getDuration();
      String channel = episode.getChannel();
      final String seriesStr = episode.getSeries();
      final String episodeStr = episode.getEpisode();
      int seriesNumber = 0;
      int episodeNumber = 0;
      int duration = 0;

      if (seriesStr != null && !seriesStr.isEmpty()) {
        try {
          seriesNumber = Integer.parseInt(seriesStr);
        } catch (NumberFormatException e) {

        }
      }
      if (episodeStr != null && !episodeStr.isEmpty()) {
        try {
          episodeNumber = Integer.parseInt(episodeStr);
        } catch (NumberFormatException e) {

        }
      }

      if (durationStr != null && !durationStr.isEmpty()) {
        try {
          duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {

        }
      }

      List<String> categoriesList = new ArrayList<String>();
      for (String category : categories) {
        categoriesList.add(category);
      }

      if (seriesNumber != 0 && episodeNumber != 0) {
        if (episodeTitle == null || episodeTitle.isEmpty()) {
          episodeTitle = "Season" + seriesNumber + " - Episode" + episodeNumber;

        } else {
          episodeTitle = episodeTitle + " - Season" + seriesNumber + " - Episode" + episodeNumber;
        }
      }

      getCatchupPluginRemote().addRecordingToSageTV(
              sourceId,
              id,
              recordingFile.getAbsolutePath(),
              programmeTitle,
              episodeTitle,
              description,
              categoriesList,
              origAirDate,
              origAirTime,
              airDate,
              airTime,
              seriesNumber,
              episodeNumber,
              icon,
              duration,
              channel
      );

        logger.info("Completed uploading " + recording + " to SageTV");

    } catch (Exception e) {
        setFailed(recording, e);
      logger.error("Failed to upload recording", e);
    }
  }

  private CatchupPluginRemote getCatchupPluginRemote() throws Exception {
    int rmiRegistryPort = context.getCatchupPluginRmiPort();

    return (CatchupPluginRemote) RmiHelper.lookup("127.0.0.1", rmiRegistryPort, "CatchupPlugin");
  }


/**********************************************************************************************
 * RECORDING STATE
 **********************************************************************************************/

  /**
   * Indicates whether a recording is complete
   *
   * @param id the recording id
   * @return <code>true</code> if the recording has completed
   */
  public boolean isCompleted(String id) {
    Recording recording = currentRecordings.get(id);
    if (recording == null) {
      return true;
    } else {
      return recording.isComplete();
    }
  }

  /**
   * Indicates whether a recording has been stopped
   *
   * @param id the recording id
   * @return <code>true</code> if the recording has completed
   */
  public boolean isStopped(String id) {
    Recording recording = currentRecordings.get(id);
    if (recording == null) {
      return true;
    } else {
      return recording.isStopped();
    }
  }

  public boolean isStalled(String id) {
    Recording recording = currentRecordings.get(id);
    if (recording == null) {
      return true;
    } else {
      return recording.isStalled();
    }
  }

  /**
   * Indicates if a recording with the specified id is currently in progress
   *
   * @param id the id of the recording
   * @return <code>true</code> if the recording is in progress
   */
  public boolean isInProgress(String id) {
    Recording recording = currentRecordings.get(id);

    if (recording != null) {
      logger.info("Checking recording " + id + " is in progress");

      return recording.isInProgress();
    }

    return false;
  }

  /**
   * Gets a count of the number of spawned processes involved in current recordings
   *
   * @return the number of processes
   */
  public int getProcessCount() {

    if ((System.currentTimeMillis() - lastChecked) > 10000) {
        updateProcessCount();
    }
    return processCount.get();
  }

    private void updateProcessCount() {
        int count = 0;
        try {
            osUtils.findProcessesMatching(".*get_iplayer.*").size();
            count += osUtils.findProcessesMatching(".*rtmpdump.*").size();
            processCount.set(count);
        } catch (Exception e) {
            logger.error("Failed to count recording processes", e);
        } finally {
            lastChecked = System.currentTimeMillis();
        }
    }

    /**
   * Indicates if any recording is currently in progress
   *
   * @return <code>true</code> if there are any recording records
   */
  public boolean isRecording() {
    return !currentRecordings.isEmpty();
  }

  /**
   * Gets a list of current recordings
   *
   * @return a list of current recordings
   */
  public Collection<Recording> getCurrentRecordings() {
    return currentRecordings.values();
  }

  /**
   * Gets a count of the number of items being recorded
   *
   * @return a count of recordings
   */
  public int getRecordingCount() {
    return currentRecordings.size();
  }

  /**
   * Gets a count of the number of recordings that have failed
   *
   * @return a count of failed recordings
   */
  public int getFailedCount() {
    return failedRecordings.size();
  }

  /**
   * Gets a count of the number of recordings that have completed
   *
   * @return a count of completed recordings
   */
  public int getCompletedCount() {
    return completedRecordings.size();
  }

  public int getStoppingCount() {
    int count = 0;
    for (Recording rec : currentRecordings.values()) {
      if (rec.isStopped()) {
        count++;
      }
    }
    return count;
  }

  /**
   * Gets a set of errors which occurred during recording
   *
   * @return
   */
  public Collection<Recording> getFailedRecordings() {
    return failedRecordings.values();
  }

  /**
   * Sets the recording to be completed
   *
   * @param recording the recording that is complete
   */
  public void setCompleted(Recording recording) {
    recording.setCompleted();
    completedRecordings.put(recording.getId() + "-" + recording.getStartTime(), recording);
    logger.info("Recording of " + recording + " has completed");
  }

  /**
   * Sets the recording to be failed
   *
   * @param recording the recording that is failed
   */
  private void setFailed(Recording recording, Throwable e) {
    final String message = "Recording " + recording + " failed due to exception";
    if (e instanceof RemoteException) {
        if (e.getCause() != null) {
            e = e.getCause();
        }
    }
    recording.setFailed(message, e);
    failedRecordings.put(recording.getId() + "-" + recording.getStartTime(), recording);
    logger.warn(message, e);
  }

  public Collection<Recording> getCompletedRecordings() {
    return completedRecordings.values();
  }
}
