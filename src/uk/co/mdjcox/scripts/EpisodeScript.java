package uk.co.mdjcox.scripts;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Episode;
import uk.co.mdjcox.model.Programme;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */
public class EpisodeScript extends Script {

    @AssistedInject
    public EpisodeScript(LoggerInterface logger, @Assisted String base, HtmlUtilsInterface htmlUtils, DownloadUtilsInterface downloadUtils) {
        super(logger, base + File.separator + "getEpisode", htmlUtils, downloadUtils);
    }

    public void getEpisode(Programme programme, Episode episode) {
        try {
            getLogger().info("Getting episode at URL " + episode.getServiceUrl());
            call("url", episode.getServiceUrl(), "episode", episode, "logger", getLogger());
            String iconUrl = programme.getIconUrl();
            if ((iconUrl == null) || iconUrl.isEmpty()) {
                programme.setIconUrl(episode.getIconUrl());
            }
            getLogger().info("Found episode " + episode);
        } catch (Throwable e) {
            programme.removeEpisode(episode);
            getLogger().severe("Unable to get an episode for: " + programme + " " + episode.getServiceUrl(), e);
        }
    }

}
