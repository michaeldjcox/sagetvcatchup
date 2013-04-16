package Iplayer

import uk.co.mdjcox.model.Episode

String str = GET_WEB_PAGE(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

String orig = str;
String end = "/>"; // "</a>";
String start = "<meta property=\"og:url\"";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("content=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    if (link == null) continue
    link = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", link);
    Episode subCat = new Episode(
            "Iplayer",
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
end = "</ul>"; // "</a>";
start = "<div class=\"content\" id=\"tabpanel-moreepisodes\">";

str = MOVE_TO(start, str);
str = EXTRACT_TO(end, str)

end = "</a"
start = "<a "


while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    if (link == null) continue
    link = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", link);
    Episode subCat = new Episode(
            "Iplayer",
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







