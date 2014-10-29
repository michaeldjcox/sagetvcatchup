package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.RssBuilder;

import java.text.SimpleDateFormat;

/**
 * Created by michael on 07/10/14.
 */
public class RecordingErrorsPodcast extends AbstractPodcast {

    private final Recorder recorder;

    public RecordingErrorsPodcast(String baseUrl, Recorder recorder) {
        super(baseUrl);
        this.recorder = recorder;
    }

    public String buildPage() {
        String errorsUrl = getPodcastBaseUrl() + "/" + getUri();
        String title = "ERRORS";
        String desc = "Recording errors";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, errorsUrl);

      SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

      for (Recording failedRecording : recorder.getFailedRecordings()) {
            String error =
                    format.format(failedRecording.getStopTime()) + "<br/>" +
                    failedRecording.getId() + "<br/>" +
                    failedRecording.getFailedReason() + "<br/>" +
                    failedRecording.getFailureException();
            builder.addTextItem("ERROR", error, "");
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
