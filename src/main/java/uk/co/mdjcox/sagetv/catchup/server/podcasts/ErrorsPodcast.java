package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.utils.HtmlUtilsInterface;
import uk.co.mdjcox.sagetv.utils.RssBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by michael on 07/10/14.
 */
public class ErrorsPodcast extends AbstractPodcast {

  private final HtmlUtilsInterface htmlUtils;
  private Map<String, Integer> errorSummary = new HashMap<String, Integer>();
    private String page;

    public ErrorsPodcast(String baseUrl, HtmlUtilsInterface htmlUtils, Map<String, Integer> errorSummary) {
        super(baseUrl);
        this.errorSummary = errorSummary;
      this.htmlUtils = htmlUtils;
        this.page = buildPage();
    }

    public String buildPage() {
        String errorsUrl = getPodcastBaseUrl() + "/"+ getUri();
        String title = "ERRORS";
        String desc = "Cataloging errors";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, errorsUrl);

      for (Map.Entry<String, Integer> entry : errorSummary.entrySet()) {
        final String key = entry.getKey();
        final String[] keys = key.split("\\|");
        final String value = entry.getValue().toString();
        String id = htmlUtils.makeIdSafe(keys[1]);
        String link = getPodcastBaseUrl() + "/errors?message=" + id+";type=xml";
        builder.addCategoryItem(keys[1], value + " " + keys[0], link);
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
