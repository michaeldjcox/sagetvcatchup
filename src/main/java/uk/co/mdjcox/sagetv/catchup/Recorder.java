package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.OsUtilsInterface;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

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
    private PluginManager pluginManager;
    private ConcurrentHashMap<String, Recording> currentRecordings = new ConcurrentHashMap<String, Recording>();
    private final String recordingDir;

    @Inject
    private Recorder(Logger theLogger, PluginManager pluginManager, PropertiesInterface props, OsUtilsInterface osUtils) {
        this.logger = theLogger;
        this.pluginManager = pluginManager;
        this.osUtils = osUtils;
        this.recordingDir = props.getProperty("recordingDir", "/opt/sagetv/server/sagetvcatchup/plugins");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (Recording recording : currentRecordings.values()) {
                    try {
                        logger.info("Shutting down - stopping " + recording);

                        stop(recording);
                    } catch (Exception e) {
                        logger.info("Failed to stop " + recording);
                    }
                }
            }
        }));
    }

    private File start(String sourceId, String id, String name, String url) throws Exception {

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
            if (recording.getFile() == null) {
                synchronized (recording) {
                    recording.wait();
                }
            }
            logger.info("Returning file " + recording.getFile() + " for " + id);
            return recording.getFile();
        }

        logger.info("Starting recording of " + url);


        recording = new Recording(sourceId, id, name, url, recordingDir);
        currentRecordings.put(id, recording);

        Plugin plugin = pluginManager.getPlugin(sourceId);
        plugin.playEpisode(recording);

        synchronized (recording) {
            recording.notifyAll();
        }

        logger.info("Returning file " + recording.getFile() + " for " + url);

        File file = recording.getFile();

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
                recording.setStopped();
            }
            return "All recordings stopping";
        }
    }

    public String requestStop(String id) {
        Recording recording = currentRecordings.get(id);
        if (recording != null) {
            if (!recording.isStopped()) {
                recording.setStopped();
                return "Recording " + id + " stopping";
            }
        }
        return "Recording " + id + " already stopped";
    }

    public void stop(String id) {
        Recording recording = currentRecordings.get(id);

        if (recording != null) {
            logger.info("Going to stop playback of " + id);

            stop(recording);
        } else {
            logger.info("Cannot find recording of " + id);

        }
    }

    private void stop(final Recording recording) {

        if (recording != null) {
            logger.info("Stopping recording of " + recording.getId());
            recording.setStopped();
            Plugin plugin = pluginManager.getPlugin(recording.getSourceId());
            plugin.stopEpisode(recording);
            // TODO If resume would work
            recording.getFile().delete();
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

    public void record(OutputStream out, String sourceId, final String id, String name, String url) throws Exception {
        FileInputStream in = null;

        try {

            File file = start(sourceId, id, name, url);

            logger.info("Streaming " + file + " exists=" + file.exists());

            in = new FileInputStream(file);

            // Copy the contents of the file to the output stream

            byte[] buf = new byte[100];
            int served = 0;
            int count = 0;
            int lastReport = 0;

            long lastServed = System.currentTimeMillis();

            try {
                while (isRecording(id) && !isStopped(id)) {
                    while ((count = in.read(buf)) >= 0 && !isStopped(id)) {
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

    public int getProcessCount() {
        int count = osUtils.findProcessesMatching(".*get_iplayer.*").size();
        count += osUtils.findProcessesMatching(".*rtmpdump.*").size();
        return count;
    }
}
