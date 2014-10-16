package Demand5

import uk.co.mdjcox.sagetv.model.Programme

String str = GET_WEB_PAGE(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = MOVE_TO("<h4 id=\"group_A\">A</h4>", str);
str = EXTRACT_TO("</ul>", str)


String end = "</li>"; // "</a>";
String start = "<li";

    if (str == null) {
        LOG_ERROR(source, "Cannot list programmes for " + url + " - no programmes block found");
    }

while (str != null) {
    str = MOVE_TO(start, str);
        if (str == null) {
            if (programmes.isEmpty()) {
                LOG_ERROR(source, "Cannot list programmes for " + site + " - no programme block found");
            }
            break;
        }
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    linkName = MOVE_TO("<em>", programmeBlock);
    linkName = EXTRACT_TO("</em>", linkName);
    linkName = REMOVE_HTML_TAGS(linkName);
    if (link == null) {
        LOG_ERROR(source, "Cannot add programme - no programme link found");
        continue;
    }
    link = MAKE_LINK_ABSOLUTE("http://www.channel5.com", link);
    if ((linkName == null) || linkName.isEmpty()) {
        LOG_WARNING(source, "No programme name found - using link");
        linkName = link;
    }

    subCatId = MAKE_ID(linkName);
    Programme programme = new Programme(
            source.getId(),
            subCatId,
            linkName,
            linkName,
            link,
            "", // Icon
            ""
    );
        programme.addMetaUrl(link);

    programmes.add(programme);
}






