package ITVPlayer

String command = "get_flash_videos -f " + outDir + File.separator + recording.getId() + " " + recording.getUrl() ;
ArrayList<String> output = new ArrayList<String>();
EXECUTE(command, "record", false, output, null);

// TODO need to deal with completely downloaded files

String outDir = GET_STRING_PROPERTY("recordingDir", "/opt/sagetv/server/sagetvcatchup/recordings");
String filename = WAIT_FOR_OUTPUT("INFO: File name prefix = ", output, 30000)
filename = outDir + File.separator + filename.substring(prefix.length()).trim() + ".partial.mp4.flv";

LOG_INFO("Recording to " + filename);

File file = WAIT_FOR_FILE_OF_SIZE(filename, 1024000, 10000)

recording.setFile(file);


