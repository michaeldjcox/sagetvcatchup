package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class RecordEpisodePage extends AbstractHtmlPage {

    private final Episode episode;
    private Recorder recorder;

    public RecordEpisodePage(Episode episode, Recorder recorder) {
        this.episode = episode;
        this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "record?id="+episode.getId()+";type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        recorder.record(episode);
        String message = "Recording " + episode.getPodcastTitle();
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
