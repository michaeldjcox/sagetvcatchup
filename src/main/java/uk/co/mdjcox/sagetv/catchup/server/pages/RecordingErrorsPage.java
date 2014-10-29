package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.HtmlBuilder;

import java.text.SimpleDateFormat;

/**
 * Created by michael on 06/10/14.
 */
public class RecordingErrorsPage extends AbstractHtmlPage {

    private final Recorder recorder;

    public RecordingErrorsPage(Recorder recorder) {
        this.recorder = recorder;
    }

    public String buildPage() {
      SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader("Errors page");
        htmlBuilder.startBody();
        htmlBuilder.addHeading1("Recording errors");
        htmlBuilder.startTable();
        htmlBuilder.addTableHeader("Start Time", "Stop Time", "Episode Id", "Error", "Exception");
        for (Recording failedRecording : recorder.getFailedRecordings()) {
            htmlBuilder.addTableRow(
                    format.format(failedRecording.getStartTime()),
                    format.format(failedRecording.getStopTime()),
                    failedRecording.getId(),
                    failedRecording.getFailedReason(),
                    failedRecording.getFailureException().getMessage());
        }
        htmlBuilder.stopTable();
        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();

        return htmlBuilder.toString();

    }
    @Override
    public String getUri() {
        return "recerrors?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
        }
}
