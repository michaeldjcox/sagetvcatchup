package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.sagetv.model.ParseError;
import uk.co.mdjcox.utils.RssBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by michael on 07/10/14.
 */
public class StatusPodcastProvider extends PodcastPageProvider {

    private final String podcastbaseUrl;
    private final Recorder recorder;
    private final Cataloger cataloger;

    public StatusPodcastProvider(String baseUrl, Recorder recorder, Cataloger cataloger) {
        this.podcastbaseUrl = baseUrl;
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
        String errorsUrl = podcastbaseUrl + "/errors?type=xml";
        String recordingsUrl = podcastbaseUrl + "/recordings?type=xml";
        String statusUrl = podcastbaseUrl + "/status?type=xml";
        String stopUrl = podcastbaseUrl + "/stopall?type=xml";
        String startCatUrl = podcastbaseUrl + "/startcat?type=xml";
        String stopCatUrl = podcastbaseUrl + "/stopcat?type=xml";
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
