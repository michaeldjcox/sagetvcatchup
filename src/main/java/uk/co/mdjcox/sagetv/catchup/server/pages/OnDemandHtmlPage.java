package uk.co.mdjcox.sagetv.catchup.server.pages;

/**
 * Created by michael on 17/11/2014.
 */
public abstract class OnDemandHtmlPage extends AbstractHtmlPage {

    private String page = null;

    @Override
    public final String getPage() {
        if (page == null) {
            page = buildPage();
        }
        return page;
    }
}
