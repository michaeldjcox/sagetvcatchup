package Iplayer

String details = GET(url);

String pid = MOVE_TO("pid=", details)
pid = extractTo("&", pid)

metadetails = GET("http://www.bbc.co.uk/iplayer/playlist/" + pid)

// PROGRAMME TITLE
details2 = MOVE_TO("<passionSite ", metadetails)
details2 = MOVE_TO(">", details2)
String seriesTitle = extractTo("<", details2)
seriesTitle = REMOVE_HTML(seriesTitle);
episode.setProgrammeTitle(seriesTitle);
info("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
details2 = MOVE_TO("<title>", metadetails)
String title = extractTo("<", details2)
title = REMOVE_HTML(title);
episode.setEpisodeTitle(title);
info("EpisodeTitle: " + title)

// ID
episode.setId(makeIdSafe(title))

// SYNOPSIS
details2 = MOVE_TO("<summary>", metadetails)
String desc = extractTo("<", details2)
desc = REMOVE_HTML(desc);
episode.setDescription(desc);
info("Synopsis: " + desc)

// IMAGE URL
details2 = MOVE_TO("<link rel=\"holding\"", metadetails)
details2 = MOVE_TO("href=\"", details2)
String image = extractTo("\"", details2)
image = REMOVE_HTML(image);
image = makeLinkAbsolute("http://www.bbc.co.uk", image);
episode.setIconUrl(image);
info("Icon: " + image)

// TUNE URL
details2 = MOVE_TO("<link rel=\"alternate\"", metadetails)
details2 = MOVE_TO("href=\"", details2)
String tuneurl = extractTo("\"", details2)
tuneurl = REMOVE_HTML(tuneurl);
tuneurl = makeLinkAbsolute("http://www.bbc.co.uk", tuneurl);
episode.setServiceUrl(tuneurl);
info("URL: " + tuneurl)

// CATEGORY
details2 = MOVE_TO("<ul class=\"categories\"", details)
details2 = MOVE_TO("<a href=\"/iplayer/tv/categories/", details2)

String genre = extractTo("\"", details2)
genre = REMOVE_HTML(genre)
if (genre == null) genre = "Other"
genre = genre.replace("&", " and ")
genre = genre.replace("_", " ")
episode.setCategory(genre);
info("Category: " + genre)

// CHANNEL
details2 = MOVE_TO("<service id=", metadetails)
details2 = MOVE_TO(">", details2)
String channel = extractTo("<", details2)
channel = REMOVE_HTML(channel);
episode.setChannel(channel);
info("Channel: " + channel)

// SERIES
details2 = MOVE_TO("<programmeSeries id=", details)
details2 = MOVE_TO(">", details2)
String seriesNumber = extractTo("<", details2)
seriesNumber = REMOVE_HTML(seriesNumber);
episode.setSeries(seriesNumber);
info("Series: " + seriesNumber)

// EPISODE
details2 = MOVE_TO("<div class=\"field-episode-number\"", details)
details2 = MOVE_TO("<div class=\"field-item even\">", details2)
String episodeNo = extractTo("<", details2)
episodeNo = REMOVE_HTML(episodeNo);
episode.setEpisode(episodeNo);
info("Episode: " + episodeNo)

//AIRING DATE
details2 = MOVE_TO("<li class=\"last_broadcast_date\">", details)
details2 = MOVE_TO("<span>", details2)
details2 = extractTo("<", details2)
//  <span>BBC Two, 9:15AM Sun, 10 Mar 2013</span>

details3 = MOVE_TO(", ", details2);
String date = MOVE_TO(" ", details3);
date = REMOVE_HTML(date);
if (date == null) date = ""
info("Date: " + date)
episode.setAirDate(date)

time = extractTo(" ", details3);
time = REMOVE_HTML(time);
if (time == null) time = ""
info("Time: " + time)
episode.setAirTime(time)

return episode;
