package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.sagetv.utils.DownloadUtilsInterface;
import uk.co.mdjcox.sagetv.utils.HtmlUtilsInterface;
import uk.co.mdjcox.sagetv.utils.LoggerInterface;
import uk.co.mdjcox.sagetv.utils.OsUtilsInterface;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */
public class CategoriesScript extends Script {

    @AssistedInject
    public CategoriesScript(LoggerInterface logger, @Assisted String base, HtmlUtilsInterface htmlUtils,
                            DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils,
                            CatchupContextInterface context) {
        super(logger, base + File.separator + "getCategories.groovy", htmlUtils, downloadUtils, osUtils, context);
    }

    public void getCategories(Source source, Map<String, List<String>> categories, AtomicBoolean stopFlag) {
        try {
            call("categories", categories, "stopFlag", stopFlag);
            getLogger().info("Got categories " + categories);
        } catch (Throwable e) {
          String message = e.getMessage();
          if (message == null) {
            message = e.getClass().getSimpleName();
          }
          if (message.equals(STOPPED_ON_REQUEST)) {
            getLogger().info("Stopped on request getting categories for " + source);
          } else {
            source.addError("ERROR", "Unable to get categories: " + message);
            getLogger().error("Unable to get an categories for: " + source, e);
          }
        }
    }

}
