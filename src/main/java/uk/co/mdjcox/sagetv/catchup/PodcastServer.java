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
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

/**
 * The web server.
 *
 * This holds no state whatsoever
 */
@Singleton
public class PodcastServer {


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

    @Inject
    private PodcastServer(Logger logger, PropertiesInterface props, HtmlUtilsInterface htmlUtils, OsUtilsInterface osUtils, Recorder recorder) throws Exception {
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
        htdocsDir = props.getString("htdocsDir");
        File file = new File(htdocsDir);
        Files.createDirectories(file.toPath());
        stagingDir = props.getString("stagingDir");
        file = new File(stagingDir);
        Files.createDirectories(file.toPath());

        logDir = System.getProperty("user.dir") + File.separator + "sagetvcatchup" + File.separator + "logs";
    }

    public void start() throws Exception {
        logger.info("Starting the podcast server on port " + port);
        server.start();
    }

    public void stop() {
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
            String[] otherIds = getEpisodeFromCache(id);
            getVideoResponse(response, otherIds[0], id, otherIds[1], otherIds[2]);
        } else if (target.startsWith("/stopall")) {
            stopAllRecording(response);
        } else if (target.startsWith("/stop")) {
            String id = request.getParameter("id");
            stopVideoResponse(response, id);
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
        } else {
            String serviceName = target.substring(1);
            getCachedPodcastResponse(response, "podcast-" + serviceName);
        }
          ((Request) request).setHandled(true);
    }

    private String[] getEpisodeFromCache(String id) throws ServletException {
        try {
            String message = getFromCache(htdocsDir, "episode-" + id + ".html");
            message = htmlUtils.moveTo("<td>SourceId", message);
            message = htmlUtils.moveTo("<td>", message);
            String sourceId = htmlUtils.extractTo("</td>", message);
            message = htmlUtils.moveTo("<td>PodcastTitle", message);
            message = htmlUtils.moveTo("<td>", message);
            String name = htmlUtils.extractTo("</td>", message);

            message = htmlUtils.moveTo("<td>ServiceUrl", message);
            message = htmlUtils.moveTo("<td>", message);

            message = htmlUtils.moveTo("<a", message);

            message = htmlUtils.moveTo(">", message);
            String url = htmlUtils.extractTo("</a>", message);

            return new String[] {sourceId, name, url};
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

    private void getVideoResponse(HttpServletResponse response, String sourceId, final String id, String name, String url) throws ServletException, IOException {
        FileInputStream in = null;
        OutputStream out = null;

        try {

            File file = recorder.start(sourceId, id, name, url);

            logger.info("Streaming " + file + " exists=" + file.exists());

            in = new FileInputStream(file);
            response.setContentType("video/mp4");
            response.setCharacterEncoding("ISO-8859-1");
            response.setContentLength(Integer.MAX_VALUE);

            out = response.getOutputStream();

            // Copy the contents of the file to the output stream

            byte[] buf = new byte[100];
            int served = 0;
            int count = 0;
            int lastReport = 0;

            long lastServed = System.currentTimeMillis();

            try {
                while (recorder.isRecording(id) && !recorder.isStopped(id)) {
                    while ((count = in.read(buf)) >= 0 && !recorder.isStopped(id)) {
                        out.write(buf, 0, count);
                        served += count;
                        out.flush();
                    }
                    osUtils.waitFor(1000);
                    if (served > lastReport) {
                        logger.info("Streaming of " + id + " continues after serving " + served + "/" + file.length());
                        lastReport = served;
                        lastServed = System.currentTimeMillis();
                    } else {
                        if ((System.currentTimeMillis() - lastServed) > 10000) {
                            break;
                        }
                    }
                }
                if (recorder.isStopped(id)) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    logger.info("Streaming of " + id + " stopped due to stop request");
                } else {
                    logger.info("Streaming of " + id + " stopped due to completion");
                }
            } finally {
                logger.info("Streaming of " + id + " stopped after serving " + served + "/" + file.length());
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            logger.warn("Streaming of " + id + " stopped due to exception ", e);
            throw new ServletException("Failed to stream video", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // Ignore
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // Ignore
            }
            try {
                recorder.stop(id);
            } catch (Exception e) {
                logger.error("Failed to stop recording in the recorder", e);
            }
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
        response.setContentType("image/png");
        response.setCharacterEncoding("ISO-8859-1");

        final ClassLoader cl = PodcastServer.class.getClassLoader();
        final URL resource = cl.getResource("logo.png");

        // Set content size

        String filename = resource.getFile();
        File file = new File(filename);
        response.setContentLength((int) file.length());

        // Open the file and output streams
        FileInputStream in = new FileInputStream(file);
        OutputStream out = response.getOutputStream();

        // Copy the contents of the file to the output stream
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
        in.close();
        out.close();
    }

    private String buildPodcastFor(Category service) throws Exception {
        String resultStr = "";
        String CRLF = System.getProperty("line.separator");
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
                resultStr += "<item>" + CRLF;
                resultStr += "<title>" + htmlUtils.makeContentSafe(episode.getPodcastTitle()) + "</title>" + CRLF;
                resultStr += "<link>" + episode.getServiceUrl() + "</link>" + CRLF;
                resultStr += "<guid>" + episode.getServiceUrl() + "</guid>" + CRLF;
                resultStr += "<description>" + htmlUtils.makeContentSafe(episode.getDescription()) + "</description>" + CRLF;
                resultStr += "<itunes:image href=\"" + episode.getIconUrl() + "\"/>" + CRLF;

//                if (!episode.getGenres().isEmpty()) {
//                    for (String genre : episode.getGenres()) {
//                        resultStr += " <itunes:category text=\""+genre+"\">";
//                    }
//                    resultStr +="</itunes:category>";
//                }

                resultStr += "<media:thumbnail url=\"" + episode.getIconUrl() + "\"/>" + CRLF;
                int length = 999999;
                String type = "video/mp4";
                resultStr += "<enclosure url=\"" + "http://localhost:"+port+"/play?id=" + episode.getId() + "\" length=\"" + length + "\" type=\"" + type + "\"/>" + CRLF;
                resultStr += "</item>" + CRLF;
            }
        } else if (service.isSubCategory()) {
            Map<String, Category> subCats = ((SubCategory) service).getSubCategories();
            for (Category subCat : subCats.values()) {
                resultStr += "<item>" + CRLF;
                resultStr += "<title>" + subCat.getShortName() + "</title>" + CRLF;
                resultStr += "<link>" + "http://localhost:"+port+"/" + subCat.getId() + "</link>" + CRLF;
                resultStr += "<guid>" + "http://localhost"+port+"/" + subCat.getId() + "</guid>" + CRLF;
                resultStr += "<description>" + subCat.getLongName() + "</description>" + CRLF;
                resultStr += "<itunes:image href=\"" + subCat.getIconUrl() + "\"/>" + CRLF;
                resultStr += "<media:thumbnail url=\"" + subCat.getIconUrl() + "\"/>" + CRLF;
                resultStr += "</item>" + CRLF;
            }
        }


        resultStr += "</channel>" + CRLF;
        resultStr += "</rss>";
        return resultStr;
    }

    public void publish(Catalog catalog) {
        try {
            clearStaging();
            stagePages(catalog);
            pushContent();
        } catch (Exception e) {
            logger.error("Failed to publish catalog to html server", e);
            e.printStackTrace();
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

          if (cat instanceof Programme) {
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

        buildErrorResponse(catalog);
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

  private void pushContent() throws Exception {
      logger.info("Pushing staging to htdocs");
      File staging = new File(stagingDir);
      File htdocs = new File(htdocsDir);
      File htdocsOld = new File(htdocsDir+".old");
      Files.move(htdocs.toPath(), htdocsOld.toPath());
      Files.move(staging.toPath(), htdocs.toPath());
      deleteFolder(htdocsOld);
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


  private void buildErrorResponse(Catalog catalog) throws Exception {
    TreeSet<ParseError> errorList = new TreeSet<ParseError>();
    for (Category cat : catalog.getCategories()) {
      if (cat.isSource()) {
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
    htmlBuilder.addHeading1("Parsing errors");
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
    writeToStaging("errors.html", errorResponse);
  }
}
