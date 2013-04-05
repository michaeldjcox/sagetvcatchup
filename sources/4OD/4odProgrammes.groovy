import uk.co.mdjcox.model.Programme

String str = downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = moveTo("<a name=\"A\" id=\"A\">", str);
//str = extractTo("</div>",str)


String end = "</li>"; // "</a>";
String start = "<li class=\"promo-list-item\"";



while (str != null) {
    str = moveTo(start, str)
    if (str == null) break;
    String programmeBlock = extractTo(end, str)
    programmeBlock = moveTo("href=\"", programmeBlock);
    link = extractTo("\"", programmeBlock)
    linkName = moveTo("<span class=\"promo-list-item-info promo-list-item-title", programmeBlock);
    linkName = moveTo(">", linkName);
    linkName = extractTo("<", linkName);
    linkName = removeHtml(linkName);
    if (link == null) continue
    link = makeLinkAbsolute("http://www.channel4.com", link);
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






