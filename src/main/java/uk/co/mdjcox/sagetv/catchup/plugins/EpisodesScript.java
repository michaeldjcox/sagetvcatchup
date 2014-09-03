package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.slf4j.Logger;

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
 * Time: 17:48
 * To change this template use File | Settings | File Templates.
 */
public class EpisodesScript extends Script {

    @AssistedInject
    public EpisodesScript(Logger logger, @Assisted String base, HtmlUtilsInterface htmlUtils,
                          DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils,
                          PropertiesInterface properties) {
        super(logger, base + File.separator + "getEpisodes.groovy", htmlUtils, downloadUtils, osUtils, properties);
    }

    public void getEpisodes(Source source, Programme category) {
        try {
            getLogger().info("Getting episodes for " + category);
            call("url", category.getServiceUrl(), "category", category);
        } catch (Throwable e) {
            category.addError("ERROR", source.getId(), category.getId(), "", category.getServiceUrl(), "Unable to get episodes: " + e.getMessage());
            getLogger().error("Unable to get episodes for: " + category, e);
        }finally {
          if (category.hasErrors()) {
            getLogger().warn("Programme " + category.getShortName() + " has errors");
          }
          getLogger().info(category + " has " + category.getEpisodes().size() + " episodes");
        }
    }

}
