package Iplayer

import uk.co.mdjcox.sagetv.model.Programme

String str = GET_WEB_PAGE(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) {
        LOG_ERROR(category, "Iplayer", category.getId(), "", "Cannot list programmes - programmes not found", url );
        str = null;
    }
} else {
    LOG_ERROR(category, "Iplayer", category.getId(), "", "Cannot list programmes", url );
}

String end = "<div id=\"tvip-footer-wrap\">"; // "</a>";
String start = "Results beginning with";

str = MOVE_TO(start, str)
str = EXTRACT_TO(end, str)

end = "</li>"
start = "<li>"

if (str == null) {
    LOG_ERROR(category, "Iplayer", category.getId(), "", "Cannot list programmes - not programmes block found", url );
}

while (str != null) {
    str = MOVE_TO(start, str);
    if (str == null) {
        if (programmes.isEmpty()) {
            LOG_ERROR(category, "Iplayer", category.getId(), "", "Cannot list programmes - no programme block found", url);
        }
        break;
    }
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    linkName = MOVE_TO("<span class=\"title\">", programmeBlock);
    linkName = EXTRACT_TO("<", linkName);
    linkName = REMOVE_HTML_TAGS(linkName);
    if (link == null) {
        LOG_ERROR(category, "Iplayer", category.getId(), "", "Cannot add programme - no programme link found", url );
        continue;
    }
    link = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", link);
    if ((linkName == null) || linkName.isEmpty()) {
        LOG_WARNING(category, "Iplayer", category.getId(), link, "No programme name found - using link", url );
        linkName = link;
    }
    subCatId = MAKE_ID(linkName);
    Programme subCat = new Programme(
            subCatId,
            linkName,
            linkName,
            link,
            "", // Icon
            ""
    );

    programmes.add(subCat);
}






