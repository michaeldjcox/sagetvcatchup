package uk.co.mdjcox.sagetv.utils;

import sun.net.www.protocol.http.HttpURLConnection;

import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
* Created by michael on 14/11/14.
*/
public class DownloadInterruptThread extends Thread {

  private static class Download {
    private long timeoutTime;
    private URLConnection con;
    private AtomicBoolean stopFlag;

    private Download(long timeoutTime, URLConnection con, AtomicBoolean stopFlag) {
      this.timeoutTime = timeoutTime;
      this.con = con;
      this.stopFlag = stopFlag;
    }

    private boolean tryDisconnect() {

      if (System.currentTimeMillis() > timeoutTime) {
        disconnect();
        return true;
      }

      if (stopFlag != null && stopFlag.get()) {
        disconnect();
        return true;
      }

      return false;
    }

    private void disconnect() {
      try {
        ((HttpURLConnection)con).disconnect();
      } catch (Exception e) {
        // Ignore
      }
    }
  }

  private ConcurrentHashMap<String, Download> queue = new ConcurrentHashMap<String, Download>();

  public DownloadInterruptThread() {
    super("download-interrupt-thread");
    setDaemon(true);
  }

  public void addDownload(URLConnection con, AtomicBoolean stopFlag, int timeout) {
    queue.put(con.getURL().toString() + "-" + timeout,  new Download(System.currentTimeMillis() + timeout, con, stopFlag));
  }

  public void run() {
    while (true) {
      try {
        Iterator<Map.Entry<String, Download>> itr = queue.entrySet().iterator();
        while (itr.hasNext()) {
          Map.Entry<String, Download> downloadEntry = itr.next();

          Download download = downloadEntry.getValue();

          boolean disconnected = download.tryDisconnect();

          if (disconnected) {
            itr.remove();
          }
        }

        Thread.sleep(2000);

      } catch (Exception e) {
        // Ignore
      }
    }
  }
}
