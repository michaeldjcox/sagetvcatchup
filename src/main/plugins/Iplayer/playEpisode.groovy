package Iplayer

iplayerCmd = BUILD_RECORDING_COMMAND(recording);

ArrayList<String> output = new ArrayList<String>();
ArrayList<String> errors = new ArrayList<String>();
Process proc = EXECUTE(iplayerCmd, "get_iplayer", output, errors);

String prefix = "INFO: File name prefix = "
String filename = WAIT_FOR_PARTIAL_FILE(prefix, output, recording.getStopFlag())

filename = recording.getRecordingDir() + File.separator + filename.substring(prefix.length()).trim() + ".video.ts";

File file = WAIT_FOR_PARTIAL_CONTENT(filename, recording.getStopFlag())

recording.setPartialFile(file);

TRACK_PROGRESS(".*kB.*sec.*", "^.*\\(", "\\)", errors, recording);

String completedName = filename.replace(".video.ts", ".mp4");
File completedFile = new File(completedName);
recording.setCompletedFile(completedFile);

recording.setProcess(proc);


