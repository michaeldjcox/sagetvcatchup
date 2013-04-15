package ITVPlayer

import uk.co.mdjcox.model.Episode

String str = GET(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

String orig = str;
String end = "<div class=\"body\">"; // "</a>";
String start = "<div class=\"node node-episode episode";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = MOVE_TO("rel=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    if (link == null) continue
    link = makeLinkAbsolute("http://www.itv.com", link);
    Episode subCat = new Episode(
            "ITVPlayer",
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

if (!category.getSubCategories().isEmpty()) return

str = orig;
end = "<meta property="; // "</a>";
start = "<meta property=\"og:url\"";

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = MOVE_TO("content=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    if (link == null) continue
    link = makeLinkAbsolute("http://www.itv.com", link);
    Episode subCat = new Episode(
            "ITVPlayer",
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







