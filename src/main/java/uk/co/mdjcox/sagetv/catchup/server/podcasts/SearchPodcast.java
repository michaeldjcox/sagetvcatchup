package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.model.Category;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.utils.RssBuilder;

/**
 * Created by michael on 17/10/14.
 */
public class SearchPodcast extends AbstractPodcast {

  private Catalog catalog;
  private String searchString="+$df!";

  public SearchPodcast(String podcastUrl, Catalog catalog) {
    super(podcastUrl);
    this.catalog = catalog;
  }

  @Override
  public String getUri() {
    return "search?type=xml";
  }


  @Override
  public String getPage() {
    return buildPage();
  }

  @Override
  public String buildPage() {
    String errorsUrl = getPodcastBaseUrl() + getUri();
    String title = "SEARCH";
    String desc = "Search for '" + searchString + "'";

    RssBuilder builder = new RssBuilder();
    builder.startDocument(title, desc, errorsUrl);

    for (Category cat : catalog.getCategories()) {
      if (cat.isProgrammeCategory() && cat.getParentId().isEmpty()) {
        Programme prog  = (Programme)cat;

        if (!prog.getLongName().contains(searchString) && !prog.getShortName().contains(searchString)) {
          continue;
        }

        builder.addCategoryItem(prog.getShortName(), prog.getLongName(), prog.getPodcastUrl());
//        if (prog.hasEpisodes()) {
//          for (String episodeId : ((Programme)cat).getEpisodes()) {
//            Episode episode = catalog.getEpisode(episodeId);
//            if (episode.hasErrors()) {
//              errorList.addAll(episode.getErrors());
//            }
//          }
//        }
      }

    }

    builder.stopDocument();

    return builder.toString();
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }
}
