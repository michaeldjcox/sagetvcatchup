package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.utils.RssBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StartCatalogingPodcast extends AbstractPodcast {

    private Cataloger cataloger;

    public StartCatalogingPodcast(String podcastUrl, Cataloger cataloger) {
        super(podcastUrl);
        this.cataloger = cataloger;
    }

    @Override
    public String getUri() {
        return "startcat?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String message = cataloger.startCataloging();
        RssBuilder builder = new RssBuilder();
        final String url = getPodcastBaseUrl() + "/"+ getUri();
        final String title = "CATALOGING";
        builder.startDocument(title, message, url);
        builder.addTextItem(title, message, url);
        builder.stopDocument();
        return builder.toString();
    }
}
