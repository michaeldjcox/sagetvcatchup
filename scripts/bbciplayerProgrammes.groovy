import uk.co.mdjcox.model.Programme
import uk.co.mdjcox.utils.HtmlUtils

String str = downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

String end = "<div class=\"sidebar\""; // "</a>";
String start = "<div id=\"results\"";

str = HtmlUtils.instance().moveTo(start, str)
str = HtmlUtils.instance().extractTo(end, str)

end = "</li>"
start = "<li>"

while (str != null) {
    str = HtmlUtils.instance().moveTo(start, str)
    if (str == null) break;
    String programmeBlock = HtmlUtils.instance().extractTo(end, str)
    programmeBlock = HtmlUtils.instance().moveTo("href=\"", programmeBlock);
    link = HtmlUtils.instance().extractTo("\"", programmeBlock)
    linkName = HtmlUtils.instance().moveTo(">", programmeBlock);
    linkName = HtmlUtils.instance().extractTo("<", linkName);
    linkName = HtmlUtils.instance().removeHtml(linkName);
    if (link == null) continue
    link = HtmlUtils.instance().makeLinkAbsolute("http://www.bbc.co.uk", link);
    if ((linkName == null) || linkName.isEmpty()) linkName = link;
    subCatId = HtmlUtils.instance().makeIdSafe(linkName);
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






