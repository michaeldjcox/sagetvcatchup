package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.catchup.server.pages.HtmlPageProvider;
import uk.co.mdjcox.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StartCatalogingHtmlProvider extends HtmlPageProvider {

    private Cataloger cataloger;

    public StartCatalogingHtmlProvider(Cataloger cataloger) {
        this.cataloger = cataloger;
    }

    @Override
    public String getUri() {
        return "/startcat?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String message = cataloger.start();
        HtmlBuilder builder = new HtmlBuilder();
        builder.startDocument();
        builder.addPageHeader("Cataloging");
        builder.startBody();
        builder.addHeading1(message);
        builder.stopBody();
        builder.stopDocument();
        return builder.toString();
    }
}
