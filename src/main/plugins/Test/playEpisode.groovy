package Test

import java.nio.file.Files
import java.nio.file.StandardCopyOption

String filename = recording.getRecordingDir() + File.separator + recording.getId() + ".partial.mp4.flv";
File file = new File(filename);
recording.setPartialFile(file);

String pluginsDir = GET_PLUGIN_DIR() + "Test";

File sourcePath = new File(pluginsDir, recording.getId() + ".mp4");

Files.copy(sourcePath.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

String completedName = recording.getRecordingDir() + File.separator + recording.getId() + ".mp4";
File completedFile = new File(completedName);
recording.setCompletedFile(completedFile);

Thread thread = new Thread(new Runnable() {
    @Override
    void run() {
        try {
            Thread.sleep(10000);
        } catch (Exception ex) {
        }

        LOG_INFO("TEST RECORDING IS COMPLETE");

        if (file.exists()) {
            Files.copy(file.toPath(), completedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

    }
}).start();



//recording.setProcess(proc);


