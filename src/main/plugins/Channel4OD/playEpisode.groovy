package Channel4OD

String filename = recording.getRecordingDir() + File.separator + recording.getId() + ".flv"

String command = "get_flash_videos -y -f " + filename + " " + recording.getUrl() ;
ArrayList<String> output = new ArrayList<String>();
EXECUTE(command, "record", output, null);

LOG_INFO("Recording to " + filename);

File file = WAIT_FOR_PARTIAL_CONTENT(filename, recording.getStopFlag())

recording.setPartialFile(file);

// Needs to be like Iplayer
// recording.setCompletedFile(file)
