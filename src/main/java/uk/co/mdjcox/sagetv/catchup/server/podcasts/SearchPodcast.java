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
    String errorsUrl = getPodcastBaseUrl() + "/" + getUri();
    String title = "SEARCH";
    String desc = "Search for '" + searchString + "'";

    RssBuilder builder = new RssBuilder();
    builder.startDocument(title, desc, errorsUrl);

    for (Programme prog : catalog.getProgrammes()) {
        if (!prog.getLongName().toUpperCase().contains(searchString.toUpperCase()) &&
                !prog.getShortName().toUpperCase().contains(searchString.toUpperCase())) {
          continue;
        }

        String url = prog.getPodcastUrl();
        if ((url != null) && url.startsWith("/")) {
          url = getPodcastBaseUrl() + url;
        }

        builder.addCategoryItem(prog.getShortName(), prog.getLongName(), url);
      }

    builder.stopDocument();

    return builder.toString();
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }
}
