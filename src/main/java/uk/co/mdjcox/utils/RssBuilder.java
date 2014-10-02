package uk.co.mdjcox.utils;

/**
 * Created by michael on 04/09/14.
 */
public class RssBuilder {

    private static final String CRLF = System.getProperty("line.separator");
    private final StringBuilder resultStr = new StringBuilder();

  public RssBuilder() {
  }

  public void startDocument(String title, String message, String url) {
      resultStr.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      resultStr.append(CRLF);
      resultStr.append("<rss xmlns:media=\"http://search.yahoo.com/mrss/\" xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\" version=\"2.0\">");
      resultStr.append(CRLF);
      resultStr.append("<channel>");
      resultStr.append(CRLF);
      resultStr.append("<title>");
      resultStr.append(title);
      resultStr.append("</title>");
      resultStr.append(CRLF);
      resultStr.append("<description>");
      resultStr.append(message);
      resultStr.append("</description>");
      resultStr.append(CRLF);
      resultStr.append("<link>");
      resultStr.append(url);
      resultStr.append("</link>");
      resultStr.append(CRLF);
      resultStr.append("<language>en-gb</language>");
      resultStr.append(CRLF);
  }

    public void addImage(String iconUrl, String name, String url) {
        resultStr.append("<image>");
        resultStr.append(CRLF);
        resultStr.append("<url>");
        resultStr.append(iconUrl);
        resultStr.append("</url> ");
        resultStr.append(CRLF);
        resultStr.append("<title>");
        resultStr.append(name);
        resultStr.append("</title>");
        resultStr.append(CRLF);
        resultStr.append("<link>");
        resultStr.append(url);
        resultStr.append("</link>");
        resultStr.append(CRLF);
        resultStr.append("</image>");
        resultStr.append(CRLF);
    }

  public void stopDocument() {
      resultStr.append("</channel>");
      resultStr.append(CRLF);
      resultStr.append("</rss>");
      resultStr.append(CRLF);
  }

    public void addTextItem(String title, String message, String url) {
       addItem(title, message, url, "sagetv/textonly", "");
    }

    public void addCategoryItem(String title, String message, String url) {
        addItem(title, message, url, "sagetv/subcategory", "");
    }

    public void addCategoryItem(String title, String message, String url, String iconUrl) {
        addItem(title, message, url, "sagetv/subcategory", iconUrl);
    }

    public void addVideoItem(String title, String message, String url, String iconUrl) {
        addItem(title, message, url, "video/mp4", iconUrl);
    }

    private void addItem(String title, String message, String url, String type, String iconUrl) {
        resultStr.append("<item>");
        resultStr.append(CRLF);
        resultStr.append("<title>");
        resultStr.append(title);
        resultStr.append("</title>");
        resultStr.append(CRLF);
        resultStr.append("<description><![CDATA[");
        resultStr.append(message);
        resultStr.append("]]></description>");
        resultStr.append(CRLF);
        resultStr.append("<link>");
        resultStr.append(url);
        resultStr.append("</link>");
        resultStr.append(CRLF);
        resultStr.append("<pubDate></pubDate>");
        resultStr.append(CRLF);
        resultStr.append("<enclosure url=\"");
        resultStr.append(url);
        resultStr.append("\" length=\"\" type=\"");
        resultStr.append(type);
        resultStr.append("\"/>");
        resultStr.append(CRLF);
        resultStr.append("<media:content duration = \"\" medium = \"video\" fileSize = \"\" url =\"");
        resultStr.append(url);
        resultStr.append("\" type = \"" + type + "\">");
        resultStr.append(CRLF);
        resultStr.append("<media:title>");
        resultStr.append(title);
        resultStr.append("</media:title>");
        resultStr.append(CRLF);
        resultStr.append("<media:description><![CDATA[");
        resultStr.append(message);
        resultStr.append("]]></media:description>");
        resultStr.append(CRLF);
        resultStr.append("<media:thumbnail url=\"");
        resultStr.append(iconUrl);
        resultStr.append("\"/>");
        resultStr.append(CRLF);
        resultStr.append("</media:content>");
        resultStr.append(CRLF);
        resultStr.append("</item>");
        resultStr.append(CRLF);
    }

    @Override
    public String toString() {
        return resultStr.toString();
    }

}
