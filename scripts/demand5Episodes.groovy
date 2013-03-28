import uk.co.mdjcox.model.SubCategory
import uk.co.mdjcox.utils.HtmlUtils
import uk.co.mdjcox.model.Episode

String str = DownloadUtils.instance().downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = HtmlUtils.instance().moveTo("<div id=\"more_episodes_container\"", str);
str = HtmlUtils.instance().extractTo("<p class=\"more_resources\">", str);

String end = "</li>"; // "</a>";
String start = "<li class=\"clearfix\">";

while (str != null) {
    str = HtmlUtils.instance().moveTo(start, str)
    if (str == null) break;
    String programmeBlock = HtmlUtils.instance().extractTo(end, str)
    programmeBlock = HtmlUtils.instance().moveTo("href=\"", programmeBlock);
    link = HtmlUtils.instance().extractTo("\"", programmeBlock)
    if (link == null) continue
    link = HtmlUtils.instance().makeLinkAbsolute("http://www.channel5.com", link);
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







