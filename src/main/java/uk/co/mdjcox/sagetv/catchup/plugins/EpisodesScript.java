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
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:48
 * To change this template use File | Settings | File Templates.
 */
public class EpisodesScript extends Script {

    @AssistedInject
    public EpisodesScript(LoggerInterface logger, @Assisted String base, HtmlUtilsInterface htmlUtils,
                          DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils,
                          CatchupContextInterface context) {
        super(logger, base + File.separator + "getEpisodes.groovy", htmlUtils, downloadUtils, osUtils, context);
    }

    public Collection<Episode> getEpisodes(Source source, Programme category, AtomicBoolean stopFlag) {
        ArrayList<Episode> episodes = new ArrayList<Episode>();
        try {
            getLogger().info("Getting episodes for " + category);
            call("source", source, "url", category.getServiceUrl(), "category", category, "episodes", episodes, "stopFlag", stopFlag);

        } catch (Throwable e) {
          String message = e.getMessage();
          if (message == null) {
            message = e.getClass().getSimpleName();
          }
          if (message.equals(STOPPED_ON_REQUEST)) {
            getLogger().info("Stopped on request getting episodes for " + category);
          } else {
            category.addError("ERROR", "Unable to get episodes: " + message);
            getLogger().error("Unable to get episodes for: " + category, e);
          }
        }finally {
          if (category.hasErrors()) {
            getLogger().warn("Programme " + category.getShortName() + " has errors");
          }
          getLogger().info(category + " has " + episodes.size() + " episodes");
        }
        return episodes;
    }

}
