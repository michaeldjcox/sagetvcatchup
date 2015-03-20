package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.sagetv.utils.HtmlUtilsInterface;
import uk.co.mdjcox.sagetv.utils.RssBuilder;

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
          if (subCatId.endsWith("/Search")) {
              continue;
          }
        Category subCat =  catalog.getCategory(subCatId);
        subCatSet.add(subCat);
      }
      for (Category subCat: subCatSet) {
          // Massive hack to get rid of UPnP artificial programme categories with only one
          // Episode and the same details as the Programme
        if (subCat.isSubCategory()) {
            SubCategory prog = (SubCategory)subCat;
            Set<String> episodes = prog.getEpisodes();
            if (episodes.size() == 1) {
                String episodeId = episodes.iterator().next();
                Episode episode = catalog.getEpisode(episodeId);
                if (episode.getEpisodeTitle().equals(subCat.getShortName()) &&
                    episode.getEpisodeTitle().equals(subCat.getLongName())) {
                    //      Episode icon - takes too much space
                    String episodeIconUrl = episode.getIconUrl();
                    if (episodeIconUrl != null && episodeIconUrl.startsWith("/")) {
                        episodeIconUrl = getPodcastBaseUrl() + episodeIconUrl;
                    }

                    if (!episode.getPodcastUrl().startsWith("/control")) {
                        String controlUrl = episode.getPodcastUrl();
                        builder.addVideoItem(episode.getEpisodeTitle(), episode.getDescription(), controlUrl, episodeIconUrl);
                    } else {
                        String controlUrl=getPodcastBaseUrl() +  "/control?id=" + episode.getId() + ";type=xml";
                        builder.addCategoryItem(episode.getEpisodeTitle(), episode.getDescription(), controlUrl, episodeIconUrl);
                    }
                    continue;
                }
            }
        }

        if (subCat.hasEpisodes()) {
          String programmeIconUrl = subCat.getIconUrl();
          if (programmeIconUrl != null && programmeIconUrl.startsWith("/")) {
            programmeIconUrl = getPodcastBaseUrl() + programmeIconUrl;
          }
          final String categoryUrl = getPodcastBaseUrl() + "/programme?id=" + subCat.getId() + ";type=xml";
          builder.addCategoryItem(subCat.getShortName(), subCat.getLongName(), categoryUrl, programmeIconUrl);
        } else {
          String programmeIconUrl = subCat.getIconUrl();
          if (programmeIconUrl != null && programmeIconUrl.startsWith("/")) {
            programmeIconUrl = getPodcastBaseUrl() + programmeIconUrl;
          }
          final String categoryUrl = getPodcastBaseUrl() + "/category?id=" + subCat.getId() + ";type=xml";
          builder.addCategoryItem(subCat.getShortName(), subCat.getLongName(), categoryUrl, programmeIconUrl);
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
