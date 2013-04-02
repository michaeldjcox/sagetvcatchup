package uk.co.mdjcox.scripts;

import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Programme;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:48
 * To change this template use File | Settings | File Templates.
 */
public class EpisodesScript extends Script {

    public EpisodesScript(LoggerInterface logger, String script, HtmlUtilsInterface htmlUtils, DownloadUtilsInterface downloadUtils) {
        super(logger, script, htmlUtils, downloadUtils);
    }

    public void getEpisodes(Programme category) {
        try {
            getLogger().info("Getting episodes for " + category);
            call("url", category.getServiceUrl(), "category", category);
        } catch (Throwable e) {
            getLogger().severe("Unable to get episodes for: " + category, e);
        } finally {
            getLogger().info(category + " has " + category.getEpisodes().size() + " episodes");
        }
    }

}
