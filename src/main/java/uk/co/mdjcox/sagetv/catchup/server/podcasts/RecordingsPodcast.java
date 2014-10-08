package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.RssBuilder;

import java.util.Collection;

/**
 * Created by michael on 07/10/14.
 */
public class RecordingsPodcast extends AbstractPodcast {

    private final Recorder recorder;
    private final HtmlUtilsInterface htmlUtils;

    public RecordingsPodcast(HtmlUtilsInterface htmlUtils, String baseUrl, Recorder recorder) {
        super(baseUrl);
        this.recorder = recorder;
        this.htmlUtils = htmlUtils;
    }

    @Override
    public String getUri() {
        return "/recordings?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    public String buildPage() {
        String recordingsUrl = getPodcastBaseUrl() + getUri();
        String title = "RECORDING NOW";
        String desc = "Recordings in progress";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, recordingsUrl);

        Collection<Recording> recordings = recorder.getCurrentRecordings();
        for (Recording recording : recordings) {
            final Episode episode = recording.getEpisode();
            final String episodeTitle = htmlUtils.makeContentSafe(episode.getPodcastTitle());
            final String episodeDesc = htmlUtils.makeContentSafe(episode.getDescription());
            final String episodeIconUrl = episode.getIconUrl();
            final String controlUrl = getPodcastBaseUrl() + "/control?id=" + episode.getId() + ";type=xml";

            builder.addCategoryItem(episodeTitle, episodeDesc, controlUrl, episodeIconUrl);
        }

        builder.stopDocument();

        return builder.toString();
    }
}
