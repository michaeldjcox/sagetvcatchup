/**
 * TmpFileManager.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.sagetv.utils;




import java.io.File;
import java.util.ArrayList;


public class TmpFileManager {

    private static ArrayList<String> tmpFiles = new ArrayList<String>();

    public static void addTmpFile(String file) {
        tmpFiles.add(0, file);
    }

    /**
     * Deletes the specfied file or directory including any children.
     *
     * @param fileOrDir the file or directory to delete
     *
     * @return <code>true</code> is successful
     */
    private static boolean deleteFileOrDir(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            String[] children = fileOrDir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteFileOrDir(
                        new File(fileOrDir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return fileOrDir.delete();
    }

    public static void housekeep(final LoggerInterface logger, final boolean later) {
        final ArrayList<String> hkpFiles = new ArrayList<String>();
        hkpFiles.addAll(tmpFiles);
        tmpFiles.clear();

        Thread thread = new Thread("tidy-thread") {

            public void run() {
                long sleepTime=1000;
                if (later) {
                    sleepTime = 10000;
                }
                for (String fileName : hkpFiles) {
                    File file = new File(fileName);
                    if (file.exists()) {
                        logger.info("Tidy up file " + file);
                        deleteFileOrDir(file);
                    }
                }
            }
        };
        if (later) {
            thread.start();
        } else {
            thread.run();
        }
    }
}


