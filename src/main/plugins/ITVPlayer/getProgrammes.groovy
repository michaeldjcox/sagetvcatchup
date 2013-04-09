package ITVPlayer

import uk.co.mdjcox.model.Programme

String str = downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

String end = "<script>var page_valid "; // "</a>";
String start = "<li class=\"node node-programme\"";

while (str != null) {
    str = moveTo(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = moveTo("href=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    linkName = moveTo(">", programmeBlock);
    linkName = extractTo("<", linkName);
    linkName = removeHtml(linkName);
    if (link == null) continue
    link = makeLinkAbsolute("http://www.itv.com", link);
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





