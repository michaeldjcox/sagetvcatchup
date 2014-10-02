package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.HtmlBuilder;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.PropertiesInterface;
import uk.co.mdjcox.utils.RssBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * The web server.
 *
 * This holds no state whatsoever
 */
@Singleton
public class PodcastServer implements CatalogPublisher {

    private final Cataloger cataloger;
    private Logger logger;
    private Server server;
    private Handler handler;
    private int port;
    private Recorder recorder;
    private HtmlUtilsInterface htmlUtils;
    private final String htdocsDir;
    private final String logDir;
    private final String stagingDir;
    private String errorSummary="";

    @Inject
    private PodcastServer(Logger logger, PropertiesInterface props, HtmlUtilsInterface htmlUtils, Cataloger cataloger, Recorder recorder) throws Exception {
        this.logger = logger;
        this.htmlUtils = htmlUtils;

      handler = new AbstractHandler() {
            public void handle(String target,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               int dispatch) throws IOException, ServletException {
                PodcastServer.this.handle(target, request, response, dispatch);
            }
        };
        server = new Server();
        Connector connector = new SocketConnector();
        port = props.getInt("port", props.getInt("podcasterPort"));
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        server.setHandler(handler);
        this.recorder = recorder;
        this.cataloger = cataloger;
        htdocsDir = props.getString("htdocsDir");
        File file = new File(htdocsDir);
        Files.createDirectories(file.toPath());
        stagingDir = props.getString("stagingDir");
        file = new File(stagingDir);
        Files.createDirectories(file.toPath());
        logDir = props.getString("logDir");
    }

    public void start() throws Exception {
        logger.info("Starting the podcast server on port " + port);
        server.start();
    }

    public void shutdown() {
        logger.info("Stopping the podcast server on port " + port);
        try {
            server.stop();
        } catch (Exception e) {
            logger.warn("Failed to stop the podcast server", e);
        }
    }

    private void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);

        logger.info("Got http request: " + request);


        String agent = request.getHeader("User-Agent");
        boolean fromSage = agent != null && agent.contains("Java");

        if (target.equals("/logo.png")) {
            getLogoResponse(response);
        } else if (target.startsWith("/watch")) {
            String id = request.getParameter("id");
            Episode episode = getEpisodeFromCache(id);
            getVideoResponse(response, episode);
        } else if (target.startsWith("/recordings")) {
            if (fromSage) {
               getRecordingsPodcastResponse(response);
            } else {
                getRecordingsHtmlResponse(response);
            }
        } else if (target.startsWith("/record")) {
            String id = request.getParameter("id");
            Episode episode = getEpisodeFromCache(id);
            recorder.record(episode);
            if (fromSage) {
                getMessagePodcastResponse(response, "RECORDING", "Recording " + episode.getPodcastTitle(), "");
            } else {
                getMessageHtmlResponse(response, "Recording " + episode.getPodcastTitle());
            }
        } else if (target.startsWith("/control")) {
            String id = request.getParameter("id");
            Episode episode = getEpisodeFromCache(id);
            getControlPodcastResponse(response, episode, !recorder.isStopped(id));
        } else if (target.startsWith("/status")) {
            getStatusPodcastResponse(response);
        } else if (target.startsWith("/stopcat")) {
            String result = cataloger.stop();
            if (fromSage) {
                getMessagePodcastResponse(response, "CATALOGING", result, "");
            } else {
                getMessageHtmlResponse(response, result);
            }
        } else if (target.startsWith("/startcat")) {
            String result = cataloger.start();
            if (fromSage) {
                getMessagePodcastResponse(response, "CATALOGING", result, "");
            } else {
                getMessageHtmlResponse(response, result);
            }
        } else if (target.startsWith("/stopall")) {
            String result = recorder.requestStopAll();
            if (fromSage) {
                getMessagePodcastResponse(response, "RECORDING", result, "");
            } else {
                getMessageHtmlResponse(response, result);
            }
        } else if (target.startsWith("/stop")) {
            String id = request.getParameter("id");
            String message = recorder.requestStop(id);
            if (fromSage) {
                getMessagePodcastResponse(response, "STOPPING", message, "");
            } else {
                getMessageHtmlResponse(response, message);
            }
        } else if (target.startsWith("/log")) {
            getLogFileResponse(response, "sagetvcatchup.log");
        } else if (target.startsWith("/errors")) {
            if (fromSage) {
                getCachedPodcastResponse(response, "errors");
            } else {
                getCachedHtmlResponse(response, "errors");
            }
        } else if (target.startsWith("/programmes")) {
            getCachedHtmlResponse(response, "programmes");
        } else if (target.startsWith("/episodes")) {
            getCachedHtmlResponse(response, "episodes");
        } else if (target.startsWith("/categories")) {
            getCachedHtmlResponse(response, "categories");
        } else if (target.startsWith("/programme=")) {
            String id = target.substring(target.indexOf('=') + 1);
            getCachedHtmlResponse(response, "programme-" + id);
        } else if (target.startsWith("/episode=")) {
            String id = target.substring(target.indexOf('=') + 1);
            getCachedHtmlResponse(response, "episode-" + id);
        } else if (target.startsWith("/category=")) {
            String id = target.substring(target.indexOf('=') + 1);
            getCachedHtmlResponse(response, "category-" + id);
        } else if (target.equals("/") || target.equals("/index.html")) {
            getIndexResponse(response);
        } else {
            String serviceName = target.substring(1);
            getCachedPodcastResponse(response, "podcast-" + serviceName);
        }
          ((Request) request).setHandled(true);
    }

    private Episode getEpisodeFromCache(String id) throws ServletException {
        try {
            String message = getFromCache(htdocsDir, "episode-" + id + ".html");
            message = htmlUtils.moveTo("<td>SourceId", message);
            message = htmlUtils.moveTo("<td>", message);
            String sourceId = htmlUtils.extractTo("</td>", message);
            message = htmlUtils.moveTo("<td>Channel", message);
            message = htmlUtils.moveTo("<td>", message);
            String channel = htmlUtils.extractTo("</td>", message);
            message = htmlUtils.moveTo("<td>ProgrammeTitle", message);
            message = htmlUtils.moveTo("<td>", message);
            String programmeTitle = htmlUtils.extractTo("</td>", message);
            message = htmlUtils.moveTo("<td>SeriesTitle", message);
            message = htmlUtils.moveTo("<td>", message);
            String seriesTitle = htmlUtils.extractTo("</td>", message);
            message = htmlUtils.moveTo("<td>EpisodeTitle", message);
            message = htmlUtils.moveTo("<td>", message);
            String episodeTitle = htmlUtils.extractTo("</td>", message);
            message = htmlUtils.moveTo("<td>Description", message);
            message = htmlUtils.moveTo("<td>", message);
            String description = htmlUtils.extractTo("</td>", message);
            message = htmlUtils.moveTo("<td>Series", message);
            message = htmlUtils.moveTo("<td>", message);
            String series = htmlUtils.extractTo("</td>", message);
            message = htmlUtils.moveTo("<td>Episode", message);
            message = htmlUtils.moveTo("<td>", message);
            String episode = htmlUtils.extractTo("</td>", message);
            // Skip genres - need to parse a bullet list

            message = htmlUtils.moveTo("<td>Genres", message);
            message = htmlUtils.moveTo("<td><ul>", message);
            String genresStr = htmlUtils.extractTo("</ul>", message);

            Set<String> genres = new HashSet<String>();
            do {
                genresStr = htmlUtils.moveTo("<li>", genresStr);
                String genre = htmlUtils.extractTo("</li>", genresStr);
                if (genre != null && !genre.trim().isEmpty()) {
                    genres.add(genre);
                }

            } while (genresStr != null);
            // Skip date and time
            message = htmlUtils.moveTo("<td>Date", message);
            message = htmlUtils.moveTo("<td>", message);
            String date = htmlUtils.extractTo("</td>", message);
            message = htmlUtils.moveTo("<td>Time", message);
            message = htmlUtils.moveTo("<td>", message);
            String time = htmlUtils.extractTo("</td>", message);


            message = htmlUtils.moveTo("<td>IconUrl", message);
            message = htmlUtils.moveTo("<td>", message);
            message = htmlUtils.moveTo("<a", message);
            message = htmlUtils.moveTo(">", message);
            String iconUrl = htmlUtils.extractTo("</a>", message);

            message = htmlUtils.moveTo("<td>ServiceUrl", message);
            message = htmlUtils.moveTo("<td>", message);
            message = htmlUtils.moveTo("<a", message);
            message = htmlUtils.moveTo(">", message);
            String serviceUrl = htmlUtils.extractTo("</a>", message);

            return new Episode(sourceId, id, programmeTitle, seriesTitle, episodeTitle, series, episode, description, iconUrl, serviceUrl, date, time, channel, genres);
        } catch (Exception e) {
            throw new ServletException("Cannot find episode data", e);
        }

    }

    private void getRecordingsPodcastResponse(HttpServletResponse response)  throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xhtml+xml");

        String recordingsUrl = "http://localhost:" + port + "/recordings";
        String title = "RECORDING NOW";
        String desc = "Recordings in progress";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, recordingsUrl);

        Collection<Recording> recordings = recorder.getCurrentRecordings();
        for (Recording recording : recordings) {
            final Episode episode = recording.getEpisode();
            final String episodeTitle = htmlUtils.makeContentSafe(episode.getPodcastTitle());
            final String episodeDesc = htmlUtils.makeContentSafe(episode.getDescription());
            final String episodeIconUrl = episode.getIconUrl();
            final String controlUrl="http://localhost:" + port + "/control?id=" + episode.getId();

            builder.addCategoryItem(episodeTitle, episodeDesc, controlUrl, episodeIconUrl );
        }

        builder.stopDocument();

        String resultStr = builder.toString();
        response.getWriter().println(resultStr);
     }

    private void getRecordingsHtmlResponse(HttpServletResponse response)  throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader("Recordings");
        htmlBuilder.startBody();
        htmlBuilder.addHeading1("Recordings");
        Collection<Recording> recordings = recorder.getCurrentRecordings();
        htmlBuilder.startTable();
        htmlBuilder.addTableHeader("Source", "Episode", "In progress", "Stop");
        for (Recording recording : recordings) {
            HtmlBuilder linkBuilder = new HtmlBuilder();
            String stopLink = "/stop?id=" + recording.getId();
            linkBuilder.addLink("stop", stopLink);
            htmlBuilder.addTableRow(recording.getSourceId(), recording.getId(), String.valueOf(recording.isInProgress()), linkBuilder.toString());
        }
        htmlBuilder.stopTable();

        htmlBuilder.addBreak();
        String stopLink = "/stopall";
        htmlBuilder.addLink("Stop all recordings", stopLink);


        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();
        response.getWriter().println(htmlBuilder.toString());
    }

    private void getVideoResponse(HttpServletResponse response, Episode episode) throws ServletException, IOException {
         OutputStream out = null;

        try {
            response.setContentType("video/mp4");
            response.setCharacterEncoding("ISO-8859-1");
            response.setContentLength(Integer.MAX_VALUE);
            out = response.getOutputStream();
            recorder.watch(out, episode);
        } catch (Exception e) {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e1) {
                // Ignore
            }

            logger.warn("Streaming of " + episode.getId() + " stopped due to exception ", e);
            throw new ServletException("Failed to stream video", e);
        } finally {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.flushBuffer();
        }
    }

    private void getMessageHtmlResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.getWriter().println("<h1>" + message + "</h1>");
    }

    private void getCachedPodcastResponse(HttpServletResponse response, String podcast)
            throws ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xhtml+xml");
        try {
            String message = getFromCache(htdocsDir, podcast + ".xml");
            response.getWriter().println(message);
        } catch (Exception e) {
            throw new ServletException("Failed to find podcast", e);
        }
    }

    private void getCachedHtmlResponse(HttpServletResponse response, String pageName)
            throws ServletException {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html");
            String message = getFromCache(htdocsDir, pageName + ".html");
            response.getWriter().println(message);
        } catch (Exception e) {
            throw new ServletException("Failed to find page", e);
        }
    }

    private void getLogFileResponse(HttpServletResponse response, String name)
            throws ServletException {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain");
            File file = new File(logDir + File.separator + name);
            FileReader reader = null;
            BufferedReader breader = null;
            try {
                reader = new FileReader(file);
                breader = new BufferedReader(reader);
                String line = "";
                while (true) {
                    while ((line = breader.readLine()) != null) {
                        response.getWriter().println(line);
                    }

                    response.flushBuffer();
                    Thread.sleep(2000);
                }
            } finally {
                if (breader != null) {
                    try {
                        breader.close();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                            }
        } catch (Exception e) {
            throw new ServletException("Failed to find page", e);
        }
    }


    private void getLogoResponse(HttpServletResponse response)
            throws IOException {
        final ClassLoader cl = PodcastServer.class.getClassLoader();
        InputStream in = cl.getResourceAsStream("logo.png");
        int fileSize = findResourceLength(in);

        response.setContentType("image/png");
        response.setCharacterEncoding("ISO-8859-1");
        response.setContentLength((int) fileSize);

        in = cl.getResourceAsStream("logo.png");

        // Open the file and output streams
        OutputStream out = response.getOutputStream();

        // Copy the contents of the file to the output stream
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
        response.flushBuffer();

        try {
            in.close();
        } catch (IOException e) {
        // Ignore
        }
        try {
            out.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    private String buildPodcastFor(Category service) throws Exception {

        final String shortName = htmlUtils.makeContentSafe(service.getShortName());
        final String longName = htmlUtils.makeContentSafe(service.getLongName());
        final String url = service.getServiceUrl();
        final String iconUrl = service.getIconUrl();

        RssBuilder builder = new RssBuilder();
        builder.startDocument(shortName, longName, url);
        builder.addImage(iconUrl, shortName, url);
        if (service.isProgrammeCategory()) {
            Programme programme = (Programme) service;
            Map<String, Episode> episodes = programme.getEpisodes();
            for (Episode episode : episodes.values()) {
                final String title = htmlUtils.makeContentSafe(episode.getPodcastTitle());
                final String desc = htmlUtils.makeContentSafe(episode.getDescription());
                final String episodeIconUrl = episode.getIconUrl();
                final String controlUrl="http://localhost:" + port + "/control?id=" + episode.getId();
                builder.addCategoryItem(title, desc, controlUrl, episodeIconUrl);
            }
        } else if (service.isSubCategory()) {
            Map<String, Category> subCats = ((SubCategory) service).getSubCategories();
            for (Category subCat : subCats.values()) {
                final String categoryUrl = "http://localhost:" + port + "/" + subCat.getId();
                builder.addCategoryItem(subCat.getShortName(), subCat.getLongName(), categoryUrl);
            }
        }
        builder.stopDocument();
        return builder.toString();
    }

    private void getStatusPodcastResponse(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xhtml+xml");

        String errorsUrl = "http://localhost:" + port + "/errors";
        String recordingsUrl = "http://localhost:" + port + "/recordings";
        String statusUrl = "http://localhost:" + port + "/status";
        String stopUrl = "http://localhost:" + port + "/stopall";
        String startCatUrl = "http://localhost:" + port + "/startcat";
        String stopCatUrl = "http://localhost:" + port + "/stopcat";
        String title = "Status";
        String desc = "Catchup TV status";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, statusUrl);
        builder.addCategoryItem("RECORDING NOW", String.valueOf(recorder.getRecordingCount()), recordingsUrl);
        builder.addTextItem("RECORDING PROCESSES", String.valueOf(recorder.getProcessCount()), statusUrl);
        builder.addTextItem("CATALOGING PROGRESS", cataloger.getProgress(), statusUrl);
        builder.addCategoryItem("CATALOGING ERRORS", errorSummary, errorsUrl);
        builder.addCategoryItem("STOP ALL RECORDING", "Abandon all recording", stopUrl);
        builder.addCategoryItem("START CATALOGING", "Start cataloging", startCatUrl);
        builder.addCategoryItem("STOP CATALOGING", "Stop cataloging", stopCatUrl);
        builder.stopDocument();

        String resultStr = builder.toString();
        response.getWriter().println(resultStr);
    }

    private void getMessagePodcastResponse(HttpServletResponse response, String title, String message, String url) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xhtml+xml");
        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, message, url);
        builder.addTextItem(title, message, url);
        builder.stopDocument();
        String resultStr = builder.toString();
        response.getWriter().println(resultStr);
    }

    private void getControlPodcastResponse(HttpServletResponse response, Episode episode, boolean isRecording) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xhtml+xml");
        final String title = htmlUtils.makeContentSafe(episode.getPodcastTitle());
        final String desc = htmlUtils.makeContentSafe(episode.getDescription());
        final String url = episode.getServiceUrl();
        final String stopUrl = "http://localhost:" + port + "/stop?id=" + episode.getId();
        final String watchUrl = "http://localhost:" + port + "/watch?id=" + episode.getId();
        final String recordUrl = "http://localhost:" + port + "/record?id=" + episode.getId();

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, url);
        if (isRecording) {
            builder.addCategoryItem("STOP", "Stop recording", stopUrl);
        } else {
            builder.addVideoItem("WATCH", "Watch now", watchUrl, "");
            builder.addCategoryItem("RECORD", "Record for later", recordUrl);
        }
        builder.stopDocument();
        String resultStr = builder.toString();
        response.getWriter().println(resultStr);
    }

    private int findResourceLength(InputStream in) {
        int length = 0;
        try {
            byte[] buf = new byte[1024];
            int count = 0;
            length = 0;
            while ((count = in.read(buf)) >= 0) {
                length += count;
            }
        } catch (IOException e) {
            // Ignore
        }
        return length;
    }

    public void publish(Catalog catalog) {
        try {
            clearStaging();
            stagePages(catalog);
        } catch (Exception e) {
            catalog.addError("Failed to publish to html server " + e.getMessage());
            logger.error("Failed to publish catalog to html server", e);
        } finally {
            errorSummary = buildErrorHtmlResponse(catalog);
            buildErrorPodcastResponse(catalog);
            pushContent();
        }
    }

    private void stagePages(Catalog catalog) throws Exception {
        Map<String, Episode> newEpisodes = new HashMap<String, Episode>();
        for (Category cat : catalog.getCategories()) {
            try {
                logger.info("Building podcast for " + cat);
                String podcast = buildPodcastFor(cat);
                writeToStaging("podcast-" + cat.getId() + ".xml", podcast);

                if (cat instanceof Programme) {
                    for (Episode ep : ((Programme)cat).getEpisodes().values()) {
                        newEpisodes.put(ep.getId(), ep);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to build podcast for " + cat, e);
            }
        }

        HtmlBuilder categoriesBuilder = new HtmlBuilder();
        HtmlBuilder programmesBuilder = new HtmlBuilder();
        HtmlBuilder episodesBuilder = new HtmlBuilder();

        programmesBuilder.startDocument();
        programmesBuilder.addPageHeader("Programmes");
        programmesBuilder.startBody();
        programmesBuilder.addHeading1("Programmes");
        programmesBuilder.startTable();
        programmesBuilder.addTableHeader("SourceId", "ParentId", "Id", "ShortName", "LongName", "ServiceUrl", "IconUrl", "PodcastUrl");

        categoriesBuilder.startDocument();
        categoriesBuilder.addPageHeader("Categories");
        categoriesBuilder.startBody();
        categoriesBuilder.addHeading1("Categories");
        categoriesBuilder.startTable();
        categoriesBuilder.addTableHeader("SourceId", "ParentId", "Id", "ShortName", "LongName", "ServiceUrl", "IconUrl");

        episodesBuilder.startDocument();
        episodesBuilder.addPageHeader("Episodes");
        episodesBuilder.startBody();
        episodesBuilder.addHeading1("Episodes");
        episodesBuilder.startTable();
        episodesBuilder.addTableHeader("SourceId", "Id", "Channel", "ProgrammeTitle", "Series", "SeriesTitle", "Episode", "EpisodeTitle", "Description", "PodcastTitle", "AirDate", "AirTime", "ServiceUrl", "IconUrl" );


        for (Category cat : catalog.getCategories()) {
          String detailStr = buildDetailsFor(cat);
          if (cat.isProgrammeCategory() && cat.getParentId().isEmpty()) {
            writeToStaging("programme-" + cat.getId() + ".html", detailStr);
            Programme prog = (Programme)cat;
            HtmlBuilder linkBuilder1 = new HtmlBuilder();
            linkBuilder1.addLink(cat.getServiceUrl(), cat.getServiceUrl());
            HtmlBuilder linkBuilder2 = new HtmlBuilder();
            linkBuilder2.addLink(cat.getIconUrl(), cat.getIconUrl());
              HtmlBuilder linkBuilder3 = new HtmlBuilder();
              linkBuilder3.addLink(prog.getPodcastUrl(), prog.getPodcastUrl());

            HtmlBuilder linkBuilder4 = new HtmlBuilder();
            String link = "/programme=" + prog.getId();
            linkBuilder4.addLink(prog.getId(), link);

            programmesBuilder.addTableRow(prog.getSourceId(), prog.getParentId(), linkBuilder4.toString(), prog.getShortName(), prog.getLongName(), linkBuilder1.toString(), linkBuilder2.toString(),linkBuilder3.toString());
          } else {
              writeToStaging("category-" + cat.getId() + ".html", detailStr);
            HtmlBuilder linkBuilder1 = new HtmlBuilder();
            linkBuilder1.addLink(cat.getServiceUrl(), cat.getServiceUrl());
            HtmlBuilder linkBuilder2 = new HtmlBuilder();
            linkBuilder2.addLink(cat.getIconUrl(), cat.getIconUrl());

            HtmlBuilder linkBuilder4 = new HtmlBuilder();
            String link = "/category=" + cat.getId();
            linkBuilder4.addLink(cat.getId(), link);

            categoriesBuilder.addTableRow(cat.getSourceId(), cat.getParentId(), linkBuilder4.toString(), cat.getShortName(), cat.getLongName(), linkBuilder1.toString(), linkBuilder2.toString());

          }

          if (cat.isProgrammeCategory() && cat.getParentId().isEmpty()) {
            for (Episode ep : ((Programme)cat).getEpisodes().values()) {
              String detailStr2 = buildDetailsFor(ep);
                writeToStaging("episode-" + ep.getId() + ".html", detailStr2);
                HtmlBuilder linkBuilder1 = new HtmlBuilder();
              linkBuilder1.addLink(ep.getServiceUrl(), ep.getServiceUrl());
              HtmlBuilder linkBuilder2 = new HtmlBuilder();
              linkBuilder2.addLink(ep.getIconUrl(), ep.getIconUrl());

              HtmlBuilder linkBuilder4 = new HtmlBuilder();
              String link = "/episode=" + ep.getId();
              linkBuilder4.addLink(ep.getId(), link);

              episodesBuilder.addTableRow(ep.getSourceId(), linkBuilder4.toString(), ep.getChannel(), ep.getProgrammeTitle(), ep.getSeries(), ep.getSeriesTitle(), ep.getEpisode(), ep.getEpisodeTitle(), ep.getDescription(), ep.getPodcastTitle(), ep.getAirDate(), ep.getAirTime(), linkBuilder1.toString(), linkBuilder2.toString() );
            }
          }
        }

        programmesBuilder.stopTable();
        programmesBuilder.stopBody();
        programmesBuilder.stopDocument();

        categoriesBuilder.stopTable();
        categoriesBuilder.stopBody();
        categoriesBuilder.stopDocument();

        episodesBuilder.stopTable();
        episodesBuilder.stopBody();
        episodesBuilder.stopDocument();

        String programmeResponse = programmesBuilder.toString();
        writeToStaging("programmes.html", programmeResponse);

        String categoryResponse = categoriesBuilder.toString();
        writeToStaging("categories.html", categoryResponse);

        String episodesResponse = episodesBuilder.toString();
        writeToStaging("episodes.html", episodesResponse);
    }

    private void getIndexResponse(HttpServletResponse response) throws IOException {
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader("SageTV Catchup");
        htmlBuilder.startBody();
        htmlBuilder.addHeading1("SageTV Catchup");
        htmlBuilder.addHeading2("Status");
        htmlBuilder.boldOn();
        htmlBuilder.addParagraph("Catalog progress:");
        htmlBuilder.boldOff();
        htmlBuilder.addParagraph(cataloger.getProgress());
        htmlBuilder.boldOn();
        htmlBuilder.addParagraph("Recording progress:");
        htmlBuilder.boldOff();
        htmlBuilder.addParagraph(String.valueOf(recorder.getRecordingCount()));
        htmlBuilder.boldOn();
        htmlBuilder.addParagraph("Recording processes:");
        htmlBuilder.boldOff();
        htmlBuilder.addParagraph(String.valueOf(recorder.getProcessCount()));
        htmlBuilder.addLink("Recordings", "/recordings");
        htmlBuilder.addBreak();

        htmlBuilder.addLink("Logs", "/logs");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Catalog Errors", "/errors");
        htmlBuilder.addParagraph(errorSummary);
        htmlBuilder.addHeading2("Controls");
        htmlBuilder.addLink("Stop all recording", "/stopall");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Start cataloging", "/startcat");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Stop cataloging", "/stopcat");
        htmlBuilder.addBreak();

        htmlBuilder.addHeading2("Catalog");
        htmlBuilder.addLink("Podcasts", "/Catchup");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Categories", "/categories");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Programmes", "/programmes");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Episodes", "/episodes");
        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();

        String indexResponse = htmlBuilder.toString();

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.getWriter().println(indexResponse);
    }

    private void writeToStaging(String name, String content) throws Exception {
        name = name.replace("/", "_");
        name = name.replace("\\", "_");
        File file = new File(stagingDir + File.separator + name);
        FileWriter fwriter = null;
        PrintWriter writer = null;

        try  {
            fwriter = new FileWriter(file);
            writer = new PrintWriter(fwriter);
            writer.println(content);
            writer.flush();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
            if (fwriter != null) {
                try {
                    fwriter.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

  private String getFromCache(String dir, String name) throws Exception {
      name = name.replace("/", "_");
      name = name.replace("\\", "_");

      File file = new File(dir + File.separator + name);
      FileReader reader = null;
      BufferedReader breader = null;
      try {
          reader = new FileReader(file);
          breader = new BufferedReader(reader);
          String result = "";
          String line = "";
          while ((line = breader.readLine()) != null) {
              result+=line;
              result+="\n";
          }

          if (result.isEmpty()) {
              throw new Exception("No data found in page " + name);
          }
          return result;
      } finally {
          if (breader != null) {
              try {
                  breader.close();
              } catch (Exception e) {
                  // Ignore
              }
          }
          if (reader != null) {
              try {
                  reader.close();
              } catch (Exception e) {
                  // Ignore
              }
          }
      }
  }

  private void clearStaging() throws Exception {
      logger.info("Clear staging");
      File folder = new File(stagingDir);
      deleteFolder(folder);
      Files.createDirectories(folder.toPath());
  }

    private void deleteFolder(File folder) {
        logger.info("Deleting old htdocs");
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

  private void pushContent()  {
      try {
          logger.info("Pushing staging to htdocs");
          File staging = new File(stagingDir);
          File htdocs = new File(htdocsDir);
          File htdocsOld = new File(htdocsDir+".old");
          File htdocsOld2 = new File(htdocsDir+".old2");
          if (htdocsOld.exists()) {
              htdocsOld.delete();
          }
          if (htdocsOld2.exists()) {
              htdocsOld2.delete();
          }
          if (htdocsOld.exists()) {
                logger.warn("Failed to delete " + htdocsOld + " - moving to " + htdocsOld2);
              Files.move(htdocsOld.toPath(), htdocsOld2.toPath());
          }
          Files.move(htdocs.toPath(), htdocsOld.toPath());
          Files.move(staging.toPath(), htdocs.toPath());
          deleteFolder(htdocsOld);
      } catch (Exception e) {
          logger.error("Failed to publish staged html content", e);
      }
  }

  private String buildDetailsFor(Episode cat) {
    String pageTitle = "Details page for " + cat.getPodcastTitle();
    HtmlBuilder htmlBuilder = new HtmlBuilder();
    htmlBuilder.startDocument();
    htmlBuilder.addPageHeader(pageTitle);
    htmlBuilder.startBody();
    htmlBuilder.addHeading1(cat.getPodcastTitle());

    htmlBuilder.addHeading2("Details");

    htmlBuilder.startTable();
    htmlBuilder.addTableHeader("Field", "Value");

    Map<String,String> data = new LinkedHashMap<String, String>();
    data.put("SourceId", cat.getSourceId());
    data.put("Type", cat.getClass().getSimpleName());
    data.put("Id", cat.getId());
    data.put("Channel", cat.getChannel());
    data.put("ProgrammeTitle", cat.getProgrammeTitle());
    data.put("SeriesTitle", cat.getSeriesTitle());
    data.put("EpisodeTitle", cat.getEpisodeTitle());
    data.put("Description", cat.getDescription());
    data.put("Series", cat.getSeries());
    data.put("Episode", cat.getEpisode());
    HtmlBuilder listBuilder = new HtmlBuilder();
    listBuilder.startList();
    for (String genre : cat.getGenres()) {
      listBuilder.addListItem(genre);
    }
    listBuilder.stopList();
    data.put("Genres", listBuilder.toString());
    data.put("Date", cat.getAirDate());
    data.put("Time", cat.getAirTime());
    data.put("PodcastTitle", cat.getPodcastTitle());
    HtmlBuilder linkBuilder1 = new HtmlBuilder();
    linkBuilder1.addLink(cat.getIconUrl(), cat.getIconUrl());
    data.put("IconUrl", linkBuilder1.toString());
    HtmlBuilder linkBuilder2 = new HtmlBuilder();
    linkBuilder2.addLink(cat.getServiceUrl(), cat.getServiceUrl());
    data.put("ServiceUrl", linkBuilder2.toString());

    HtmlBuilder metaListBuilder = new HtmlBuilder();
    metaListBuilder.startList();
    for (String sourceUrl : cat.getMetaUrls()) {
      HtmlBuilder linkBuilder = new HtmlBuilder();
      linkBuilder.addLink(sourceUrl, sourceUrl);
      metaListBuilder.addListItem(linkBuilder.toString());
    }
    metaListBuilder.stopList();

    data.put("MetaUrls", metaListBuilder.toString());

    for (Map.Entry<String, String> entry : data.entrySet()) {
      htmlBuilder.addTableRow(entry.getKey(), entry.getValue());
    }

    htmlBuilder.stopTable();

    htmlBuilder.addHeading2("Errors");
    htmlBuilder.startTable();
    htmlBuilder.addTableHeader( "Level", "Error");
    for (ParseError error : cat.getErrors()) {
      htmlBuilder.addTableRow(error.getLevel(), error.getMessage());
    }
    htmlBuilder.stopTable();
    htmlBuilder.stopBody();
    htmlBuilder.stopDocument();
    return htmlBuilder.toString();
  }

  private String buildDetailsFor(Category cat) {

    String pageTitle = "Details page for " + cat.getLongName();
    HtmlBuilder htmlBuilder = new HtmlBuilder();
    htmlBuilder.startDocument();
    htmlBuilder.addPageHeader(pageTitle);
    htmlBuilder.startBody();
    htmlBuilder.addHeading1(cat.getShortName());
    htmlBuilder.addHeading2("Details");
    htmlBuilder.startTable();
    htmlBuilder.addTableHeader("Field", "Value");

    Map<String,String> data = new LinkedHashMap<String, String>();
    data.put("SourceId", cat.getSourceId());
    data.put("Type", cat.getClass().getSimpleName());
    data.put("Id", cat.getId());
    data.put("ParentId", cat.getParentId());
    data.put("ShortName", cat.getShortName());
    data.put("LongName", cat.getLongName());
    HtmlBuilder linkBuilder1 = new HtmlBuilder();
    linkBuilder1.addLink(cat.getIconUrl(), cat.getIconUrl());
    data.put("IconUrl", linkBuilder1.toString());
    HtmlBuilder linkBuilder2 = new HtmlBuilder();
    linkBuilder2.addLink(cat.getServiceUrl(), cat.getServiceUrl());
    data.put("ServiceUrl", linkBuilder2.toString());

    if (cat instanceof Programme) {
      Programme prog = (Programme)cat;
      HtmlBuilder linkBuilder3 = new HtmlBuilder();
      linkBuilder3.addLink(prog.getPodcastUrl(), prog.getPodcastUrl());
      data.put("PodcastUrl", linkBuilder3.toString());
    }

    for (Map.Entry<String, String> entry : data.entrySet()) {
      htmlBuilder.addTableRow(entry.getKey(), entry.getValue());
    }

    HtmlBuilder listBuilder = new HtmlBuilder();
    listBuilder.startList();
    for (String sourceUrl : cat.getMetaUrls()) {
      HtmlBuilder linkBuilder = new HtmlBuilder();
      linkBuilder.addLink(sourceUrl, sourceUrl);
      listBuilder.addListItem(linkBuilder.toString());
    }
    listBuilder.stopList();

    data.put("MetaUrls", listBuilder.toString());

    htmlBuilder.stopTable();

    if (cat instanceof Programme) {

      htmlBuilder.addHeading2("Episodes");

      htmlBuilder.startTable();

      htmlBuilder.addTableHeader("SourceId", "Id", "Channel", "ProgrammeTitle", "Series", "SeriesTitle", "Episode", "EpisodeTitle", "Description", "PodcastTitle", "AirDate", "AirTime", "ServiceUrl", "IconUrl" );


      for (Episode ep : ((Programme)cat).getEpisodes().values()) {
        HtmlBuilder linkBuilder5 = new HtmlBuilder();
        linkBuilder5.addLink(ep.getServiceUrl(), ep.getServiceUrl());
        HtmlBuilder linkBuilder6 = new HtmlBuilder();
        linkBuilder6.addLink(ep.getIconUrl(), ep.getIconUrl());

        HtmlBuilder linkBuilder4 = new HtmlBuilder();
        String link = "/episode=" + ep.getId();
        linkBuilder4.addLink(ep.getId(), link);

        htmlBuilder.addTableRow(ep.getSourceId(), linkBuilder4.toString(), ep.getChannel(), ep.getProgrammeTitle(), ep.getSeries(), ep.getSeriesTitle(), ep.getEpisode(), ep.getEpisodeTitle(), ep.getDescription(), ep.getPodcastTitle(), ep.getAirDate(), ep.getAirTime(), linkBuilder5.toString(), linkBuilder6.toString());
      }
      htmlBuilder.stopTable();
    }


    htmlBuilder.addHeading2("Errors");
    htmlBuilder.startTable();
    htmlBuilder.addTableHeader( "Level", "Error");
    for (ParseError error : cat.getErrors()) {
      htmlBuilder.addTableRow(error.getLevel(), error.getMessage());
    }
    htmlBuilder.stopTable();

    htmlBuilder.stopBody();
    htmlBuilder.stopDocument();
    return htmlBuilder.toString();
  }

    private void buildErrorPodcastResponse(Catalog catalog) {
        TreeSet<ParseError> errorList = new TreeSet<ParseError>();
        for (Category cat : catalog.getCategories()) {
            if (cat.isSource() || cat.isRoot()) {
                if (cat.hasErrors()) {
                    errorList.addAll(cat.getErrors());
                }
            }
            if (cat.isProgrammeCategory()) {
                if (cat.hasErrors()) {
                    errorList.addAll(cat.getErrors());
                }
                Programme prog  = (Programme)cat;
                if (prog.hasEpisodes()) {
                    for (Episode episode : prog.getEpisodes().values()) {
                        if (episode.hasErrors()) {
                            errorList.addAll(episode.getErrors());
                        }
                    }
                }
            }

        }

        String recordingsUrl = "http://localhost:" + port + "/errors";
        String title = "ERRORS";
        String desc = "Cataloging errors";

        RssBuilder builder = new RssBuilder();
        builder.startDocument(title, desc, recordingsUrl);

        for (ParseError error : errorList) {
            builder.addTextItem(error.getLevel(), error.getSource() + " " + error.getType() + " " + error.getId() + " " +  error.getMessage(), "");
        }

        builder.stopDocument();

        String errorResponse = builder.toString();
        try {
            writeToStaging("errors.xml", errorResponse);
        } catch (Exception e) {
            logger.error("Failed to build errors page", e);
        }
    }


  private String buildErrorHtmlResponse(Catalog catalog) {
    TreeSet<ParseError> errorList = new TreeSet<ParseError>();
    for (Category cat : catalog.getCategories()) {
      if (cat.isSource() || cat.isRoot()) {
        if (cat.hasErrors()) {
          errorList.addAll(cat.getErrors());
        }
      }
      if (cat.isProgrammeCategory()) {
        if (cat.hasErrors()) {
          errorList.addAll(cat.getErrors());
        }
        Programme prog  = (Programme)cat;
        if (prog.hasEpisodes()) {
          for (Episode episode : prog.getEpisodes().values()) {
            if (episode.hasErrors()) {
              errorList.addAll(episode.getErrors());
            }
          }
        }
      }

    }
    HtmlBuilder htmlBuilder = new HtmlBuilder();
    htmlBuilder.startDocument();
    htmlBuilder.addPageHeader("Errors page");
    htmlBuilder.startBody();
    htmlBuilder.addHeading1("Cataloging errors");
    htmlBuilder.startTable();
    htmlBuilder.addTableHeader("Source", "Level", "Programme", "Episode", "Error", "URL");
    for (ParseError error : errorList) {
      HtmlBuilder listBuilder = new HtmlBuilder();
      listBuilder.startList();
      for (String sourceUrl : error.getSourceUrl()) {
        HtmlBuilder linkBuilder = new HtmlBuilder();
        linkBuilder.addLink(sourceUrl, sourceUrl);
        listBuilder.addListItem(linkBuilder.toString());
      }

      String link = error.getId();
      HtmlBuilder linkBuilder = new HtmlBuilder();
      if (error.getType().equals(Programme.class.getSimpleName())) {
        linkBuilder.addLink(error.getId(), "/programme=" + error.getId());
        link = linkBuilder.toString();
      } else
      if (error.getType().equals(Episode.class.getSimpleName())) {
        linkBuilder.addLink(error.getId(), "/episode=" + error.getId());
        link = linkBuilder.toString();
      } else {
        linkBuilder.addLink(error.getId(), "/category=" + error.getId());
        link = linkBuilder.toString();
      }

      htmlBuilder.addTableRow(error.getSource(), error.getLevel(), error.getType(),
              link, error.getMessage(), listBuilder.toString());
    }
    htmlBuilder.stopTable();
    htmlBuilder.stopBody();
    htmlBuilder.stopDocument();

    String errorResponse = htmlBuilder.toString();
      try {
          writeToStaging("errors.html", errorResponse);
      } catch (Exception e) {
          logger.error("Failed to build errors page", e);
      }

      HashMap<String, Integer> errorSum = new HashMap<String, Integer>();
      for (ParseError error : errorList) {
          Integer count = errorSum.get(error.getLevel());
          if (count == null) {
              errorSum.put(error.getLevel(), 1);
          } else {
              errorSum.put(error.getLevel(), count + 1);
          }
      }

      String errors = "( ";
      for (Map.Entry<String, Integer> entry : errorSum.entrySet()) {
          errors += entry.getKey()+ " " + entry.getValue()+ " ";
      }
      errors+=")";

      if (errors.equals("()")) {
          return "";
      } else {
          return errors;
      }
  }
}
