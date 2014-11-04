package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.LoggerInterface;
import uk.co.mdjcox.utils.OsUtilsInterface;

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
    public EpisodeScript(LoggerInterface logger, @Assisted String base, HtmlUtilsInterface htmlUtils,
                         DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils,
                         CatchupContextInterface context) {
        super(logger, base + File.separator + "getEpisode.groovy", htmlUtils, downloadUtils, osUtils, context);
    }

    public void getEpisode(Source source, Programme programme, Episode episode) {
        try {
            call("programme", programme, "url", episode.getServiceUrl(), "episode", episode);
            String iconUrl = programme.getIconUrl();
            if ((iconUrl == null) || iconUrl.isEmpty()) {
                programme.setIconUrl(episode.getIconUrl());
            }
            getLogger().info("Got episode " + episode);
        } catch (Throwable e) {
            programme.addError("ERROR", "Unable to get an episode: " + e.getMessage());
            programme.removeEpisode(episode);
            getLogger().error(
                "Unable to get an episode for: " + programme + " " + episode.getServiceUrl(), e);
        } finally {
          if (episode.hasErrors()) {
            getLogger().warn("Programme " + programme.getShortName() + " episode " + episode.getEpisodeTitle() + " has errors");
          }
        }
    }

}
