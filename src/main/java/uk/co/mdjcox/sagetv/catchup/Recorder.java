package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;
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
    private PluginManager pluginManager;
    private ConcurrentHashMap<String, Recording> currentRecordings = new ConcurrentHashMap<String, Recording>();
    private final String recordingDir;

    @Inject
    private Recorder(Logger theLogger, PluginManager pluginManager, PropertiesInterface props) {
        this.logger = theLogger;
        this.pluginManager = pluginManager;
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

    public File start(String sourceId, String id, String name, String url) throws Exception {

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
}
