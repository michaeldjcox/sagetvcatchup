package uk.co.mdjcox.scripts;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Episode;
import uk.co.mdjcox.model.Programme;
import uk.co.mdjcox.model.Source;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;

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
    public SourceScript(LoggerInterface logger, @Assisted String id, @Assisted String base, HtmlUtilsInterface htmlUtils, DownloadUtilsInterface downloadUtils) {
        super(logger, base + File.separator + "getSource", htmlUtils, downloadUtils);
        sourceId = id;
    }

    public Source getSource() {
        Source source = new Source();
        try {
            getLogger().info("Getting source at URL " + sourceId);
            call("source", source, "logger", getLogger());
            getLogger().info("Found source " + source);
        } catch (Throwable e) {
            getLogger().severe("Unable to get source: " + sourceId, e);
        }
        return source;
    }

}
