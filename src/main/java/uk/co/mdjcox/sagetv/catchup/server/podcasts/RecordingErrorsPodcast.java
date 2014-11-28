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

        StringBuilder descBuilder = new StringBuilder("");

        if (!episode.getSeries().isEmpty() && !episode.getSeries().equals("0")) {
          descBuilder.append(episode.getSeries());
        }
        if (!episode.getEpisode().isEmpty() && !episode.getEpisode().equals("0")) {
          if (descBuilder.length() != 0) {
            descBuilder.append(".");
          }
          descBuilder.append(episode.getEpisode());
          descBuilder.append(": ");
        } else {
          if (descBuilder.length() != 0) {
            descBuilder.append(": ");
          }
        }
        descBuilder.append(episode.getEpisodeTitle());
        descBuilder.append("<br/>");
        descBuilder.append("<i>");
        descBuilder.append(episode.getAirDate());
        descBuilder.append(' ');
        descBuilder.append(episode.getAirTime());
        descBuilder.append("</i>");
        descBuilder.append("<br/>");
        descBuilder.append(failedRecording.getRecordingStatus());
        descBuilder.append(" at: ");
        descBuilder.append(format.format(failedRecording.getStopTime()));
        descBuilder.append("<br/>");
        descBuilder.append(failedRecording.getFailedReason());
        descBuilder.append("<br/>");
        descBuilder.append(failedRecording.getFailureException());

        final String episodeTitle = htmlUtils.makeContentSafe("" + episode.getProgrammeTitle());
        final String episodeDesc = htmlUtils.makeContentSafe(descBuilder.toString());

        final String controlUrl = getPodcastBaseUrl() + "/control?id=" + episode.getId() + ";type=xml";

        builder.addCategoryItem(episodeTitle, episodeDesc, controlUrl);

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
