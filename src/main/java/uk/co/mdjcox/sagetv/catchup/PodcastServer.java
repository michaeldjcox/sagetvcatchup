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
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.PropertiesInterface;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
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
    private Map<String, Episode> episodes = new HashMap<String, Episode>();
    private String errorResponse = "";

    @Inject
    private PodcastServer(Logger logger, PropertiesInterface props, HtmlUtilsInterface htmlUtils, Recorder recorder) throws Exception {
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
            String name = request.getParameter("name");
            getVideoResponse(response, name);
        } else if (target.startsWith("/stop")) {
          String name = request.getParameter("name");
          stopVideoResponse(response, name);
        } else if (target.startsWith("/errors")) {
          getMessageResponse(response, errorResponse);
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
            response.setContentLength((int) file.length());


//            URLConnection conn = resource.openConnection();
//            response.setContentType(conn.getContentType());
//            response.setCharacterEncoding(conn.getContentEncoding());
//            response.setContentLength(conn.getContentLength());
//            InputStream in = conn.getInputStream();
            OutputStream out = response.getOutputStream();

            // Copy the contents of the file to the output stream
            byte[] buf = new byte[100];
            int served = 0;
            int count = 0;
            while ((count = in.read(buf)) >= 0) {
                out.write(buf, 0, count);
                served += count;
                out.flush();
            }
            in.close();
            out.close();
            logger.info("Streamed " + served + " bytes of " + name + " of expected " + file.length());
        } catch (Exception e) {
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


      buildErrorResponse(catalog);
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
    StringBuilder errorsBuilder = new StringBuilder("<html>\n");
    errorsBuilder.append("<head>\n");
    errorsBuilder.append("<title>Errors page</title>\n");
    errorsBuilder.append("<style>\n");
    errorsBuilder.append("table, th, td {\n");
    errorsBuilder.append("border: 1px solid black;\n");
    errorsBuilder.append("  border-collapse: collapse;\n");
    errorsBuilder.append("}\n");
    errorsBuilder.append("th, td {\n");
    errorsBuilder.append("padding: 5px;\n");
    errorsBuilder.append("text-align: left;\n");
    errorsBuilder.append("}\n");
    errorsBuilder.append("table.names th	{\n");
    errorsBuilder.append("background-color: #c1c1c1;\n");
    errorsBuilder.append("}\n");
    errorsBuilder.append("</style>\n");
    errorsBuilder.append("</head>\n");
    errorsBuilder.append("<body>\n");
    errorsBuilder.append("<table style=\"width:100%\">\n");
    errorsBuilder.append("<tr>\n<th>Source</th>\n<th>Level</th>\n<th>Programme</th>\n<th>Episode</th>\n<th>Error</th>\n<th>URL</th>\n</tr>\n");
    for (ParseError error : errorList) {
      errorsBuilder.append("<tr>\n");
      errorsBuilder.append("<td>");
      errorsBuilder.append(error.getPlugin());
      errorsBuilder.append("</td>\n");
      errorsBuilder.append("<td>");
      errorsBuilder.append(error.getLevel());
      errorsBuilder.append("</td>\n");
      errorsBuilder.append("<td>");
      errorsBuilder.append(error.getProgramme());
      errorsBuilder.append("</td>\n");
      errorsBuilder.append("<td>");
      errorsBuilder.append(error.getEpisode());
      errorsBuilder.append("</td>\n");
      errorsBuilder.append("<td>");
      errorsBuilder.append(error.getMessage());
      errorsBuilder.append("</td>\n");
      errorsBuilder.append("<td><ul>");
      for (String sourceUrl : error.getSourceUrl()) {
        errorsBuilder.append("<li><a href=\"");
        errorsBuilder.append(sourceUrl);
        errorsBuilder.append("\">");
        errorsBuilder.append(sourceUrl);
        errorsBuilder.append("</a></li>");
      }
      errorsBuilder.append("</ul></td>\n");
      errorsBuilder.append("</tr>\n");
    }
    errorsBuilder.append("</table>\n");
    errorsBuilder.append("</body>\n");
    errorsBuilder.append("</html>");
    errorResponse = errorsBuilder.toString();
  }

}
