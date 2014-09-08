package ITVPlayer

import uk.co.mdjcox.sagetv.model.Episode

String str = GET_WEB_PAGE(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

String orig = str;
String end = "<div class=\"body\">"; // "</a>";
String start = "<div class=\"node node-episode episode";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("rel=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    if (link == null) continue
    link = MAKE_LINK_ABSOLUTE("http://www.itv.com", link);
    Episode subCat = new Episode(
            category.getSourceId(),
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
            "", // channel
            new HashSet(), // genre
    );

    category.addEpisode(subCat);
}

if (!category.getSubCategories().isEmpty()) return

str = orig;
end = "<meta property="; // "</a>";
start = "<meta property=\"og:url\"";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("content=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    if (link == null) continue
    link = MAKE_LINK_ABSOLUTE("http://www.itv.com", link);
    Episode subCat = new Episode(
            source.getId(),
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
            "", // channel
            new HashSet(), // genre
    );

    category.addEpisode(subCat);
}







