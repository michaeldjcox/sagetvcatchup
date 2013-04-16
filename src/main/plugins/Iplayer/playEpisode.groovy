package Iplayer

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 15/04/13
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */

ArrayList<String> output = new ArrayList<String>();
String outDir = GET_STRING_PROPERTY("recordingDir", "/opt/sagetv/server/sagetvcatchup/recordings");
String command = "get_iplayer " + recording.getUrl() + " --force -o " + outDir + File.separator;
RUN(command, "record", false, output, null);

// TODO need to deal with completely downloaded files

String filename = "";
out:
while (true) {
    for (String result : output) {
        String prefix = "INFO: File name prefix = ";
        if (result.startsWith(prefix)) {
            filename = outDir + File.separator + result.substring(prefix.length()).trim() + ".partial.mp4.flv";
            LOG_INFO("Recording to " + filename);
            break out;
        }
    }
    LOG_INFO("Waiting for file name...");
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {

    }
}

LOG_INFO("Found file name " + filename);

LOG_INFO("Waiting for existence of " + filename);

File file = new File(filename);
while (!file.exists() || file.length() < (1000*1024)) {
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {

    }
}

recording.setFile(file);


