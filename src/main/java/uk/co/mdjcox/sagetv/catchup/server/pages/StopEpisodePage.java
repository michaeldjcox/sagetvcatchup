package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StopEpisodePage extends AbstractHtmlPage {

    private final String id;
    private Recorder recorder;

    public StopEpisodePage(String episodeId, Recorder recorder) {
        this.id = episodeId;
        this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "stop?id="+id+";type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String message = recorder.requestStop(id);
        HtmlBuilder builder = new HtmlBuilder();
        builder.startDocument();
        builder.addPageHeader("Recording");
        builder.startBody();
        builder.addHeading1(message);
        builder.stopBody();
        builder.stopDocument();
        return builder.toString();
    }
}
