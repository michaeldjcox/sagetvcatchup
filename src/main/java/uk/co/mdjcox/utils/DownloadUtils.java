package uk.co.mdjcox.utils;

import sun.net.www.protocol.http.HttpURLConnection;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 07:10
 * To change this template use File | Settings | File Templates.
 */
public class DownloadUtils implements DownloadUtilsInterface {

  private static final int DOWNLOAD_TIMEOUT = 30000;
  private static DownloadUtilsInterface instance;
    private final String DEFAULT_ENCODING = "UTF-8";

    public static DownloadUtilsInterface instance() {
        if (instance == null) {
            instance = new DownloadUtils();
        }
        return instance;
    }

    @Override
    public String sampleFileString(String source) throws Exception {
        return downloadFileString(source, DEFAULT_ENCODING, true);

    }

    @Override
    public String sampleFileString(String source, String encoding) throws Exception {
        return downloadFileString(source, encoding, true);
    }


    @Override
    public String downloadFileString(String source) throws Exception {
        return downloadFileString(source, DEFAULT_ENCODING);
    }

    @Override
    public String downloadFileString(String source, String encoding) throws Exception {
        return downloadFileString(source, encoding, false);
    }

    private String downloadFileString(String source, String encoding, boolean sample) throws Exception {
        String nowPlaying = "";
        InputStream stream = null;
        BufferedInputStream bis = null;
      URLConnection conn = null;
      try {
            URL url = new URL(source);

             conn = url.openConnection();
          conn.setReadTimeout(DOWNLOAD_TIMEOUT);
          conn.setConnectTimeout(DOWNLOAD_TIMEOUT);
           new Thread(new InterruptThread(Thread.currentThread(), conn)).start();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.52 Safari/537.17");
//            conn.setRequestProperty("Accept-Encoding", "tgzip,deflate,sdch");
            stream = conn.getInputStream();
            bis = new BufferedInputStream(stream);
            byte[] buf = new byte[1024];
            int len;
            while ((len = bis.read(buf)) > 0) {
                nowPlaying += new String(buf, 0, len, Charset.forName(encoding));
                if (sample) break;
            }
            return nowPlaying;
        } catch (IOException ex) {
            if (!ex.getMessage().contains("Bogus chunk") && !ex.getMessage().contains("missing CR")) {
                throw ex;
            }
            return nowPlaying;
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
            ((HttpURLConnection)conn).disconnect();
          } catch (Exception e) {
            // Ignore
          }
        }
        }
    }

  public class InterruptThread implements Runnable {
    Thread parent;
    URLConnection con;
    public InterruptThread(Thread parent, URLConnection con) {
      this.parent = parent;
      this.con = con;
    }

    public void run() {
      try {
        Thread.sleep(DOWNLOAD_TIMEOUT);
      } catch (InterruptedException e) {

      }
      try {
        ((HttpURLConnection)con).disconnect();
      } catch (Exception e) {
        // Ignore
      }
    }
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
