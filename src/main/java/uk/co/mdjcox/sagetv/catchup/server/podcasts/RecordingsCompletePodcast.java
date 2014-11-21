package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.RssBuilder;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Created by michael on 07/10/14.
 */
public class RecordingsCompletePodcast extends AbstractPodcast {

    private final Recorder recorder;
    private final HtmlUtilsInterface htmlUtils;

    public RecordingsCompletePodcast(HtmlUtilsInterface htmlUtils, String baseUrl, Recorder recorder) {
        super(baseUrl);
        this.recorder = recorder;
        this.htmlUtils = htmlUtils;
    }

    @Override
    public String getUri() {
        return "recordingsdone?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    public String buildPage() {
        String recordingsUrl = getPodcastBaseUrl() + "/" + getUri();
        String title = "RECORDINGS";
        String desc = "Recordings completed";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, recordingsUrl);
      SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");


      Collection<Recording> recordings = recorder.getCompletedRecordings();
        for (Recording recording : recordings) {
            final Episode episode = recording.getEpisode();

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
          descBuilder.append(episode.getOrigAirDate());
          descBuilder.append(' ');
          descBuilder.append(episode.getOrigAirTime());
          descBuilder.append("</i>");
          descBuilder.append("<br/>");
          descBuilder.append(recording.getRecordingStatus());
          descBuilder.append(" at: ");
          descBuilder.append(format.format(new Date(recording.getStopTime())));

            final String episodeTitle = htmlUtils.makeContentSafe("" + episode.getProgrammeTitle());
          final String episodeDesc = htmlUtils.makeContentSafe(descBuilder.toString());

            final String controlUrl = getPodcastBaseUrl() + "/control?id=" + episode.getId() + ";type=xml";

            builder.addCategoryItem(episodeTitle, episodeDesc, controlUrl);
        }

        builder.stopDocument();

        return builder.toString();
    }


}
