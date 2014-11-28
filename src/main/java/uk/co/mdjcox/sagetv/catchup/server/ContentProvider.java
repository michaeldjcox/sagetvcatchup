package uk.co.mdjcox.sagetv.catchup.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by michael on 06/10/14.
 */
public interface ContentProvider {

    String getUri();

    String getPage();

    String getType();

    String getEncoding();

    String buildPage();

    void serve(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException;
}
