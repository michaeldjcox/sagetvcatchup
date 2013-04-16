package uk.co.mdjcox.sagetvcatchup.plugins;

import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.OsUtilsInterface;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
    private PropertiesInterface properties;

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

    public void setProperties(PropertiesInterface properties) {
        this.properties = properties;
    }

    /* From here on down are methods available to the plugin user */

    public String GET_WEB_PAGE(String url) throws Exception {
        return downloadUtils.downloadFileString(url);
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

    public Process RUN(String osCommand, String loggerName, boolean wait) throws Exception {
        return osUtils.spawnProcess(osCommand, loggerName, wait);
    }

    public Process RUN(String radioCommand, String loggerName, boolean wait, ArrayList<String> output, ArrayList<String> errors) throws Exception {
        return osUtils.spawnProcess(radioCommand, loggerName, wait, output, errors);
    }

    public void KILL(String pid, String cmd) {
        osUtils.killProcess(pid, cmd);
    }

    public HashMap<String, String> GET_PROCESSES() {
        return osUtils.getProcesses();
    }

    public String GET_STRING_PROPERTY(String token) {
        return properties.getString(token);
    }

    public String GET_STRING_PROPERTY(String token, String defaultString) {
        return properties.getString(token, defaultString);
    }

    public int GET_INT_PROPERTY(String token) {
        return properties.getInt(token);
    }

    public int GET_INT_PROPERTY(String token, int defaultValue) {
        return properties.getInt(token, defaultValue);
    }

    public boolean GET_BOOLEAN_PROPERTY(String token) {
        return properties.getBoolean(token);
    }

    public boolean GET_BOOLEAN_PROPERTY(String token, boolean defaultValue) {
        return properties.getBoolean(token, defaultValue);
    }

    public void LOG_ERROR(String msg) {
        logger.severe(msg);
    }

    public void LOG_ERROR(String msg, Throwable thrown) {
        logger.severe(msg, thrown);
    }

    public void LOG_WARNING(String msg) {
        logger.warning(msg);
    }

    public void LOG_WARNING(String msg, Throwable thrown) {
        logger.warning(msg, thrown);
    }

    public void LOG_INFO(String msg) {
        logger.info(msg);
    }
}