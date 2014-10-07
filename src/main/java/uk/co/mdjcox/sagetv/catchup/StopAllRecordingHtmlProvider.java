package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StopAllRecordingHtmlProvider extends HtmlPageProvider {

    private Recorder recorder;

    public StopAllRecordingHtmlProvider(Recorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "/stopcat?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String message = cataloger.stop();
        HtmlBuilder builder = new HtmlBuilder();
        builder.startDocument();
        builder.addPageHeader("Cataloging");
        builder.startBody();
        builder.addHeading1(message);
        builder.stopBody();
        builder.stopDocument();
        return builder.toString();
    }
}
