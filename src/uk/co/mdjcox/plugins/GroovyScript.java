package uk.co.mdjcox.plugins;

import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 31/03/13
 * Time: 00:11
 * To change this template use File | Settings | File Templates.
 */
public abstract class GroovyScript extends groovy.lang.Script implements HtmlUtilsInterface, DownloadUtilsInterface {

    private HtmlUtilsInterface htmlUtils;
    private DownloadUtilsInterface downloadUtils;

    public void setHtmlUtils(HtmlUtilsInterface htmlUtils) {
        this.htmlUtils = htmlUtils;
    }

    public void setDownloadUtils(DownloadUtilsInterface downloadUtils) {
        this.downloadUtils = downloadUtils;
    }

    public String downloadFileString(String url) throws Exception {
        return downloadUtils.downloadFileString(url);
    }

    @Override
    public String sampleFileString(String source) throws Exception {
        return downloadUtils.sampleFileString(source);
    }

    @Override
    public String sampleFileString(String source, String encoding) throws Exception {
        return downloadUtils.sampleFileString(source, encoding);
    }

    @Override
    public String downloadFileString(String source, String encoding) throws Exception {
        return downloadUtils.downloadFileString(source, encoding);
    }

    @Override
    public void downloadFile(URL url, String file) throws IOException {
        downloadUtils.downloadFile(url, file);
    }

    public String moveTo(String token, String fileStr) {
        return htmlUtils.moveTo(token, fileStr);
    }

    @Override
    public String removeHtml(String html) {
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
}
