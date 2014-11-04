package Iplayer

String iplayerDir = GET_STRING_PROPERTY("Iplayer.scriptDir");
String iplayerCmd = GET_STRING_PROPERTY("Iplayer.command");

String command = iplayerCmd + " " + recording.getUrl() + " --attempts 0 --force -o " + recording.getRecordingDir() + File.separator;

if (IS_WINDOWS()) {
    String relative = GET_RELATIVE_PATH(iplayerDir, recording.getRecordingDir());
    command = "cmd.exe /c \"" + "cd " + iplayerDir + " && "+ iplayerCmd + " " + recording.getUrl() + " --attempts 0 --force -o " + relative + File.separator + "\"";
}

ArrayList<String> output = new ArrayList<String>();
ArrayList<String> errors = new ArrayList<String>();
Process proc = EXECUTE(command, "get_iplayer", output, errors);

long TIMEOUT = 30000;

String prefix = "INFO: File name prefix = "
String filename = WAIT_FOR_OUTPUT(prefix, output, TIMEOUT, recording.getStopFlag())

if (filename == null || filename.trim().isEmpty()) {
    throw new Exception("get_iplayer returned no file after " + TIMEOUT);
}

filename = recording.getRecordingDir() + File.separator + filename.substring(prefix.length()).trim() + ".partial.mp4.flv";

File file = WAIT_FOR_FILE_OF_SIZE(filename, 1024000, 10000, recording.getStopFlag())

recording.setPartialFile(file);

TRACK_PROGRESS(".*kB.*sec.*", errors, recording);

String completedName = filename.replace("default.partial.mp4.flv", "default.mp4");
File completedFile = new File(completedName);
recording.setCompletedFile(completedFile);

recording.setProcess(proc);


