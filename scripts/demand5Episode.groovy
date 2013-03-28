
import uk.co.mdjcox.utils.HtmlUtils

String details = DownloadUtils.instance().downloadFileString(url);

//details = HtmlUtils.instance().moveTo("<meta property=\"fb:app_id", details);
//details = HtmlUtils.instance().extractTo("<body class=\"episodes_show_page\">", details);

// PROGRAMME TITLE
details2 = HtmlUtils.instance().moveTo("property=\"og:title\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String jointTitle = HtmlUtils.instance().extractTo("\"", details2)
jointTitle.replace(" | ", "|")
String[] result = jointTitle.split("\\|")

seriesTitle = HtmlUtils.instance().removeHtml(result[1]);
episode.setProgrammeTitle(seriesTitle);
logger.info("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
//details2 = HtmlUtils.instance().moveTo("<meta name=\"episodeTitle\"", details)
//details2 = HtmlUtils.instance().moveTo("content=\"", details2)
//String title = HtmlUtils.instance().extractTo("\"", details2)
title = HtmlUtils.instance().removeHtml(result[0]);
episode.setEpisodeTitle(title);
logger.info("EpisodeTitle: " + title)

// SYNOPSIS
details2 = HtmlUtils.instance().moveTo("<meta property=\"og:description\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String desc = HtmlUtils.instance().extractTo("\"", details2)
desc = HtmlUtils.instance().removeHtml(desc);
episode.setDescription(desc);
logger.info("Synopsis: " + desc)

// IMAGE URL
String details2 = HtmlUtils.instance().moveTo("<meta property=\"og:image\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String image = HtmlUtils.instance().extractTo("\"", details2)
image = HtmlUtils.instance().removeHtml(image);
image = HtmlUtils.instance().makeLinkAbsolute("http://www.channel5.com", image);

episode.setIconUrl(image);
logger.info("Icon: " + image)

// TUNE URL
details2 = HtmlUtils.instance().moveTo("<meta property=\"og:url\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String tuneurl = HtmlUtils.instance().extractTo("\"", details2)
tuneurl = HtmlUtils.instance().removeHtml(tuneurl);
tuneurl = HtmlUtils.instance().makeLinkAbsolute("http://www.channel5.com", tuneurl);
episode.setServiceUrl(tuneurl);
logger.info("URL: " + tuneurl)

// CATEGORY
details2 = HtmlUtils.instance().moveTo("category=", details)
String genre = HtmlUtils.instance().extractTo("\"", details2)
genre = HtmlUtils.instance().removeHtml(genre);
episode.setCategory(genre);
logger.info("Category: " + genre)

// CHANNEL
details2 = HtmlUtils.instance().moveTo("<meta property=\"og:site_name\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String channel = HtmlUtils.instance().extractTo("\"", details2)
channel = HtmlUtils.instance().removeHtml(channel);
episode.setChannel(channel);
logger.info("Channel: " + channel)

// SERIES
details2 = HtmlUtils.instance().moveTo("<meta name=\"seriesNumber\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String seriesNumber = HtmlUtils.instance().extractTo("\"", details2)
seriesNumber = HtmlUtils.instance().removeHtml(seriesNumber);
episode.setSeries(seriesNumber);
logger.info("Series: " + seriesNumber)

// EPISODE
details2 = HtmlUtils.instance().moveTo("<meta name=\"episodeSequenceNumber\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String episodeNo = HtmlUtils.instance().extractTo("\"", details2)
episodeNo = HtmlUtils.instance().removeHtml(episodeNo);
episode.setEpisode(episodeNo);
logger.info("Episode: " + episodeNo)

// AIRING DATE
details2 = HtmlUtils.instance().moveTo("<col class=\"onTvDate\"", details)
details2 = HtmlUtils.instance().moveTo("<td", details2)
details2 = HtmlUtils.instance().moveTo(">", details2)
String date = HtmlUtils.instance().extractTo("<", details2)
if (date == null) date = ""
logger.info("Date: " + date)
episode.setAirDate(date)

details2 = HtmlUtils.instance().moveTo("<td", details2)
details2 = HtmlUtils.instance().moveTo(">", details2)
String time = HtmlUtils.instance().extractTo("</", details2)
time = HtmlUtils.instance().removeHtml(time);
if (time == null) time = ""
logger.info("Time: " + time)
episode.setAirTime(time)

return episode;









