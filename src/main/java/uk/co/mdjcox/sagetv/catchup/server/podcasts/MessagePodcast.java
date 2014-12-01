package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.utils.RssBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class MessagePodcast extends AbstractPodcast {

    private String message;

    public MessagePodcast(String baseUrl, String message) {
        super(baseUrl);
        this.message = message;
    }

    @Override
    public String getUri() {
        return "error?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        RssBuilder builder = new RssBuilder();
        final String url = getPodcastBaseUrl() + "/" + getUri();
        final String title = "ERROR";
        builder.startDocument(title, message, url);
        builder.addTextItem(title, message, url);
        builder.stopDocument();
        return builder.toString();
    }
}
