package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.Recording;
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
public class StopScript extends Script {

    @AssistedInject
    public StopScript(LoggerInterface logger, @Assisted String base, HtmlUtilsInterface htmlUtils,
                      DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils,
                      CatchupContextInterface context) {
        super(logger, base + File.separator + "stopEpisode.groovy", htmlUtils, downloadUtils, osUtils, context);
    }

    public void stop(Recording recording) {
        try {
            getLogger().info("Stopping recording of " + recording);
            call("recording", recording);
            getLogger().info("Stopped recording of " + recording);
        } catch (Throwable e) {
            getLogger().error("Unable to stop recording of " + recording, e);
        }
    }

}
