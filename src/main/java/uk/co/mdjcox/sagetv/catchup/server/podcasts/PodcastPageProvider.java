package uk.co.mdjcox.sagetv.catchup.server.podcasts;

/**
 * Created by michael on 07/10/14.
 */

import uk.co.mdjcox.sagetv.catchup.server.ContentProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class PodcastPageProvider implements ContentProvider {

    private String podcastBaseUrl;

    protected PodcastPageProvider(String podcastUrl) {
        this.podcastBaseUrl = podcastUrl;
    }

    protected String getPodcastBaseUrl() {
        return podcastBaseUrl;
    }

    @Override
    public void serve(HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding(getEncoding());
        response.setContentType(getType());
        response.getWriter().println(getPage());
    }

    @Override
    public String getType() {
        return  "application/xhtml+xml";
    }

    @Override
    public String getEncoding() {
        return "UTF-8";
    }
}
