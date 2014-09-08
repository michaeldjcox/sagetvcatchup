package Channel4OD

import uk.co.mdjcox.sagetv.model.Programme

String str = GET_WEB_PAGE(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = MOVE_TO("<a name=\"A\" id=\"A\">", str);
//str = EXTRACT_TO("</div>",str)


String end = "</li>"; // "</a>";
String start = "<li class=\"promo-list-item\"";



while (str != null) {
    str = MOVE_TO(start, str)
    if (str == null) break;
    String programmeBlock = EXTRACT_TO(end, str)
    programmeBlock = MOVE_TO("href=\"", programmeBlock);
    link = EXTRACT_TO("\"", programmeBlock)
    linkName = MOVE_TO("<span class=\"promo-list-item-info promo-list-item-title", programmeBlock);
    linkName = MOVE_TO(">", linkName);
    linkName = EXTRACT_TO("<", linkName);
    linkName = REMOVE_HTML_TAGS(linkName);
    if (link == null) continue
    link = MAKE_LINK_ABSOLUTE("http://www.channel4.com", link);
    if ((linkName == null) || linkName.isEmpty()) linkName = link;
    subCatId = MAKE_ID(linkName);
    Programme subCat = new Programme(
            source.getSourceId(),
            subCatId,
            linkName,
            linkName,
            link,
            "", // Icon
            ""
    );

    programmes.add(subCat);
}






