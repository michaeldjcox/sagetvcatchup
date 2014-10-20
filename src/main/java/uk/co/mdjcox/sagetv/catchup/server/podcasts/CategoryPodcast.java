package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.model.Category;
import uk.co.mdjcox.sagetv.model.SubCategory;
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
        String url = service.getServiceUrl();
        String iconUrl = service.getIconUrl();
        if (iconUrl != null && iconUrl.startsWith("/")) {
          iconUrl = getPodcastBaseUrl() + iconUrl;
        }

      if (url != null && url.startsWith("/")) {
        url = getPodcastBaseUrl() + url;
      }

      RssBuilder builder = new RssBuilder();
        builder.startDocument(shortName, longName, url);
        builder.addImage(iconUrl, shortName, url);

        Set<String> subCats = ((SubCategory) service).getSubCategories();
        for (String subCatId : subCats) {
            SubCategory subCat = (SubCategory)catalog.getCategory(subCatId);
            if (subCat.isProgrammeCategory()) {
                final String categoryUrl = getPodcastBaseUrl() + "/programme?id=" + subCat.getId() + ";type=xml";
                builder.addCategoryItem(subCat.getShortName(), subCat.getLongName(), categoryUrl);
            } else {
                final String categoryUrl = getPodcastBaseUrl() + "/category?id=" + subCat.getId() + ";type=xml";
                builder.addCategoryItem(subCat.getShortName(), subCat.getLongName(), categoryUrl);
            }
        }
        builder.stopDocument();
        return builder.toString();
    }

    @Override
    public String getUri() {
        return "category?id=" + service.getId() + ";type=xml";
    }

    @Override
    public String getPage() {
        return page;
    }


}
