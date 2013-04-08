package Iplayer

import uk.co.mdjcox.model.Episode

String str = downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

String orig = str;
String end = "/>"; // "</a>";
String start = "<meta property=\"og:url\"";

while (str != null) {
    str = moveTo(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = moveTo("content=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    if (link == null) continue
    link = makeLinkAbsolute("http://www.bbc.co.uk", link);
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

str = moveTo(start, str);
str = extractTo(end, str)

end = "</a"
start = "<a "


while (str != null) {
    str = moveTo(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = moveTo("href=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    if (link == null) continue
    link = makeLinkAbsolute("http://www.bbc.co.uk", link);
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







