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

String prefix = "INFO: File name prefix = "
String filename = WAIT_FOR_PARTIAL_FILE(prefix, output, recording.getStopFlag())

filename = recording.getRecordingDir() + File.separator + filename.substring(prefix.length()).trim() + ".partial.mp4.flv";

File file = WAIT_FOR_PARTIAL_CONTENT(filename, recording.getStopFlag())

recording.setPartialFile(file);

TRACK_PROGRESS(".*kB.*sec.*", "^.*\\(", "\\)", errors, recording);

String completedName = filename.replace("default.partial.mp4.flv", "default.mp4");
File completedFile = new File(completedName);
recording.setCompletedFile(completedFile);

recording.setProcess(proc);


