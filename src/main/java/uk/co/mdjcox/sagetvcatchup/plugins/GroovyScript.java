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
public abstract class GroovyScript extends groovy.lang.Script implements
         OsUtilsInterface {

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

    public String GET(String url) throws Exception {
        return downloadUtils.downloadFileString(url);
    }

    public String SAMPLE(String url) throws Exception {
        return downloadUtils.sampleFileString(url);
    }

    public void DOWNLOAD(URL url, String file) throws IOException {
        downloadUtils.downloadFile(url, file);
    }

    public String MOVE_TO(String token, String fileStr) {
        return htmlUtils.moveTo(token, fileStr);
    }

    public String REMOVE_HTML(String html) {
        return htmlUtils.removeHtml(html);
    }

    @Override
    public String makeLinkAbsolute(String base, String relative) {
        return htmlUtils.makeLinkAbsolute(base, relative);
    }

    @Override
    public String makeIdSafe(String id) {
        return htmlUtils.makeIdSafe(id);
    }

    @Override
    public String makeContentSafe(String id) {
        return htmlUtils.makeContentSafe(id);
    }

    @Override
    public String extractTo(String token, String fileStr) {
        return htmlUtils.extractTo(token, fileStr);
    }

    @Override
    public boolean hasToken(String token, String fileStr) {
        return htmlUtils.hasToken(token, fileStr);
    }

    @Override
    public String getFileString(String htmlfile) throws IOException {
        return htmlUtils.getFileString(htmlfile);
    }

    @Override
    public String getFileString(String htmlfile, String filter) throws IOException {
        return htmlUtils.getFileString(htmlfile, filter);
    }

    @Override
    public String moveToInSteps(ArrayList<String> tokens, String fileStr) {
        return htmlUtils.moveToInSteps(tokens, fileStr);
    }

    @Override
    public ArrayList<String> extractItem(String fileStr, ArrayList<String> start, String stop, boolean removeHtml) {
        return htmlUtils.extractItem(fileStr, start, stop, removeHtml);
    }

    @Override
    public Process spawnProcess(String radioCommand, String action, boolean wait) throws Exception {
        return osUtils.spawnProcess(radioCommand, action, wait);
    }

    @Override
    public Process spawnProcess(String radioCommand, String action, boolean wait, ArrayList<String> output, ArrayList<String> errors) throws Exception {
        return osUtils.spawnProcess(radioCommand, action, wait, output, errors);
    }

    @Override
    public void killOsProcess(String pid, String cmd) {
        osUtils.killOsProcess(pid, cmd);
    }

    @Override
    public HashMap<String, String> processList() {
        return osUtils.processList();
    }

    public String getString(String token) {
        return properties.getString(token);
    }

    public String getString(String token, String defaultString) {
        return properties.getString(token, defaultString);
    }

    public int getInt(String token) {
        return properties.getInt(token);
    }

    public int getInt(String token, int defaultValue) {
        return properties.getInt(token, defaultValue);
    }

    public boolean getBoolean(String token) {
        return properties.getBoolean(token);
    }

    public boolean getBoolean(String token, boolean defaultValue) {
        return properties.getBoolean(token, defaultValue);
    }

    public ArrayList<String> getPropertySequence(String token) {
        return properties.getPropertySequence(token);
    }

    public ArrayList<String> getPropertySequenceAllowBlanks(String token) {
        return properties.getPropertySequenceAllowBlanks(token);

    }

    public Set<String> getPropertiesLike(String regex) {
        return properties.getPropertiesLike(regex);
    }

    public void severe(String msg) {
        logger.severe(msg);
    }

    public void severe(String msg, Throwable thrown) {
        logger.severe(msg, thrown);
    }

    public void warning(String msg) {
        logger.warning(msg);
    }

    public void warning(String msg, Throwable thrown) {
        logger.warning(msg, thrown);
    }

    public void info(String msg) {
        logger.info(msg);
    }
}