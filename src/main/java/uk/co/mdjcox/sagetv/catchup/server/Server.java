package uk.co.mdjcox.sagetv.catchup.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.catchup.CatalogPublisher;
import uk.co.mdjcox.sagetv.catchup.Cataloger;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.catchup.server.media.CssPage;
import uk.co.mdjcox.sagetv.catchup.server.media.LogoImage;
import uk.co.mdjcox.sagetv.catchup.server.media.WatchEpisode;
import uk.co.mdjcox.sagetv.catchup.server.pages.*;
import uk.co.mdjcox.sagetv.catchup.server.podcasts.*;
import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.model.Category;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.utils.HtmlUtilsInterface;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * The web server.
 *
 * This holds no state whatsoever
 */
@Singleton
public class Server implements CatalogPublisher {

    private final String baseUrl;
    private final String cssDir;
    private final String xsltDir;
    private final String logDir;
    private Logger logger;
    private org.mortbay.jetty.Server server;
    private int port;
    private Recorder recorder;
    private HtmlUtilsInterface htmlUtils;

    private Map<String, ContentProvider> publishedContent = new HashMap<String, ContentProvider>();
    private Map<String, ContentProvider> staticContent = new HashMap<String, ContentProvider>();

    @Inject
    private Server(Logger logger, CatchupContextInterface context, HtmlUtilsInterface htmlUtils,
                   Cataloger cataloger, Recorder recorder) throws Exception {
        this.logger = logger;
        this.htmlUtils = htmlUtils;

        Handler handler = new AbstractHandler() {
            public void handle(String target,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               int dispatch) throws IOException, ServletException {
                Server.this.handle(target, request, response, dispatch);
            }
        };
        server = new org.mortbay.jetty.Server();
        Connector connector = new SocketConnector();
        port = context.getPort();
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        server.setHandler(handler);
        this.recorder = recorder;

        baseUrl = context.getPodcastBase();

        logDir = context.getLogDir();
        cssDir = context.getCssDir();
        xsltDir = context.getXsltDir();

        init(htmlUtils, cataloger, recorder);
    }

    private void init(HtmlUtilsInterface htmlUtils, Cataloger cataloger, Recorder recorder) {
        RecordingsPage recProvider = new RecordingsPage(recorder);
        addStaticContent(recProvider);

        RecordingsPodcast recordingsPodcastProvider = new RecordingsPodcast(htmlUtils, baseUrl, recorder);
        addStaticContent(recordingsPodcastProvider);

        LogsPage logsProvider = new LogsPage(logDir + File.separator + "sagetvcatchup.log");
        addStaticContent(logsProvider);

        LogoImage logoProvider = new LogoImage();
        addStaticContent(logoProvider);

        StatusPodcast statusProvider = new StatusPodcast(baseUrl, recorder, cataloger);
        addStaticContent(statusProvider);

        HomePage homeProvider = new HomePage(cataloger, recorder);
        addStaticContent(homeProvider);

        StartCatalogingPage startCatalogingHtmlProvider = new StartCatalogingPage(cataloger);
        addStaticContent(startCatalogingHtmlProvider);

        StopCatalogingPage stopCatalogingHtmlProvider = new StopCatalogingPage(cataloger);
        addStaticContent(stopCatalogingHtmlProvider);

        StartCatalogingPodcast startCatalogingPodcastProvider = new StartCatalogingPodcast(baseUrl, cataloger);
        addStaticContent(startCatalogingPodcastProvider);

        StopCatalogingPodcast stopCatalogingPodcastProvider = new StopCatalogingPodcast(baseUrl, cataloger);
        addStaticContent(stopCatalogingPodcastProvider);

        StopAllRecordingPage stopAllRecordingHtmlProvider = new StopAllRecordingPage(recorder);
        addStaticContent(stopAllRecordingHtmlProvider);

        StopAllRecordingPodcast stopAllRecordingPodcastProvider = new StopAllRecordingPodcast(baseUrl, recorder);
        addStaticContent(stopAllRecordingPodcastProvider);
    }

    public void addStaticContent(ContentProvider provider) {
        staticContent.put(provider.getUri(), provider);
    }

    public void addPublishedContent(Map<String, ContentProvider> publishedContent, ContentProvider provider) {
        publishedContent.put(provider.getUri(), provider);
    }

    public void commitPublishedContent(Map<String, ContentProvider> publishedContent) {
        this.publishedContent.clear();
        this.publishedContent = publishedContent;
    }

    public void start() throws Exception {
        logger.info("Starting the podcast server on port " + port);
      try {
        server.start();
        logger.info("Started the podcast server on port " + port);
      } catch (Exception e) {
        logger.warn("Failed to start the podcast server", e);
      }
    }

    public void shutdown() {
        logger.info("Stopping the podcast server on port " + port);
        try {
            server.stop();
          logger.info("Stopped the podcast server on port " + port);
        } catch (Exception e) {
            logger.warn("Failed to stop the podcast server", e);
        }
    }

    private void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);

        logger.info("Got http request: " + request);

        while (target.startsWith("/")) {
            target = target.substring(1);
        }

        String page = target;
        Enumeration params = request.getParameterNames();
        while (params.hasMoreElements()) {
            Object name = params.nextElement();
            if (page.equals(target)) {
                page +="?";
            } else {
                page += "&";
            }
            page += name +"=" + request.getParameter(name.toString());
        }


        if (target.endsWith(".css")) {
            new CssPage(logger, cssDir, target).serve(response);
        } else
        if (staticContent.containsKey(page)) {
            staticContent.get(page).serve(response);
        } else
        if (publishedContent.containsKey(page)) {
            publishedContent.get(page).serve(response);
        } else {
            if (page.contains(";type=xml")) {
                new MessagePodcast(baseUrl, "Podcast not found " + page).serve(response);
            } else {
                new MessagePage("Page not found " + page).serve(response);
            }
        }

          ((Request) request).setHandled(true);
    }

    public void publish(Catalog catalog) {
        try {
            Map<String, ContentProvider> publishedContent = new HashMap<String, ContentProvider>();

            ErrorsPage errors = new ErrorsPage(catalog);
            addPublishedContent(publishedContent, errors);

            StyledPage programmes = new StyledPage(xsltDir, logger, "Programmes", "programmes.html", null, catalog);
            addPublishedContent(publishedContent, programmes);

            StyledPage categories = new StyledPage(xsltDir, logger, "Categories", "categories.html", null, catalog);
            addPublishedContent(publishedContent, categories);

            StyledPage episodes = new StyledPage(xsltDir, logger, "Episodes", "episodes.html", null, catalog);
            addPublishedContent(publishedContent, episodes);

            for (Category cat : catalog.getCategories()) {
                boolean isProgramme =cat.isProgrammeCategory() && cat.getParentId().isEmpty();
                String title=isProgramme ? "Programme" : "Category";
                String webpage= isProgramme ? "programme.html" : "category.html";
                StyledPage provider = new StyledPage(xsltDir, logger, title, webpage, cat.getId(), cat);
                addPublishedContent(publishedContent, provider);
                if (cat.isProgrammeCategory()) {
                    ProgrammePodcast catProvider = new ProgrammePodcast(baseUrl, catalog, (Programme)cat, htmlUtils);
                    addPublishedContent(publishedContent, catProvider);
                } else {
                    CategoryPodcast catProvider = new CategoryPodcast(baseUrl, catalog, cat, htmlUtils);
                    addPublishedContent(publishedContent, catProvider);
                }

            }

            for (Episode episode : catalog.getEpisodes()) {
                StyledPage provider = new StyledPage(xsltDir, logger, "Episode", "episode.html", episode.getId(), episode);
                addPublishedContent(publishedContent, provider);

                ControlPodcast controlPodcastProvider = new ControlPodcast(baseUrl, recorder, episode, htmlUtils);
                addPublishedContent(publishedContent, controlPodcastProvider);

                StopEpisodePage stopEpisodeRecordingHtmlProvider = new StopEpisodePage(episode.getId(), recorder);
                addPublishedContent(publishedContent, stopEpisodeRecordingHtmlProvider);
                StopEpisodePodcast stopEpisodeRecordingPodcastProvider = new StopEpisodePodcast(baseUrl, episode.getId(), recorder);
                addPublishedContent(publishedContent, stopEpisodeRecordingPodcastProvider);

                RecordEpisodePage recordEpisodeRecordingHtmlProvider = new RecordEpisodePage(episode, recorder);
                addPublishedContent(publishedContent, recordEpisodeRecordingHtmlProvider);
                RecordEpisodePodcast recordEpisodeRecordingPodcastProvider = new RecordEpisodePodcast(baseUrl, episode, recorder);
                addPublishedContent(publishedContent, recordEpisodeRecordingPodcastProvider);

                WatchEpisode watchEpisodeVideoProvider = new WatchEpisode(logger, episode, recorder);
                addPublishedContent(publishedContent, watchEpisodeVideoProvider);

            }

            commitPublishedContent(publishedContent);
        } catch (Exception e) {
            catalog.addError("FATAL", "Failed to publish to html server " + e.getMessage());
            logger.error("Failed to publish catalog to html server", e);
        } finally {
            ErrorsPage provider = new ErrorsPage(catalog);
            addPublishedContent(publishedContent, provider);
            ErrorsPodcast errProvider = new ErrorsPodcast(baseUrl, catalog);
            addPublishedContent(publishedContent, errProvider);
        }
    }
}
