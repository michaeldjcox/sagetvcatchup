package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.utils.RssBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StatusPodcast extends AbstractPodcast {

    private final Recorder recorder;
    private final Cataloger cataloger;

    public StatusPodcast(String baseUrl, Recorder recorder, Cataloger cataloger) {
        super(baseUrl);
        this.recorder = recorder;
        this.cataloger = cataloger;
    }

    @Override
    public String getUri() {
        return "category?id=Catchup/status;type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String recordingsUrl = getPodcastBaseUrl() + "/recstatus?type=xml";
        String catalogingUrl = getPodcastBaseUrl() + "/catstatus?type=xml";

        String statusUrl = getPodcastBaseUrl() + "/category?id=Catchup/status;type=xml";
        String title = "Control";
        String desc = "Catchup TV status and controls";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, statusUrl);

        builder.addCategoryItem("RECORDING STATUS", "Show recording status", recordingsUrl);
        builder.addCategoryItem("CATALOGING STATUS", "Show cataloging status", catalogingUrl);

        builder.stopDocument();

        return builder.toString();

    }

}
