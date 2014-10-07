package uk.co.mdjcox.sagetv.catchup.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by michael on 07/10/14.
 */
public class LogoImageProvider implements ContentProvider {
    @Override
    public String getUri() {
        return "/logo.png";
    }

    @Override
    public String getPage() {
        return "";
    }

    @Override
    public String buildPage() {
        return "";
    }

    @Override
    public String getType() {
        return "image/png";
    }

    @Override
    public String getEncoding() {
        return "ISO-8859-1";
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

    @Override
    public void serve(HttpServletResponse response) throws ServletException, IOException {
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
}
