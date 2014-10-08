package uk.co.mdjcox.utils;

/**
 * Created by michael on 04/09/14.
 */
public class HtmlBuilder  {

  private final StringBuilder html = new StringBuilder();

  public HtmlBuilder() {
  }

  public void startDocument() {
    html.append("<html>\n");
  }

  public void stopDocument() {
    html.append("</html>\n");
  }

  public void addPageHeader(String pageTitle) {
      html.append("<head>\n");
      html.append("<title>");
      html.append(pageTitle);
      html.append("</title>\n");
      html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\">\n");
      html.append("</head>\n");
  }

  public void startBody() {
    html.append("<body>\n");
  }

  public void stopBody() {
    html.append("</body>\n");
  }

  public void startTable() {
    html.append("<table>\n");
  }

  public void addTableHeader(String... columns) {
    html.append("<tr>\n");
    for (String col : columns) {
      html.append("<th>");
      html.append(col);
      html.append("</th>\n");
    }
    html.append("</tr>\n");
  }

  public void addTableRow(String... columns) {
    html.append("<tr>\n");
    for (String col : columns) {
      html.append("<td>");
      html.append(col);
      html.append("</td>\n");
    }
    html.append("</tr>\n");
  }

  public void stopTable() {
    html.append("</table>\n");
  }

  @Override
  public String toString() {
    return html.toString();
  }

  public void startList() {
    html.append("\n<ul>\n");
  }

  public void stopList() {
    if (html.substring(html.length()-5).equals("<ul>\n")) {
        html.deleteCharAt(html.length()-1);
    }
    html.append("</ul>\n");
  }

  public void addListItem(String item) {
    html.append("<li>");
    html.append(item);
    html.append("</li>\n");
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
    html.append("</h1>\n");
  }

  public void addHeading2(String title) {
    html.append("<h2>");
    html.append(title);
    html.append("</h2>\n");
  }

    public void addHeading3(String title) {
        html.append("<h3>");
        html.append(title);
        html.append("</h3>\n");
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
        html.append("\n");
    }
}
