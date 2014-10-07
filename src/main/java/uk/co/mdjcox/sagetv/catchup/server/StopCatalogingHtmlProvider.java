package uk.co.mdjcox.sagetv.catchup.server;

import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.catchup.server.HtmlPageProvider;
import uk.co.mdjcox.utils.HtmlBuilder;

/**
 * Created by michael on 07/10/14.
 */
public class StopCatalogingHtmlProvider extends HtmlPageProvider {

    private Cataloger cataloger;

    public StopCatalogingHtmlProvider(Cataloger cataloger) {
        this.cataloger = cataloger;
    }

    @Override
    public String getUri() {
        return "/stopcat?type=html";
    }

    @Override
    public String getPage() {
        return buildPage();
    }

    @Override
    public String buildPage() {
        String message = cataloger.stop();
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
