package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class MessagePage extends AbstractHtmlPage {

    private String message;

    public MessagePage(String message) {
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
