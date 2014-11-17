package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.model.Category;
import uk.co.mdjcox.sagetv.model.SubCategory;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.RssBuilder;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by michael on 07/10/14.
 */
public class CategoryPodcast extends OnDemandPodcast {

    private final HtmlUtilsInterface htmlUtils;
    private final Category category;
    private final Catalog catalog;

    public CategoryPodcast(String baseUrl, Catalog catalog, Category category, HtmlUtilsInterface htmlUtils) {
        super(baseUrl);
        this.htmlUtils = htmlUtils;
        this.catalog = catalog;
        this.category = category;
    }

    public String buildPage() {

        final String shortName = htmlUtils.makeContentSafe(category.getShortName());
        final String longName = htmlUtils.makeContentSafe(category.getLongName());
        String url = category.getServiceUrl();
        String iconUrl = category.getIconUrl();
        if (iconUrl != null && iconUrl.startsWith("/")) {
          iconUrl = getPodcastBaseUrl() + iconUrl;
        }

      if (url != null && url.startsWith("/")) {
        url = getPodcastBaseUrl() + url;
      }

      RssBuilder builder = new RssBuilder();
        builder.startDocument(shortName, longName, url);
      if (iconUrl != null && !iconUrl.isEmpty()) {
        builder.addImage(iconUrl, shortName, url);
      }

        Set<String> subCats = ((SubCategory) category).getSubCategories();
      CategoryComparator comparator = new CategoryComparator();
        Set<Category> subCatSet = new TreeSet<Category>(comparator);
      for (String subCatId : subCats) {
        Category subCat =  catalog.getCategory(subCatId);
        subCatSet.add(subCat);
      }
      for (Category subCat: subCatSet) {
        if (subCat.hasEpisodes()) {
          String programmeIconUrl = subCat.getIconUrl();
          if (programmeIconUrl != null && programmeIconUrl.startsWith("/")) {
            programmeIconUrl = getPodcastBaseUrl() + programmeIconUrl;
          }
          final String categoryUrl = getPodcastBaseUrl() + "/programme?id=" + subCat.getId() + ";type=xml";
          builder.addCategoryItem(subCat.getShortName(), subCat.getLongName(), categoryUrl, programmeIconUrl);
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
        return "category?id=" + category.getId() + ";type=xml";
    }
}
