package Demand5

import uk.co.mdjcox.model.Episode

String str = GET(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = MOVE_TO("<div id=\"more_episodes_container\"", str);
str = extractTo("<p class=\"more_resources\">", str);

String end = "</li>"; // "</a>";
String start = "<li class=\"clearfix\">";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    if (link == null) continue
    link = makeLinkAbsolute("http://www.channel5.com", link);
    Episode subCat = new Episode(
            "Demand5",
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







