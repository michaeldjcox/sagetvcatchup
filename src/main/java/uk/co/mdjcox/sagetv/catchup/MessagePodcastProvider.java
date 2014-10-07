package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.utils.RssBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class MessagePodcastProvider extends PodcastPageProvider {

    private Recorder recorder;

    public MessagePodcastProvider(String baseUrl, Recorder recorder) {
        super(baseUrl);
        this.recorder = recorder;
    }

    @Override
    public String getUri() {
        return "/stopall?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String message = recorder.requestStopAll();
        RssBuilder builder = new RssBuilder();
        final String url = getPodcastBaseUrl() + getUri();
        final String title = "RECORDING";
        builder.startDocument(title, message, url);
        builder.addTextItem(title, message, url);
        builder.stopDocument();
        return builder.toString();
    }
}
