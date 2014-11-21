package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.HtmlBuilder;

import java.text.SimpleDateFormat;
import java.util.Collection;

/**
 * Created by michael on 07/10/14.
 */
public class RecordingsPage extends AbstractHtmlPage {

    private final Recorder recorder;

    public RecordingsPage(Recorder recorder) {
        this.recorder = recorder;
    }

    public String buildPage() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader("Recordings");
        htmlBuilder.startBody();
        htmlBuilder.addHeading1("In progress");
        Collection<Recording> recordings = recorder.getCurrentRecordings();
        htmlBuilder.startTable();
        htmlBuilder.addTableHeader("Source", "Episode", "Start Time", "Stop Time", "Duration", "Size", "Percent", "Status", "Stop");
        for (Recording recording : recordings) {
            HtmlBuilder linkBuilder = new HtmlBuilder();
            String stopLink = "/stop?id=" + recording.getId()+";type=html";
            linkBuilder.addLink("stop", stopLink);

            int duration = (int)(recording.getStopTime() - recording.getStartTime());

            final int millisInHour = 60 * 60 * 1000;
            final int millisInMin = 60 * 1000;
            int hours = (int)(duration / millisInHour) ;
            int minutes = (int)((duration % millisInHour) / millisInMin);
            int seconds = (int)((duration % millisInMin) / 1000);
            String durationStr = String.format("After: %02d:%02d:%02d", hours, minutes, seconds);
            double size = recording.getLastSize() / 1024000;
            String sizeStr = size + "Mb";

          String status = recording.getRecordingStatus();
          htmlBuilder.addTableRow(recording.getSourceId(),
                  recording.getId(),
                  format.format(recording.getStartTime()),
                  format.format(recording.getStopTime()),
                  durationStr, sizeStr,
                  recording.getPercentRecorded(), status, linkBuilder.toString());
        }
        htmlBuilder.stopTable();

        htmlBuilder.addBreak();
        String stopLink = "/stopall?type=html";
        htmlBuilder.addLink("Stop all recordings", stopLink);

        htmlBuilder.addBreak();

        htmlBuilder.addHeading1("Failed");
        htmlBuilder.startTable();
        htmlBuilder.addTableHeader("Source", "Episode", "Start Time", "Stop Time", "Duration", "Size", "Error", "Exception");
        for (Recording failedRecording : recorder.getFailedRecordings()) {
            String message = failedRecording.getFailureException().getMessage();
            if (message == null) {
                message = failedRecording.getFailureException().getClass().getSimpleName();
            }

            int duration = (int)(failedRecording.getStopTime() - failedRecording.getStartTime());

            final int millisInHour = 60 * 60 * 1000;
            final int millisInMin = 60 * 1000;
            int hours = (int)(duration / millisInHour) ;
            int minutes = (int)((duration % millisInHour) / millisInMin);
            int seconds = (int)((duration % millisInMin) / 1000);
            String durationStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            double size = failedRecording.getLastSize() / 1024000;
            String sizeStr = size + "Mb";

            htmlBuilder.addTableRow(
                    failedRecording.getSourceId(),
                    failedRecording.getId(),
                    format.format(failedRecording.getStartTime()),
                    format.format(failedRecording.getStopTime()),
                    durationStr, sizeStr,
                    failedRecording.getFailedReason(),
                    message);
        }
        htmlBuilder.stopTable();

        htmlBuilder.addBreak();

        htmlBuilder.addHeading1("Completed");
        htmlBuilder.startTable();
        htmlBuilder.addTableHeader("Source", "Episode", "Start Time", "Stop Time", "Duration", "Size");
        for (Recording completedRecording : recorder.getCompletedRecordings()) {

            int duration = (int)(completedRecording.getStopTime() - completedRecording.getStartTime());

            final int millisInHour = 60 * 60 * 1000;
            final int millisInMin = 60 * 1000;
            int hours = (int)(duration / millisInHour) ;
            int minutes = (int)((duration % millisInHour) / millisInMin);
            int seconds = (int)((duration % millisInMin) / 1000);
            String durationStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            double size = completedRecording.getLastSize() / 1024000;
            String sizeStr = size + "Mb";

            htmlBuilder.addTableRow(
                    completedRecording.getSourceId(),
                    completedRecording.getId(),
                    format.format(completedRecording.getStartTime()),
                    format.format(completedRecording.getStopTime()),
                    durationStr, sizeStr);
        }
        htmlBuilder.stopTable();

        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();
        return htmlBuilder.toString();
    }

    @Override
    public String getUri() {
        return "recordings?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }
}
