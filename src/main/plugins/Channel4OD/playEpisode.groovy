package Channel4OD

String outDir = GET_STRING_PROPERTY("recordingDir", "/opt/sagetv/server/catchup/recordings");
String filename = outDir + File.separator + recording.getId() + ".flv"

String command = "get_flash_videos -y -f " + filename + " " + recording.getUrl() ;
ArrayList<String> output = new ArrayList<String>();
EXECUTE(command, "record", output, null);

// TODO need to deal with completely downloaded files

LOG_INFO("Recording to " + filename);

File file = WAIT_FOR_FILE_OF_SIZE(filename, 1024000, 10000)

recording.setFile(file);


