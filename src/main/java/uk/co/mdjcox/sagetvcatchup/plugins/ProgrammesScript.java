package uk.co.mdjcox.sagetvcatchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Programme;
import uk.co.mdjcox.model.Source;
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
    public ProgrammesScript(LoggerInterface logger, @Assisted String base, HtmlUtilsInterface htmlUtils, DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils) {
        super(logger, base + File.separator + "getProgrammes.groovy", htmlUtils, downloadUtils, osUtils);
    }

    public Collection<Programme> getProgrammes(Source category) {
        Collection<Programme> programmes = new ArrayList<Programme>();
        try {
            call("url", category.getServiceUrl(), "programmes", programmes);
        } catch (Throwable e) {
            getLogger().severe("Unable to get programmes for: " + category, e);
        } finally {
            getLogger().info(category + " has " + programmes.size() + " programmes");
        }
        return programmes;
    }
}
