package Iplayer


String pid = REPLACE_LINK_PREFIX(url, "http://www.bbc.co.uk/iplayer/episode/", "");
pid = REPLACE_LINK_TARGET(pid, "");
String metaurl = "http://www.bbc.co.uk/programmes/" + pid + ".xml";
String programmeUrl = REPLACE_LINK_PREFIX(url, "http://www.bbc.co.uk/iplayer/episode/", "http://www.bbc.co.uk/iplayer/playlist/");

String[] urls = [url, metaurl, programmeUrl];

String metadetails = GET_WEB_PAGE(metaurl);
String programmeDetails = GET_WEB_PAGE(programmeUrl);

// EPISODE TITLE
String details = MOVE_TO("<title>", metadetails)
String title = EXTRACT_TO("<", details)
title = REMOVE_HTML_TAGS(title);

if (title != null) {
    episode.setEpisodeTitle(title);
    LOG_INFO("EpisodeTitle: " + title)
} else {
    LOG_ERROR(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Episode title not found", urls);
}

// SERIES TITLE

details = MOVE_TO("<programme type=\"series\">", metadetails);
details = MOVE_TO("<title>", details);
String seriesTitle = EXTRACT_TO("<", details)
seriesTitle = REMOVE_HTML_TAGS(seriesTitle);

if (seriesTitle != null) {
    episode.setSeriesTitle(seriesTitle);
    LOG_INFO("SeriesTitle: " + seriesTitle)
} else {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Series title not found", urls);
}

//details2 = MOVE_TO("<passionSite ", metadetails)
//details2 = MOVE_TO(">", details2)
//String seriesTitle = EXTRACT_TO("<", details2)
//seriesTitle = REMOVE_HTML_TAGS(seriesTitle);
//if (seriesTitle != null) {
//    episode.setProgrammeTitle(seriesTitle);
//    LOG_INFO("SeriesTitle: " + seriesTitle)
//} else {
//    LOG_ERROR(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Series title not found");
//}




// ID
if (title != null) {
    episode.setId(MAKE_ID(title))
} else {
    episode.setId(MAKE_ID(url));
}

// SYNOPSIS
details = MOVE_TO("<long_synopsis>", metadetails)
String desc = EXTRACT_TO("<", details)
desc = REMOVE_HTML_TAGS(desc);

if (desc == null) {
    details = MOVE_TO("<medium_synopsis>", metadetails)
    desc = EXTRACT_TO("<", details)
    desc = REMOVE_HTML_TAGS(desc);
}

if (desc == null) {
    details = MOVE_TO("<short_synopsis>", metadetails)
    desc = EXTRACT_TO("<", details)
    desc = REMOVE_HTML_TAGS(desc);
}

if (desc != null) {
    episode.setDescription(desc);
    LOG_INFO("Synopsis: " + desc);
} else {
    LOG_ERROR(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Episode description not found", urls );
}

// IMAGE URL
details = MOVE_TO("<image>", metadetails);
details = MOVE_TO("<pid>", details);
String image = EXTRACT_TO("<", details);
image = REMOVE_HTML_TAGS(image);
image = "http://ichef.bbci.co.uk/images/ic/272x153/" + image + ".jpg";

if (image != null) {
    episode.setIconUrl(image);
    LOG_INFO("Icon: " + image);
} else {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Episode image not found", urls );
}

// TUNE URL
details = MOVE_TO("<link rel=\"alternate\"", programmeDetails)
details = MOVE_TO("href=\"", details)
String tuneurl = EXTRACT_TO("\"", details)
tuneurl = REMOVE_HTML_TAGS(tuneurl);
tuneurl = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", tuneurl);

if (tuneurl != null) {
    episode.setServiceUrl(tuneurl);
    LOG_INFO("URL: " + tuneurl);
} else {
    LOG_ERROR(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Tuning URL not found", urls );
}

// CHANNEL
details = MOVE_TO("<service type=", metadetails)
details = MOVE_TO("<title>", details)
String channel = EXTRACT_TO("<", details)
channel = REMOVE_HTML_TAGS(channel);
if (channel == null) {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Channel not found - defaulting to 'BBC'", urls );
    channel="BBC";
}
episode.setChannel(channel);
LOG_INFO("Channel: " + channel)

// CATEGORY
details = MOVE_TO("<categories>", metadetails);
String genres = EXTRACT_TO("</categories>", details);

while (genres != null) {
    genres = MOVE_TO("<category type=\"genre", genres);
    genres = MOVE_TO("<title>", genres);
    genre = EXTRACT_TO("</title>", genres);
    genre = REMOVE_HTML_TAGS(genre);
    if (genre != null && !genre.isEmpty()) {
        genre = genre.replace(" & ", " and ");
        genre = genre.replace("&", " and ");
        genre = genre.replace("_", " ");
        LOG_INFO("Genre: " + genre);
        episode.addGenre(genre);
    }
}

if (episode.getGenres().size() == 0) {
        LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Genres not found - defaulting to 'Other'", urls );
        episode.addGenre("Other");
}

//genre = REMOVE_HTML_TAGS(genre)
//if (genre != null) {
//    genre = genre.replace("&", " and ")
//    genre = genre.replace("_", " ");
//}

//if (genre == null) {
//    details2 = MOVE_TO("<passionSite href=\"", metadetails);
//    details2 = EXTRACT_TO("\"", details2);
//    String programmeDetails = GET_WEB_PAGE(details2);
//    programmeDetails = MOVE_TO("By genre:", programmeDetails);
//    programmeDetails = MOVE_TO("<li", programmeDetails);
//    programmeDetails = EXTRACT_TO("</li", programmeDetails);
//
//    String readGenres = "";
//    while (programmeDetails != null) {
//        programmeDetails = MOVE_TO("<a href=\"", programmeDetails);
//        programmeDetails = MOVE_TO(">", programmeDetails);
//        String newGenre = EXTRACT_TO("<", programmeDetails);
//        if (newGenre != null) {details2 = MOVE_TO("<a href=\"/iplayer/tv/categories/", details2)
//
//            readGenres = readGenres + newGenre;
//            readGenres = readGenres + ",";
//        }
//    }
//    if (readGenres != "") {
//        genre = readGenres.substring(0, readGenres.lastIndexOf(","));
//    }
//}

//if (genre == null && channel != null) {
//    if (channel.equals("CBeebies") || channel.equals("CBBC")) {
//        genre="Childrens";
//    }
//}



//String[] genres = genre.split(",");
//for (String genreInstance : genres) {
//    episode.addGenre(genreInstance);
//    LOG_INFO("Genre: " + genreInstance);
//}



// SERIES
details = MOVE_TO("<programme type=\"series\">", metadetails)
details = MOVE_TO("<position>", details)
String seriesNumber = EXTRACT_TO("<", details)
seriesNumber = REMOVE_HTML_TAGS(seriesNumber);
//if (seriesNumber != null) {
//    seriesNumber = seriesNumber.replace("Series ", "");
//}

//if (seriesNumber == null) {
//    String series = MOVE_TO("series-", url);
//    seriesNumber = EXTRACT_TO("-", series);
//}

if (seriesNumber != null) {
    if (!seriesNumber.matches("[0-9]*")) {
        LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Series number '"+ seriesNumber+ "' is not a number", urls);
    }
    LOG_INFO("Series: " + seriesNumber)
    episode.setSeries(seriesNumber);
} else {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Series number not found", urls);
}


// EPISODE
details = MOVE_TO("<position>", metadetails)
String episodeNo = EXTRACT_TO("</position", details)
episodeNo = REMOVE_HTML_TAGS(episodeNo);

//if (episodeNo == null) {
//    series = MOVE_TO("series-", url);
//    series = MOVE_TO("-", series);
//    if (series != null && series.matches("[0-9]*\\-.*")) {
//        episodeNo = EXTRACT_TO("-", series);
//    }
//}


//if (episodeNo == null) {
//    series = MOVE_TO("episode-", url);
//    if (series != null && series.matches("[0-9]*")) {
//        episodeNo = series;
//    } else
//    if (series != null && series.matches("[0-9]*-.*")) {
//        episodeNo = EXTRACT_TO("-", series);
//    }
//}

if (episodeNo != null) {
    if (!episodeNo.matches("[0-9]*")) {
        LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Episode number '"+episodeNo+"' is not a number", urls);
    }
    LOG_INFO("Episode: " + episodeNo);
    episode.setEpisode(episodeNo);
} else {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Episode number not found", urls);
}


//AIRING DATE AND TIME
details = MOVE_TO("<first_broadcast_date>", metadetails)
details = EXTRACT_TO("</first_broadcast_date>", details)
//details2 = EXTRACT_TO("<", details2)
//  <span>BBC Two, 9:15AM Sun, 10 Mar 2013</span>
//details3 = MOVE_TO(", ", details2);

//     <first_broadcast_date>2014-08-28T22:00:00+01:00</first_broadcast_date>


String time=null;
String date = null;

if (details != null) {
    date = EXTRACT_TO("T", details);
    date = REMOVE_HTML_TAGS(date);
    time = MOVE_TO("T", details);
    String time2 = EXTRACT_TO("+", time);
    if (time2 == null) {
        time2 = EXTRACT_TO("Z", time);
    }
    time = REMOVE_HTML_TAGS(time2);
}
//
//if (details3 == null) {
//    details2 = MOVE_TO("<span class=\"release\">", details);
//    details3 = EXTRACT_TO("</span>", details2);
//    details3 = MOVE_TO(": ", details3);
//
//    timeDate = SPLIT(details3, " ");
//
//    if (timeDate.length <=3) {
//        date = REMOVE_HTML_TAGS(details3);
//        time = null;
//    } else {
//        time = EXTRACT_TO(" ", details3);
//        time = REMOVE_HTML_TAGS(time);
//
//        date = MOVE_TO(" ", details3);
//        date = REMOVE_HTML_TAGS(date);
//    }
//}

if (date != null) {
    LOG_INFO("Date: " + date)
    episode.setAirDate(date)
} else {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Original air date not found", urls);
}


if (time != null) {
    LOG_INFO("Time: " + time)
    episode.setAirTime(time);
} else {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), "Original air time not found", urls);
}

return episode;
