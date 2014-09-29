package Demand5

String filename = recording.getRecordingDir() + File.separator + recording.getId() + ".flv"

String command = "get_flash_videos -y -f " + filename + " " + recording.getUrl() ;
ArrayList<String> output = new ArrayList<String>();
EXECUTE(command, "record", output, null);

LOG_INFO("Recording to " + filename);

File file = WAIT_FOR_FILE_OF_SIZE(filename, 1024000, 10000)

recording.setPartialFile(file);

// TODO set the complete file


