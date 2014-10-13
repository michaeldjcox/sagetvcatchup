package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.OsUtilsInterface;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */
public class PlayScript extends Script {

    @AssistedInject
    public PlayScript(Logger logger, @Assisted String base, HtmlUtilsInterface htmlUtils,
                      DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils, CatchupContextInterface context) {
        super(logger, base + File.separator + "playEpisode.groovy", htmlUtils, downloadUtils, osUtils, context);
    }

    public void play(Recording recording) {
        try {
            getLogger().info("Starting playback of " + recording);
            call("recording", recording);
            getLogger().info("Playing episode " + recording);
        } catch (Throwable e) {
            getLogger().error("Unable to playback: " + recording, e);
        }
    }

}
