package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.HtmlBuilder;

import java.util.*;

/**
 * Created by michael on 06/10/14.
 */
public class ErrorsPage extends AbstractHtmlPage {

    private String page;
    private Collection<ParseError> errorList = new ArrayList<ParseError>();
    private Map<String, Integer> errorSummary = new HashMap<String, Integer>();

    public ErrorsPage(Collection<ParseError> errorList, Map<String, Integer> errorSummary) {
      this.errorList = errorList;
      this.errorSummary = errorSummary;
      this.page = buildPage();
    }

    public String buildPage() {
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader("Errors page");
        htmlBuilder.startBody();
        htmlBuilder.addHeading1("Cataloging errors");

      htmlBuilder.addHeading2("Summary");

      htmlBuilder.startTable();
      htmlBuilder.addTableHeader("Level", "Message", "Count");
      for (Map.Entry<String, Integer> entry : errorSummary.entrySet()) {
        final String key = entry.getKey();
        final String[] keys = key.split("\\|");
        final String value = entry.getValue().toString();
        htmlBuilder.addTableRow(keys[0], keys[1], value);
      }

      htmlBuilder.stopTable();

      htmlBuilder.addBreak();

      htmlBuilder.addHeading2("Detail");

        htmlBuilder.startTable();
        htmlBuilder.addTableHeader("Source", "Level", "Programme", "Episode", "Error", "URL");
        for (ParseError error : errorList) {
            HtmlBuilder listBuilder = new HtmlBuilder();
            listBuilder.startList();
            for (String sourceUrl : error.getSourceUrl()) {
                HtmlBuilder linkBuilder = new HtmlBuilder();
                linkBuilder.addLink(sourceUrl, sourceUrl);
                listBuilder.addListItem(linkBuilder.toString());
            }

            String link = error.getId();
            HtmlBuilder linkBuilder = new HtmlBuilder();
            if (error.getType().equals(Programme.class.getSimpleName())) {
                linkBuilder.addLink(error.getId(), "/programme?id=" + error.getId() + ";type=html");
                link = linkBuilder.toString();
            } else
            if (error.getType().equals(Episode.class.getSimpleName())) {
                linkBuilder.addLink(error.getId(), "/episode?id=" + error.getId() + ";type=html");
                link = linkBuilder.toString();
            } else {
                linkBuilder.addLink(error.getId(), "/category?id=" + error.getId() + ";type=html");
                link = linkBuilder.toString();
            }

            htmlBuilder.addTableRow(error.getSource(), error.getLevel(), error.getType(),
                    link, error.getMessage(), listBuilder.toString());
        }
        htmlBuilder.stopTable();
        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();

        return htmlBuilder.toString();

    }
    @Override
    public String getUri() {
        return "errors?type=html";
    }

    @Override
    public String getPage() {
        return page;
        }
}
