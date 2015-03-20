package PlayOn

String filename = recording.getRecordingDir() + File.separator + recording.getId() + ".mov.part"
String completedFilename = recording.getRecordingDir() + File.separator + recording.getId() + ".mov";

DOWNLOAD_WEB_PAGE_BACKGROUND(new URL(recording.getUrl()), filename, completedFilename, recording.getStopFlag());

File partialFile = WAIT_FOR_PARTIAL_CONTENT(filename, recording.getStopFlag())

recording.setPartialFile(partialFile);

File completedFile = new File(completedFilename);
recording.setCompletedFile(completedFile);



