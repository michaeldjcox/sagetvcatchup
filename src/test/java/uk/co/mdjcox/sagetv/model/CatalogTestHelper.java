package uk.co.mdjcox.sagetv.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by michael on 09/10/14.
 */
public class CatalogTestHelper {

    public static Catalog getTestCatalog() {
        Catalog catalog = new Catalog();
        Root root = new Root("rootId", "rootShort", "rootLong", "rootServiceUrl", "iconUrl");

        root.addError("ERROR", "Root Error");
        root.addMetaUrl("metaUrl1");
        root.addMetaUrl("metaUrl2");

        Source source = new Source("rootId", "sourceId", "sourceShortName", "sourceLongName",
                "sourceServiceUrl", "sourceIconUrl");
        source.addMetaUrl("metaUrl1");
        source.addMetaUrl("metaUrl2");

        source.addError("ERROR", "Source Error");

        SubCategory subCategory1 = new SubCategory("sourceId", "subCatId1", "subCatShortName1", "subCatLongName",
                "subcatServiceUrl1", "subcatIconUrl1", "sourceId");

        subCategory1.addMetaUrl("metaUrl1");
        subCategory1.addMetaUrl("metaUrl2");

        SubCategory subCategory2 = new SubCategory("sourceId", "subCatId2", "subCatShortName2", "subCatLongName",
                "subcatServiceUrl2", "subcatIconUrl2", "sourceId");

        source.addSubCategory(subCategory1);
        source.addSubCategory(subCategory2);
        subCategory2.addMetaUrl("metaUrl1");
        subCategory2.addMetaUrl("metaUrl2");


        subCategory1.addSubCategory(subCategory2);

        subCategory2.addOtherParentId(subCategory1.getId());

        subCategory1.addError("ERROR", "SubCat1 Error");
        subCategory2.addError("ERROR", "SubCat2 Error");


        Programme programme = new Programme("sourceId", "programmeId", "programmeShortName",
                "programmeLongname", "programmeServiceUrl", "programmeIconUrl", "");

        programme.setPodcastUrl("/programme?id=programmeId;type=xml");
        programme.addMetaUrl("metaUrl1");
        programme.addMetaUrl("metaUrl2");

        Set<String> genres1 = new HashSet<String>();
        genres1.add("genre1");
        genres1.add("genre2");

        programme.addError("ERROR", "Programme Error");

        Episode ep1 = new Episode("sourceId", "episodeId1", "programmeTitle1", "seriesTitle1", "episodeTitle1",
                "series1", "episode1", "description1", "episodeIconUrl1", "episodeServiceUrl1", "airDate1",
                "airTime1", "origAirDate1", "origAirTime1", "channel1", genres1);
      ep1.setPodcastUrl("podcastUrl1");

        Set<String> genres2 = new HashSet<String>();
        genres2.add("genre3");
        genres2.add("genre4");

        Episode ep2 = new Episode("sourceId", "episodeId2", "programmeTitle2", "seriesTitle2", "episodeTitle2",
                "series2", "episode2", "description2", "episodeIconUrl2", "episodeServiceUrl2", "airDate2",
                "airTime2", "origAirDate2", "origAirTime2", "channel2", genres2);
      ep2.setPodcastUrl("podcastUrl1");

        programme.addEpisode(ep1);
        programme.addEpisode(ep2);
        ep1.addMetaUrl("metaUrl1");
        ep1.addMetaUrl("metaUrl2");

        ep1.addError("ERROR", "Episode1 Error");
        ep1.addError("ERROR", "Episode2 Error");

        catalog.addRoot(root);
        catalog.addSource(source);
        catalog.addSubCategory(subCategory1);
        catalog.addSubCategory(subCategory2);
        catalog.addProgramme(programme);
        catalog.addEpisode(ep1);
        catalog.addEpisode(ep2);
        catalog.getRoot().addError("ERROR", "Catalog Error");
        return catalog;
    }
}
