package uk.co.mdjcox.sagetv.utils;

/**
 * Created by michael on 04/09/14.
 */
public class HtmlBuilder  {

  private final StringBuilder html = new StringBuilder();
  private final static String CRLF = System.getProperty("line.separator");

  public HtmlBuilder() {
  }

  public void startDocument() {
    html.append("<html>").append(CRLF);
  }

  public void stopDocument() {
    html.append("</html>").append(CRLF);
  }

  public void addPageHeader(String pageTitle) {
    addPageHeader(pageTitle, false, "");
  }

  public void addPageHeader(String pageTitle, boolean redirect, String redirectTo) {
      html.append("<head>").append(CRLF);
      if (redirect) {
        html.append("<meta HTTP-EQUIV=\"REFRESH\" content=\"0; url="+redirectTo+"\">");
      }
      html.append("<title>");
      html.append(pageTitle);
      html.append("</title>").append(CRLF);
      html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\">").append(CRLF);
      html.append("</head>").append(CRLF);
  }

  public void startBody() {
    html.append("<body>").append(CRLF);
  }

  public void stopBody() {
    html.append("</body>").append(CRLF);
  }

  public void startTable() {
    html.append("<table>").append(CRLF);
  }

  public void addTableHeader(String... columns) {
    html.append("<tr>").append(CRLF);
    for (String col : columns) {
      html.append("<th>");
      html.append(col);
      html.append("</th>").append(CRLF);
    }
    html.append("</tr>").append(CRLF);
  }

  public void addTableRow(String... columns) {
    html.append("<tr>").append(CRLF);
    for (String col : columns) {
      html.append("<td>");
      html.append(col);
      html.append("</td>").append(CRLF);
    }
    html.append("</tr>").append(CRLF);
  }

  public void stopTable() {
    html.append("</table>").append(CRLF);
  }

  @Override
  public String toString() {
    return html.toString();
  }

  public void startList() {
    html.append(CRLF).append("<ul>").append(CRLF);
  }

  public void stopList() {
    if (html.substring(html.length()-5).equals("<ul>" + CRLF)) {
        html.deleteCharAt(html.length()-1);
    }
    html.append("</ul>").append(CRLF);
  }

  public void addListItem(String item) {
    html.append("<li>");
    html.append(item);
    html.append("</li>").append(CRLF);
  }

  public void addLink(String name, String link) {
    html.append("<a href=\"");
    html.append(link);
    html.append("\">");
    html.append(name);
    html.append("</a>");
  }

  public void addHeading1(String title) {
    html.append("<h1>");
    html.append(title);
    html.append("</h1>").append(CRLF);
  }

  public void addHeading2(String title) {
    html.append("<h2>");
    html.append(title);
    html.append("</h2>").append(CRLF);
  }

    public void addHeading3(String title) {
        html.append("<h3>");
        html.append(title);
        html.append("</h3>").append(CRLF);
    }
    public void addBreak() {
        html.append("<br/>");
    }

    public void addParagraph(String paragraph) {
        html.append("<p>");
        html.append(paragraph);
        html.append("</p>");

    }

    public void boldOn() {
        html.append("<b>");
    }

    public void boldOff() {
        html.append("</b>");
    }

    public void addLineFeed() {
        html.append("").append(CRLF);
    }
}
