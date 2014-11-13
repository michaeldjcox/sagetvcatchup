package Channel4OD

String details = GET_WEB_PAGE(url, stopFlag);

// PROGRAMME TITLE
details2 = MOVE_TO("<meta name=\"brandTitle\"", details)
details2 = MOVE_TO("content=\"", details2)
String seriesTitle = EXTRACT_TO("\"", details2)
seriesTitle = REMOVE_HTML_TAGS(seriesTitle);
episode.setProgrammeTitle(seriesTitle);
LOG_INFO("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
details2 = MOVE_TO("<meta name=\"episodeTitle\"", details)
details2 = MOVE_TO("content=\"", details2)
String title = EXTRACT_TO("\"", details2)
title = REMOVE_HTML_TAGS(title);
episode.setEpisodeTitle(title);
LOG_INFO("EpisodeTitle: " + title)

// ID
episode.setId(MAKE_ID(title))

// SYNOPSIS
details2 = MOVE_TO("<meta name=\"synopsis\"", details)
details2 = MOVE_TO("content=\"", details2)
String desc = EXTRACT_TO("\"", details2)
desc = REMOVE_HTML_TAGS(desc);
episode.setDescription(desc);
LOG_INFO("Synopsis: " + desc)

// IMAGE URL
details2 = MOVE_TO("<meta property=\"og:image\"", details)
details2 = MOVE_TO("content=\"", details2)
String image = EXTRACT_TO("\"", details2)
image = REMOVE_HTML_TAGS(image);
image = MAKE_LINK_ABSOLUTE("http://www.channel4.com", image);
episode.setIconUrl(image);
LOG_INFO("Icon: " + image)

// TUNE URL
details2 = MOVE_TO("<meta property=\"og:url\"", details)
details2 = MOVE_TO("content=\"", details2)
String tuneurl = EXTRACT_TO("\"", details2)
tuneurl = REMOVE_HTML_TAGS(tuneurl);
tuneurl = MAKE_LINK_ABSOLUTE("http://www.channel4.com", tuneurl);
episode.setServiceUrl(tuneurl);
LOG_INFO("URL: " + tuneurl)

// CATEGORY
details2 = MOVE_TO("<meta name=\"primaryBrandCategory\"", details)
details2 = MOVE_TO("content=\"", details2)
String genre = EXTRACT_TO("\"", details2)
genre = REMOVE_HTML_TAGS(genre);
episode.addGenre(genre);
LOG_INFO("Category: " + genre)

// CHANNEL
details2 = MOVE_TO("<meta property=\"og:site\"", details)
details2 = MOVE_TO("content=\"", details2)
String channel = EXTRACT_TO("\"", details2)
channel = REMOVE_HTML_TAGS(channel);
episode.setChannel(channel);
LOG_INFO("Channel: " + channel)

// SERIES
details2 = MOVE_TO("<meta name=\"seriesNumber\"", details)
details2 = MOVE_TO("content=\"", details2)
String seriesNumber = EXTRACT_TO("\"", details2)
seriesNumber = REMOVE_HTML_TAGS(seriesNumber);
if (seriesNumber == null) seriesNumber = "";
episode.setSeries(seriesNumber);
LOG_INFO("Series: " + seriesNumber)

// EPISODE
details2 = MOVE_TO("<meta name=\"episodeSequenceNumber\"", details)
details2 = MOVE_TO("content=\"", details2)
String episodeNo = EXTRACT_TO("\"", details2)
episodeNo = REMOVE_HTML_TAGS(episodeNo);
if (episodeNo == null) episodeNo = "";
episode.setEpisode(episodeNo);
LOG_INFO("Episode: " + episodeNo)

// AIRING DATE
details2 = MOVE_TO("<col class=\"onTvDate\"", details)
details2 = MOVE_TO("<td", details2)
details2 = MOVE_TO(">", details2)

String date = EXTRACT_TO("<", details2)
if (date == null) date = ""
LOG_INFO("Date: " + date)
episode.setAirDate(date)

details2 = MOVE_TO("<td", details2)
details2 = MOVE_TO(">", details2)
String time = EXTRACT_TO("</", details2)
time = REMOVE_HTML_TAGS(time);
if (time == null) time = ""
episode.setAirTime(time)

return episode;









