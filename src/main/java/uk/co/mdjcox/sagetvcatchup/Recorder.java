package uk.co.mdjcox.sagetvcatchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sage.Sage;
import sage.b3;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.logger.LoggingManager;
import uk.co.mdjcox.utils.HtmlUtils;
import uk.co.mdjcox.utils.OsUtils;
import uk.co.mdjcox.utils.PropertiesFile;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private OsUtils osUtils;
    private ConcurrentHashMap<String, Recording> currentRecordings = new ConcurrentHashMap<String, Recording>();
    private final HtmlUtils htmlUtils;

    public static class Recording {
        File file;
        String fileName="";
        String url="";
        String name="";

        public Recording(String url, String name) {
            this.url = url;
            this.name = name;
        }
    }

    @Inject
    private Recorder(LoggerInterface thelogger, PropertiesInterface props, OsUtils osUtils, HtmlUtils htmlUtils) {
        this.logger = thelogger;
        this.props = props;
        this.osUtils = osUtils;
        this.htmlUtils = htmlUtils;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (Recording recording : currentRecordings.values()) {
                    try {
                        logger.info("Shutting down - stopping " + recording.name);

                        stop(recording.url, recording.name );
                    } catch (Exception e) {
                        logger.info("Failed to stop " + recording.name);
                    }
                }
            }
        }));
    }

    public File start(String url, String episodeName) throws Exception {
        logger.info("Looking for recording of " + episodeName);
        Recording recording = currentRecordings.get(episodeName);
        if (recording != null) {
            logger.info("Recording in progress for " + episodeName);
            if (recording.file == null) {
                synchronized (recording) {
                    recording.wait();
                }
            }
            logger.info("Returning file " + recording.fileName + " for " + url);
            return recording.file;
        }

        logger.info("Starting recording of " + url);


        String outDir = props.getString("recordingDir", "/opt/sagetv/server/sagetvcatchup/recordings");
        String command = "get_iplayer " + url + " --force -o " + outDir + File.separator;
        ArrayList<String> output = new ArrayList<String>();
        recording = new Recording(url, episodeName);
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

        recording.file = file;

        synchronized (recording) {
            recording.notifyAll();
        }

        logger.info("Returning file " + file.getAbsolutePath() + " for " + url);

        return file;
    }

    public void stop(Map map) {
        // TODO make this more robust
        String value = map.toString();
        value = htmlUtils.moveTo("MediaFile[", value);
        value = htmlUtils.moveTo("\"", value);
        String name = htmlUtils.extractTo("\"", value);
        name = htmlUtils.makeContentSafe(name);

        Recording recording = currentRecordings.get(name);

        if (recording != null) {
            logger.info("Going to stop plaback of " + name);
        } else {
            logger.info("Cannot find recording of " + name);

        }
    }

    public void stop(String url, String episodeName) {
        Recording recording = currentRecordings.get(episodeName);
        if (recording != null) {
            HashMap<String, String> processes = osUtils.processList();
            for (String process : processes.keySet()) {
                String pid = processes.get(process);
                logger.info("Checking " + pid + " " + process);
                if (process.contains(recording.url)) {
                    osUtils.killOsProcess(pid, process);
                }
            }
            currentRecordings.remove(episodeName);
        }
    }
}
