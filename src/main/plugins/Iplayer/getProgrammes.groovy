package Iplayer

import uk.co.mdjcox.model.Programme

String str = GET(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

String end = "<div class=\"sidebar\""; // "</a>";
String start = "<div id=\"results\"";

str = MOVE_TO(start, str)
str = extractTo(end, str)

end = "</li>"
start = "<li>"

while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    linkName = MOVE_TO(">", programmeBlock);
    linkName = extractTo("<", linkName);
    linkName = REMOVE_HTML(linkName);
    if (link == null) continue
    link = makeLinkAbsolute("http://www.bbc.co.uk", link);
    if ((linkName == null) || linkName.isEmpty()) linkName = link;
    subCatId = makeIdSafe(linkName);
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






