package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.sagetv.utils.DownloadUtilsInterface;
import uk.co.mdjcox.sagetv.utils.HtmlUtilsInterface;
import uk.co.mdjcox.sagetv.utils.LoggerInterface;
import uk.co.mdjcox.sagetv.utils.OsUtilsInterface;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */
public class SourceScript extends Script {

    private String sourceId;

    @AssistedInject
    public SourceScript(LoggerInterface logger, @Assisted("id") String id, @Assisted("base") String base,
                        HtmlUtilsInterface htmlUtils, DownloadUtilsInterface downloadUtils,
                        OsUtilsInterface osUtils, CatchupContextInterface context) {
        super(logger, base + File.separator + "getSource.groovy", htmlUtils, downloadUtils, osUtils, context);
        sourceId = id;
    }

    public Source getSource() {
        Source source = new Source("", "", "", "", "", "");
        try {
            getLogger().info("Getting details of source " + sourceId);
            call("source", source);
            getLogger().info("Found source " + source);
        } catch (Throwable e) {
            getLogger().error("Unable to get details of source: " + sourceId, e);
        }
        return source;
    }

}
