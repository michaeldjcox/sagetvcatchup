package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.ParseError;
import uk.co.mdjcox.sagetv.utils.HtmlUtilsInterface;
import uk.co.mdjcox.sagetv.utils.RssBuilder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by michael on 07/10/14.
 */
public class ErrorDetailsPodcast extends AbstractPodcast {

  private final HtmlUtilsInterface htmlUtils;
  private Collection<ParseError> errorList = new ArrayList<ParseError>();
  private String message = null;
    private String page;

    public ErrorDetailsPodcast(String baseUrl, HtmlUtilsInterface htmlUtils, Collection<ParseError> errorList, String message) {
        super(baseUrl);
        this.errorList = errorList;
      this.message = message;
      this.htmlUtils = htmlUtils;
        this.page = buildPage();
    }

    public String buildPage() {
        String errorsUrl = getPodcastBaseUrl() + "/"+ getUri();
        String title = "ERRORS";
        String desc = "Cataloging errors";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, errorsUrl);

      for (ParseError error : errorList) {
        if (error.getMessage().split(":")[0].equals(message)) {
          builder.addTextItem(error.getLevel(), error.getSource() + "<br/>" + error.getType() + " " + error.getId() + "<br/>" + error.getMessage(), "");
        }
      }

        builder.stopDocument();

        return builder.toString();
    }

    @Override
    public String getUri() {
        return "errors?message=" + htmlUtils.makeIdSafe(message) + ";type=xml";
    }

    @Override
    public String getPage() {
        return page;
    }
}
