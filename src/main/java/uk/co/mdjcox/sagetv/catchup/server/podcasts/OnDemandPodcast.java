package uk.co.mdjcox.sagetv.catchup.server.podcasts;

/**
 * Created by michael on 17/11/2014.
 */
public abstract class OnDemandPodcast extends AbstractPodcast {

    private String page = null;

    protected OnDemandPodcast(String podcastUrl) {
        super(podcastUrl);
    }

    @Override
    public final String getPage() {
        if (page == null) {
            page = buildPage();
        }
        return page;
    }
}
