package ITVPlayer

String details = downloadFileString(url);

// PROGRAMME TITLE
details2 = moveTo("<h2 class=\"title episode-title\">", details)
String seriesTitle = extractTo("<", details2)
seriesTitle = removeHtml(seriesTitle);
episode.setProgrammeTitle(seriesTitle);
logger.info("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
details2 = moveTo("<meta property=\"og:title\"", details)
details2 = moveTo("content=\"", details2)
String title = extractTo("\"", details2)
title = removeHtml(title);
episode.setEpisodeTitle(title);
logger.info("EpisodeTitle: " + title)

// SYNOPSIS
details2 = moveTo("<meta property=\"og:description\"", details)
details2 = moveTo("content=\"", details2)
String desc = extractTo("\"", details2)
desc = removeHtml(desc);
episode.setDescription(desc);
logger.info("Synopsis: " + desc)

// IMAGE URL
String details2 = moveTo("<meta property=\"og:image\"", details)
details2 = moveTo("content=\"", details2)
String image = extractTo("\"", details2)
image = removeHtml(image);
image = makeLinkAbsolute("http://www.itv.com", image);
episode.setIconUrl(image);
logger.info("Icon: " + image)

// TUNE URL
details2 = moveTo("<meta property=\"og:url\"", details)
details2 = moveTo("content=\"", details2)
String tuneurl = extractTo("\"", details2)
tuneurl = removeHtml(tuneurl);
tuneurl = makeLinkAbsolute("http://www.itv.com", tuneurl);
episode.setServiceUrl(tuneurl);
logger.info("URL: " + tuneurl)

// CATEGORY
details2 = moveTo("class=\"active\">More ", details)
String genre = extractTo("<", details2)
genre = removeHtml(genre)
if (genre == null) genre = "Other"
genre = genre.replace("&", " and ")
episode.setCategory(genre);
logger.info("Category: " + genre)

// CHANNEL
details2 = moveTo("<meta property=\"og:site_name\"", details)
details2 = moveTo("content=\"", details2)
String channel = extractTo("\"", details2)
channel = removeHtml(channel);
episode.setChannel(channel);
logger.info("Channel: " + channel)

// SERIES
details2 = moveTo("<div class=\"field-season-number\"", details)
details2 = moveTo("<div class=\"field-item even\">", details2)
String seriesNumber = extractTo("<", details2)
seriesNumber = removeHtml(seriesNumber);
episode.setSeries(seriesNumber);
logger.info("Series: " + seriesNumber)

// EPISODE
details2 = moveTo("<div class=\"field-episode-number\"", details)
details2 = moveTo("<div class=\"field-item even\">", details2)
String episodeNo = extractTo("<", details2)
episodeNo = removeHtml(episodeNo);
episode.setEpisode(episodeNo);
logger.info("Episode: " + episodeNo)

// AIRING DATE
//details2 = moveTo("<col class=\"onTvDate\"", details)
//details2 = moveTo("<td", details2)
//details2 = moveTo(">", details2)
//String date = extractTo("<", details2)
//if (date == null) date = ""
//logger.info("Date: " + date)
//episode.setAirDate(date)
//
//details2 = moveTo("<td", details2)
//details2 = moveTo(">", details2)
//String time = extractTo("</", details2)
//time = removeHtml(time);
//if (time == null) time = ""
//episode.setAirTime(time)

return episode;









