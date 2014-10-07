package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class HomeHtmlProvider extends HtmlPageProvider {

    private final Cataloger cataloger;
    private final Recorder recorder;

    public HomeHtmlProvider(Cataloger cataloger, Recorder recorder) {
        this.cataloger = cataloger;
        this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "/";
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
        htmlBuilder.boldOn();
        htmlBuilder.addParagraph("Catalog progress:");
        htmlBuilder.boldOff();
        htmlBuilder.addParagraph(cataloger.getProgress());
        htmlBuilder.boldOn();
        htmlBuilder.addParagraph("Recording progress:");
        htmlBuilder.boldOff();
        htmlBuilder.addParagraph(String.valueOf(recorder.getRecordingCount()));
        htmlBuilder.boldOn();
        htmlBuilder.addParagraph("Recording processes:");
        htmlBuilder.boldOff();
        htmlBuilder.addParagraph(String.valueOf(recorder.getProcessCount()));
        htmlBuilder.addLink("Recordings", "/recordings");
        htmlBuilder.addBreak();

        htmlBuilder.addLink("Logs", "/logs");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Catalog Errors", "/errors");
        htmlBuilder.addParagraph(cataloger.getErrorSummary());
        htmlBuilder.addHeading2("Controls");
        htmlBuilder.addLink("Stop all recording", "/stopall");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Start cataloging", "/startcat");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Stop cataloging", "/stopcat");
        htmlBuilder.addBreak();

        htmlBuilder.addHeading2("Catalog");
        htmlBuilder.addLink("Podcasts", "/Catchup");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Categories", "/categories");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Programmes", "/programmes");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Episodes", "/episodes");
        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();

        return htmlBuilder.toString();
    }
}
