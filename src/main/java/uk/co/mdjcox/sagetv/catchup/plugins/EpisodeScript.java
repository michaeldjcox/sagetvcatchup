package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.slf4j.Logger;

import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.OsUtilsInterface;
import uk.co.mdjcox.utils.PropertiesInterface;

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
    public EpisodeScript(Logger logger, @Assisted String base, HtmlUtilsInterface htmlUtils,
                         DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils,
                         PropertiesInterface properties) {
        super(logger, base + File.separator + "getEpisode.groovy", htmlUtils, downloadUtils, osUtils, properties);
    }

    public void getEpisode(Source source, Programme programme, Episode episode) {
        try {
            getLogger().info("Getting episode at URL " + episode.getServiceUrl());
            call("programme", programme, "url", episode.getServiceUrl(), "episode", episode);
            String iconUrl = programme.getIconUrl();
            if ((iconUrl == null) || iconUrl.isEmpty()) {
                programme.setIconUrl(episode.getIconUrl());
            }
            getLogger().info("Found episode " + episode);
        } catch (Throwable e) {
            programme.addError("ERROR", source.getId(), programme.getId(), episode.getEpisodeTitle(), "Unable to get an episode: " + e.getMessage(), episode.getServiceUrl());
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
