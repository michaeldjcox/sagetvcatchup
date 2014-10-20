package uk.co.mdjcox.sagetv.catchup.server.pages;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.co.mdjcox.sagetv.catchup.CatalogPersister;
import uk.co.mdjcox.sagetv.catchup.CatchupTestModule;
import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.HtmlBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by michael on 09/10/14.
 */
public class StyledPageTest {

    private CatchupTestModule module = new CatchupTestModule();
    private Injector injector = Guice.createInjector(module);


    @Test
    public void testEpisodesTransformation() throws Exception {

        final String title = "Episodes";
        final String webpage = "episodes.html";

        testTransformation(title, webpage);
    }

    @Test
    public void testProgrammesTransformation() throws Exception {
        final String title = "Programmes";
        final String webpage = "programmes.html";

        testTransformation(title, webpage);
    }

    @Test
    public void testCategoriesTransformation() throws Exception {
        final String title = "Categories";
        final String webpage = "categories.html";

        testTransformation(title, webpage);
    }

    @Test
    public void testEpisodeTransformation() throws Exception {

        final String title = "Episode";
        final String webpage = "episode.html";
        final String id = "episodeId1";

        testSingleEpisodeTransformation(title, webpage, id);
    }

    @Test
    public void testProgrammeTransformation() throws Exception {
        final String title = "Programme";
        final String webpage = "programme.html";
        final String id = "programmeId";

        testSingleCategoryTransformation(title, webpage, id);
    }

    @Test
    public void testCategoryTransformation() throws Exception {
        final String title = "Category";
        final String webpage = "category.html";
        final String id = "subCatId1";

        testSingleCategoryTransformation(title, webpage, id);
    }


    @Test
    public void testRootTransformation() throws Exception {
        final String title = "Category";
        final String webpage = "category.html";
        final String id = "rootId";

        testSingleCategoryTransformation(title, webpage, id);
    }

    private void testTransformation(String title, String webpage) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        Catalog catalog = CatalogTestHelper.getTestCatalog();

        Map<String,String> webPages = buildTestHtml(catalog);

        CatalogPersister persister = injector.getInstance(CatalogPersister.class);
        String xml = persister.parseIntoXML(catalog);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));

        Document document = builder.parse(is);

        HtmlBuilder html = new HtmlBuilder();
        html.addPageHeader(title);

        final String fileName = "src"+ File.separator + "main"+File.separator + "xslt"+File.separator + webpage + ".xslt";
        StreamSource stylesource = new StreamSource(new FileReader(fileName));

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(stylesource);

        DOMSource source = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);

        String resultStr = writer.getBuffer().toString();
        resultStr = resultStr.replaceFirst("<META.*>\n", "");

        assertEquals(title, webPages.get(webpage), resultStr);
    }

    private void testSingleCategoryTransformation(String title, String webpage, String id) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        Catalog catalog = CatalogTestHelper.getTestCatalog();

        Category category = catalog.getCategory(id);

        Map<String,String> webPages = buildTestHtml(catalog);

        CatalogPersister persister = injector.getInstance(CatalogPersister.class);
        String xml = persister.parseIntoXML(category);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));

        Document document = builder.parse(is);

        HtmlBuilder html = new HtmlBuilder();
        html.addPageHeader(title);

        final String fileName = "src"+ File.separator + "main"+File.separator + "xslt"+File.separator + webpage + ".xslt";
        StreamSource stylesource = new StreamSource(new FileReader(fileName));

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(stylesource);

        DOMSource source = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);

        String resultStr = writer.getBuffer().toString();
        resultStr = resultStr.replaceFirst("<META.*>\n", "");

        assertEquals(title, webPages.get(webpage.replace(".", "-" + id + ".")), resultStr);
    }

    private void testSingleEpisodeTransformation(String title, String webpage, String id) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        Catalog catalog = CatalogTestHelper.getTestCatalog();

        Episode episode = catalog.getEpisode(id);

        Map<String,String> webPages = buildTestHtml(catalog);

        CatalogPersister persister = injector.getInstance(CatalogPersister.class);
        String xml = persister.parseIntoXML(episode);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));

        Document document = builder.parse(is);

        HtmlBuilder html = new HtmlBuilder();
        html.addPageHeader(title);

        final String fileName = "src"+ File.separator + "main"+File.separator + "xslt"+File.separator + webpage + ".xslt";
        StreamSource stylesource = new StreamSource(new FileReader(fileName));

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(stylesource);

        DOMSource source = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);

        String resultStr = writer.getBuffer().toString();
        resultStr = resultStr.replaceFirst("<META.*>\n", "");

        assertEquals(title, webPages.get(webpage.replace(".", "-" + id + ".")), resultStr);
    }

    private Map<String, String> buildTestHtml(Catalog catalog) {

        Map<String, String> webPages = new HashMap<String, String>();
        HtmlBuilder categoriesBuilder = new HtmlBuilder();
        HtmlBuilder programmesBuilder = new HtmlBuilder();
        HtmlBuilder episodesBuilder = new HtmlBuilder();

        programmesBuilder.startDocument();
        programmesBuilder.addPageHeader("Programmes");
        programmesBuilder.startBody();
        programmesBuilder.addHeading1("Programmes");
        programmesBuilder.startTable();
        programmesBuilder.addTableHeader("SourceId", "ParentId", "Type", "Id", "ShortName", "LongName", "ServiceUrl", "IconUrl", "PodcastUrl");

        categoriesBuilder.startDocument();
        categoriesBuilder.addPageHeader("Categories");
        categoriesBuilder.startBody();
        categoriesBuilder.addHeading1("Categories");
        categoriesBuilder.startTable();
        categoriesBuilder.addTableHeader("SourceId", "ParentId", "Type", "Id", "ShortName", "LongName", "ServiceUrl", "IconUrl", "PodcastUrl");

        episodesBuilder.startDocument();
        episodesBuilder.addPageHeader("Episodes");
        episodesBuilder.startBody();
        episodesBuilder.addHeading1("Episodes");
        episodesBuilder.startTable();
        episodesBuilder.addTableHeader("SourceId", "Id", "Channel", "ProgrammeTitle", "Series", "SeriesTitle", "Episode", "EpisodeTitle", "Description", "PodcastTitle", "AirDate", "AirTime", "OrigAirDate", "OrigAirTime", "ServiceUrl", "IconUrl", "PodcastUrl" );


        for (Category cat : catalog.getCategories()) {
            String detailStr = buildDetailsFor(cat);
            if (cat.isProgrammeCategory() && cat.getParentId().isEmpty()) {
                webPages.put("programme-" + cat.getId() + ".html", detailStr);
                Programme prog = (Programme)cat;
                HtmlBuilder linkBuilder1 = new HtmlBuilder();
                linkBuilder1.addLink(cat.getServiceUrl(), cat.getServiceUrl());
                HtmlBuilder linkBuilder2 = new HtmlBuilder();
                linkBuilder2.addLink(cat.getIconUrl(), cat.getIconUrl());
                HtmlBuilder linkBuilder3 = new HtmlBuilder();
                linkBuilder3.addLink(prog.getPodcastUrl(), prog.getPodcastUrl());
                HtmlBuilder linkBuilder4 = new HtmlBuilder();
                String link = "/programme?id=" + prog.getId() + ";type=html";
                linkBuilder4.addLink(prog.getId(), link);

                programmesBuilder.addTableRow(prog.getSourceId(), prog.getParentId(), prog.getType(), linkBuilder4.toString(), prog.getShortName(), prog.getLongName(), linkBuilder1.toString(), linkBuilder2.toString(),linkBuilder3.toString());
            } else {
                webPages.put("category-" + cat.getId() + ".html", detailStr);
                HtmlBuilder linkBuilder1 = new HtmlBuilder();
                linkBuilder1.addLink(cat.getServiceUrl(), cat.getServiceUrl());
                HtmlBuilder linkBuilder2 = new HtmlBuilder();
                linkBuilder2.addLink(cat.getIconUrl(), cat.getIconUrl());
                HtmlBuilder linkBuilder3 = new HtmlBuilder();
                linkBuilder3.addLink(cat.getPodcastUrl(), cat.getPodcastUrl());

                HtmlBuilder linkBuilder4 = new HtmlBuilder();
                String link = "/category?id=" + cat.getId() + ";type=html";
                linkBuilder4.addLink(cat.getId(), link);

                categoriesBuilder.addTableRow(cat.getSourceId(), cat.getParentId(), cat.getType(), linkBuilder4.toString(), cat.getShortName(), cat.getLongName(), linkBuilder1.toString(), linkBuilder2.toString(), linkBuilder3.toString());

            }

            if (cat.isProgrammeCategory() && cat.getParentId().isEmpty()) {
                for (String episodeId : ((Programme)cat).getEpisodes()) {
                    Episode ep = catalog.getEpisode(episodeId);
                    String detailStr2 = buildDetailsFor(ep);
                    webPages.put("episode-" + ep.getId() + ".html", detailStr2);
                    HtmlBuilder linkBuilder1 = new HtmlBuilder();
                    linkBuilder1.addLink(ep.getServiceUrl(), ep.getServiceUrl());
                    HtmlBuilder linkBuilder2 = new HtmlBuilder();
                    linkBuilder2.addLink(ep.getIconUrl(), ep.getIconUrl());
                  HtmlBuilder linkBuilder3 = new HtmlBuilder();
                  linkBuilder3.addLink(ep.getPodcastUrl(), ep.getPodcastUrl());

                    HtmlBuilder linkBuilder4 = new HtmlBuilder();
                    String link = "/episode?id=" + ep.getId() + ";type=html";
                    linkBuilder4.addLink(ep.getId(), link);

                    episodesBuilder.addTableRow(ep.getSourceId(), linkBuilder4.toString(), ep.getChannel(), ep.getProgrammeTitle(), ep.getSeries(), ep.getSeriesTitle(), ep.getEpisode(), ep.getEpisodeTitle(), ep.getDescription(), ep.getPodcastTitle(), ep.getAirDate(), ep.getAirTime(), ep.getOrigAirDate(), ep.getOrigAirTime(), linkBuilder1.toString(), linkBuilder2.toString(), linkBuilder3.toString() );
                }
            }
        }

        programmesBuilder.stopTable();
        programmesBuilder.stopBody();
        programmesBuilder.stopDocument();

        categoriesBuilder.stopTable();
        categoriesBuilder.stopBody();
        categoriesBuilder.stopDocument();


        episodesBuilder.stopTable();
        episodesBuilder.stopBody();
        episodesBuilder.stopDocument();

        String programmeResponse = programmesBuilder.toString();
        webPages.put("programmes.html", programmeResponse);

        String categoryResponse = categoriesBuilder.toString();
        webPages.put("categories.html", categoryResponse);

        String episodesResponse = episodesBuilder.toString();
        webPages.put("episodes.html", episodesResponse);

        return webPages;
    }

    private String buildDetailsFor(Episode cat) {
        String pageTitle = "Episode: " + cat.getPodcastTitle();
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader(pageTitle);
        htmlBuilder.startBody();
        htmlBuilder.addHeading1(pageTitle);

        htmlBuilder.addHeading2("Details");

        htmlBuilder.startTable();
        htmlBuilder.addTableHeader("Field", "Value");

        Map<String,String> data = new LinkedHashMap<String, String>();
        data.put("SourceId", cat.getSourceId());
        data.put("Type", cat.getClass().getSimpleName());
        data.put("Id", cat.getId());
        data.put("Channel", cat.getChannel());
        data.put("ProgrammeTitle", cat.getProgrammeTitle());
        data.put("SeriesTitle", cat.getSeriesTitle());
        data.put("EpisodeTitle", cat.getEpisodeTitle());
        data.put("Description", cat.getDescription());
        data.put("Series", cat.getSeries());
        data.put("Episode", cat.getEpisode());
        HtmlBuilder listBuilder = new HtmlBuilder();
        listBuilder.startList();
        for (String genre : cat.getGenres()) {
            listBuilder.addListItem(genre);
        }
        listBuilder.stopList();
        data.put("Genres", listBuilder.toString());
        data.put("AirDate", cat.getAirDate());
        data.put("AirTime", cat.getAirTime());
        data.put("OrigAirDate", cat.getOrigAirDate());
        data.put("OrigAirTime", cat.getOrigAirTime());
        data.put("PodcastTitle", cat.getPodcastTitle());
        HtmlBuilder linkBuilder1 = new HtmlBuilder();
        linkBuilder1.addLink(cat.getIconUrl(), cat.getIconUrl());
        data.put("IconUrl", linkBuilder1.toString());
        HtmlBuilder linkBuilder2 = new HtmlBuilder();
        linkBuilder2.addLink(cat.getServiceUrl(), cat.getServiceUrl());
        data.put("ServiceUrl", linkBuilder2.toString());
      HtmlBuilder linkBuilder3 = new HtmlBuilder();
      linkBuilder3.addLink(cat.getPodcastUrl(), cat.getPodcastUrl());
      data.put("PodcastUrl", linkBuilder3.toString());

        HtmlBuilder metaListBuilder = new HtmlBuilder();
        metaListBuilder.startList();
        for (String sourceUrl : cat.getMetaUrls()) {
            HtmlBuilder linkBuilder = new HtmlBuilder();
            linkBuilder.addLineFeed();
            linkBuilder.addLink(sourceUrl, sourceUrl);
            linkBuilder.addLineFeed();
            metaListBuilder.addListItem(linkBuilder.toString());
        }
        metaListBuilder.stopList();

        data.put("MetaUrls", metaListBuilder.toString());

        for (Map.Entry<String, String> entry : data.entrySet()) {
            htmlBuilder.addTableRow(entry.getKey(), entry.getValue());
        }

        htmlBuilder.stopTable();

        htmlBuilder.addHeading2("Errors");
        htmlBuilder.startTable();
        htmlBuilder.addTableHeader( "Level", "Error");
        for (ParseError error : cat.getErrors()) {
            htmlBuilder.addTableRow(error.getLevel(), error.getMessage());
        }
        htmlBuilder.stopTable();
        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();
        return htmlBuilder.toString();
    }

    private String buildDetailsFor(Category cat) {
        String type = cat.isProgrammeCategory() && cat.getParentId().isEmpty() ? "Programme" : "Category";
        String pageTitle = type + ": " + cat.getLongName();
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.startDocument();
        htmlBuilder.addPageHeader(pageTitle);
        htmlBuilder.startBody();
        htmlBuilder.addHeading1(pageTitle);
        htmlBuilder.addHeading2("Details");
        htmlBuilder.startTable();
        htmlBuilder.addTableHeader("Field", "Value");

        Map<String, String> data = new LinkedHashMap<String, String>();
        data.put("SourceId", cat.getSourceId());
        data.put("Type", cat.getClass().getSimpleName());
        data.put("Id", cat.getId());
        data.put("ParentId", cat.getParentId());
        data.put("ShortName", cat.getShortName());
        data.put("LongName", cat.getLongName());
        HtmlBuilder linkBuilder1 = new HtmlBuilder();
        linkBuilder1.addLink(cat.getIconUrl(), cat.getIconUrl());
        data.put("IconUrl", linkBuilder1.toString());
        HtmlBuilder linkBuilder2 = new HtmlBuilder();
        linkBuilder2.addLink(cat.getServiceUrl(), cat.getServiceUrl());
        data.put("ServiceUrl", linkBuilder2.toString());

//        if (cat instanceof Programme) {
//            Programme prog = (Programme) cat;
        if (cat.getPodcastUrl() != null && !cat.getPodcastUrl().isEmpty()) {
            HtmlBuilder linkBuilder3 = new HtmlBuilder();
            linkBuilder3.addLink(cat.getPodcastUrl(), cat.getPodcastUrl());
            data.put("PodcastUrl", linkBuilder3.toString());
        } else {
            data.put("PodcastUrl", "");

        }
//        }

        HtmlBuilder listBuilder = new HtmlBuilder();
        listBuilder.startList();

        for (String sourceUrl : cat.getMetaUrls()) {
            HtmlBuilder linkBuilder = new HtmlBuilder();
            linkBuilder.addLineFeed();
            linkBuilder.addLink(sourceUrl, sourceUrl);
            linkBuilder.addLineFeed();
            listBuilder.addListItem(linkBuilder.toString());
        }
        listBuilder.stopList();
        data.put("MetaUrls", listBuilder.toString());


        if (cat instanceof Programme) {
            listBuilder = new HtmlBuilder();

            listBuilder.startList();

            for (String episodeId : ((Programme) cat).getEpisodes()) {
                HtmlBuilder linkBuilder = new HtmlBuilder();
                linkBuilder.addLineFeed();
                linkBuilder.addLink(episodeId, "/episode?id=" + episodeId + ";type=html");
                linkBuilder.addLineFeed();
                listBuilder.addListItem(linkBuilder.toString());
            }
            listBuilder.stopList();
            data.put("Episodes", listBuilder.toString());
        }



        for (Map.Entry<String, String> entry : data.entrySet()) {
            htmlBuilder.addTableRow(entry.getKey(), entry.getValue());
        }

        htmlBuilder.stopTable();

        htmlBuilder.addHeading2("Errors");
        htmlBuilder.startTable();
        htmlBuilder.addTableHeader( "Level", "Error");
        for (ParseError error : cat.getErrors()) {
            htmlBuilder.addTableRow(error.getLevel(), error.getMessage());
        }
        htmlBuilder.stopTable();

        htmlBuilder.stopBody();
        htmlBuilder.stopDocument();
        return htmlBuilder.toString();
    }

}
