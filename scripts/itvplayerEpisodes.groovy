import uk.co.mdjcox.utils.HtmlUtils

import uk.co.mdjcox.model.SubCategory
import uk.co.mdjcox.model.Episode

String str = DownloadUtils.instance().downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

String orig = str;
String end = "<div class=\"body\">"; // "</a>";
String start = "<div class=\"node node-episode episode";

while (str != null) {
    str = HtmlUtils.instance().moveTo(start, str)
    if (str == null) break;
    String programmeBlock = HtmlUtils.instance().extractTo(end, str)
    programmeBlock = HtmlUtils.instance().moveTo("rel=\"", programmeBlock);
    link = HtmlUtils.instance().extractTo("\"", programmeBlock)
    if (link == null) continue
    link = HtmlUtils.instance().makeLinkAbsolute("http://www.itv.com", link);
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

if (!category.getSubCategories().isEmpty()) return

str = orig;
end = "<meta property="; // "</a>";
start = "<meta property=\"og:url\"";

while (str != null) {
    str = HtmlUtils.instance().moveTo(start, str)
    if (str == null) break;
    String programmeBlock = HtmlUtils.instance().extractTo(end, str)
    programmeBlock = HtmlUtils.instance().moveTo("content=\"", programmeBlock);
    link = HtmlUtils.instance().extractTo("\"", programmeBlock)
    if (link == null) continue
    link = HtmlUtils.instance().makeLinkAbsolute("http://www.itv.com", link);
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







