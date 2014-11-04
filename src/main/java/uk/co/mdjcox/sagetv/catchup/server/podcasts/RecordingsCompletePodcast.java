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
            final String episodeTitle = htmlUtils.makeContentSafe(episode.getPodcastTitle());

            Date stopDate = new Date(recording.getStopTime());
            String status = recording.getRecordingStatus() + "<br/>" + format.format(stopDate);
            final String statusDesc = htmlUtils.makeContentSafe(status);

            final String controlUrl = getPodcastBaseUrl() + "/control?id=" + episode.getId() + ";type=xml";

            builder.addCategoryItem(episodeTitle, statusDesc, controlUrl);
        }

        builder.stopDocument();

        return builder.toString();
    }


}
