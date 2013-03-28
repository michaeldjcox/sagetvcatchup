import uk.co.mdjcox.model.Episode
import uk.co.mdjcox.utils.HtmlUtils
import uk.co.mdjcox.utils.DownloadUtils

String str = DownloadUtils.instance().downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

String orig = str;
String end = "/>"; // "</a>";
String start = "<meta property=\"og:url\"";

while (str != null) {
    str = HtmlUtils.instance().moveTo(start, str)
    if (str == null) break;
    String programmeBlock = HtmlUtils.instance().extractTo(end, str)
    programmeBlock = HtmlUtils.instance().moveTo("content=\"", programmeBlock);
    link = HtmlUtils.instance().extractTo("\"", programmeBlock)
    if (link == null) continue
    link = HtmlUtils.instance().makeLinkAbsolute("http://www.bbc.co.uk", link);
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
end = "</ul>"; // "</a>";
start = "<div class=\"content\" id=\"tabpanel-moreepisodes\">";

str = HtmlUtils.instance().moveTo(start, str);
str = HtmlUtils.instance().extractTo(end, str)

end = "</a"
start= "<a "


while (str != null) {
    str = HtmlUtils.instance().moveTo(start, str)
    if (str == null) break;
    String programmeBlock = HtmlUtils.instance().extractTo(end, str)
    programmeBlock = HtmlUtils.instance().moveTo("href=\"", programmeBlock);
    link = HtmlUtils.instance().extractTo("\"", programmeBlock)
    if (link == null) continue
    link = HtmlUtils.instance().makeLinkAbsolute("http://www.bbc.co.uk", link);
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







