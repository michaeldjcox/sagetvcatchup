package uk.co.mdjcox.sagetv.catchup.server;

import uk.co.mdjcox.sagetv.catchup.server.HtmlPageProvider;
import uk.co.mdjcox.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class MessageHtmlProvider extends HtmlPageProvider {

    private String message;

    public MessageHtmlProvider(String message) {
        this.message = message;
    }

    @Override
    public String getUri() {
        return "/error?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        HtmlBuilder builder = new HtmlBuilder();
        builder.startDocument();
        builder.addPageHeader("Error");
        builder.startBody();
        builder.addHeading1(message);
        builder.stopBody();
        builder.stopDocument();
        return builder.toString();
    }
}
