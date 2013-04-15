package uk.co.mdjcox.sagetvcatchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Source;
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
public class SourceScript extends Script {

    private String sourceId;

    @AssistedInject
    public SourceScript(LoggerInterface logger, @Assisted("id") String id, @Assisted("base") String base,
                        HtmlUtilsInterface htmlUtils, DownloadUtilsInterface downloadUtils,
                        OsUtilsInterface osUtils, PropertiesInterface properties) {
        super(logger, base + File.separator + "getSource.groovy", htmlUtils, downloadUtils, osUtils, properties);
        sourceId = id;
    }

    public Source getSource() {
        Source source = new Source();
        try {
            getLogger().info("Getting details of source " + sourceId);
            call("source", source);
            getLogger().info("Found source " + source);
        } catch (Throwable e) {
            getLogger().severe("Unable to get details of source: " + sourceId, e);
        }
        return source;
    }

}
