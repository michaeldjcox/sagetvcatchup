package uk.co.mdjcox.sagetv.catchup.server;

import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by michael on 07/10/14.
 */
public class CachedPageProvider implements ContentProvider {
    // TODO some form of staging and cleanup

    private final String htdocsDir;
    private final String encoding;
    private final Logger logger;
    private String type;
    private String uri;
    private String fileName;

    public CachedPageProvider(Logger logger, String htdocsDir, ContentProvider provider) {
        type = provider.getType();
        uri = provider.getUri();
        this.logger = logger;
        encoding = provider.getEncoding();
        this.htdocsDir = htdocsDir;
        fileName = uri.substring(1).replace("?", "-");
        cacheHtml(fileName, provider.getPage());
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getPage() {
        return null;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getEncoding() {
        return encoding;
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
            String message = getFromCache(htdocsDir, fileName);
            response.getWriter().println(message);
        } catch (Exception e) {
            throw new ServletException("Failed to find page", e);
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

    private void cacheHtml(String name, String content) {
        name = name.replace("/", "_");
        name = name.replace("\\", "_");
        File file = new File(htdocsDir + File.separator + name);
        FileWriter fwriter = null;
        PrintWriter writer = null;

        try  {
            fwriter = new FileWriter(file);
            writer = new PrintWriter(fwriter);
            writer.println(content);
            writer.flush();
        } catch (Exception ex) {
            logger.error("Failed to cache " + fileName, ex);
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
}
