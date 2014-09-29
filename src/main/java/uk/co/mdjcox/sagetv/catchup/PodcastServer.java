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
import uk.co.mdjcox.utils.OsUtilsInterface;
import uk.co.mdjcox.utils.PropertiesInterface;

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


    private static final String CRLF = System.getProperty("line.separator");
    private final Cataloger cataloger;
    private Logger logger;
    private Server server;
    private Handler handler;
    private int port;
    private Recorder recorder;
    private HtmlUtilsInterface htmlUtils;
    private OsUtilsInterface osUtils;
    private final String htdocsDir;
    private final String logDir;
    private final String stagingDir;
    private boolean withControl = true;

    @Inject
    private PodcastServer(Logger logger, PropertiesInterface props, HtmlUtilsInterface htmlUtils, OsUtilsInterface osUtils, Cataloger cataloger, Recorder recorder) throws Exception {
        this.logger = logger;
        this.htmlUtils = htmlUtils;
        this.osUtils = osUtils;

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
        withControl = props.getBoolean("withControlPodcasts", true);
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

        if (target.equals("/logo.png")) {
            getLogoResponse(response);
        } else if (target.startsWith("/play")) {
            String id = request.getParameter("id");
            Episode episode = getEpisodeFromCache(id);
            getVideoResponse(response, episode);
        } else if (target.startsWith("/record")) {
            String id = request.getParameter("id");
            Episode episode = getEpisodeFromCache(id);
            recorder.record(episode);
            getControlPodcastResponse(response, episode, !recorder.isStopped(id));
        } else if (target.startsWith("/control")) {
            String id = request.getParameter("id");
            Episode episode = getEpisodeFromCache(id);
            getControlPodcastResponse(response, episode, !recorder.isStopped(id));
        } else if (target.startsWith("/stopcat")) {
            stopCataloging(response);
        } else if (target.startsWith("/startcat")) {
            startCataloging(response);
        } else if (target.startsWith("/stopall")) {
            stopAllRecording(response);
        } else if (target.startsWith("/stop")) {
            String id = request.getParameter("id");
            Episode episode = getEpisodeFromCache(id);
            if (withControl) {
                recorder.requestStop(id);
                getControlPodcastResponse(response, episode, !recorder.isStopped(id));
            } else {
                stopVideoResponse(response, id);
            }
        } else if (target.startsWith("/log")) {
            getLogFileResponse(response, "sagetvcatchup.log");
        } else if (target.startsWith("/recordings")) {
            getRecordingsResponse(response);
        } else if (target.startsWith("/errors")) {
            getCachedHtmlResponse(response, "errors");
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

    private void stopCataloging(HttpServletResponse response) throws ServletException, IOException {
        String result = cataloger.stop();
        getMessageResponse(response, result);
    }

    private void startCataloging(HttpServletResponse response) throws ServletException, IOException {
        String result = cataloger.start();
        getMessageResponse(response, result);
    }

    private Episode getEpisodeFromCache(String id) throws ServletException {
        try {

//            SourceId	Iplayer
//            Type	Episode
//            Id	TheAdventuresofAbneyandTealSeries1TheTrain
//            Channel	CBeebies
//            ProgrammeTitle	The Adventures of Abney and Teal
//            SeriesTitle	Series 1
//            EpisodeTitle	The Train
//            Description	Animated adventures of two friends who live on an island in the middle of the big city. They share their home with a group of friendly and hilarious characters. Everybody is bored. Abney builds a train so they can go on a tour. Then Teal has an idea - why don't they tour the whole island? It is a fantastic tour until something gets in the way.
//            Series	1
//            Episode	11
//            Genres
//            Children's
//            Entertainment and Comedy
//                    Learning
//            Pre-School
//            Date	2011-10-10
//            Time	17:50:00
//            PodcastTitle	The Adventures of Abney and Teal - Series 1 - The Train
//            IconUrl	http://ichef.bbci.co.uk/images/ic/272x153/p01h709f.jpg
//            ServiceUrl

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

    private void getRecordingsResponse(HttpServletResponse response)  throws ServletException, IOException {
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

    private void stopAllRecording(HttpServletResponse response) throws ServletException, IOException {
        String result = recorder.requestStopAll();
        getMessageResponse(response, result);
    }

    private void stopVideoResponse(HttpServletResponse response,String id) throws ServletException, IOException {
        logger.info("Stop Streaming " + id);

        if (id != null && !id.isEmpty()) {
            String message = recorder.requestStop(id);
            getMessageResponse(response, message);
        }
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

    private void getMessageResponse(HttpServletResponse response, String message)
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

        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildPodcastFor(Category service) throws Exception {
        String resultStr = "";
        resultStr += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + CRLF;
        resultStr += "<rss xmlns:media=\"http://search.yahoo.com/mrss/\" xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\" version=\"2.0\">" + CRLF;
        resultStr += "<channel>" + CRLF;
        resultStr += "<title>" + htmlUtils.makeContentSafe(service.getShortName()) + "</title>" + CRLF;
        resultStr += "<description>" + htmlUtils.makeContentSafe(service.getLongName()) + "</description>" + CRLF;
        resultStr += "<link>" + service.getServiceUrl() + "</link>" + CRLF;
        resultStr += "<language>en-gb</language>" + CRLF;
        resultStr += "<image>" + CRLF;
        resultStr += "<url>" + service.getIconUrl() + "</url> " + CRLF;
        resultStr += "<title>" + htmlUtils.makeContentSafe(service.getShortName()) + "</title>" + CRLF;
        resultStr += "<link>" + service.getServiceUrl() + "</link>" + CRLF;
        resultStr += "</image>" + CRLF;

        if (service.isProgrammeCategory()) {
            Programme programme = (Programme) service;
            Map<String, Episode> episodes = programme.getEpisodes();
            for (Episode episode : episodes.values()) {
                resultStr += buildEpisodeItem(episode);
            }
        } else if (service.isSubCategory()) {
            Map<String, Category> subCats = ((SubCategory) service).getSubCategories();
            for (Category subCat : subCats.values()) {
                resultStr += buildCategoryItem(subCat);
            }
        }
        resultStr += "</channel>" + CRLF;
        resultStr += "</rss>";
        return resultStr;
    }

    private String buildCategoryItem(Category subCat) {
        String resultStr = "";
        resultStr += "<item>" + CRLF;
        resultStr += "<title>" + subCat.getShortName() + "</title>" + CRLF;
        resultStr += "<link>" + "http://localhost:"+port+"/" + subCat.getId() + "</link>" + CRLF;
//        resultStr += "<guid>" + "http://localhost"+port+"/" + subCat.getId() + "</guid>" + CRLF;
        resultStr += "<description>" + subCat.getLongName() + "</description>" + CRLF;
        resultStr += "<itunes:image href=\"" + subCat.getIconUrl() + "\"/>" + CRLF;
        resultStr += "<media:thumbnail url=\"" + subCat.getIconUrl() + "\"/>" + CRLF;
        resultStr += "</item>" + CRLF;
        return resultStr;
    }

    private void getControlPodcastResponse(HttpServletResponse response, Episode episode, boolean isRecording) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xhtml+xml");

        String resultStr = "";
        resultStr += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + CRLF;
        resultStr += "<rss xmlns:media=\"http://search.yahoo.com/mrss/\" xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\" version=\"2.0\">" + CRLF;
        resultStr += "<channel>" + CRLF;
        resultStr += "<title>" + htmlUtils.makeContentSafe(episode.getPodcastTitle()) + "</title>" + CRLF;
        resultStr += "<description>" + htmlUtils.makeContentSafe(episode.getDescription()) + "</description>" + CRLF;
        resultStr += "<link>" + episode.getServiceUrl() + "</link>" + CRLF;
        resultStr += "<language>en-gb</language>" + CRLF;
//        resultStr += "<image>" + CRLF;
//        resultStr += "<url>" + episode.getIconUrl() + "</url> " + CRLF;
//        resultStr += "<title>" + htmlUtils.makeContentSafe(episode.getPodcastTitle()) + "</title>" + CRLF;
//        resultStr += "<link>" + episode.getServiceUrl() + "</link>" + CRLF;
//        resultStr += "</image>" + CRLF;

        if (isRecording) {

            resultStr += "<item>" + CRLF;

            resultStr += "<title>STOP</title>" + CRLF;
//            resultStr += "<description>STOP</description>" + CRLF;
            resultStr += "<link>" + "http://localhost:"+port+"/stop?id=" + episode.getId() + "</link>" + CRLF;
            resultStr += "<pubDate></pubDate>" + CRLF;
//            resultStr += "<itunes: subtitle ><![CDATA[STOP]]></itunes: subtitle > <itunes: duration ></itunes: duration >";
            resultStr += "<enclosure url=\"" + "http://localhost:" + port + "/stop?id=" + episode.getId() + "\" length=\"\" type=\"sagetv/subcategory\"/>" + CRLF;
            resultStr += "<media:content duration = \"\" medium = \"video\" fileSize = \"\" url =\"" + "http://localhost:" + port + "/stop?id=" + episode.getId() + "\" type = \"sagetv/subcategory\">" + CRLF;
            resultStr += "<media:title >STOP</media:title>" + CRLF;
            resultStr += "<media:description >STOP</media:description>" + CRLF;
            resultStr += "<media:thumbnail url=\"\"/>" + CRLF;
            resultStr += "</media:content>" + CRLF;
            resultStr += "</item>" + CRLF;

        } else {
            resultStr += "<item>" + CRLF;
            resultStr += "<title>WATCH</title>" + CRLF;
            resultStr += "<link>" + "http://localhost:"+port+"/play?id=" + episode.getId() + "</link>" + CRLF;
//            resultStr += "<guid>" + "http://localhost:"+port+"/play?id=" + episode.getId() + "</guid>" + CRLF;
//            resultStr += "<description>Watch it now</description>" + CRLF;
//        resultStr += "<itunes:image href=\"" + subCat.getIconUrl() + "\"/>" + CRLF;
//        resultStr += "<media:thumbnail url=\"" + subCat.getIconUrl() + "\"/>" + CRLF;
        resultStr += "<enclosure url=\"" + "http://localhost:"+port+"/play?id=" + episode.getId() + "\" length=\"9999999\" type=\"video/mp4\"/>" + CRLF;
            resultStr += "</item>" + CRLF;
        resultStr += "<item>" + CRLF;

        resultStr += "<title>RECORD</title>" + CRLF;
//        resultStr += "<description>RECORD</description>" + CRLF;
            resultStr += "<link>" + "http://localhost:"+port+"/record?id=" + episode.getId() + "</link>" + CRLF;
        resultStr += "<pubDate></pubDate>" + CRLF;
//            resultStr += "<itunes: subtitle ><![CDATA[STOP]]></itunes: subtitle > <itunes: duration ></itunes: duration >";
        resultStr += "<enclosure url=\"" + "http://localhost:" + port + "/record?id=" + episode.getId() + "\" length=\"\" type=\"sagetv/subcategory\"/>" + CRLF;
        resultStr += "<media:content duration = \"\" medium = \"video\" fileSize = \"\" url =\"" + "http://localhost:" + port + "/record?id=" + episode.getId() + "\" type = \"sagetv/subcategory\">" + CRLF;
        resultStr += "<media:title>RECORD</media:title>" + CRLF;
        resultStr += "<media:description >RECORD</media:description>" + CRLF;
        resultStr += "<media:thumbnail url=\"\"/>" + CRLF;
        resultStr += "</media:content>" + CRLF;
        resultStr += "</item>" + CRLF;
        }

        resultStr += "</channel>" + CRLF;
        resultStr += "</rss>";
        response.getWriter().println(resultStr);
    }

    private String buildEpisodeItem(Episode episode) {
        String resultStr = "" ;
        resultStr += "<item>" + CRLF;
        resultStr += "<title>" + htmlUtils.makeContentSafe(episode.getPodcastTitle()) + "</title>" + CRLF;
        if (withControl) {
            resultStr += "<link>" + "http://localhost:"+port+"/control?id=" + episode.getId() + "</link>" + CRLF;
//            resultStr += "<guid>" + "http://localhost:"+port+"/control?id=" + episode.getId() + "</guid>" + CRLF;

        } else {
            resultStr += "<link>" + episode.getServiceUrl() + "</link>" + CRLF;
//            resultStr += "<guid>" + episode.getServiceUrl() + "</guid>" + CRLF;
        }

        resultStr += "<description>" + htmlUtils.makeContentSafe(episode.getDescription()) + "</description>" + CRLF;
        resultStr += "<itunes:image href=\"" + episode.getIconUrl() + "\"/>" + CRLF;

//                if (!episode.getGenres().isEmpty()) {
//                    for (String genre : episode.getGenres()) {
//                        resultStr += " <itunes:category text=\""+genre+"\">";
//                    }
//                    resultStr +="</itunes:category>";
//                }

        resultStr += "<media:thumbnail url=\"" + episode.getIconUrl() + "\"/>" + CRLF;
        if (!withControl) {
            resultStr += "<enclosure url=\"" + "http://localhost:"+port+"/play?id=" + episode.getId() + "\" length=\"9999999\" type=\"video/mp4\"/>" + CRLF;
        }
        resultStr += "</item>" + CRLF;
        return resultStr;
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
            buildErrorResponse(catalog);
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
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Catalog Errors", "/errors");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Recordings", "/recordings");
        htmlBuilder.addBreak();

        htmlBuilder.addLink("Logs", "/logs");
        htmlBuilder.addHeading2("Controls");
        htmlBuilder.addLink("Stop all recording", "/stopall");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Start cataloging", "/startcat");
        htmlBuilder.addBreak();
        htmlBuilder.addLink("Stop cataloging", "/stopcat");
        htmlBuilder.addBreak();

        htmlBuilder.addHeading2("Catalog");
        htmlBuilder.addLink("Podcasts", "/ALL");
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

          if (result == null) {
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


  private void buildErrorResponse(Catalog catalog) {
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
  }
}
