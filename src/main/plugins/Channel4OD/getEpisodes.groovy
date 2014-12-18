package Channel4OD

import uk.co.mdjcox.sagetv.model.Episode

String str = GET_WEB_PAGE(url, stopFlag);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = MOVE_TO("<ol class=\"all-series\">", str);
str = EXTRACT_TO("</ol>", str);

String orig = str;
String end = "</li>"; // "</a>";
String start = "<li data";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("ata-episodeurl=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    link = MAKE_LINK_ABSOLUTE("http://www.channel4.com", link);
    Episode subCat = new Episode(
            source.getSourceId(),
            "", // id
            category.getShortName(), //programmeTitle
            "", //seriesTitle
            "", //episodeTitle
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

    category.addEpisode(subCat);
}

str = orig;
end = "Watch</a>"; // "</a>";
start = "<div class=\"node node-episode";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    if (link == null) continue
    link = MAKE_LINK_ABSOLUTE("http://www.channel4.com", link);
    Episode subCat = new Episode(
            source.getSourceId(),
            "", // id
            category.getShortName(), //programmeTitle
            "", //seriesTitle
            "", //episodeTitle
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

    category.addEpisode(subCat);
}







