package Test

import uk.co.mdjcox.sagetv.model.Episode

String link = "/episode?id=testEpisode;type=html"

    Episode subCat = new Episode(
            source.getSourceId(),
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
            "", // origAirDate
            "", // origAirTime
            "", // channel
            new HashSet(), // genre
    );

    episodes.add(subCat);




