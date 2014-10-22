package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.utils.RssBuilder;

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
        return "category?id=status;type=xml";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String errorsUrl = getPodcastBaseUrl() + "/errors?type=xml";
        String recerrorsUrl = getPodcastBaseUrl() + "/recerrors?type=xml";
        String recordingsUrl = getPodcastBaseUrl() + "/recordings?type=xml";
        String statusUrl = getPodcastBaseUrl() + "/category?id=status;type=xml";
        String stopUrl = getPodcastBaseUrl() + "/stopall?type=xml";
        String startCatUrl = getPodcastBaseUrl() + "/startcat?type=xml";
        String stopCatUrl = getPodcastBaseUrl() + "/stopcat?type=xml";
        String title = "Status";
        String desc = "Catchup TV status";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, statusUrl);
      StringBuilder recordingProgress = new StringBuilder();
      recordingProgress.append(recorder.getRecordingCount());
      recordingProgress.append(" in progress<br/>");
      recordingProgress.append(recorder.getCompletedCount());
      recordingProgress.append(" completed<br/>");
      recordingProgress.append(recorder.getFailedCount());
      recordingProgress.append(" failed<br/>");
      recordingProgress.append(recorder.getProcessCount());
      recordingProgress.append(" processes<br/>");

      builder.addCategoryItem("RECORDING PROGRESS", recordingProgress.toString(), recordingsUrl);
        builder.addCategoryItem("RECORDING ERRORS", String.valueOf(recorder.getErrors().size()), recerrorsUrl);
        builder.addTextItem("CATALOGING PROGRESS", cataloger.getProgress() + "<br/>" + cataloger.getStatsSummary(), statusUrl);
        builder.addCategoryItem("CATALOGING ERRORS", cataloger.getErrorSummary(), errorsUrl);
        builder.addCategoryItem("STOP ALL RECORDING", "Abandon all recording", stopUrl);
        builder.addCategoryItem("START CATALOGING", "Start cataloging", startCatUrl);
        builder.addCategoryItem("STOP CATALOGING", "Stop cataloging", stopCatUrl);
        builder.stopDocument();

        return builder.toString();

    }

}
