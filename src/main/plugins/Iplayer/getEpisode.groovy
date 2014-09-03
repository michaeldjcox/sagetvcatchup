package Iplayer

String details = GET_WEB_PAGE(url);

metaurl = REPLACE_LINK_PREFIX(url, "http://www.bbc.co.uk/iplayer/episode/", "http://www.bbc.co.uk/iplayer/playlist/");

String metadetails = GET_WEB_PAGE(metaurl);

// PROGRAMME TITLE
details2 = MOVE_TO("<passionSite ", metadetails)
details2 = MOVE_TO(">", details2)
String seriesTitle = EXTRACT_TO("<", details2)
seriesTitle = REMOVE_HTML_TAGS(seriesTitle);
if (seriesTitle != null) {
    episode.setProgrammeTitle(seriesTitle);
    LOG_INFO("SeriesTitle: " + seriesTitle)
} else {
    LOG_ERROR(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Series title not found");
}


// EPISODE TITLE
details2 = MOVE_TO("<title>", metadetails)
String title = EXTRACT_TO("<", details2)
title = REMOVE_HTML_TAGS(title);

if (title != null) {
    episode.setEpisodeTitle(title);
    LOG_INFO("EpisodeTitle: " + title)
} else {
    LOG_ERROR(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Episode title not found");
}

// ID
if (title != null) {
    episode.setId(MAKE_ID(title))
} else {
    episode.setId(MAKE_ID(url));
}

// SYNOPSIS
details2 = MOVE_TO("<summary>", metadetails)
String desc = EXTRACT_TO("<", details2)
desc = REMOVE_HTML_TAGS(desc);

if (desc != null) {
    episode.setDescription(desc);
    LOG_INFO("Synopsis: " + desc);
} else {
    LOG_ERROR(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Episode description not found" );
}

// IMAGE URL
details2 = MOVE_TO("<link rel=\"holding\"", metadetails)
details2 = MOVE_TO("href=\"", details2)
String image = EXTRACT_TO("\"", details2)
image = REMOVE_HTML_TAGS(image);
image = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", image);

if (image != null) {
    episode.setIconUrl(image);
    LOG_INFO("Icon: " + image);
} else {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Episode image not found" );
}

// TUNE URL
details2 = MOVE_TO("<link rel=\"alternate\"", metadetails)
details2 = MOVE_TO("href=\"", details2)
String tuneurl = EXTRACT_TO("\"", details2)
tuneurl = REMOVE_HTML_TAGS(tuneurl);
tuneurl = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", tuneurl);

if (tuneurl != null) {
    episode.setServiceUrl(tuneurl);
    LOG_INFO("URL: " + tuneurl);
} else {
    LOG_ERROR(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Tuning URL not found" );
}

// CHANNEL
details2 = MOVE_TO("<service id=", metadetails)
details2 = MOVE_TO(">", details2)
String channel = EXTRACT_TO("<", details2)
channel = REMOVE_HTML_TAGS(channel);
if (channel == null) {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Channel not found - defaulting to 'BBC'" );
    channel="BBC";
}
episode.setChannel(channel);
LOG_INFO("Channel: " + channel)

// CATEGORY
details2 = MOVE_TO("<ul class=\"categories\"", details)
details2 = MOVE_TO("<a href=\"/iplayer/tv/categories/", details2)
String genre = EXTRACT_TO("\"", details2)
genre = REMOVE_HTML_TAGS(genre)
if (genre != null) {
    genre = genre.replace("&", " and ")
    genre = genre.replace("_", " ");
}

if (genre == null) {
    details2 = MOVE_TO("<passionSite href=\"", metadetails);
    details2 = EXTRACT_TO("\"", details2);
    String programmeDetails = GET_WEB_PAGE(details2);
    programmeDetails = MOVE_TO("By genre:", programmeDetails);
    programmeDetails = MOVE_TO("<li", programmeDetails);
    programmeDetails = EXTRACT_TO("</li", programmeDetails);

    String readGenres = "";
    while (programmeDetails != null) {
        programmeDetails = MOVE_TO("<a href=\"", programmeDetails);
        programmeDetails = MOVE_TO(">", programmeDetails);
        String newGenre = EXTRACT_TO("<", programmeDetails);
        if (newGenre != null) {
            readGenres = readGenres + newGenre;
            readGenres = readGenres + ",";
        }
    }
    if (readGenres != "") {
        genre = readGenres.substring(0, readGenres.lastIndexOf(","));
    }
}

if (genre == null && channel != null) {
    if (channel.equals("CBeebies") || channel.equals("CBBC")) {
        genre="Childrens";
    }
}

if (genre == null) {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Genres not found - defaulting to 'Other'" );
    genre = "Other";
}

String[] genres = genre.split(",");
for (String genreInstance : genres) {
    episode.addGenre(genreInstance);
    LOG_INFO("Genre: " + genreInstance);
}



// SERIES
details2 = MOVE_TO("<programmeSeries id=", metadetails)
details2 = MOVE_TO(">", details2)
String seriesNumber = EXTRACT_TO("<", details2)
seriesNumber = REMOVE_HTML_TAGS(seriesNumber);
if (seriesNumber != null) {
    seriesNumber = seriesNumber.replace("Series ", "");
}

if (seriesNumber == null) {
    String series = MOVE_TO("series-", url);
    seriesNumber = EXTRACT_TO("-", series);
}

if (seriesNumber == null) {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Series number not found");
    seriesNumber = ""
} else {
    if (!seriesNumber.matches("[0-9]*")) {
        LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Series number '"+ seriesNumber+ "' is not a number");
    }
    LOG_INFO("Series: " + seriesNumber)
}

episode.setSeries(seriesNumber);

// EPISODE
details2 = MOVE_TO("<div class=\"field-episode-number\"", details)
details2 = MOVE_TO("<div class=\"field-item even\">", details2)
String episodeNo = EXTRACT_TO("<", details2)
episodeNo = REMOVE_HTML_TAGS(episodeNo);

if (episodeNo == null) {
    series = MOVE_TO("series-", url);
    series = MOVE_TO("-", series);
    if (series != null && series.matches("[0-9]*\\-.*")) {
        episodeNo = EXTRACT_TO("-", series);
    }
}


if (episodeNo == null) {
    series = MOVE_TO("episode-", url);
    if (series != null && series.matches("[0-9]*")) {
        episodeNo = series;
    } else
    if (series != null && series.matches("[0-9]*-.*")) {
        episodeNo = EXTRACT_TO("-", series);
    }
}

if (episodeNo == null) {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Episode number not found");
    episodeNo = "";
} else {
    if (!episodeNo.matches("[0-9]*")) {
        LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), metaurl, "Episode number '"+episodeNo+"' is not a number");
    }
    LOG_INFO("Episode: " + episodeNo);
}
episode.setEpisode(episodeNo);


//AIRING DATE AND TIME
details2 = MOVE_TO("<li class=\"last_broadcast_date\">", details)
details2 = MOVE_TO("<span>", details2)
details2 = EXTRACT_TO("<", details2)
//  <span>BBC Two, 9:15AM Sun, 10 Mar 2013</span>
details3 = MOVE_TO(", ", details2);

String time=null;
String date = null;

if (details3 != null) {
    date = MOVE_TO(" ", details3);
    date = REMOVE_HTML_TAGS(date);
    time = EXTRACT_TO(" ", details3);
    time = REMOVE_HTML_TAGS(time);
}

if (details3 == null) {
    details2 = MOVE_TO("<span class=\"release\">", details);
    details3 = EXTRACT_TO("</span>", details2);
    details3 = MOVE_TO(": ", details3);

    timeDate = SPLIT(details3, " ");

    if (timeDate.length <=3) {
        date = REMOVE_HTML_TAGS(details3);
        time = null;
    } else {
        time = EXTRACT_TO(" ", details3);
        time = REMOVE_HTML_TAGS(time);

        date = MOVE_TO(" ", details3);
        date = REMOVE_HTML_TAGS(date);
    }
}

if (date == null) {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), url, "Original air date not found");
    date = "";
} else {
    LOG_INFO("Date: " + date)
    }

episode.setAirDate(date)

if (time == null) {
    LOG_WARNING(episode, "Iplayer", programme.getId(), episode.getEpisodeTitle(), url, "Original air time not found");
    time = "";
} else {
    LOG_INFO("Time: " + time)
}
episode.setAirTime(time);

return episode;
