package uk.co.mdjcox.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.*;
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


@Singleton
public class PodcastServer {

    private LoggerInterface logger;
    private Server server;
    private Handler handler;
    private int port;
    private Map<String, String> podcasts = new HashMap<String, String>();
    private Recorder recorder;
    private HtmlUtilsInterface htmlUtils;

    @Inject
    private PodcastServer(LoggerInterface logger, PropertiesInterface props, HtmlUtilsInterface htmlUtils, Recorder recorder) throws Exception {
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
        port = props.getInt("port", 8081);
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
            logger.warning("Failed to stop the podcast server", e);
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
            String url = request.getParameter("link");
            getVideoResponse(response, url);
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

    private void getVideoResponse(HttpServletResponse response, String podcast) throws ServletException {
        try {

            File file = recorder.start(podcast);
            logger.info("Streaming " + podcast + " exists=" + file.exists());

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
            logger.info("Streamed " + served + " bytes of " + podcast + " of expected " + file.length());
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
        final URL resource = cl.getResource("resources/logo.png");

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
            HashMap<String, Episode> episodes = programme.getEpisodes();
            for (Episode episode : episodes.values()) {
                resultStr += "<item>" + CRLF;
                resultStr += "<title>" + htmlUtils.makeContentSafe(episode.getPodcastTitle()) + "</title>" + CRLF;
                resultStr += "<link>" + episode.getServiceUrl() + "</link>" + CRLF;
                resultStr += "<guid>" + episode.getServiceUrl() + "</guid>" + CRLF;
                resultStr += "<description>" + htmlUtils.makeContentSafe(episode.getDescription()) + "</description>" + CRLF;
                resultStr += "<itunes:image href=\"" + episode.getIconUrl() + "\"/>" + CRLF;
                resultStr += "<media:thumbnail url=\"" + episode.getIconUrl() + "\"/>" + CRLF;
                int length = 999999;
                String type = "video/mp4";
                resultStr += "<enclosure url=\"" + episode.getPodcastUrl() + "\" length=\"" + length + "\" type=\"" + type + "\"/>" + CRLF;
                resultStr += "</item>" + CRLF;
            }
        } else if (service.isSubCategory()) {
            Map<String, Category> subCats = ((SubCategory) service).getSubCategories();
            for (Category subCat : subCats.values()) {
                resultStr += "<item>" + CRLF;
                resultStr += "<title>" + subCat.getShortName() + "</title>" + CRLF;
                resultStr += "<link>" + subCat.getPodcastUrl() + "</link>" + CRLF;
                resultStr += "<guid>" + subCat.getPodcastUrl() + "</guid>" + CRLF;
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
        for (Category cat : catalog.getCategories()) {
            try {
                logger.info("Building podcast for " + cat);
                String podcast = buildPodcastFor(cat);
                newPodcasts.put(cat.getId(), podcast);
            } catch (Exception e) {
                logger.severe("Failed to build podcast for " + cat, e);
            }
        }
        podcasts = newPodcasts;
    }

}
