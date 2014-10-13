package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.RssBuilder;

import java.util.TreeSet;

/**
 * Created by michael on 07/10/14.
 */
public class ErrorsPodcast extends AbstractPodcast {

    private final Catalog catalog;
    private String page;

    public ErrorsPodcast(String baseUrl, Catalog catalog) {
        super(baseUrl);
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

        String errorsUrl = getPodcastBaseUrl() + getUri();
        String title = "ERRORS";
        String desc = "Cataloging errors";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, errorsUrl);

        for (ParseError error : errorList) {
            builder.addTextItem(error.getLevel(), error.getSource() + " " + error.getType() + " " + error.getId() + " " +  error.getMessage(), "");
        }

        builder.stopDocument();

        return builder.toString();
    }

    @Override
    public String getUri() {
        return "errors?type=xml";
    }

    @Override
    public String getPage() {
        return page;
    }
}
