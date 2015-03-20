package Plex

String filename = recording.getRecordingDir() + File.separator + recording.getId() + ".mov.part"
String completedFilename = recording.getRecordingDir() + File.separator + recording.getId() + ".mov";

File partialFile = new File(filename);
recording.setPartialFile(partialFile);

DOWNLOAD_WEB_PAGE_BACKGROUND(new URL(recording.getUrl()), filename, completedFilename, recording.getStopFlag());

partialFile = WAIT_FOR_PARTIAL_CONTENT(filename, recording.getStopFlag())
recording.setPartialFile(partialFile);

File completedFile = new File(completedFilename);
recording.setCompletedFile(completedFile);



