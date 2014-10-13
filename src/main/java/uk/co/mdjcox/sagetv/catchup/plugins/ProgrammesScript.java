package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.OsUtilsInterface;

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
                            CatchupContextInterface context) {
        super(logger, base + File.separator + "getProgrammes.groovy", htmlUtils, downloadUtils, osUtils, context);
    }

    public Collection<Programme> getProgrammes(Source source) {
        Collection<Programme> programmes = new ArrayList<Programme>();
        try {
          source.addMetaUrl(source.getServiceUrl());
            call("source", source,  "url", source.getServiceUrl(), "programmes", programmes);
        } catch (Throwable e) {
          source.addError("ERROR", "Unable to get programmes: " + e.getMessage());
          getLogger().error("Unable to get programmes for: " + source, e);
        } finally {
          if (source.hasErrors()) {
            getLogger().warn("Source " + source.getShortName() + " has errors");
          }
            getLogger().info(source + " has " + programmes.size() + " programmes");
        }
        return programmes;
    }
}
