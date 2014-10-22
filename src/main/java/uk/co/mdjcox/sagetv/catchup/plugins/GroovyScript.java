package uk.co.mdjcox.sagetv.catchup.plugins;


import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.ErrorRecorder;
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

    public String GET_WEB_PAGE(String url) throws Exception {
        return downloadUtils.downloadFileString(url);
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

    public File WAIT_FOR_FILE(String filename, long timeoutMillis, AtomicBoolean stopFlag) {
        logger.info("Waiting for existence of " + filename);
        long stopTime = System.currentTimeMillis() + timeoutMillis;
        File file = new File(filename);
        while (!file.exists() || (System.currentTimeMillis() > stopTime)) {
            if (stopFlag.get()) {
                break;
            }
            WAIT_FOR(1000);
        }
        return file;
    }

    public File WAIT_FOR_FILE_OF_SIZE(String filename, long atLeastSize, long timeoutMillis, AtomicBoolean stopFlag) {
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

    public String WAIT_FOR_OUTPUT(String prefix, ArrayList<String> output, long timeoutMillis, AtomicBoolean stopFlag) {
        long stopTime = System.currentTimeMillis() + timeoutMillis;
        out:
        while (System.currentTimeMillis() < stopTime) {
            for (String result : output) {

                if (stopFlag.get()) {
                    break;
                }

                if (result.startsWith(prefix)) {
                    return result;
                }
            }

            if (stopFlag.get()) {
                break;
            }

            LOG_INFO("Waiting for '" + prefix + "' in job output");

            WAIT_FOR(1000);
        }
        return "";
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
      } catch (ParseException e) {
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
    } catch (ParseException e) {
      return null;
    }

  }
}