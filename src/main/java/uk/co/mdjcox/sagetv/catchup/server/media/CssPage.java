package uk.co.mdjcox.sagetv.catchup.server.media;


import uk.co.mdjcox.logger.Logger;
import uk.co.mdjcox.sagetv.catchup.server.ContentProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by michael on 07/10/14.
 */
public class CssPage implements ContentProvider {

    private final String cssDir;
    private final Logger logger;
    private String stylesheet;

    public CssPage(Logger logger, String cssDir, String stylesheet) {
        this.stylesheet = stylesheet;
        this.cssDir = cssDir;
        this.logger = logger;
    }

    @Override
    public String getUri() {
        return stylesheet;
    }

    @Override
    public String getPage() {
        return "";
    }

    @Override
    public String getType() {
        return "text/css";
    }

    @Override
    public String getEncoding() {
        return "UTF-8";
    }

    @Override
    public String buildPage() {
        return "";
    }

    @Override
    public void serve(HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setCharacterEncoding(getEncoding());
            response.setContentType(getType());
            String message = getFromCache(cssDir, stylesheet);
            response.getWriter().println(message);
            response.flushBuffer();
        } catch (Exception e) {
            throw new ServletException("Failed to find page", e);
        }

    }

    private String getFromCache(String dir, String name) {
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
            return result;
        } catch (Exception ex) {
            logger.error("Failed to open " + file);
            return "";
        }finally {
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

}
