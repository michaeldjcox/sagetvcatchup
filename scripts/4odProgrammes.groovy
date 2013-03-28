

import uk.co.mdjcox.model.Programme
import uk.co.mdjcox.utils.HtmlUtils

String str = DownloadUtils.instance().downloadFileString(url);

if (str != null) {
    if (str.contains("The programme you're looking for can't be found")) str = null;
}

str = HtmlUtils.instance().moveTo("<a name=\"A\" id=\"A\">", str);
//str = HtmlUtils.instance().extractTo("</div>",str)


String end = "</li>"; // "</a>";
String start = "<li class=\"promo-list-item\"";



while (str != null) {
    str = HtmlUtils.instance().moveTo(start, str)
    if (str == null) break;
    String programmeBlock = HtmlUtils.instance().extractTo(end, str)
    programmeBlock = HtmlUtils.instance().moveTo("href=\"", programmeBlock);
    link = HtmlUtils.instance().extractTo("\"", programmeBlock)
    linkName = HtmlUtils.instance().moveTo("<span class=\"promo-list-item-info promo-list-item-title", programmeBlock);
    linkName = HtmlUtils.instance().moveTo(">", linkName);
    linkName = HtmlUtils.instance().extractTo("<", linkName);
    linkName = HtmlUtils.instance().removeHtml(linkName);
    if (link == null) continue
    link = HtmlUtils.instance().makeLinkAbsolute("http://www.channel4.com", link);
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






