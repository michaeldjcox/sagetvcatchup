package uk.co.mdjcox.sagetv.catchup.server;


import uk.co.mdjcox.utils.LoggerInterface;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by michael on 07/10/14.
 */
public class CachedContentProvider implements ContentProvider {
    private final String htdocsDir;
    private final String encoding;
    private final LoggerInterface logger;
  private final String stagingDir;
  private String type;
    private String uri;
    private String fileName;

    public CachedContentProvider(LoggerInterface logger, String stagingDir, String htdocsDir, ContentProvider provider) {
        type = provider.getType();
        uri = provider.getUri();
        this.logger = logger;
        encoding = provider.getEncoding();
        this.stagingDir = stagingDir;
        this.htdocsDir = htdocsDir;
        fileName = uri.replace("?", "-");
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
            getFromCache(response.getWriter(), htdocsDir, fileName);
        } catch (Exception e) {
            throw new ServletException("Failed to get page", e);
        }
    }

    private void getFromCache(Writer writer, String dir, String name) throws Exception {
        name = name.replace("/", "_");
        name = name.replace("\\", "_");

        File file = new File(dir + File.separator + name);
        FileReader reader = null;
        BufferedReader breader = null;

        try {
            reader = new FileReader(file);
            breader = new BufferedReader(reader, 4096);

          char[] chars = new char[4096];

          int len = -1;
          while( (len = breader.read(chars)) != -1 ) {
              writer.write(chars, 0, len);
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
    }

    private void cacheHtml(String name, String content) {
      File dirFile = new File(stagingDir);

      if (!dirFile.exists()) {
        dirFile.mkdirs();
      }

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
