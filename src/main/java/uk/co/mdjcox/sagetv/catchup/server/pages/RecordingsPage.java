package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.utils.HtmlBuilder;

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
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader("Recordings");
        htmlBuilder.startBody();
        htmlBuilder.addHeading1("Recordings");
        Collection<Recording> recordings = recorder.getCurrentRecordings();
        htmlBuilder.startTable();
        htmlBuilder.addTableHeader("Source", "Episode", "In progress", "Stop");
        for (Recording recording : recordings) {
            HtmlBuilder linkBuilder = new HtmlBuilder();
            String stopLink = "/stop?id=" + recording.getId();
            linkBuilder.addLink("stop", stopLink);
            htmlBuilder.addTableRow(recording.getSourceId(), recording.getId(), String.valueOf(recording.isInProgress()), linkBuilder.toString());
        }
        htmlBuilder.stopTable();

        htmlBuilder.addBreak();
        String stopLink = "/stopall";
        htmlBuilder.addLink("Stop all recordings", stopLink);


        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();
        return htmlBuilder.toString();
    }

    @Override
    public String getUri() {
        return "/recordings?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }
}
