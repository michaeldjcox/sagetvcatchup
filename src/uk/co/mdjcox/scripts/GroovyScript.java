package uk.co.mdjcox.scripts;

import uk.co.mdjcox.utils.DownloadUtils;
import uk.co.mdjcox.utils.HtmlUtils;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 31/03/13
 * Time: 00:11
 * To change this template use File | Settings | File Templates.
 */
public abstract class GroovyScript extends groovy.lang.Script {

    private HtmlUtils htmlUtils;
    private DownloadUtils downloadUtils;

    public void setHtmlUtils(HtmlUtils htmlUtils) {
        this.htmlUtils = htmlUtils;
    }

    public void setDownloadUtils(DownloadUtils downloadUtils) {
        this.downloadUtils = downloadUtils;
    }

    public String downloadFileString(String url) throws Exception {
        return downloadUtils.downloadFileString(url);
    }

    public String moveTo(String token, String fileStr) {
        return htmlUtils.moveTo(token, fileStr);
    }

}
