package uk.co.mdjcox.sagetv.catchup.server.media;


import uk.co.mdjcox.sagetv.catchup.Recorder;
import uk.co.mdjcox.sagetv.catchup.server.ContentProvider;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.utils.LoggerInterface;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by michael on 07/10/14.
 */
public class WatchEpisode implements ContentProvider {

    private final Episode episode;
    private final LoggerInterface logger;
    private Recorder recorder;

    public WatchEpisode(LoggerInterface logger, Episode episode, Recorder recorder) {
        this.episode = episode;
        this.recorder = recorder;
        this.logger = logger;
    }

    @Override
    public String getType() {
        return "video/mp4";
    }

    @Override
    public String getEncoding() {
        return "ISO-8859-1";
    }

    @Override
    public void serve(HttpServletResponse response) throws ServletException, IOException {

        OutputStream out = null;

        try {
            response.setContentType(getType());
            response.setCharacterEncoding(getEncoding());
            response.setContentLength(Integer.MAX_VALUE);

            out = response.getOutputStream();
            recorder.watch(out, episode, false);
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

    @Override
    public String getUri() {
        return "watch?id="+episode.getId() +";type=mpeg4";
    }

    @Override
    public String getPage() {
        return "";
    }

    @Override
    public String buildPage() {
        return "";
    }


}
