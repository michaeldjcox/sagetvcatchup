package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StatusPage extends AbstractHtmlPage {

    private Cataloger cataloger;
    private Recorder recorder;

    public StatusPage(Cataloger cataloger, Recorder recorder) {
      this.cataloger = cataloger;
      this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "status?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        HtmlBuilder builder = new HtmlBuilder();
        builder.startDocument();
        builder.addPageHeader("Status");
        builder.startBody();
        builder.addHeading1("Status");
        builder.startTable();
        builder.addTableHeader("Item", "Status");
        builder.addTableRow("Catalog Progress", cataloger.getProgress());
        builder.addTableRow("Recording Progress", String.valueOf(recorder.getRecordingCount()));
        builder.addTableRow("Recording Processes", String.valueOf(recorder.getProcessCount()));
        builder.stopTable();
        builder.stopBody();
        builder.stopDocument();
        return builder.toString();
    }
}
