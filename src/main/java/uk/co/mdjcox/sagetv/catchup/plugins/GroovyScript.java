package uk.co.mdjcox.sagetv.catchup.plugins;


import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.ErrorRecorder;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.LoggerInterface;
import uk.co.mdjcox.utils.OsUtilsInterface;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 31/03/13
 * Time: 00:11
 * To change this template use File | Settings | File Templates.
 */
public abstract class GroovyScript extends groovy.lang.Script {

    private LoggerInterface logger;
    private HtmlUtilsInterface htmlUtils;
    private DownloadUtilsInterface downloadUtils;
    private OsUtilsInterface osUtils;
    private CatchupContextInterface context;

    public void setLogger(LoggerInterface logger) {
        this.logger = logger;
    }

    public void setHtmlUtils(HtmlUtilsInterface htmlUtils) {
        this.htmlUtils = htmlUtils;
    }

    public void setDownloadUtils(DownloadUtilsInterface downloadUtils) {
        this.downloadUtils = downloadUtils;
    }

    public void setOsUtils(OsUtilsInterface osUtils) {
        this.osUtils = osUtils;
    }

    public void setContext(CatchupContextInterface context) {
      this.context = context;
    }

    /* From here on down are methods available to the plugin user */

    public String GET_WEB_PAGE(String url, AtomicBoolean stopFlag) throws Exception {
        return downloadUtils.downloadFileString(url, 30000, 2, stopFlag);
    }

    public String REPLACE_LINK_PREFIX(String existingUrl, String newPrefix) {
        existingUrl = existingUrl.substring(existingUrl.lastIndexOf('/'));

        if (newPrefix.endsWith("/")) {
            newPrefix = newPrefix.substring(0, newPrefix.length()-1);
        }
        return newPrefix + existingUrl;
    }

    public String REPLACE_LINK_PREFIX(String existingUrl, String oldPrefix, String newPrefix) {
        return existingUrl.replaceFirst(oldPrefix, newPrefix);
    }

  public String REPLACE_LINK_TARGET(String existingUrl, String newSuffix) {
    existingUrl = existingUrl.substring(0, existingUrl.indexOf("/"));
    return existingUrl + newSuffix;
  }

  public String BUILD_RECORDING_COMMAND(Recording recording) {
    final String sourceId = recording.getSourceId();
    String command = context.getProperties().getString(sourceId +".command");
    command = command.replace("<URL>", recording.getUrl());
    command = command.replace("<DIR>", recording.getRecordingDir());
    return command;
  }

    public String SAMPLE_WEB_PAGE(String url) throws Exception {
        return downloadUtils.sampleFileString(url);
    }

    public void DOWNLOAD_WEB_PAGE(URL url, String file) throws IOException {
        downloadUtils.downloadFile(url, file);
    }

    public String MOVE_TO(String token, String fileStr) {
        return htmlUtils.moveTo(token, fileStr);
    }

    public String REMOVE_HTML_TAGS(String html) {
        return htmlUtils.removeHtml(html);
    }

    public String MAKE_LINK_ABSOLUTE(String base, String relative) {
        return htmlUtils.makeLinkAbsolute(base, relative);
    }

    public String MAKE_ID(String id) {
        return htmlUtils.makeIdSafe(id);
    }

    public String MAKE_HTML_SAFE(String id) {
        return htmlUtils.makeContentSafe(id);
    }

    public String EXTRACT_TO(String token, String fileStr) {
        return htmlUtils.extractTo(token, fileStr);
    }

    public boolean CONTAINS_TOKEN(String token, String fileStr) {
        return htmlUtils.hasToken(token, fileStr);
    }


    public Process EXECUTE(String osCommand, String loggerName) throws Exception {
        return osUtils.spawnProcess(osCommand, loggerName, false, null);
    }
    public Process EXECUTE_AND_WAIT(String osCommand, String loggerName) throws Exception {
        return osUtils.spawnProcess(osCommand, loggerName, true, null);
    }

    public Process EXECUTE(String osCommand, String loggerName, ArrayList<String> output, ArrayList<String> errors) throws Exception {
        return osUtils.spawnProcess(osCommand, loggerName, false, output, errors, null);
    }

    public Process EXECUTE_AND_WAIT(String osCommand, String loggerName, ArrayList<String> output, ArrayList<String> errors) throws Exception {
        return osUtils.spawnProcess(osCommand, loggerName, true, output, errors, null);
    }

    public void KILL(String pid, String cmd) {
        osUtils.killProcess(pid, cmd);
    }

    public void KILL_CONTAINING(String expression) {
        osUtils.killProcessesContaining(expression);
    }

  public void KILL_MATCHING(String regex) {
    osUtils.killProcessesMatching(regex);
  }


  public HashMap<String, String> GET_PROCESSES() {
        return osUtils.getProcesses();
    }

    public String GET_STRING_PROPERTY(String token) {
        return context.getProperties().getString(token);
    }

    public String GET_STRING_PROPERTY(String token, String defaultString) {
        return context.getProperties().getString(token, defaultString);
    }

    public int GET_INT_PROPERTY(String token) {
        return context.getProperties().getInt(token);
    }

    public int GET_INT_PROPERTY(String token, int defaultValue) {
        return context.getProperties().getInt(token, defaultValue);
    }

    public boolean GET_BOOLEAN_PROPERTY(String token) {
        return context.getProperties().getBoolean(token);
    }

    public boolean GET_BOOLEAN_PROPERTY(String token, boolean defaultValue) {
        return context.getProperties().getBoolean(token, defaultValue);
    }

    public void LOG_ERROR(String msg) {
        logger.error(msg);
    }

    public void LOG_ERROR(ErrorRecorder item, String message) {
      item.addError("ERROR", message);
      logger.error(message);
    }

    public void LOG_ERROR(String msg, Throwable thrown) {
        logger.error(msg, thrown);
    }

    public void LOG_WARNING(String msg) {
        logger.warn(msg);
    }

    public void LOG_WARNING(String msg, Throwable thrown) {
        logger.warn(msg, thrown);
    }

  public void LOG_WARNING(ErrorRecorder item, String message) {
    item.addError("WARNING", message);
    logger.warn(message);
  }

    public void LOG_INFO(String msg) {
        logger.info(msg);
    }

    public void WAIT_FOR(long millis) {
      osUtils.waitFor(millis);
    }

    public String[] SPLIT(String string, String regex) {
      if (string == null) {
        return new String[0];
      } else {
        return string.split(regex);
      }
    }

    public File WAIT_FOR_PARTIAL_CONTENT(String filename, AtomicBoolean stopFlag) {
      long timeoutMillis = context.getPartialSizeForStreamingTimeout();
      long atLeastSize = context.getPartialSizeForStreaming();

        logger.info("Waiting for " + filename + " to attain size of " + atLeastSize);
        long stopTime = System.currentTimeMillis() + timeoutMillis;
        File file = new File(filename);
        while (!file.exists() || (file.length() < atLeastSize)) {
            if (stopFlag.get()) {
                break;
            }
            WAIT_FOR(1000);
            if (System.currentTimeMillis() > stopTime) break;
        }
        return file;
    }

    public String WAIT_FOR_PARTIAL_FILE(String prefix, ArrayList<String> output, AtomicBoolean stopFlag) throws Exception {
        long timeoutMillis = context.getPartialFileNameConfirmationTimeout();

        long stopTime = System.currentTimeMillis() + timeoutMillis;

        LOG_INFO("Waiting for " + timeoutMillis + "ms for '" + prefix + "' in job output");

        out:
        while (System.currentTimeMillis() < stopTime) {
            for (String result : output) {

                if (stopFlag.get()) {
                    break;
                }

                if (result.startsWith(prefix)) {
                  LOG_INFO("Partial file name prefix will be " + result);
                  return result;
                }
            }

            if (stopFlag.get()) {
                break;
            }

            WAIT_FOR(1000);
        }

        throw new Exception("get_iplayer returned no file after timeout");
    }

  public void TRACK_PROGRESS(final String regex, final String prefix, final String suffix, final ArrayList<String> output, final Recording recording) {
    Runnable runnable = new Runnable() {

      public void run() {
        int last = 0;
        while (!recording.getStopFlag().get()) {
          int i = 0;
          for (i = last; i < output.size(); i++) {
            String result = output.get(i);
            if (result.matches(regex)) {
              result = result.replaceAll(prefix, "");
              result = result.replaceAll(suffix, "");

              recording.setPercentRecorded(result);
            } else {
              recording.setProgress(result);
            }
          }
          last = i;
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // Ignore
          }
        }
      }
    };
    new Thread(runnable, "recording-tracker-" + recording.getId()).start();
  }

    public boolean IS_WINDOWS() {
        return osUtils.isWindows();
    }

    public String GET_RELATIVE_PATH(String absoluteStart, String absoluteFinish) {
        File start = new File(absoluteStart);
        File finish = new File(absoluteFinish);
        return start.toPath().relativize(finish.toPath()).toString();
    }

    public String GET_PLUGIN_DIR() {
      String pluginDir = context.getPluginDir();
      if (!pluginDir.endsWith(File.separator)) {
        pluginDir += File.separator;
      }
      return pluginDir;
    }

    public String FIX_DATE(String sourceFormat, String date) {
      SimpleDateFormat sourceFormatter = new SimpleDateFormat(sourceFormat);
      SimpleDateFormat catchupFormatter = new SimpleDateFormat("dd-MM-yyyy");
      sourceFormatter.setTimeZone(TimeZone.getTimeZone("Europe/London"));
      catchupFormatter.setTimeZone(TimeZone.getTimeZone("Europe/London"));

      try {
        Date parseDate = sourceFormatter.parse(date);
        return catchupFormatter.format(parseDate);
      } catch (Exception e) {
        return null;
      }

    }

  public String FIX_TIME(String sourceFormat, String time) {
    SimpleDateFormat sourceFormatter = new SimpleDateFormat(sourceFormat);
    SimpleDateFormat catchupFormatter = new SimpleDateFormat("HH:mm:ss");
    sourceFormatter.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    catchupFormatter.setTimeZone(TimeZone.getTimeZone("Europe/London"));

    try {
      Date parseTime = sourceFormatter.parse(time);
      return catchupFormatter.format(parseTime);
    } catch (Exception e) {
      return null;
    }

  }

  public boolean DATE_AFTER(String oldDate, String oldTime, String newDate, String newTime) {
    SimpleDateFormat catchupFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    catchupFormatter.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    try {
      Date oldDateTime = catchupFormatter.parse(oldDate + " " + oldTime);
      Date newDateTime = catchupFormatter.parse(newDate + " " + newTime);
      return newDateTime.after(oldDateTime);
    } catch (Exception e) {
      logger.warn("Failed to parse comparison dates: " + oldDate + " " + oldTime + " and " + newDate + " " + newTime);
      return false;
    }
  }
}