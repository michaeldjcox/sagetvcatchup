package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.RssBuilder;

import java.util.Set;

/**
 * Created by michael on 07/10/14.
 */
public class CategoryPodcast extends AbstractPodcast {

    private final HtmlUtilsInterface htmlUtils;
    private final Category service;
    private final Catalog catalog;
    private String page;

    public CategoryPodcast(String baseUrl, Catalog catalog, Category category, HtmlUtilsInterface htmlUtils) {
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
        if (service.isProgrammeCategory()) {
            Programme programme = (Programme) service;
            Set<String> episodes = programme.getEpisodes();
            for (String episodeId : episodes) {
                Episode episode = catalog.getEpisode(episodeId);
                final String title = htmlUtils.makeContentSafe(episode.getPodcastTitle());
                final String desc = htmlUtils.makeContentSafe(episode.getDescription());
                final String episodeIconUrl = episode.getIconUrl();
                final String controlUrl=getPodcastBaseUrl() +  "/control?id=" + episode.getId();
                builder.addCategoryItem(title, desc, controlUrl, episodeIconUrl);
            }
        } else if (service.isSubCategory()) {
            Set<String> subCats = ((SubCategory) service).getSubCategories();
            for (String subCatId : subCats) {
                SubCategory subCat = (SubCategory)catalog.getCategory(subCatId);
                final String categoryUrl = getPodcastBaseUrl() + "/" + subCat.getId() + ".xml";
                builder.addCategoryItem(subCat.getShortName(), subCat.getLongName(), categoryUrl);
            }
        }
        builder.stopDocument();
        return builder.toString();
    }

    @Override
    public String getUri() {
        return "/" + service.getId() + ".xml";
    }

    @Override
    public String getPage() {
        return page;
    }


}
