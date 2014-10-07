package uk.co.mdjcox.sagetv.catchup.server.pages;

import uk.co.mdjcox.sagetv.catchup.server.pages.HtmlPageProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by michael on 07/10/14.
 */
public class LogsHtmlProvider extends HtmlPageProvider {

    private final String logfileName;

    public LogsHtmlProvider(String logfileName) {
        this.logfileName = logfileName;
    }

    @Override
    public String getUri() {
        return "/logs";
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
    public void serve(HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain");
            File file = new File(logfileName);
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
}
