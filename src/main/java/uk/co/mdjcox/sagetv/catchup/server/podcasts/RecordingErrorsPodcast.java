package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.RssBuilder;

import java.text.SimpleDateFormat;

/**
 * Created by michael on 07/10/14.
 */
public class RecordingErrorsPodcast extends AbstractPodcast {

    private final Recorder recorder;
    private final HtmlUtilsInterface htmlUtils;

    public RecordingErrorsPodcast(HtmlUtilsInterface htmlUtils, String baseUrl, Recorder recorder) {
        super(baseUrl);
        this.recorder = recorder;
        this.htmlUtils = htmlUtils;

    }

    public String buildPage() {
        String errorsUrl = getPodcastBaseUrl() + "/" + getUri();
        String title = "ERRORS";
        String desc = "Recording errors";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, errorsUrl);

      SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

      for (Recording failedRecording : recorder.getFailedRecordings()) {
        final Episode episode = failedRecording.getEpisode();
        final String episodeTitle = htmlUtils.makeContentSafe(episode.getPodcastTitle());

        String error =
                    format.format(failedRecording.getStopTime()) + "<br/>" +
                    failedRecording.getId() + "<br/>" +
                    failedRecording.getFailedReason() + "<br/>" +
                    failedRecording.getFailureException();

        final String controlUrl = getPodcastBaseUrl() + "/control?id=" + episode.getId() + ";type=xml";

        final String errorDesc = htmlUtils.makeContentSafe(error);

        builder.addCategoryItem(episodeTitle, errorDesc, controlUrl);

      }
        builder.stopDocument();

        return builder.toString();
    }

    @Override
    public String getUri() {
        return "recerrors?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }
}
