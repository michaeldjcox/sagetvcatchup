package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StopCatalogingPage extends AbstractHtmlPage {

    private Cataloger cataloger;

    public StopCatalogingPage(Cataloger cataloger) {
        this.cataloger = cataloger;
    }

    @Override
    public String getUri() {
        return "stopcat?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String message = cataloger.stopCataloging();
        HtmlBuilder builder = new HtmlBuilder();
        builder.startDocument();
        builder.addPageHeader("Cataloging", true, "/");
        builder.startBody();
        builder.addHeading1(message);
        builder.stopBody();
        builder.stopDocument();
        return builder.toString();
    }
}
