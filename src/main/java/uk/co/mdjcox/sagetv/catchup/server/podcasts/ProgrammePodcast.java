package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.RssBuilder;

import java.util.Set;

/**
 * Created by michael on 07/10/14.
 */
public class ProgrammePodcast extends AbstractPodcast {

    private final HtmlUtilsInterface htmlUtils;
    private final Programme service;
    private final Catalog catalog;
    private String page;

    public ProgrammePodcast(String baseUrl, Catalog catalog, Programme category, HtmlUtilsInterface htmlUtils) {
        super(baseUrl);
        this.htmlUtils = htmlUtils;
        this.catalog = catalog;
        this.service = category;
        page = buildPage();
    }

    public String buildPage() {

        final String shortName = htmlUtils.makeContentSafe(service.getShortName());
        final String longName = htmlUtils.makeContentSafe(service.getLongName());
        final String url = service.getServiceUrl();
        final String iconUrl = service.getIconUrl();

        RssBuilder builder = new RssBuilder();
        builder.startDocument(shortName, longName, url);
        builder.addImage(iconUrl, shortName, url);
        Programme programme = (Programme) service;
        Set<String> episodes = programme.getEpisodes();
        for (String episodeId : episodes) {
            Episode episode = catalog.getEpisode(episodeId);
            final String title = htmlUtils.makeContentSafe(episode.getPodcastTitle());
            final String desc = htmlUtils.makeContentSafe(episode.getDescription());
            final String episodeIconUrl = episode.getIconUrl();
            final String controlUrl=getPodcastBaseUrl() +  "/control?id=" + episode.getId() + ";type=xml";
            builder.addCategoryItem(title, desc, controlUrl, episodeIconUrl);
        }
        builder.stopDocument();
        return builder.toString();
    }

    @Override
    public String getUri() {
        return "/programme?id=" + service.getId() + ";type=xml";
    }

    @Override
    public String getPage() {
        return page;
    }


}
