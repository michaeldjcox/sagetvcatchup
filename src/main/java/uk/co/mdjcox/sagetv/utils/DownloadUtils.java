package uk.co.mdjcox.sagetv.utils;

import sun.net.www.protocol.http.HttpURLConnection;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 07:10
 * To change this template use File | Settings | File Templates.
 */
public class DownloadUtils implements DownloadUtilsInterface {

  private static final String STOPPED_ON_REQUEST = "Stopped on request";
  private static final int DOWNLOAD_TIMEOUT = 30000;
  private static DownloadInterruptThread interruptThread;
  private static DownloadUtilsInterface instance;
    private final String DEFAULT_ENCODING = "UTF-8";

    public static synchronized DownloadUtilsInterface instance() {
        if (instance == null) {
            instance = new DownloadUtils();
        }
        return instance;
    }

  private DownloadUtils() {
    interruptThread = new DownloadInterruptThread();
    interruptThread.start();
  }

  @Override
    public String sampleFileString(String source) throws Exception {
        return downloadFileString(source, DEFAULT_ENCODING, true, DOWNLOAD_TIMEOUT, 1, null);

    }

    @Override
    public String sampleFileString(String source, String encoding) throws Exception {
        return downloadFileString(source, encoding, true, DOWNLOAD_TIMEOUT, 1, null);
    }


    @Override
    public String downloadFileString(String source, AtomicBoolean stopFlag) throws Exception {
        return downloadFileString(source, DEFAULT_ENCODING, stopFlag);
    }

    @Override
    public String downloadFileString(String source, String encoding, AtomicBoolean stopFlag) throws Exception {
        return downloadFileString(source, encoding, false, DOWNLOAD_TIMEOUT, 1, stopFlag);
    }

  public String downloadFileString(String source, int timeout, int attempts, AtomicBoolean stopFlag) throws Exception {
    return downloadFileString(source, DEFAULT_ENCODING, false, timeout, attempts, stopFlag);
  }

    private String downloadFileString(String source, String encoding, boolean sample, int timeout, int attempts, AtomicBoolean stopFlag) throws Exception {
      for (int i =0 ; i < attempts; i++) {
        String webpage = "";
        InputStream stream = null;
        BufferedInputStream bis = null;
        URLConnection conn = null;
        try {
          URL url = new URL(source);

          conn = url.openConnection();
          conn.setReadTimeout(timeout);
          conn.setConnectTimeout(timeout);
          interruptThread.addDownload(conn, stopFlag, timeout);
          conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.52 Safari/537.17");
//            conn.setRequestProperty("Accept-Encoding", "tgzip,deflate,sdch");
          stream = conn.getInputStream();
          bis = new BufferedInputStream(stream);
          byte[] buf = new byte[1024];
          int len;
          while ((len = bis.read(buf)) > 0) {
            webpage += new String(buf, 0, len, Charset.forName(encoding));
            if (sample) break;
            if (stopFlag.get()) {
              throw new RuntimeException(STOPPED_ON_REQUEST);
            }
          }
          return webpage;
        } catch (Exception ex) {
          String message = ex.getMessage();
          if (message == null) {
            message = ex.getClass().getSimpleName();
          }
          if (!message.contains("Bogus chunk") && !message.contains("missing CR")) {
            if (message.equals(STOPPED_ON_REQUEST) || (i == (attempts-1))) {
              throw ex;
            } else {
              continue;
            }
          }
          return webpage;
        } finally {
          if (bis != null) {
            try {
              bis.close();
            } catch (IOException e) {
              //Ignore
            }
          }
          if (stream != null) {
            try {
              stream.close();
            } catch (IOException e) {
              // Ignore
            }
          }
          if (conn != null && conn instanceof HttpURLConnection) {
            try {
              ((HttpURLConnection) conn).disconnect();
            } catch (Exception e) {
              // Ignore
            }
          }
        }
      }

      throw new Exception("Failed to download web page " + source );
    }


  /**
     * Downloads the file at the specfied URL and stores in in the specified
     * path.
     *
     * @param url  the URL of the resource to download
     * @param file the path to save the content at
     * @throws IOException any exception thrown
     */
    @Override
    public void downloadFile(URL url, String file) throws IOException {
        URLConnection uc = url.openConnection();
        String contentType = uc.getContentType();
        int contentLength = uc.getContentLength();
        InputStream raw = uc.getInputStream();
        InputStream in = new BufferedInputStream(raw);
        FileOutputStream out = new FileOutputStream(file);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        out.flush();
        in.close();
        out.close();
        TmpFileManager.addTmpFile(file);
    }

}
