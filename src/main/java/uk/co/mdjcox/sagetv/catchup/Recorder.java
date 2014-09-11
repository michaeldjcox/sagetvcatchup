package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;

import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;
import java.nio.file.Files;
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
    private Recorder(Logger thelogger, PluginManager pluginManager, PropertiesInterface props) {
        this.logger = thelogger;
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

    public File start(Episode episode) throws Exception {

        File dir = new File(recordingDir);
        if (!dir.exists()) {
            Files.createDirectories(dir.toPath());
        }

        String url = episode.getServiceUrl();
        String episodeName = episode.getEpisodeTitle();

        logger.info("Looking for recording of " + episode);
        Recording recording = currentRecordings.get(episodeName);
        if ((recording != null) && recording.isInProgress()) {
            logger.info("Recording in progress for " + episode);
            if (recording.getFile() == null) {
                synchronized (recording) {
                    recording.wait();
                }
            }
            logger.info("Returning file " + recording.getFile() + " for " + episodeName);
            return recording.getFile();
        }

        logger.info("Starting recording of " + url);


        recording = new Recording(episode, recordingDir);
        currentRecordings.put(episodeName, recording);

        Plugin plugin = pluginManager.getPlugin(episode.getSourceId());
        plugin.playEpisode(recording);

        synchronized (recording) {
            recording.notifyAll();
        }

        logger.info("Returning file " + recording.getFile() + " for " + url);

        return recording.getFile();
    }

    public void stop(Episode episode) {
        String name = episode.getEpisodeTitle();

        Recording recording = currentRecordings.get(name);

        if (recording != null) {
            logger.info("Going to stop playback of " + name);

            stop(recording);
        } else {
            logger.info("Cannot find recording of " + name);

        }
    }

    private void stop(Recording recording) {

        if (recording != null) {
            Plugin plugin = pluginManager.getPlugin(recording.getSourceId());
            plugin.stopEpisode(recording);
            currentRecordings.remove(recording.getName());
//            recording.getFile().delete();
        }
    }

    public boolean isRecording(Episode episode) {
        String name = episode.getEpisodeTitle();

        Recording recording = currentRecordings.get(name);

        if (recording != null) {
            logger.info("Checking playback of " + name);

            return recording.isInProgress();
        }

        return false;
    }
}
