

import uk.co.mdjcox.utils.HtmlUtils

import uk.co.mdjcox.model.Programme

String str = DownloadUtils.instance().downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = HtmlUtils.instance().moveTo("<h4 id=\"group_A\">A</h4>", str);
str = HtmlUtils.instance().extractTo("</ul>",str)


String end = "</li>"; // "</a>";
String start = "<li";


while (str != null) {
    str = HtmlUtils.instance().moveTo(start, str)
    if (str == null) break;
    String programmeBlock = HtmlUtils.instance().extractTo(end, str)
    programmeBlock = HtmlUtils.instance().moveTo("href=\"", programmeBlock);
    link = HtmlUtils.instance().extractTo("\"", programmeBlock)
    linkName = HtmlUtils.instance().moveTo("<em>", programmeBlock);
    linkName = HtmlUtils.instance().extractTo("</em>", linkName);
    linkName = HtmlUtils.instance().removeHtml(linkName);
    if (link == null) continue
    link = HtmlUtils.instance().makeLinkAbsolute("http://www.channel5.com", link);
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

    programmes.add( subCat);
}






