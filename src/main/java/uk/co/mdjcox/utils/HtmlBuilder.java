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

      html.append("<style type=\"text/css\">\n");
      html.append("BODY { color: #000000; background-color: white; font-family: Verdana; margin-left: 10px; margin-top: 0px; }\n");
      html.append("#content { margin-left: 10px; font-size: .70em; padding-bottom: 2em; }\n");
      html.append("A:link { color: #336699; font-weight: bold; text-decoration: underline; margin-left: -10px;padding-left: 15px;}\n");
      html.append("A:visited { color: #6699cc; font-weight: bold; text-decoration: underline; margin-left: -10px;padding-left: 15px;}\n");
      html.append("A:active { color: #336699; font-weight: bold; text-decoration: underline; margin-left: -10px;padding-left: 15px;}\n");
      html.append("A:hover { color: cc3300; font-weight: bold; text-decoration: underline; margin-left: -10px;padding-left: 15px;}\n");
      html.append("P { color: #000000; margin-top: 0px; margin-bottom: 12px; margin-left: -10px; font-family: Verdana; padding-left: 15px;}\n");
      html.append("pre { background-color: #e5e5cc; padding: 5px; font-family: Courier New; font-size: x-small; margin-top: -5px; border: 1px #f0f0e0 solid; }\n");
      html.append("td { color: #000000; font-family: Verdana; font-size: .7em; }\n");
      html.append("h1 { color: #ffffff; font-family: Tahoma; font-size: 26px; font-weight: normal; background-color: #003366; margin-top: 0px; margin-bottom: 0px; margin-left: -10px; padding-top: 10px; padding-bottom: 3px; padding-left: 15px; width: 105%; }\n");
      html.append("h2 { font-size: 1.5em; font-weight: bold; margin-top: 25px; margin-bottom: 10px; border-top: 1px solid #003366; margin-left: -10px; padding-left: 15px; color: #003366; }\n");
      html.append("h3 { font-size: 1.1em; color: #000000; margin-left: -10px; margin-top: 10px; margin-bottom: 10px; padding-left: 15px; }\n");
      html.append("ul { margin-top: 10px; margin-left: 20px; }\n");
      html.append("ol { margin-top: 10px; margin-left: 20px; }\n");
      html.append("li { margin-top: 10px; color: #000000; }\n");
      html.append("font.value { color: darkblue; font: bold; }\n");
      html.append("font.key { color: darkgreen; font: bold; }\n");
      html.append("font.error { color: darkred; font: bold; }\n");
      html.append(".heading1 { color: #ffffff; font-family: Tahoma; font-size: 26px; font-weight: normal; background-color: #003366; margin-top: 0px; margin-bottom: 0px; margin-left: 10px; padding-top: 10px; padding-bottom: 3px; padding-left: 15px; width: 105%; }\n");
      html.append(".button { background-color: #dcdcdc; font-family: Verdana; font-size: 1em; border-top: #cccccc 1px solid; border-bottom: #666666 1px solid; border-left: #cccccc 1px solid; border-right: #666666 1px solid; }\n");
      html.append(".frmheader { color: #000000; background: #dcdcdc; font-family: Verdana; font-size: .7em; font-weight: normal; border-bottom: 1px solid #dcdcdc; padding-top: 2px; padding-bottom: 2px; }\n");
      html.append(".frmtext { font-family: Verdana; font-size: .7em; margin-top: 8px; margin-bottom: 0px; margin-left: 10px; }\n");
      html.append(".frmInput { font-family: Verdana; font-size: 1em; }\n");
      html.append(".intro { margin-left: 10px; }\n");
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

    public void addHeading3(String title) {
        html.append("<h3>");
        html.append(title);
        html.append("</h3>");
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
}
