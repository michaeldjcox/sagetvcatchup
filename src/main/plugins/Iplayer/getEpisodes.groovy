package Iplayer

import uk.co.mdjcox.sagetv.model.Episode


url = REPLACE_LINK_PREFIX(url, "http://www.bbc.co.uk/iplayer/episodes");

String str = GET_WEB_PAGE(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

String end = "</li>"; // "</a>";
String start = "<li class=\"list-item episode\"";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("<a href=\"", programmeBlock);
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
            new HashSet(), // genre
    );

    category.addEpisode(subCat);
}




