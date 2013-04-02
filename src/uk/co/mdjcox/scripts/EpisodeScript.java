package uk.co.mdjcox.scripts;

import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Episode;
import uk.co.mdjcox.model.Programme;
import uk.co.mdjcox.utils.DownloadUtils;
import uk.co.mdjcox.utils.HtmlUtils;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */
public class EpisodeScript extends Script {
    public EpisodeScript(LoggerInterface logger, String script, HtmlUtils htmlUtils, DownloadUtils downloadUtils) {
        super(logger, script, htmlUtils, downloadUtils);
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
