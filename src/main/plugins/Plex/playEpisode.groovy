package Plex

String filename = recording.getRecordingDir() + File.separator + recording.getId() + ".mov.part"

DOWNLOAD_WEB_PAGE_BACKGROUND(new URL(recording.getUrl()), filename, recording.getStopFlag());

File partialFile = WAIT_FOR_PARTIAL_CONTENT(filename, recording.getStopFlag())

recording.setPartialFile(partialFile);

String completedFilename = recording.getRecordingDir() + File.separator + recording.getId() + ".mov";

File completedFile = new File(completedFilename);
recording.setCompletedFile(completedFile);



