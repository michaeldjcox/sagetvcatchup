package uk.co.mdjcox.sagetv.catchup.server;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.catchup.server.HtmlPageProvider;
import uk.co.mdjcox.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StopEpisodeHtmlProvider extends HtmlPageProvider {

    private final String id;
    private Recorder recorder;

    public StopEpisodeHtmlProvider(String episodeId, Recorder recorder) {
        this.id = episodeId;
        this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "/stop?id="+id+"&type=html";
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
