package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.RssBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by michael on 07/10/14.
 */
public class RecordingsPodcastProvider extends PodcastPageProvider {

    private final Recorder recorder;
    private final HtmlUtilsInterface htmlUtils;

    public RecordingsPodcastProvider(HtmlUtilsInterface htmlUtils, String baseUrl, Recorder recorder) {
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
            final String controlUrl = getPodcastBaseUrl() + "/control?id=" + episode.getId();

            builder.addCategoryItem(episodeTitle, episodeDesc, controlUrl, episodeIconUrl);
        }

        builder.stopDocument();

        return builder.toString();
    }
}
