package uk.co.mdjcox.sagetv.catchup.server.podcasts;

/**
 * Created by michael on 07/10/14.
 */

import org.mortbay.jetty.Request;
import uk.co.mdjcox.sagetv.catchup.server.ContentProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractPodcast implements ContentProvider {

    private String podcastBaseUrl;

    protected AbstractPodcast(String podcastUrl) {
        this.podcastBaseUrl = podcastUrl;
    }

    protected String getPodcastBaseUrl() {
        return podcastBaseUrl;
    }

    @Override
    public void serve(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding(getEncoding());
        response.setContentType(getType());
      String page = getPage();
      String newBaseUrl = "http://" + ((Request)request).getServerName() + ":" + ((Request)request).getServerPort();
      page = page.replaceAll(podcastBaseUrl, newBaseUrl);
      response.getWriter().println(page);
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
