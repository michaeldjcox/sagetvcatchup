package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.utils.RssBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class CatalogingStatusPodcast extends AbstractPodcast {

    private final Cataloger cataloger;

    public CatalogingStatusPodcast(String baseUrl, Cataloger cataloger) {
        super(baseUrl);
        this.cataloger = cataloger;
    }

    public String buildPage() {
        String title = "RECORDING STATUS";
        String desc = "Recording status";

        String errorsUrl = getPodcastBaseUrl() + "/errors?type=xml";
        String statusUrl = getPodcastBaseUrl() + "/category?id=status;type=xml";
        String catstatusUrl = getPodcastBaseUrl() + "/" + getUri();
        String startCatUrl = getPodcastBaseUrl() + "/startcat?type=xml";
        String stopCatUrl = getPodcastBaseUrl() + "/stopcat?type=xml";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, catstatusUrl);

        final String status = cataloger.getProgress() + "<br/>" + cataloger.getStatsSummaryNew() + "<br/>" + cataloger.getErrorSummaryNew();
        builder.addTextItem("CATALOGING STATUS", status, statusUrl);
        builder.addCategoryItem("LAST CATALOG ERRORS", cataloger.getErrorSummary(), errorsUrl);
        builder.addCategoryItem("START CATALOGING", "Start cataloging", startCatUrl);
        builder.addCategoryItem("STOP CATALOGING", "Stop cataloging", stopCatUrl);

        builder.stopDocument();

        return builder.toString();
    }

    @Override
    public String getUri() {
        return "catstatus?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }
}
