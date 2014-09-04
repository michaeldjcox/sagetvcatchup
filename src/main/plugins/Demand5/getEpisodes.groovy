package Demand5

import uk.co.mdjcox.sagetv.model.Episode

String str = GET_WEB_PAGE(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = MOVE_TO("<div id=\"more_episodes_container\"", str);
str = EXTRACT_TO("<p class=\"more_resources\">", str);

String end = "</li>"; // "</a>";
String start = "<li class=\"clearfix\">";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    if (link == null) continue
    link = MAKE_LINK_ABSOLUTE("http://www.channel5.com", link);
    Episode subCat = new Episode(
            "Demand5",
            "", // id
            "", //programmeTitle
            "", //seriesTitle
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







