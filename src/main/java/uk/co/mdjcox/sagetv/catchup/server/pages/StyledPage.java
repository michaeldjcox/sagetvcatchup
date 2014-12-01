package uk.co.mdjcox.sagetv.catchup.server.pages;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import uk.co.mdjcox.sagetv.catchup.CatalogPersister;
import uk.co.mdjcox.sagetv.utils.HtmlBuilder;
import uk.co.mdjcox.sagetv.utils.LoggerInterface;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by michael on 06/10/14.
 */
public class StyledPage extends OnDemandHtmlPage {

    private final LoggerInterface logger;
    private final String title;
    private final String stylesheet;
    private final Object object;
    private final String id;
    private final String xsltDir;
    private final CatalogPersister persister;

  public StyledPage(String xsltDir, LoggerInterface logger, String title, String stylesheet, String id, Object object, CatalogPersister persister) {
        this.logger = logger;
        this.title = title;
        this.stylesheet = stylesheet;
        this.object = object;
        this.id = id;
        this.xsltDir = xsltDir;
      this.persister = persister;
    }

    private String parseIntoXML(Object catalog) {
        return persister.parseIntoXML(catalog);
    }

    private String buildStylesheetResponse(Object catalog, String title, String webpage) throws Exception {

            String xml = parseIntoXML(catalog);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));

            Document document = builder.parse(is);

            HtmlBuilder html = new HtmlBuilder();
            html.addPageHeader(title);

            final String fileName = xsltDir +File.separator + webpage + ".xslt";
            StreamSource stylesource = new StreamSource(new FileReader(fileName));

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(stylesource);

            DOMSource source = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            String resultStr = writer.getBuffer().toString();
            resultStr = resultStr.replaceFirst("<META.*>" + System.getProperty("line.separator"), "");

            return resultStr;
    }

    @Override
    public String getUri() {
        if (id != null) {
            return stylesheet.replace(".html", "?id=") + id + ";type=html";
        } else {
            return stylesheet.replace(".html", "") + "?type=html";
        }
    }

    public String buildPage() {
        try {
            return buildStylesheetResponse(object, title, stylesheet);
        } catch (Exception e) {
            logger.error("Unable to generate page " + stylesheet, e);
            return "";
        }
    }
}
