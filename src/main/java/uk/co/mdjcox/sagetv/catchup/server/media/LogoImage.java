package uk.co.mdjcox.sagetv.catchup.server.media;

import uk.co.mdjcox.sagetv.catchup.server.ContentProvider;
import uk.co.mdjcox.sagetv.catchup.server.Server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.StringTokenizer;

/**
 * Created by michael on 07/10/14.
 */
public class LogoImage implements ContentProvider {

  private final String imagesDir;
  private String id;
  private String image;
  private String type;

  public LogoImage(String imagesDir, String imageFileName) {
    this.imagesDir = imagesDir;
      imageFileName = imageFileName.replace(imagesDir, "");
    imageFileName = imageFileName.replace(File.separator, "/");
    while (imageFileName.startsWith("/")) {
      imageFileName = imageFileName.substring(1);
    }
    int lastSlash = imageFileName.lastIndexOf("/");
    if (lastSlash >= 0) {
      id = imageFileName.substring(0, lastSlash);
    }
    final int lastDot = imageFileName.lastIndexOf(".");
    image = imageFileName.substring(lastSlash+1, lastDot);
    type = imageFileName.substring(lastDot+1);
    if (id == null) {
      id = image;
    } else {
      id = id + "/" + image;
    }
  }

  @Override
    public String getUri() {
        return  "image?id=" + id + "." + type;
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
      if (type.equals("jpg")) {
        return "image/jpeg";
      } else {
        return "image/" + type;
      }
    }

    @Override
    public String getEncoding() {
        return "ISO-8859-1";
    }


    @Override
    public void serve(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        File imageFile = new File(imagesDir+ File.separator + id.replace("/", File.separator) + "." + type);

        FileInputStream in = new FileInputStream(imageFile);

      long fileSize = imageFile.length();

      response.setContentType(getType());
//      response.setCharacterEncoding(getEncoding());
      response.setContentLength((int) fileSize);


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
