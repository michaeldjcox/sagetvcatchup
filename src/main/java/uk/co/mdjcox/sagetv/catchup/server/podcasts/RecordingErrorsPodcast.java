package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.utils.RssBuilder;

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

        for (String error: recorder.getErrors()) {
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
