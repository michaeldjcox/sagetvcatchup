package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.utils.RssBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StartCatalogingHtmlProvider extends PodcastPageProvider {

    private Cataloger cataloger;

    public StartCatalogingHtmlProvider(String podcastUrl, Cataloger cataloger) {
        super(podcastUrl);
        this.cataloger = cataloger;
    }

    @Override
    public String getUri() {
        return "/startcat?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String message = cataloger.start();
        RssBuilder builder = new RssBuilder();
        final String url = getPodcastBaseUrl() + getUri();
        final String title = "CATALOGING";
        builder.startDocument(title, message, url);
        builder.addTextItem(title, message, url);
        builder.stopDocument();
        return builder.toString();
    }
}
