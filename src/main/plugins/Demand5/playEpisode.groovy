package Demand5

String filename = recording.getRecordingDir() + File.separator + recording.getId() + ".flv"

String command = "get_flash_videos -y -f " + filename + " " + recording.getUrl() ;
ArrayList<String> output = new ArrayList<String>();
Process proc = EXECUTE(command, "record", output, null);

LOG_INFO("Recording to " + filename);

File file = WAIT_FOR_PARTIAL_CONTENT(filename, recording.getStopFlag())

recording.setPartialFile(file);

String completedName = filename.replace("default.partial.mp4.flv", "default.mp4");
File completedFile = new File(completedName);
recording.setCompletedFile(completedFile);

recording.setProcess(proc);


