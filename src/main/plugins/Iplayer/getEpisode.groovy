package Iplayer

String details = downloadFileString(url);

String pid = moveTo("pid=", details)
pid = extractTo("&", pid)

metadetails = downloadFileString("http://www.bbc.co.uk/iplayer/playlist/" + pid)

// PROGRAMME TITLE
details2 = moveTo("<passionSite ", metadetails)
details2 = moveTo(">", details2)
String seriesTitle = extractTo("<", details2)
seriesTitle = removeHtml(seriesTitle);
episode.setProgrammeTitle(seriesTitle);
info("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
details2 = moveTo("<title>", metadetails)
String title = extractTo("<", details2)
title = removeHtml(title);
episode.setEpisodeTitle(title);
info("EpisodeTitle: " + title)

// ID
episode.setId(makeIdSafe(title))

// SYNOPSIS
details2 = moveTo("<summary>", metadetails)
String desc = extractTo("<", details2)
desc = removeHtml(desc);
episode.setDescription(desc);
info("Synopsis: " + desc)

// IMAGE URL
details2 = moveTo("<link rel=\"holding\"", metadetails)
details2 = moveTo("href=\"", details2)
String image = extractTo("\"", details2)
image = removeHtml(image);
image = makeLinkAbsolute("http://www.bbc.co.uk", image);
episode.setIconUrl(image);
info("Icon: " + image)

// TUNE URL
details2 = moveTo("<link rel=\"alternate\"", metadetails)
details2 = moveTo("href=\"", details2)
String tuneurl = extractTo("\"", details2)
tuneurl = removeHtml(tuneurl);
tuneurl = makeLinkAbsolute("http://www.bbc.co.uk", tuneurl);
episode.setServiceUrl(tuneurl);
info("URL: " + tuneurl)

// CATEGORY
details2 = moveTo("<ul class=\"categories\"", details)
details2 = moveTo("<a href=\"/iplayer/tv/categories/", details2)

String genre = extractTo("\"", details2)
genre = removeHtml(genre)
if (genre == null) genre = "Other"
genre = genre.replace("&", " and ")
genre = genre.replace("_", " ")
episode.setCategory(genre);
info("Category: " + genre)

// CHANNEL
details2 = moveTo("<service id=", metadetails)
details2 = moveTo(">", details2)
String channel = extractTo("<", details2)
channel = removeHtml(channel);
episode.setChannel(channel);
info("Channel: " + channel)

// SERIES
details2 = moveTo("<programmeSeries id=", details)
details2 = moveTo(">", details2)
String seriesNumber = extractTo("<", details2)
seriesNumber = removeHtml(seriesNumber);
episode.setSeries(seriesNumber);
info("Series: " + seriesNumber)

// EPISODE
details2 = moveTo("<div class=\"field-episode-number\"", details)
details2 = moveTo("<div class=\"field-item even\">", details2)
String episodeNo = extractTo("<", details2)
episodeNo = removeHtml(episodeNo);
episode.setEpisode(episodeNo);
info("Episode: " + episodeNo)

//AIRING DATE
details2 = moveTo("<li class=\"last_broadcast_date\">", details)
details2 = moveTo("<span>", details2)
details2 = extractTo("<", details2)
//  <span>BBC Two, 9:15AM Sun, 10 Mar 2013</span>

details3 = moveTo(", ", details2);
String date = moveTo(" ", details3);
date = removeHtml(date);
if (date == null) date = ""
info("Date: " + date)
episode.setAirDate(date)

time = extractTo(" ", details3);
time = removeHtml(time);
if (time == null) time = ""
info("Time: " + time)
episode.setAirTime(time)

return episode;
