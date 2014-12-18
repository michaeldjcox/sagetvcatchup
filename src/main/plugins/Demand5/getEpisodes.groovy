package Demand5

import uk.co.mdjcox.sagetv.model.Episode

String str = GET_WEB_PAGE(url, stopFlag);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) {
        LOG_ERROR(category, "Cannot list episodes - programme not found" );
        str = null;
    }
} else {
    LOG_ERROR(category, "Cannot list episodes" );
}

str = MOVE_TO("<div id=\"more_episodes_container\"", str);
str = EXTRACT_TO("<p class=\"more_resources\">", str);

String end = "</li>"; // "</a>";
String start = "<li class=\"clearfix\">";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) {
        break;
    }
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    if (link == null) {
        LOG_ERROR(category, "Cannot add episode - episode link not found" );
        continue;
    }
        link = MAKE_LINK_ABSOLUTE("http://www.channel5.com", link);
    Episode subCat = new Episode(
            source.getSourceId(),
            "", // id
            category.getShortName(), //programmeTitle
            "", //seriesTitle
            MAKE_ID(link), //episodeTitle
            "", // series
            "", // episode
            "", //description
            "", // iconUrl
            link,
            "", // airDate
            "", // airTime
            "", // origAirDate
            "", // origAirTime
            "", // channel
            new HashSet(), // genre
    );

    episodes.add(subCat);
}





