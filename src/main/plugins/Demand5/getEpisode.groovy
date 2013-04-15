package Demand5

String details = GET(url);

//details = MOVE_TO("<meta property=\"fb:app_id", details);
//details = extractTo("<body class=\"episodes_show_page\">", details);

// PROGRAMME TITLE
details2 = MOVE_TO("property=\"og:title\"", details)
details2 = MOVE_TO("content=\"", details2)
String jointTitle = extractTo("\"", details2)
jointTitle.replace(" | ", "|")
String[] result = jointTitle.split("\\|")

seriesTitle = REMOVE_HTML(result[1]);
episode.setProgrammeTitle(seriesTitle);
info("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
//details2 = MOVE_TO("<meta name=\"episodeTitle\"", details)
//details2 = MOVE_TO("content=\"", details2)
//String title = extractTo("\"", details2)
title = REMOVE_HTML(result[0]);
episode.setEpisodeTitle(title);
info("EpisodeTitle: " + title)

// ID
episode.setId(makeIdSafe(title))

// SYNOPSIS
details2 = MOVE_TO("<meta property=\"og:description\"", details)
details2 = MOVE_TO("content=\"", details2)
String desc = extractTo("\"", details2)
desc = REMOVE_HTML(desc);
episode.setDescription(desc);
info("Synopsis: " + desc)

// IMAGE URL
details2 = MOVE_TO("<meta property=\"og:image\"", details)
details2 = MOVE_TO("content=\"", details2)
String image = extractTo("\"", details2)
image = REMOVE_HTML(image);
image = makeLinkAbsolute("http://www.channel5.com", image);

episode.setIconUrl(image);
info("Icon: " + image)

// TUNE URL
details2 = MOVE_TO("<meta property=\"og:url\"", details)
details2 = MOVE_TO("content=\"", details2)
String tuneurl = extractTo("\"", details2)
tuneurl = REMOVE_HTML(tuneurl);
tuneurl = makeLinkAbsolute("http://www.channel5.com", tuneurl);
episode.setServiceUrl(tuneurl);
info("URL: " + tuneurl)

// CATEGORY
details2 = MOVE_TO("category=", details)
String genre = extractTo("\"", details2)
genre = REMOVE_HTML(genre);
episode.setCategory(genre);
info("Category: " + genre)

// CHANNEL
details2 = MOVE_TO("<meta property=\"og:site_name\"", details)
details2 = MOVE_TO("content=\"", details2)
String channel = extractTo("\"", details2)
channel = REMOVE_HTML(channel);
episode.setChannel(channel);
info("Channel: " + channel)

// SERIES
details2 = MOVE_TO("<meta name=\"seriesNumber\"", details)
details2 = MOVE_TO("content=\"", details2)
String seriesNumber = extractTo("\"", details2)
seriesNumber = REMOVE_HTML(seriesNumber);
episode.setSeries(seriesNumber);
info("Series: " + seriesNumber)

// EPISODE
details2 = MOVE_TO("<meta name=\"episodeSequenceNumber\"", details)
details2 = MOVE_TO("content=\"", details2)
String episodeNo = extractTo("\"", details2)
episodeNo = REMOVE_HTML(episodeNo);
episode.setEpisode(episodeNo);
info("Episode: " + episodeNo)

// AIRING DATE
details2 = MOVE_TO("<col class=\"onTvDate\"", details)
details2 = MOVE_TO("<td", details2)
details2 = MOVE_TO(">", details2)
String date = extractTo("<", details2)
if (date == null) date = ""
info("Date: " + date)
episode.setAirDate(date)

details2 = MOVE_TO("<td", details2)
details2 = MOVE_TO(">", details2)
String time = extractTo("</", details2)
time = REMOVE_HTML(time);
if (time == null) time = ""
info("Time: " + time)
episode.setAirTime(time)

return episode;









