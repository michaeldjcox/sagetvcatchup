package Iplayer

// TODO Should I need to pass in get_iplayer dir if PATH is properly set up
String getIplayerDir = GET_STRING_PROPERTY("Iplayer.scriptDir");
String getIplayerCmd = GET_STRING_PROPERTY("Iplayer.command");

String command = "cd " + getIplayerDir + " && "+ getIplayerCmd + " " + recording.getUrl() + " --attempts 0 --force -o " + recording.getRecordingDir() + File.separator;

ArrayList<String> output = new ArrayList<String>();
Process proc = EXECUTE(command, "record", output, null);

long TIMEOUT = 30000;

String prefix = "INFO: File name prefix = "
String filename = WAIT_FOR_OUTPUT(prefix, output, TIMEOUT)

if (filename == null || filename.trim().isEmpty()) {
    throw new Exception("get_iplayer returned no file after " + TIMEOUT);
}

filename = recording.getRecordingDir() + File.separator + filename.substring(prefix.length()).trim() + ".partial.mp4.flv";

LOG_INFO("Recording to " + filename);

File file = WAIT_FOR_FILE_OF_SIZE(filename, 1024000, 10000)

recording.setFile(file);

recording.setProcess(proc);


