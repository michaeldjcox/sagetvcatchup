import uk.co.mdjcox.model.Episode
import uk.co.mdjcox.model.SubCategory
import uk.co.mdjcox.utils.HtmlUtils

String str = DownloadUtils.instance().downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = HtmlUtils.instance().moveTo("<ol class=\"all-series\">", str);
str = HtmlUtils.instance().extractTo("</ol>", str);

String orig = str;
String end = "</li>"; // "</a>";
String start = "<li data";

while (str != null) {
    str = HtmlUtils.instance().moveTo(start, str)
    if (str == null) break;
    String programmeBlock = HtmlUtils.instance().extractTo(end, str)
    programmeBlock = HtmlUtils.instance().moveTo("ata-episodeurl=\"", programmeBlock);
    link = HtmlUtils.instance().extractTo("\"", programmeBlock)
    link = HtmlUtils.instance().makeLinkAbsolute("http://www.channel4.com", link);
    Episode subCat = new Episode(
            "", //programmeTitle
            "", //episodeTitle
            "", // series
            "", // episode
            "", //description
            "", // iconUrl
            link,
            "", // airDate
            "", // airTime
            "", // channel
            "", // category
    );

    category.addEpisode(subCat);
}

str = orig;
end = "Watch</a>"; // "</a>";
start = "<div class=\"node node-episode";

while (str != null) {
    str = HtmlUtils.instance().moveTo(start, str)
    if (str == null) break;
    String programmeBlock = HtmlUtils.instance().extractTo(end, str)
    programmeBlock = HtmlUtils.instance().moveTo("href=\"", programmeBlock);
    link = HtmlUtils.instance().extractTo("\"", programmeBlock)
    if (link == null) continue
    link = HtmlUtils.instance().makeLinkAbsolute("http://www.channel4.com", link);
    Episode subCat = new Episode(
            "", //programmeTitle
            "", //episodeTitle
            "", // series
            "", // episode
            "", //description
            "", // iconUrl
            link,
            "", // airDate
            "", // airTime
            "", // channel
            "", // category
    );

    category.addEpisode(subCat);
}







