package Iplayer

String outDir = GET_STRING_PROPERTY("recordingDir", "/opt/sagetv/server/catchup/recordings");
String command = "get_iplayer " + recording.getUrl() + " --force -o " + outDir + File.separator;
ArrayList<String> output = new ArrayList<String>();
EXECUTE(command, "record", output, null);

// TODO need to deal with completely downloaded files

String prefix = "INFO: File name prefix = "
String filename = WAIT_FOR_OUTPUT(prefix, output, 30000)
filename = outDir + File.separator + filename.substring(prefix.length()).trim() + ".partial.mp4.flv";

LOG_INFO("Recording to " + filename);

File file = WAIT_FOR_FILE_OF_SIZE(filename, 1024000, 10000)

recording.setFile(file);


