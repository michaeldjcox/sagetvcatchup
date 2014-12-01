package uk.co.mdjcox.sagetv.utils;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 02/04/13
 * Time: 07:58
 * To change this template use File | Settings | File Templates.
 */
public interface DownloadUtilsInterface {
    String sampleFileString(String source) throws Exception;

    String sampleFileString(String source, String encoding) throws Exception;

    String downloadFileString(String source, AtomicBoolean stopFlag) throws Exception;

    String downloadFileString(String source, String encoding, AtomicBoolean stopFlag) throws Exception;

    String downloadFileString(String source, int timeout, int attempts, AtomicBoolean stopFlag) throws Exception;

    void downloadFile(URL url, String file) throws IOException;
}
