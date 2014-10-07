package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.utils.RssBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StopEpisodePodcast extends AbstractPodcast {

    private final String id;
    private Recorder recorder;

    public StopEpisodePodcast(String baseUrl, String episodeId, Recorder recorder) {
        super(baseUrl);
        this.recorder = recorder;
        this.id = episodeId;
    }

    @Override
    public String getUri() {
        return "/stop?id=" + id + "&type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String message = recorder.requestStop(id);
        RssBuilder builder = new RssBuilder();
        final String url = getPodcastBaseUrl() + getUri();
        final String title = "RECORDING";
        builder.startDocument(title, message, url);
        builder.addTextItem(title, message, url);
        builder.stopDocument();
        return builder.toString();
    }
}
