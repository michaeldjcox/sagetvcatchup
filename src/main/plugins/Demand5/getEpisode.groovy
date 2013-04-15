package Demand5

String details = downloadFileString(url);

//details = moveTo("<meta property=\"fb:app_id", details);
//details = extractTo("<body class=\"episodes_show_page\">", details);

// PROGRAMME TITLE
details2 = moveTo("property=\"og:title\"", details)
details2 = moveTo("content=\"", details2)
String jointTitle = extractTo("\"", details2)
jointTitle.replace(" | ", "|")
String[] result = jointTitle.split("\\|")

seriesTitle = removeHtml(result[1]);
episode.setProgrammeTitle(seriesTitle);
info("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
//details2 = moveTo("<meta name=\"episodeTitle\"", details)
//details2 = moveTo("content=\"", details2)
//String title = extractTo("\"", details2)
title = removeHtml(result[0]);
episode.setEpisodeTitle(title);
info("EpisodeTitle: " + title)

// ID
episode.setId(makeIdSafe(title))

// SYNOPSIS
details2 = moveTo("<meta property=\"og:description\"", details)
details2 = moveTo("content=\"", details2)
String desc = extractTo("\"", details2)
desc = removeHtml(desc);
episode.setDescription(desc);
info("Synopsis: " + desc)

// IMAGE URL
details2 = moveTo("<meta property=\"og:image\"", details)
details2 = moveTo("content=\"", details2)
String image = extractTo("\"", details2)
image = removeHtml(image);
image = makeLinkAbsolute("http://www.channel5.com", image);

episode.setIconUrl(image);
info("Icon: " + image)

// TUNE URL
details2 = moveTo("<meta property=\"og:url\"", details)
details2 = moveTo("content=\"", details2)
String tuneurl = extractTo("\"", details2)
tuneurl = removeHtml(tuneurl);
tuneurl = makeLinkAbsolute("http://www.channel5.com", tuneurl);
episode.setServiceUrl(tuneurl);
info("URL: " + tuneurl)

// CATEGORY
details2 = moveTo("category=", details)
String genre = extractTo("\"", details2)
genre = removeHtml(genre);
episode.setCategory(genre);
info("Category: " + genre)

// CHANNEL
details2 = moveTo("<meta property=\"og:site_name\"", details)
details2 = moveTo("content=\"", details2)
String channel = extractTo("\"", details2)
channel = removeHtml(channel);
episode.setChannel(channel);
info("Channel: " + channel)

// SERIES
details2 = moveTo("<meta name=\"seriesNumber\"", details)
details2 = moveTo("content=\"", details2)
String seriesNumber = extractTo("\"", details2)
seriesNumber = removeHtml(seriesNumber);
episode.setSeries(seriesNumber);
info("Series: " + seriesNumber)

// EPISODE
details2 = moveTo("<meta name=\"episodeSequenceNumber\"", details)
details2 = moveTo("content=\"", details2)
String episodeNo = extractTo("\"", details2)
episodeNo = removeHtml(episodeNo);
episode.setEpisode(episodeNo);
info("Episode: " + episodeNo)

// AIRING DATE
details2 = moveTo("<col class=\"onTvDate\"", details)
details2 = moveTo("<td", details2)
details2 = moveTo(">", details2)
String date = extractTo("<", details2)
if (date == null) date = ""
info("Date: " + date)
episode.setAirDate(date)

details2 = moveTo("<td", details2)
details2 = moveTo(">", details2)
String time = extractTo("</", details2)
time = removeHtml(time);
if (time == null) time = ""
info("Time: " + time)
episode.setAirTime(time)

return episode;









