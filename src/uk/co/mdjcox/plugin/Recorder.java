package uk.co.mdjcox.plugin;

import com.google.inject.Inject;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.logger.LoggingManager;
import uk.co.mdjcox.utils.OsUtils;
import uk.co.mdjcox.utils.PropertiesFile;
import uk.co.mdjcox.utils.PropertiesInterface;

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
public class Recorder {

    private final LoggerInterface logger;
    private PropertiesInterface props;
    private OsUtils osUtils;
    private ConcurrentHashMap<String, File> currentRecordings = new ConcurrentHashMap<String, File>();

    @Inject
    private Recorder(LoggerInterface thelogger, PropertiesInterface props, OsUtils osUtils) {
        this.logger = thelogger;
        this.props = props;
        this.osUtils = osUtils;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (String url : currentRecordings.keySet()) {
                    try {
                        logger.info("Shutting down - stopping " + url);

                        stop(url);
                    } catch (Exception e) {
                        logger.info("Failed to stop " + url);
                    }
                }
            }
        }));
    }

    public File start(String url) throws Exception {
        File file = currentRecordings.get(url);
        if (file != null) {
            return file;
        }
        String outDir = props.getString("recordingDir", "/home/michael/Documents/CatchUp/recordings/");
        String command = "get_iplayer " + url + " -o " + outDir;
        ArrayList<String> output = new ArrayList<String>();
        currentRecordings.put(url, new File(""));
        osUtils.spawnProcess(command, "record", false, output, null);

        String filename = "";
        out:
        while (true) {
            for (String result : output) {
                String prefix = "INFO: File name prefix = ";
                if (result.startsWith(prefix)) {
                    filename = result.substring(prefix.length()).trim() + ".partial.mp4.flv";
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

        file = new File(filename);
        while (!file.exists()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        currentRecordings.put(url, file);
        return file;
    }

    public void stop(String url) {
        HashMap<String, String> processes = osUtils.processList();
        for (String process : processes.keySet()) {
            String pid = processes.get(process);
            logger.info("Checking " + pid + " " + process);
            if (process.contains(url)) {
                osUtils.killOsProcess(pid, process);
            }
        }
        currentRecordings.remove(url);

    }

    public static void main(String[] args) throws Exception {
        LoggerInterface logger = LoggingManager.getLogger(Recorder.class, "Recorder", "logs");
        LoggingManager.addConsole(logger);

        PropertiesInterface props = new PropertiesFile();
        Recorder recorder = new Recorder(logger, props, OsUtils.instance(logger));
        String url = "http://www.bbc.co.uk/iplayer/episode/b01rgpj6/The_A_to_Z_of_TV_Gardening_Letter_K/";
        recorder.start(url);
        Thread.sleep(30000);
        recorder.stop(url);


    }

}
