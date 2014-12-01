package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StopAllRecordingPage extends AbstractHtmlPage {

    private Recorder recorder;

    public StopAllRecordingPage(Recorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "stopall?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String message = recorder.requestStopAll();
        HtmlBuilder builder = new HtmlBuilder();
        builder.startDocument();
        builder.addPageHeader("Recording", true, "/");
        builder.startBody();
        builder.addHeading1(message);
        builder.stopBody();
        builder.stopDocument();
        return builder.toString();
    }
}
