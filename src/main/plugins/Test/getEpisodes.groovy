package Test

import uk.co.mdjcox.sagetv.model.Episode

String link = "http://localhost:" + GET_INT_PROPERTY("podcasterPort") + "/testEpisode"

    Episode subCat = new Episode(
            source.getId(),
            "", // id
            category.getShortName(), //programmeTitle
            "", //seriesTitle
            MAKE_ID(link), //episodeTitle
            "", // series
            "", // episode
            "", //description
            "", // iconUrl
            link,
            "", // airDate
            "", // airTime
            "", // channel
            new HashSet(), // genre
    );

    category.addEpisode(subCat);




