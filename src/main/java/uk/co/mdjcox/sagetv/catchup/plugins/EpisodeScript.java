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
import java.util.concurrent.atomic.AtomicBoolean;

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

    public void getEpisode(Source source, Programme programme, Episode episode, AtomicBoolean stopFlag) {
        try {
            call("programme", programme, "url", episode.getServiceUrl(), "episode", episode, "stopFlag", stopFlag);
            String iconUrl = programme.getIconUrl();
            if ((iconUrl == null) || iconUrl.isEmpty()) {
                programme.setIconUrl(episode.getIconUrl());
            }
            getLogger().info("Got episode " + episode);
        } catch (Throwable e) {
          String message = e.getMessage();
          if (message == null) {
            message = e.getClass().getSimpleName();
          }
          if (message.equals(STOPPED_ON_REQUEST)) {
            getLogger().info("Stopped on request getting episode for " + programme);
          } else {
            programme.addError("ERROR", "Unable to get an episode: " + message);
            getLogger().error("Unable to get an episode for: " + programme + " " + episode.getServiceUrl(), e);
          }
            programme.removeEpisode(episode);
        } finally {
          if (episode.hasErrors()) {
            getLogger().warn("Programme " + programme.getShortName() + " episode " + episode.getEpisodeTitle() + " has errors");
          }
        }
    }

}
