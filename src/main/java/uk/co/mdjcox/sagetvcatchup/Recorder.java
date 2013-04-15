package uk.co.mdjcox.sagetvcatchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Episode;
import uk.co.mdjcox.model.Recording;
import uk.co.mdjcox.utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

    private final LoggerInterface logger;
    private PropertiesInterface props;
    private OsUtilsInterface osUtils;
    private ConcurrentHashMap<String, Recording> currentRecordings = new ConcurrentHashMap<String, Recording>();

    @Inject
    private Recorder(LoggerInterface thelogger, PropertiesInterface props, OsUtilsInterface osUtils) {
        this.logger = thelogger;
        this.props = props;
        this.osUtils = osUtils;

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

        String url = episode.getServiceUrl();
        String episodeName = episode.getEpisodeTitle();

        logger.info("Looking for recording of " + episode);
        Recording recording = currentRecordings.get(episodeName);
        if (recording != null) {
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


        String outDir = props.getString("recordingDir", "/opt/sagetv/server/sagetvcatchup/recordings");
        String command = "get_iplayer " + url + " --force -o " + outDir + File.separator;
        ArrayList<String> output = new ArrayList<String>();
        recording = new Recording(episode);
        currentRecordings.put(episodeName, recording);
        osUtils.spawnProcess(command, "record", false, output, null);

        // TODO need to deal with completely downloaded files

        String filename = "";
        out:
        while (true) {
            for (String result : output) {
                String prefix = "INFO: File name prefix = ";
                if (result.startsWith(prefix)) {
                    filename = outDir + File.separator + result.substring(prefix.length()).trim() + ".partial.mp4.flv";
                    logger.info("Recording to " + filename);
                    break out;
                }
            }
            logger.info("Waiting for file name...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        logger.info("Found file name " + filename);

        logger.info("Waiting for existence of " + filename);

        File file = new File(filename);
        while (!file.exists() || file.length() < (1000*1024)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        recording.setFile(file);

        synchronized (recording) {
            recording.notifyAll();
        }

        logger.info("Returning file " + file.getAbsolutePath() + " for " + url);

        return file;
    }

    public void stop(Episode episode) {
        String name = episode.getEpisodeTitle();

        Recording recording = currentRecordings.get(name);

        if (recording != null) {
            logger.info("Going to stop plaback of " + name);

            stop(recording);
        } else {
            logger.info("Cannot find recording of " + name);

        }
    }

    private void stop(Recording recording) {
        if (recording != null) {
            HashMap<String, String> processes = osUtils.processList();
            for (String process : processes.keySet()) {
                String pid = processes.get(process);
                logger.info("Checking " + pid + " " + process);
                if (process.contains(recording.getUrl())) {
                    osUtils.killOsProcess(pid, process);
                }
            }
            currentRecordings.remove(recording.getName());
        }
    }
}
