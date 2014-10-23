package uk.co.mdjcox.sagetv.catchup;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.LoggerInterface;
import uk.co.mdjcox.utils.OrderedPropertiesFileLayout;
import uk.co.mdjcox.utils.OsUtilsInterface;
import uk.co.mdjcox.utils.PropertiesFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

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
    private PluginManager pluginManager;
    private ConcurrentHashMap<String, Recording> currentRecordings = new ConcurrentHashMap<String, Recording>();
    private final String recordingDir;
    private final String recordingsLogDir;
    private ScheduledExecutorService service;
    private Set<String> errors = new HashSet<String>();
    private int failedCount = 0;
    private int completedCount = 0;

    @Inject
    private Recorder(LoggerInterface theLogger, PluginManager pluginManager, CatchupContextInterface context,
                     OsUtilsInterface osUtils) {
        this.logger = theLogger;
        this.pluginManager = pluginManager;
        this.osUtils = osUtils;
        this.recordingDir = context.getRecordingDir();
        this.recordingsLogDir = context.getTmpDir() + File.separator + "recordings" + File.separator;
    }

    private File watch(Episode episode) throws Exception {
        Recording recording = checkForExistingRecording(episode.getId());
        if (recording != null) {
            return recording.getPartialFile();
        } else {
            recording = createNewRecording(episode, true);
            return download(recording);
        }
    }

    private Recording checkForExistingRecording(String id) throws Exception {
        File dir = new File(recordingDir);
        if (!dir.exists()) {
            Files.createDirectories(dir.toPath());
        }

        logger.info("Looking for recording of " + id);
        Recording recording = currentRecordings.get(id);
        if ((recording != null) && recording.isStopped()) {
            throw new Exception("Recording has been stopped");
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

    private Recording createNewRecording(Episode episode, boolean watchOnly) throws Exception {
        logger.info("Starting new recording of " + episode.getId());
        Recording recording = new Recording(episode, recordingDir, watchOnly);
        currentRecordings.put(episode.getId(), recording);
        return recording;
    }

    private File download(Recording recording) throws Exception {
        Plugin plugin = pluginManager.getPlugin(recording.getSourceId());
        plugin.playEpisode(recording);

        synchronized (recording) {
            recording.notifyAll();
        }

        logger.info("Returning file " + recording.getPartialFile() + " for " + recording.getUrl());

        File file = recording.getPartialFile();

        if (file == null) {
            throw new Exception("No recording file found");
        }

        return file;
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

    public void start() {
      logger.info("Starting recorder service");
      try {
        service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
          @Override
          public Thread newThread(Runnable r) {
            return new Thread(r, "catchup-recorder");
          }
        });
        logger.info("Started recorder service");
      } catch (Exception ex) {
        logger.error("Failed to start the recorder service");
      }

    }

    public void shutdown() {
        logger.info("Stopping the recorder service");
      try {
        requestStopAll();
        service.shutdownNow();
        logger.info("Stopped the recorder service");
      } catch (Exception ex) {
        logger.error("Failed to stop the recorder service");
      }
    }

    public String requestStop(String id) {
        return stop(id);
    }

    private String stop(String id) {
        final Recording recording = currentRecordings.get(id);

        if (recording != null) {
            logger.info("Going to stop playback of " + id);

            stop(recording);

            return "Recording " + id + " stopping";

        } else {
            logger.info("Cannot find recording of " + id);
            return "Recording " + id + " already stopped";
        }
    }

    private void stop(final Recording recording) {
        logger.info("Stopping recording of " + recording.getId());
        recording.setStopped();
        Plugin plugin = pluginManager.getPlugin(recording.getSourceId());
        plugin.stopEpisode(recording);
        File partialFile = recording.getPartialFile();
        if (partialFile != null && partialFile.exists()) {
            partialFile.delete();
        }

        if (recording.isWatchOnly()) {
            File completedFile = recording.getCompletedFile();
            if (completedFile != null && completedFile.exists()) {
                completedFile.delete();
            }
        }
        // Block sage form replaying for a bit
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // Ignore
                } finally {
                    currentRecordings.remove(recording.getId());
                }
            }
        }).start();
    }


    public boolean isRecording(String id) {
        Recording recording = currentRecordings.get(id);

        if (recording != null) {
            logger.info("Checking playback of " + id);

            return recording.isInProgress();
        }

        return false;
    }

    public boolean isRecording() {
        return !currentRecordings.isEmpty();
    }

    public Collection<Recording> getCurrentRecordings() {
        return currentRecordings.values();
    }


    public int getRecordingCount() {
        return currentRecordings.size();
    }

  public int getFailedCount() {
    return failedCount;
  }

  public int getCompletedCount() {
    return completedCount;
  }

  public Set<String> getErrors() {
      return errors;
    }

    public boolean isStopped(String id) {
        Recording recording = currentRecordings.get(id);
        if (recording == null) {
            return true;
        } else {
            return recording.isStopped();
        }
    }

    public void record(final Episode episode) {

        try {
            Recording existingRecording = checkForExistingRecording(episode.getId());

            if (existingRecording == null) {

                final  Recording newRecording = createNewRecording(episode, false);

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                      try {
                        try {
                            File partialFile = download(newRecording);

                            File completedFile = newRecording.getCompletedFile();
                            while (partialFile.exists() && !(newRecording.isStopped() || newRecording.isComplete())) {
                                osUtils.waitFor(1000);
                                logger.info("File " + partialFile.getAbsolutePath() + " exists size=" + partialFile.length());
                            }

                            osUtils.waitFor(1000);

                            logger.info("Done recording. File " + completedFile + " exists=" + completedFile.exists());

                            if (completedFile.exists()) {
                                documentRecording(newRecording);
                              completedCount++;
                            } else {
                                logger.error("No recording file found for " + episode);
                              throw new Exception("No recording file found for " + episode);
                            }
                        } catch (Throwable e) {
                          failedCount++;
                          errors.add("Recording " + episode.getId() + " failed due to exception");
                          logger.warn("Recording of " + episode.getId() + " stopped due to exception ", e);
                        } finally {
                            try {
                                stop(episode.getId());
                            } catch (Exception e1) {
                                logger.error("Failed to stop recording in the recorder", e1);
                            }
                        }
                      } catch (Throwable e) {
                        logger.error("Recording of " + episode.getId() + " threw exception on stop", e);
                      }

                    }
                };
                service.schedule(runnable, 0, TimeUnit.SECONDS);

            }
        } catch (Exception e) {
            logger.error("Failed to start recording", e);
        }

    }

  /**
   * Imports recording into the Sage database as an Airing.
   * <p>
   * @return The Airing if success, null otherwise.
   */
  public void documentRecording(Recording recording) {

    try {
      Preconditions.checkNotNull(recording);

      File recordingFile = recording.getCompletedFile();
      String id = recording.getId();
      Episode episode = recording.getEpisode();
      String programmeTitle = episode.getProgrammeTitle();
      String episodeTitle = episode.getEpisodeTitle();
      String description = episode.getDescription();
      String[] categories = episode.getGenres().toArray(new String[episode.getGenres().size()]);
      String origAirDate = episode.getOrigAirDate();
      String origAirTime = episode.getOrigAirTime();
      String airDate = episode.getAirDate() ;
      String airTime = episode.getAirTime();
      int seriesNumber = 0;
      int episodeNumber = 0;
      if (!episode.getSeries().isEmpty()) {
        try {
          seriesNumber = Integer.parseInt(episode.getSeries());
        } catch (NumberFormatException e) {

        }
      }
      if (!episode.getEpisode().isEmpty()) {
        try {
          episodeNumber = Integer.parseInt(episode.getSeries());
        } catch (NumberFormatException e) {

        }
      }

      String filename = recordingsLogDir + episode.getId() + ".properties";

      PropertiesFile recordingProps = new PropertiesFile();
      recordingProps.setProperty("file", recordingFile.getAbsolutePath());
      recordingProps.setProperty("id", id);
      recordingProps.setProperty("programmeTitle", programmeTitle);
      recordingProps.setProperty("episodeTitle", episodeTitle);
      recordingProps.setProperty("description", description);
      int cat = 1;
      for (String category : categories) {
        recordingProps.setProperty("category." + cat, category);
        cat++;
      }
      recordingProps.setProperty("origAirDate", origAirDate);
      recordingProps.setProperty("origAirTime", origAirTime);
      recordingProps.setProperty("airDate", airDate);
      recordingProps.setProperty("airTime", airTime);
      recordingProps.setProperty("seriesNumber", String.valueOf(seriesNumber));
      recordingProps.setProperty("episodeNumber", String.valueOf(episodeNumber));

      List<String> order = new LinkedList<String>();
      for (Object key : recordingProps.keySet()) {
        order.add(key.toString());
      }

      recordingProps.commit(filename, new OrderedPropertiesFileLayout(order, "Recording of " + episode.getId(), ""));
    } catch (Exception e) {
      logger.error("Failed to document recording", e);
    }
  }

        public void watch(final OutputStream out, final Episode episode, final boolean keep) throws Exception {
        FileInputStream in = null;
        String id = episode.getId();
          boolean clientTermination = false;

        try {

            File file = watch(episode);

            logger.info("Streaming " + file + " exists=" + file.exists());

            in = new FileInputStream(file);

            // Copy the contents of the file to the output stream

            byte[] buf = new byte[100];
            int served = 0;
            int count = 0;
            int lastReport = 0;

            long lastServed = System.currentTimeMillis();

            try {
                logger.info("isRecording=" + isRecording(id) + " isStopped=" + isStopped(id));
                while (isRecording(id) && !(isStopped(id) && !isCompleted(id))) {
                    while ((count = in.read(buf)) >= 0 && !(isStopped(id) && !isCompleted(id))) {
                        out.write(buf, 0, count);
                        served += count;
                        out.flush();
                    }
                    osUtils.waitFor(1000);
                    if (served > lastReport) {
                        logger.info("Streaming of " + id + " continues after serving " + served + "/" + file.length());
                        lastReport = served;
                        lastServed = System.currentTimeMillis();
                    } else {
                        if ((System.currentTimeMillis() - lastServed) > 10000) {
                            break;
                        }
                    }
                }
                if (isStopped(id)) {
                    logger.info("Streaming of " + id + " stopped due to stop request");
                } else {
                  completedCount++;
                    logger.info("Streaming of " + id + " stopped due to completion");
                }
            } finally {
                logger.info("Streaming of " + id + " stopped after serving " + served + "/" + file.length());
            }
        } catch (Exception e) {
          if (e.getCause() != null && e.getCause() instanceof SocketException) {
            logger.warn("Streaming of " + id + " stopped due to client termination ", e);
            clientTermination = true;
          } else {
            failedCount++;
            errors.add("Watching " + id + " failed due to exception");
            logger.warn("Streaming of " + id + " stopped due to exception ", e);
            throw new Exception("Failed to stream video", e);
          }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // Ignore
            }
            try {
              if (clientTermination && keep) {
                convertToRecord(id);
              } else {
                stop(id);
              }
            } catch (Exception e) {
                logger.error("Failed to stop recording in the recorder", e);
            }
        }
    }

  private void convertToRecord(String id) {
    final Recording newRecording = currentRecordings.get(id);
    if (newRecording != null) {
      logger.info("Converting watched item " + id + " to recording");
            
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          File partialFile = newRecording.getPartialFile();
          Episode episode = newRecording.getEpisode();
          try {
            try {

              File completedFile = newRecording.getCompletedFile();
              while (partialFile.exists() && !(newRecording.isStopped() || newRecording.isComplete())) {
                osUtils.waitFor(1000);
                logger.info("File " + partialFile.getAbsolutePath() + " exists size=" + partialFile.length());
              }

              osUtils.waitFor(1000);

              logger.info("Done recording. File " + completedFile + " exists=" + completedFile.exists());

              if (completedFile.exists()) {
                documentRecording(newRecording);
                              completedCount++;
              } else {
                logger.error("No recording file found for " + episode);
                              throw new Exception("No recording file found for " + episode);
              }
            } catch (Throwable e) {
                          failedCount++;
                          errors.add("Recording " + episode.getId() + " failed due to exception");
              logger.warn("Recording of " + episode.getId() + " stopped due to exception ", e);
            } finally {
              try {
                stop(episode.getId());
              } catch (Exception e1) {
                logger.error("Failed to stop recording in the recorder", e1);
              }
            }
          } catch (Throwable e) {
            logger.error("Recording of " + episode.getId() + " threw exception on stop", e);
          }

        }
      };

      new Thread(runnable).start();
    }

  }

  private boolean isCompleted(String id) {
        Recording recording = currentRecordings.get(id);
        if (recording == null) {
            return true;
        } else {
            return recording.isComplete();
        }
    }

    public int getProcessCount() {
        int count = 0;
      try {
        osUtils.findProcessesMatching(".*get_iplayer.*").size();
        count += osUtils.findProcessesMatching(".*rtmpdump.*").size();
      } catch (Exception e) {
        logger.error("Failed to count processes", e);
      }
      return count;
    }
}
