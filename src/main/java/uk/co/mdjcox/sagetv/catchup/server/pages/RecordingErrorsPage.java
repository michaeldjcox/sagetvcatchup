package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.HtmlBuilder;

import java.util.TreeSet;

/**
 * Created by michael on 06/10/14.
 */
public class RecordingErrorsPage extends AbstractHtmlPage {

    private final Recorder recorder;

    public RecordingErrorsPage(Recorder recorder) {
        this.recorder = recorder;
    }

    public String buildPage() {
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader("Errors page");
        htmlBuilder.startBody();
        htmlBuilder.addHeading1("Recording errors");
        htmlBuilder.startTable();
        htmlBuilder.addTableHeader("Error");
        for (String error : recorder.getErrors()) {
            htmlBuilder.addTableRow(error);
        }
        htmlBuilder.stopTable();
        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();

        return htmlBuilder.toString();

    }
    @Override
    public String getUri() {
        return "recerrors?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
        }
}
