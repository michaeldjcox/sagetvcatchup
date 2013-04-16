package Iplayer

String details = GET_WEB_PAGE(url);

String pid = MOVE_TO("pid=", details)
pid = EXTRACT_TO("&", pid)

metadetails = GET_WEB_PAGE("http://www.bbc.co.uk/iplayer/playlist/" + pid)

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
if (genre == null) genre = "Other"
genre = genre.replace("&", " and ")
genre = genre.replace("_", " ")
episode.setCategory(genre);
LOG_INFO("Category: " + genre)

// CHANNEL
details2 = MOVE_TO("<service id=", metadetails)
details2 = MOVE_TO(">", details2)
String channel = EXTRACT_TO("<", details2)
channel = REMOVE_HTML_TAGS(channel);
episode.setChannel(channel);
LOG_INFO("Channel: " + channel)

// SERIES
details2 = MOVE_TO("<programmeSeries id=", details)
details2 = MOVE_TO(">", details2)
String seriesNumber = EXTRACT_TO("<", details2)
seriesNumber = REMOVE_HTML_TAGS(seriesNumber);
episode.setSeries(seriesNumber);
LOG_INFO("Series: " + seriesNumber)

// EPISODE
details2 = MOVE_TO("<div class=\"field-episode-number\"", details)
details2 = MOVE_TO("<div class=\"field-item even\">", details2)
String episodeNo = EXTRACT_TO("<", details2)
episodeNo = REMOVE_HTML_TAGS(episodeNo);
episode.setEpisode(episodeNo);
LOG_INFO("Episode: " + episodeNo)

//AIRING DATE
details2 = MOVE_TO("<li class=\"last_broadcast_date\">", details)
details2 = MOVE_TO("<span>", details2)
details2 = EXTRACT_TO("<", details2)
//  <span>BBC Two, 9:15AM Sun, 10 Mar 2013</span>

details3 = MOVE_TO(", ", details2);
String date = MOVE_TO(" ", details3);
date = REMOVE_HTML_TAGS(date);
if (date == null) date = ""
LOG_INFO("Date: " + date)
episode.setAirDate(date)

time = EXTRACT_TO(" ", details3);
time = REMOVE_HTML_TAGS(time);
if (time == null) time = ""
LOG_INFO("Time: " + time)
episode.setAirTime(time)

return episode;
