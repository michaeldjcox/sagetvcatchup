package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class HomePage extends AbstractHtmlPage {

    private final Cataloger cataloger;
    private final Recorder recorder;

    public HomePage(Cataloger cataloger, Recorder recorder) {
        this.cataloger = cataloger;
        this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader("SageTV Catchup");
        htmlBuilder.startBody();
        htmlBuilder.addHeading1("SageTV Catchup");
        htmlBuilder.addHeading2("Status");
        htmlBuilder.addHeading3("Catalog progress:");
        htmlBuilder.addParagraph(cataloger.getProgress());
        htmlBuilder.addParagraph(cataloger.getStatsSummary());
        htmlBuilder.addLink("Catalog Errors", "/errors?type=html");
        htmlBuilder.addParagraph(cataloger.getErrorSummary());
        htmlBuilder.addHeading3("Recording progress:");
        StringBuilder recordingProgress = new StringBuilder();
        recordingProgress.append(recorder.getRecordingCount());
        recordingProgress.append(" in progress<br/>");
        recordingProgress.append(recorder.getCompletedCount());
        recordingProgress.append(" completed<br/>");
        recordingProgress.append(recorder.getFailedCount());
        recordingProgress.append(" failed<br/>");
        recordingProgress.append(recorder.getProcessCount());
        recordingProgress.append(" processes<br/>");
        htmlBuilder.addParagraph(recordingProgress.toString());
        htmlBuilder.addLink("Recording Errors", "/recerrors?type=html");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Recordings", "/recordings?type=html");
        htmlBuilder.addBreak();
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Logs", "/logs?type=html");
        htmlBuilder.addHeading2("Controls");
        htmlBuilder.addLink("Stop all recording", "/stopall?type=html");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Start cataloging", "/startcat?type=html");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Stop cataloging", "/stopcat?type=html");
        htmlBuilder.addBreak();

        htmlBuilder.addHeading2("Catalog");
        htmlBuilder.addLink("Podcasts", "/category?id=Catchup;type=xml");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Categories", "/categories?type=html");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Programmes", "/programmes?type=html");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Episodes", "/episodes?type=html");
        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();

        return htmlBuilder.toString();
    }
}
