package uk.co.mdjcox.sagetv.catchup.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;
import uk.co.mdjcox.sagetv.catchup.*;
import uk.co.mdjcox.sagetv.catchup.server.media.CssPage;
import uk.co.mdjcox.sagetv.catchup.server.media.LogoImage;
import uk.co.mdjcox.sagetv.catchup.server.media.WatchAndKeepEpisode;
import uk.co.mdjcox.sagetv.catchup.server.media.WatchEpisode;
import uk.co.mdjcox.sagetv.catchup.server.pages.*;
import uk.co.mdjcox.sagetv.catchup.server.podcasts.*;
import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.LoggerInterface;
import uk.co.mdjcox.utils.OsUtils;
import uk.co.mdjcox.utils.OsUtilsInterface;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
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
  private final String htDocsDir;
  private final String stagingDir;
  private final SocketConnector connector;
  private final CatalogPersister persister;
  private final OsUtilsInterface osUtils;
  private LoggerInterface logger;
    private org.mortbay.jetty.Server server;
    private int port;
    private Recorder recorder;
    private HtmlUtilsInterface htmlUtils;

    private Map<String, ContentProvider> publishedContent = new HashMap<String, ContentProvider>();
    private Map<String, ContentProvider> staticContent = new HashMap<String, ContentProvider>();

    @Inject
    private Server(LoggerInterface logger, CatchupContextInterface context,
                   HtmlUtilsInterface htmlUtils, OsUtilsInterface osUtils,
                   Cataloger cataloger, Recorder recorder, CatalogPersister persister) throws Exception {
        this.logger = logger;
        this.htmlUtils = htmlUtils;
      this.osUtils = osUtils;
      this.persister = persister;

        Handler handler = new AbstractHandler() {
            public void handle(String target,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               int dispatch) throws IOException, ServletException {
                Server.this.handle(target, request, response, dispatch);
            }
        };
        server = new org.mortbay.jetty.Server();
        connector = new SocketConnector();
        port = context.getPort();
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        server.setHandler(handler);
        this.recorder = recorder;

        baseUrl = context.getPodcastBase();

        logDir = context.getLogDir();
        cssDir = context.getCssDir();
        xsltDir = context.getXsltDir();
        htDocsDir = context.getTmpDir() + File.separator + "htdocs";
        stagingDir = context.getTmpDir() + File.separator + "staging";

        init(htmlUtils, cataloger, recorder);
    }

    private void init(HtmlUtilsInterface htmlUtils, Cataloger cataloger, Recorder recorder) {
        StatusPage statusPageProvider = new StatusPage(cataloger, recorder);
        addStaticContent(statusPageProvider);

        RecordingsPage recProvider = new RecordingsPage(recorder);
        addStaticContent(recProvider);

        RecordingsPodcast recordingsPodcastProvider = new RecordingsPodcast(htmlUtils, baseUrl, recorder);
        addStaticContent(recordingsPodcastProvider);

        RecordingsCompletePodcast recordingsCompletePodcast = new RecordingsCompletePodcast(htmlUtils, baseUrl, recorder);
        addStaticContent(recordingsCompletePodcast);

        RecordingStatusPodcast recordingStatusPodcast = new RecordingStatusPodcast(baseUrl, recorder);
        addStaticContent(recordingStatusPodcast);

        RecordingErrorsPage recerrors = new RecordingErrorsPage(recorder);
        addStaticContent(recerrors);

        RecordingErrorsPodcast recerrorscast = new RecordingErrorsPodcast(htmlUtils, baseUrl, recorder);
        addStaticContent(recerrorscast);

        CatalogingStatusPodcast cataloging = new CatalogingStatusPodcast(baseUrl, cataloger);
        addStaticContent(cataloging);

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

  public void addCachedPublishedContent(Map<String, ContentProvider> publishedContent, ContentProvider provider) {
    CachedContentProvider cachedContentProvider = new CachedContentProvider(logger, stagingDir, htDocsDir, provider);
    publishedContent.put(provider.getUri(), cachedContentProvider);
  }

    public void commitPublishedContent(Map<String, ContentProvider> publishedContent) throws Exception {

      final File currentContent = new File(htDocsDir);
      final File stagedContent = new File(stagingDir);

      if (currentContent.exists()) {
        osUtils.deleteFileOrDir(currentContent, true);
      }

      Files.move(stagedContent.toPath(), currentContent.toPath());

      stagedContent.mkdirs();

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

      while (target.startsWith("/")) {
        target = target.substring(1);
      }

        String page = target;
        Enumeration params = request.getParameterNames();
        while (params.hasMoreElements()) {
            Object name = params.nextElement();
            if (page.equals(target)) {
                page += "?";
            } else {
                page += "&";
            }
            page += name + "=" + request.getParameter(name.toString());
        }

        logger.debug("Got http request: " + page);

      if (target.contains("stopserver")) {
        logger.info("Calling system exit");
        System.exit(1);
      }

      if (target.endsWith(".css")) {
        new CssPage(logger, cssDir, target).serve(response);
      } else if (staticContent.containsKey(page)) {
        staticContent.get(page).serve(response);
      } else
      if (page.contains("search?text=")) {
        SearchPodcast searchPodcast = (SearchPodcast)publishedContent.get(page.replaceAll("text=.*;type=xml", "type=xml"));
        searchPodcast.setSearchString(page.replaceAll("search\\?text=", "").replaceAll(";type=xml", ""));
        searchPodcast.serve(response);
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
            logger.info("Publishing new catalog to web server");

            Map<String, ContentProvider> publishedContent = new HashMap<String, ContentProvider>();

            StyledPage programmes = new StyledPage(xsltDir, logger, "Programmes", "programmes.html", null, catalog, persister);
            addCachedPublishedContent(publishedContent, programmes);

            StyledPage categories = new StyledPage(xsltDir, logger, "Categories", "categories.html", null, catalog, persister);
            addCachedPublishedContent(publishedContent, categories);

            StyledPage episodes = new StyledPage(xsltDir, logger, "Episodes", "episodes.html", null, catalog, persister);
            addCachedPublishedContent(publishedContent, episodes);

            for (Category cat : catalog.getCategories()) {
                boolean isProgramme =cat.isProgrammeCategory();
                String title=isProgramme ? "Programme" : "Category";
                String webpage= isProgramme ? "programme.html" : "category.html";
                StyledPage provider = new StyledPage(xsltDir, logger, title, webpage, cat.getId(), cat, persister);
                addCachedPublishedContent(publishedContent, provider);
                if (cat.hasEpisodes()) {
                    ProgrammePodcast catProvider = new ProgrammePodcast(baseUrl, catalog, (SubCategory)cat, htmlUtils);
                    addCachedPublishedContent(publishedContent, catProvider);
                } else {
                    CategoryPodcast catProvider = new CategoryPodcast(baseUrl, catalog, cat, htmlUtils);
                    addCachedPublishedContent(publishedContent, catProvider);
                }

            }

            for (Episode episode : catalog.getEpisodes()) {
                StyledPage provider = new StyledPage(xsltDir, logger, "Episode", "episode.html", episode.getId(), episode, persister);
                addCachedPublishedContent(publishedContent, provider);

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
              WatchAndKeepEpisode watchAndKeepEpisodeVideoProvider = new WatchAndKeepEpisode(logger, episode, recorder);
              addPublishedContent(publishedContent, watchAndKeepEpisodeVideoProvider);
            }

          SearchPodcast searchPodcast = new SearchPodcast(baseUrl, catalog);
          addPublishedContent(publishedContent, searchPodcast);

            commitPublishedContent(publishedContent);

            logger.info("Published catalog to web server");
        } catch (Exception e) {
          String message = e.getMessage();
          if (message == null) {
            message = e.getClass().getSimpleName();
          }
           Category root = catalog.getRoot();
          if (root != null) {
            root.addError("FATAL", "Failed to publish to web server: " + message);
          }
            logger.error("Failed to publish catalog to web server", e);
        } finally {
          Collection<ParseError> errorList = catalog.getErrors();
          Map<String, Integer> errorSummary = new HashMap<String, Integer>();
          for (ParseError error : errorList) {
            String key = error.getLevel() + "|" + error.getMessage().split(":")[0];

            Integer cnt = errorSummary.get(key);
            if (cnt == null) {
              cnt = 1;
            } else {
              cnt = cnt + 1;
            }
            errorSummary.put(key, cnt);
          }
            ErrorsPage provider = new ErrorsPage(errorList, errorSummary);
            addPublishedContent(publishedContent, provider);

            ErrorsPodcast errProvider = new ErrorsPodcast(baseUrl, htmlUtils, errorSummary);
            addPublishedContent(publishedContent, errProvider);

          for (Map.Entry<String, Integer> entry : errorSummary.entrySet()) {
            final String key = entry.getKey();
            final String[] keys = key.split("\\|");
            ErrorDetailsPodcast errorDetailsPodcast = new ErrorDetailsPodcast(baseUrl, htmlUtils, errorList, keys[1]);
            addPublishedContent(publishedContent, errorDetailsPodcast);
          }

        }
    }

  public void setPort(int port) {
    this.port = port;
    connector.setPort(port);
  }
}
