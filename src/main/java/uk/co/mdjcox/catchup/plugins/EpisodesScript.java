package uk.co.mdjcox.catchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Programme;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:48
 * To change this template use File | Settings | File Templates.
 */
public class EpisodesScript extends Script {

    @AssistedInject
    public EpisodesScript(LoggerInterface logger, @Assisted String base, HtmlUtilsInterface htmlUtils, DownloadUtilsInterface downloadUtils) {
        super(logger, base + File.separator + "getEpisodes.groovy", htmlUtils, downloadUtils);
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
