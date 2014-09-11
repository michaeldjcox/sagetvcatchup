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
import uk.co.mdjcox.utils.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;


@Singleton
public class PodcastServer {

    private Logger logger;
    private Server server;
    private Handler handler;
    private int port;
    private Map<String, String> podcasts = new HashMap<String, String>();
    private Recorder recorder;
    private HtmlUtilsInterface htmlUtils;
    private OsUtilsInterface osUtils;
    private Map<String, Episode> episodes = new HashMap<String, Episode>();
    private String errorResponse = "";
    private String categoryResponse="";
    private String programmeResponse="";
    private String episodeResponse="";
    private Map<String, String> programmeDetails = new HashMap<String, String>();
    private Map<String, String> episodeDetails = new HashMap<String, String>();
    private Map<String, String> categoryDetails = new HashMap<String, String>();

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
//        if (target.equals("/")) {
//            getMessageResponse(response, "SageTV CatchupTV server works!");
//        } else
        if (target.equals("/logo.png")) {
            getLogoResponse(response);
        } else if (target.startsWith("/play")) {
          logger.info("HTTP request for " + target);
          String name = request.getParameter("name");
            getVideoResponse(response, name);
        } else if (target.startsWith("/stop")) {
          String name = request.getParameter("name");
          stopVideoResponse(response, name);
        } else if (target.startsWith("/errors")) {
          getMessageResponse(response, errorResponse);
        } else if (target.startsWith("/programmes")) {
          getMessageResponse(response, programmeResponse);
        } else if (target.startsWith("/episodes")) {
          getMessageResponse(response, episodeResponse);
        } else if (target.startsWith("/categories")) {
          getMessageResponse(response, categoryResponse);
        } else if (target.startsWith("/programme=")) {
          String id = target.substring(target.indexOf('=') + 1);
          String detailsResponse = programmeDetails.get(id);
          getMessageResponse(response, detailsResponse);
        } else if (target.startsWith("/episode=")) {
          String id = target.substring(target.indexOf('=') + 1);
          String detailsResponse = episodeDetails.get(id);
          getMessageResponse(response, detailsResponse);
        } else if (target.startsWith("/category=")) {
          String id = target.substring(target.indexOf('=') + 1);
          String detailsResponse = categoryDetails.get(id);
          getMessageResponse(response, detailsResponse);
        } else {
              String serviceName = target.substring(1);
              String podcast = podcasts.get(serviceName);
              if (podcast != null) {
                  getChannelResponse(response, podcast);
              } else {
                  getMessageResponse(response, "Online service " + serviceName + " is unknown");
              }
          }
          ((Request) request).setHandled(true);
    }

    private void stopVideoResponse(HttpServletResponse response,String name) throws ServletException, IOException {
        logger.info("Stop Streaming " + name);

        stopRecording(name);

        getMessageResponse(response, "Podcast terminated");
    }

    public void stopRecording(String name) {
        Episode episode = episodes.get(name);

        recorder.stop(episode);
    }

  private void getVideoResponse(HttpServletResponse response, String name) throws ServletException {
    try {

      Episode episode = episodes.get(name);

      File file = recorder.start(episode);
      logger.info("Streaming " + file + " exists=" + file.exists());

      FileInputStream in = new FileInputStream(file);
      response.setContentType("video/mp4");
      response.setCharacterEncoding("ISO-8859-1");
      response.setContentLength(Integer.MAX_VALUE);
      OutputStream out = response.getOutputStream();

      // Copy the contents of the file to the output stream

      byte[] buf = new byte[100];
      int served = 0;
      int count = 0;
      int lastReport = 0;

      long lastServed = System.currentTimeMillis();

      try {
        while (recorder.isRecording(episode)) {
          while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
            served += count;
            out.flush();
          }
          osUtils.waitFor(1000);
          if (served > lastReport) {
            logger.info("Streaming of " + name + " continues after serving " + served + "/" + file.length());
            lastReport = served;
            lastServed = System.currentTimeMillis();
          } else {
            if ((System.currentTimeMillis() - lastServed) > 10000) {
              break;
            }
          }
        }
        logger.info("Streaming of " + name + " stopped due to external process completion");
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
        recorder.stop(episode);
        logger.info("Streaming of " + name + " stopped after serving " + served + "/" + file.length());
      }
    } catch (Exception e) {
      logger.warn("Streaming of " + name + " stopped due to exception ", e);
      throw new ServletException("Failed to stream video", e);
    }
  }

    private void getChannelResponse(HttpServletResponse response, String podcast)
            throws ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xhtml+xml");
        try {
            response.getWriter().println(podcast);
        } catch (Exception e) {
            throw new ServletException("Failed to compose podcast XML", e);
        }
    }

    private void getMessageResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.getWriter().println("<h1>" + message + "</h1>");
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
                resultStr += "<enclosure url=\"" + "http://localhost:"+port+"/play?name=" + episode.getId() + "\" length=\"" + length + "\" type=\"" + type + "\"/>" + CRLF;
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

        Map<String, String> newPodcasts = new HashMap<String, String>();
        Map<String, Episode> newEpisodes = new HashMap<String, Episode>();
        for (Category cat : catalog.getCategories()) {
            try {
                logger.info("Building podcast for " + cat);
                String podcast = buildPodcastFor(cat);
                newPodcasts.put(cat.getId(), podcast);

                if (cat instanceof Programme) {
                    for (Episode ep : ((Programme)cat).getEpisodes().values()) {
                        newEpisodes.put(ep.getId(), ep);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to build podcast for " + cat, e);
            }
        }
        podcasts = newPodcasts;
        episodes = newEpisodes;

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
          programmeDetails.put(cat.getId(), detailStr);
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
          categoryDetails.put(cat.getId(), detailStr);
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
            episodeDetails.put(ep.getId(), detailStr2);
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

      programmeResponse = programmesBuilder.toString();
      categoryResponse = categoriesBuilder.toString();
      episodeResponse = episodesBuilder.toString();

      buildErrorResponse(catalog);
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
        String detailStr2 = buildDetailsFor(ep);
        episodeDetails.put(ep.getId(), detailStr2);
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

    errorResponse = htmlBuilder.toString();
  }



}
