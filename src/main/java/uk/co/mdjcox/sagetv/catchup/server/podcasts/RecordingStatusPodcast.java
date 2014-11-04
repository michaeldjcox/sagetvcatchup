package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.RssBuilder;

import java.text.SimpleDateFormat;

/**
 * Created by michael on 07/10/14.
 */
public class RecordingStatusPodcast extends AbstractPodcast {

    private final Recorder recorder;

    public RecordingStatusPodcast(String baseUrl, Recorder recorder) {
        super(baseUrl);
        this.recorder = recorder;
    }

    public String buildPage() {
        String title = "RECORDING STATUS";
        String desc = "Recording status";

        String recordingsUrl = getPodcastBaseUrl() + "/recordings?type=xml";
        String recordingsDoneUrl = getPodcastBaseUrl() + "/recordingsdone?type=xml";
        String statusUrl = getPodcastBaseUrl() + "/category?id=status;type=xml";
        String recerrorsUrl = getPodcastBaseUrl() + "/recerrors?type=xml";
        String recstatusUrl = getPodcastBaseUrl() + "/" + getUri();
        String stopUrl = getPodcastBaseUrl() + "/stopall?type=xml";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, recstatusUrl);
        builder.addCategoryItem("IN PROGRESS", recorder.getRecordingCount() + " in progress", recordingsUrl);
        builder.addCategoryItem("COMPLETED", recorder.getCompletedCount() + " completed", recordingsDoneUrl);
        builder.addCategoryItem("FAILED", recorder.getFailedCount() + " failed", recerrorsUrl);
        builder.addTextItem("PROCESSES", recorder.getProcessCount() + " processes running", statusUrl);
        builder.addCategoryItem("STOP ALL RECORDING", "Abandon all recording", stopUrl);
        builder.stopDocument();

        return builder.toString();
    }

    @Override
    public String getUri() {
        return "recstatus?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }
}
