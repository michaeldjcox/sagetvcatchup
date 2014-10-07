package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.RssBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class ControlPodcast extends AbstractPodcast {

    private final HtmlUtilsInterface htmlUtils;
    private final Recorder recorder;
    private Episode episode;

    public ControlPodcast(String baseUrl, Recorder recorder, Episode episode, HtmlUtilsInterface htmlUtils) {
        super(baseUrl);
        this.episode = episode;
        this.htmlUtils = htmlUtils;
        this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "/control?id=" + episode.getId();
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        final String title = htmlUtils.makeContentSafe(episode.getPodcastTitle());
        final String desc = htmlUtils.makeContentSafe(episode.getDescription());
        final String url = episode.getServiceUrl();
        final String stopUrl = getPodcastBaseUrl() + "/stop?id=" + episode.getId();
        final String watchUrl = getPodcastBaseUrl() + "/watch?id=" + episode.getId();
        final String recordUrl = getPodcastBaseUrl() + "/record?id=" + episode.getId();

        boolean isRecording = false;

        isRecording = recorder.isRecording(episode.getId());

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, url);
        if (isRecording) {
            builder.addCategoryItem("STOP", "Stop recording", stopUrl);
        } else {
            builder.addVideoItem("WATCH", "Watch now", watchUrl, "");
            builder.addCategoryItem("RECORD", "Record for later", recordUrl);
        }
        builder.stopDocument();
        return builder.toString();
    }
}
