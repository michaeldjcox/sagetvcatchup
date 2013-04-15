package Demand5

import uk.co.mdjcox.model.Programme

String str = GET(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = MOVE_TO("<h4 id=\"group_A\">A</h4>", str);
str = extractTo("</ul>", str)


String end = "</li>"; // "</a>";
String start = "<li";


while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    linkName = MOVE_TO("<em>", programmeBlock);
    linkName = extractTo("</em>", linkName);
    linkName = REMOVE_HTML(linkName);
    if (link == null) continue
    link = makeLinkAbsolute("http://www.channel5.com", link);
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






