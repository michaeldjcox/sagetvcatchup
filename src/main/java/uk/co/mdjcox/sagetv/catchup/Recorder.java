package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.OsUtilsInterface;
import uk.co.mdjcox.utils.SageUtilsInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
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

    private final Logger logger;
    private final OsUtilsInterface osUtils;
    private final SageUtilsInterface sageUtils;
    private PluginManager pluginManager;
    private ConcurrentHashMap<String, Recording> currentRecordings = new ConcurrentHashMap<String, Recording>();
    private final String recordingDir;
    private ScheduledExecutorService service;

    @Inject
    private Recorder(Logger theLogger, PluginManager pluginManager, CatchupContextInterface context,
                     OsUtilsInterface osUtils, SageUtilsInterface sageUtils) {
        this.logger = theLogger;
        this.pluginManager = pluginManager;
        this.osUtils = osUtils;
        this.sageUtils = sageUtils;
        this.recordingDir = context.getRecordingDir();
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
      service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
          return new Thread(r, "catchup-recorder");
        }
      });
    }

    public void shutdown() {
        logger.info("Shutting down recording");
        requestStopAll();
        service.shutdownNow();
        logger.info("Shutdown recording");
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
        // TODO If resume would work - do not delete
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

                            System.err.println("Done recording. File " + completedFile + " exists=" + completedFile.exists());

                            if (completedFile.exists()) {

                                File[] recordingDirs = sageUtils.getRecordingDirectories();

                                String recDir = recordingDir;

                                if (recordingDirs.length > 0) {
                                    recDir = recordingDirs[0].getAbsolutePath();
                                }

                                File savedFile = new File(recDir, episode.getId() + ".mp4");

                                Files.move(completedFile.toPath(), savedFile.toPath());

                                newRecording.setSavedFile(savedFile);

                                sageUtils.addAiringToSageTV(newRecording);
                            } else {
                                logger.error("No recording file found for " + episode);
                            }
                        } catch (Throwable e) {
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

        public void watch(final OutputStream out, final Episode episode) throws Exception {
        FileInputStream in = null;
        String id = episode.getId();

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
                    logger.info("Streaming of " + id + " stopped due to completion");
                }
            } finally {
                logger.info("Streaming of " + id + " stopped after serving " + served + "/" + file.length());
            }
        } catch (Exception e) {
            logger.warn("Streaming of " + id + " stopped due to exception ", e);
            throw new Exception("Failed to stream video", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // Ignore
            }
            try {
                stop(id);
            } catch (Exception e) {
                logger.error("Failed to stop recording in the recorder", e);
            }
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
        int count = osUtils.findProcessesMatching(".*get_iplayer.*").size();
        count += osUtils.findProcessesMatching(".*rtmpdump.*").size();
        return count;
    }
}
