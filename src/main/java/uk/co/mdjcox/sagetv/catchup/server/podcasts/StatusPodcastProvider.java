package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.catchup.server.podcasts.PodcastPageProvider;
import uk.co.mdjcox.utils.RssBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StatusPodcastProvider extends PodcastPageProvider {

    private final Recorder recorder;
    private final Cataloger cataloger;

    public StatusPodcastProvider(String baseUrl, Recorder recorder, Cataloger cataloger) {
        super(baseUrl);
        this.recorder = recorder;
        this.cataloger = cataloger;
    }

    @Override
    public String getUri() {
        return "/status?type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String errorsUrl = getPodcastBaseUrl() + "/errors?type=xml";
        String recordingsUrl = getPodcastBaseUrl() + "/recordings?type=xml";
        String statusUrl = getPodcastBaseUrl() + "/status?type=xml";
        String stopUrl = getPodcastBaseUrl() + "/stopall?type=xml";
        String startCatUrl = getPodcastBaseUrl() + "/startcat?type=xml";
        String stopCatUrl = getPodcastBaseUrl() + "/stopcat?type=xml";
        String title = "Status";
        String desc = "Catchup TV status";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, statusUrl);
        builder.addCategoryItem("RECORDING NOW", String.valueOf(recorder.getRecordingCount()), recordingsUrl);
        builder.addTextItem("RECORDING PROCESSES", String.valueOf(recorder.getProcessCount()), statusUrl);
        builder.addTextItem("CATALOGING PROGRESS", cataloger.getProgress(), statusUrl);
        builder.addCategoryItem("CATALOGING ERRORS", cataloger.getErrorSummary(), errorsUrl);
        builder.addCategoryItem("STOP ALL RECORDING", "Abandon all recording", stopUrl);
        builder.addCategoryItem("START CATALOGING", "Start cataloging", startCatUrl);
        builder.addCategoryItem("STOP CATALOGING", "Stop cataloging", stopCatUrl);
        builder.stopDocument();

        return builder.toString();

    }

}
