package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.utils.HtmlUtilsInterface;
import uk.co.mdjcox.sagetv.utils.RssBuilder;

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
        return "control?id=" + episode.getId() + ";type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        final String title = htmlUtils.makeContentSafe(episode.getPodcastTitle());
        final String desc = htmlUtils.makeContentSafe(episode.getDescription());
        String url = episode.getServiceUrl();

        if (url != null && url.startsWith("/")) {
          url = getPodcastBaseUrl() + url;
        }

        final String stopUrl = getPodcastBaseUrl() + "/stop?id=" + episode.getId() + ";type=xml";
        final String watchUrl = getPodcastBaseUrl() + "/watch?id=" + episode.getId() + ";type=mpeg4";
        final String watchAndKeepUrl = getPodcastBaseUrl() + "/watchandkeep?id=" + episode.getId() + ";type=mpeg4";
        final String recordUrl = getPodcastBaseUrl() + "/record?id=" + episode.getId() + ";type=xml";

        boolean isRecording = false;

        isRecording = recorder.isInProgress(episode.getId());

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, url);
        if (isRecording) {
            String stopDesc = "Stop recording";
          String status = recorder.getRecordingStatus(episode.getId());
          if (status != null && !status.isEmpty()) {
            stopDesc += "<br/>";
            stopDesc += status;
          }
          String percent = recorder.getPercentRecorded(episode.getId());
          if (percent != null && !percent.isEmpty()) {
            stopDesc += "<br/>";
            stopDesc += percent;
          }
            builder.addCategoryItem("STOP", stopDesc, stopUrl);
        } else {
          builder.addVideoItem("WATCH", "Watch now", watchUrl, "");
          builder.addVideoItem("WATCH AND KEEP", "Watch now and keep", watchAndKeepUrl, "");
          builder.addCategoryItem("RECORD", "Record for later", recordUrl);
        }
        builder.stopDocument();
        return builder.toString();
    }
}
