package Iplayer

String details = GET_WEB_PAGE(url);

metaurl = REPLACE_LINK_PREFIX(url, "http://www.bbc.co.uk/iplayer/episode/", "http://www.bbc.co.uk/iplayer/playlist/");


String metadetails = GET_WEB_PAGE(metaurl);


// PROGRAMME TITLE
details2 = MOVE_TO("<passionSite ", metadetails)
details2 = MOVE_TO(">", details2)
String seriesTitle = EXTRACT_TO("<", details2)
seriesTitle = REMOVE_HTML_TAGS(seriesTitle);
episode.setProgrammeTitle(seriesTitle);
LOG_INFO("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
details2 = MOVE_TO("<title>", metadetails)
String title = EXTRACT_TO("<", details2)
title = REMOVE_HTML_TAGS(title);
episode.setEpisodeTitle(title);
LOG_INFO("EpisodeTitle: " + title)

// ID
episode.setId(MAKE_ID(title))

// SYNOPSIS
details2 = MOVE_TO("<summary>", metadetails)
String desc = EXTRACT_TO("<", details2)
desc = REMOVE_HTML_TAGS(desc);
episode.setDescription(desc);
LOG_INFO("Synopsis: " + desc)

// IMAGE URL
details2 = MOVE_TO("<link rel=\"holding\"", metadetails)
details2 = MOVE_TO("href=\"", details2)
String image = EXTRACT_TO("\"", details2)
image = REMOVE_HTML_TAGS(image);
image = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", image);
episode.setIconUrl(image);
LOG_INFO("Icon: " + image)

// TUNE URL
details2 = MOVE_TO("<link rel=\"alternate\"", metadetails)
details2 = MOVE_TO("href=\"", details2)
String tuneurl = EXTRACT_TO("\"", details2)
tuneurl = REMOVE_HTML_TAGS(tuneurl);
tuneurl = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", tuneurl);
episode.setServiceUrl(tuneurl);
LOG_INFO("URL: " + tuneurl)

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

if (genre == null) genre = "Other";
String[] genres = genre.split(",");
for (String genreInstance : genres) {
    episode.addGenre(genreInstance);
    LOG_INFO("Category: " + genreInstance);
}

// CHANNEL
details2 = MOVE_TO("<service id=", metadetails)
details2 = MOVE_TO(">", details2)
String channel = EXTRACT_TO("<", details2)
channel = REMOVE_HTML_TAGS(channel);
if (channel == null) channel="BBC";
episode.setChannel(channel);
LOG_INFO("Channel: " + channel)

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

if (seriesNumber == null) seriesNumber = "";
episode.setSeries(seriesNumber);
LOG_INFO("Series: " + seriesNumber)

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


if (episodeNo == null) episodeNo = "";
episode.setEpisode(episodeNo);
LOG_INFO("Episode: " + episodeNo)


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
    time = EXTRACT_TO(" ", details3);
    time = REMOVE_HTML_TAGS(time);

    date = MOVE_TO(" ", details3);
    date = REMOVE_HTML_TAGS(date);
}

if (date == null) date = "";
LOG_INFO("Date: " + date)
episode.setAirDate(date)

if (time == null) time = ""
LOG_INFO("Time: " + time)
episode.setAirTime(time)

return episode;
