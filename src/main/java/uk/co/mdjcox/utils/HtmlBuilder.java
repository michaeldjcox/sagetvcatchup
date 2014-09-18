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
    html.append("<style>\n");
    html.append("table, th, td {\n");
    html.append("border: 1px solid black;\n");
    html.append("  border-collapse: collapse;\n");
    html.append("}\n");
    html.append("th, td {\n");
    html.append("padding: 5px;\n");
    html.append("text-align: left;\n");
    html.append("}\n");
    html.append("table.names th	{\n");
    html.append("background-color: #c1c1c1;\n");
    html.append("}\n");
    html.append("</style>\n");
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

  public void startTableWidth100() {
   html.append("<table style=\"width:100%\">\n");
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
    html.append("</table>");
  }

  @Override
  public String toString() {
    return html.toString();
  }

  public void startList() {
    html.append("<ul>");
  }

  public void stopList() {
    html.append("</ul>");
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
    html.append("</a>\n");
  }

  public void addHeading1(String title) {
    html.append("<h1>");
    html.append(title);
    html.append("</h1>");
  }

  public void addHeading2(String title) {
    html.append("<h2>");
    html.append(title);
    html.append("</h2>");

  }

    public void addBreak() {
        html.append("<br/>");
    }
}
