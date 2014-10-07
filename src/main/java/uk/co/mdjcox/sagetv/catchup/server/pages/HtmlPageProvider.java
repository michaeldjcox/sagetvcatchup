package uk.co.mdjcox.sagetv.catchup.server.pages;

/**
 * Created by michael on 07/10/14.
 */

import uk.co.mdjcox.sagetv.catchup.server.ContentProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class HtmlPageProvider implements ContentProvider {

    @Override
    public void serve(HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding(getEncoding());
        response.setContentType(getType());
        response.getWriter().println(getPage());
        response.getWriter().flush();
    }

    @Override
    public String getType() {
        return  "text/html";
    }

    @Override
    public String getEncoding() {
        return "UTF-8";
    }
}
