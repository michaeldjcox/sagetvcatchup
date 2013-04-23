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


while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    linkName = MOVE_TO("<em>", programmeBlock);
    linkName = EXTRACT_TO("</em>", linkName);
    linkName = REMOVE_HTML_TAGS(linkName);
    if (link == null) continue
    link = MAKE_LINK_ABSOLUTE("http://www.channel5.com", link);
    if ((linkName == null) || linkName.isEmpty()) linkName = link;
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






