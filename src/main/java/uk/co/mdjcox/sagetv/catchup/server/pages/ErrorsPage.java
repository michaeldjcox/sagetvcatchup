package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.HtmlBuilder;

import java.util.TreeSet;

/**
 * Created by michael on 06/10/14.
 */
public class ErrorsPage extends AbstractHtmlPage {

    private final Catalog catalog;
    private String errorSummary="";
    private String page;

    public ErrorsPage(Catalog catalog) {
        this.catalog = catalog;
        this.page = buildPage();
    }

    public String buildPage() {
        TreeSet<ParseError> errorList = new TreeSet<ParseError>();
        for (Category cat : catalog.getCategories()) {
            if (cat.isSource() || cat.isRoot()) {
                if (cat.hasErrors()) {
                    errorList.addAll(cat.getErrors());
                }
            }
            if (cat.isProgrammeCategory()) {
                if (cat.hasErrors()) {
                    errorList.addAll(cat.getErrors());
                }
                Programme prog  = (Programme)cat;
                if (prog.hasEpisodes()) {
                    for (String episodeId : ((Programme)cat).getEpisodes()) {
                        Episode episode = catalog.getEpisode(episodeId);
                        if (episode.hasErrors()) {
                            errorList.addAll(episode.getErrors());
                        }
                    }
                }
            }

        }
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader("Errors page");
        htmlBuilder.startBody();
        htmlBuilder.addHeading1("Cataloging errors");
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
                linkBuilder.addLink(error.getId(), "/programme=" + error.getId());
                link = linkBuilder.toString();
            } else
            if (error.getType().equals(Episode.class.getSimpleName())) {
                linkBuilder.addLink(error.getId(), "/episode=" + error.getId());
                link = linkBuilder.toString();
            } else {
                linkBuilder.addLink(error.getId(), "/category=" + error.getId());
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
        return "/errors?type=html";
    }

    @Override
    public String getPage() {
        return page;
        }
}
