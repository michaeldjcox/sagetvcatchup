package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.utils.RssBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class RecordEpisodePodcast extends AbstractPodcast {

    private final Episode episode;
    private Recorder recorder;

    public RecordEpisodePodcast(String baseUrl, Episode episode, Recorder recorder) {
        super(baseUrl);
        this.episode = episode;
        this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "/record?id="+episode.getId()+";type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        recorder.record(episode);
        String message = "Recording " + episode.getPodcastTitle();
        RssBuilder builder = new RssBuilder();
        final String url = getPodcastBaseUrl() + getUri();
        final String title = "RECORDING";
        builder.startDocument(title, message, url);
        builder.addTextItem(title, message, url);
        builder.stopDocument();
        return builder.toString();
    }
}
