package Channel4OD

import uk.co.mdjcox.model.Episode

String str = downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = moveTo("<ol class=\"all-series\">", str);
str = extractTo("</ol>", str);

String orig = str;
String end = "</li>"; // "</a>";
String start = "<li data";

while (str != null) {
    str = moveTo(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = moveTo("ata-episodeurl=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    link = makeLinkAbsolute("http://www.channel4.com", link);
    Episode subCat = new Episode(
            "", // id
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
    str = moveTo(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = moveTo("href=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    if (link == null) continue
    link = makeLinkAbsolute("http://www.channel4.com", link);
    Episode subCat = new Episode(
            "", // id
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







