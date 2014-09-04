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
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:51
 * To change this template use File | Settings | File Templates.
 */
public class ProgrammesScript extends Script {

    @AssistedInject
    public ProgrammesScript(Logger logger, @Assisted String base, HtmlUtilsInterface htmlUtils,
                            DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils,
                            PropertiesInterface properties) {
        super(logger, base + File.separator + "getProgrammes.groovy", htmlUtils, downloadUtils, osUtils, properties);
    }

    public Collection<Programme> getProgrammes(Source category) {
        Collection<Programme> programmes = new ArrayList<Programme>();
        try {
            call("category", category,  "url", category.getServiceUrl(), "programmes", programmes);
        } catch (Throwable e) {
          category.addError("ERROR", category.getId(), "", "", "Unable to get programmes: " + e.getMessage(), category.getServiceUrl());
          getLogger().error("Unable to get programmes for: " + category, e);
        } finally {
          if (category.hasErrors()) {
            getLogger().warn("Source " + category.getShortName() + " has errors");
          }
            getLogger().info(category + " has " + programmes.size() + " programmes");
        }
        return programmes;
    }
}
