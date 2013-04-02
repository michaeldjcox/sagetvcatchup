package uk.co.mdjcox.scripts;

import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Programme;
import uk.co.mdjcox.model.Source;
import uk.co.mdjcox.utils.DownloadUtils;
import uk.co.mdjcox.utils.HtmlUtils;

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
    public ProgrammesScript(LoggerInterface logger, String script, HtmlUtils htmlUtils, DownloadUtils downloadUtils) {
        super(logger, script, htmlUtils, downloadUtils);
    }

    public Collection<Programme> getProgrammes(Source category) {
        Collection<Programme> programmes = new ArrayList<Programme>();
        try {
            call("url", category.getServiceUrl(), "programmes", programmes);
        } catch (Throwable e) {
            getLogger().severe("Unable to get subcategories for: " + category, e);
        } finally {
            getLogger().info(category + " has " + category.getSubCategories().size() + " subcategories");
        }
        return programmes;
    }
}
