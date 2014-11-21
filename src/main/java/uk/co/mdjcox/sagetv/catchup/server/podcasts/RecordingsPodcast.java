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
        return "recordings?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    public String buildPage() {
        String recordingsUrl = getPodcastBaseUrl() + "/" + getUri();
        String title = "RECORDINGS";
        String desc = "Recordings in progress";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, recordingsUrl);

        Collection<Recording> recordings = recorder.getCurrentRecordings();
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
          descBuilder.append("<b>");
          descBuilder.append(recording.getRecordingStatus());
          descBuilder.append("</b>");
          descBuilder.append("<br/>");
          descBuilder.append("<b>");
          descBuilder.append(recording.getPercentRecorded());
          descBuilder.append("</b>");

          final String episodeTitle = htmlUtils.makeContentSafe("" + episode.getProgrammeTitle());
          final String statusDesc = htmlUtils.makeContentSafe(descBuilder.toString());

            final String controlUrl = getPodcastBaseUrl() + "/control?id=" + episode.getId() + ";type=xml";

            builder.addCategoryItem(episodeTitle, statusDesc, controlUrl);
        }

        builder.stopDocument();

        return builder.toString();
    }


}
