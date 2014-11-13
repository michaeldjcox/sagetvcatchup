package Iplayer

import uk.co.mdjcox.sagetv.model.Programme

String[] sites = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0-9"]


for (String site : sites) {

    String subUrl = url + site;

    String str = GET_WEB_PAGE(subUrl, stopFlag);

    if (str != null) {
        if (str.contains("There are no programmes available at the moment")) {
            continue;
        }
    } else {
        LOG_ERROR(source, "Cannot list programmes: " + site);
        continue;
    }

    String end = "<div id=\"tvip-footer-wrap\">"; // "</a>";
    String start = "Results beginning with";

    str = MOVE_TO(start, str)
    str = EXTRACT_TO(end, str)

    end = "</li>"
    start = "<li>"

    if (str == null) {
        LOG_ERROR(source, "Cannot list programmes - no programmes block found: " + site);
    }

    while (str != null) {
        str = MOVE_TO(start, str);
        if (str == null) {
            if (programmes.isEmpty()) {
                LOG_ERROR(source, "Cannot add programme - no programme block found");
            }
            break;
        }
        String programmeBlock = EXTRACT_TO(end, str)
        programmeBlock = MOVE_TO("href=\"", programmeBlock);
        link = EXTRACT_TO("\"", programmeBlock)
        if (link == null) {
            LOG_ERROR(source, "Cannot add programme - no programme link found");
            continue;
        }
        link = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", link);

        linkName = MOVE_TO("<span class=\"title\">", programmeBlock);
        linkName = EXTRACT_TO("<", linkName);
        linkName = REMOVE_HTML_TAGS(linkName);
        if ((linkName == null) || linkName.isEmpty()) {
            LOG_WARNING(source, "No programme name found");
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
}






