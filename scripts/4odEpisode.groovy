
import uk.co.mdjcox.utils.HtmlUtils
import uk.co.mdjcox.utils.DownloadUtils

String details = DownloadUtils.instance().downloadFileString(url);

// PROGRAMME TITLE
details2 = HtmlUtils.instance().moveTo("<meta name=\"brandTitle\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String seriesTitle = HtmlUtils.instance().extractTo("\"", details2)
seriesTitle = HtmlUtils.instance().removeHtml(seriesTitle);
episode.setProgrammeTitle(seriesTitle);
logger.info("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
details2 = HtmlUtils.instance().moveTo("<meta name=\"episodeTitle\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String title = HtmlUtils.instance().extractTo("\"", details2)
title = HtmlUtils.instance().removeHtml(title);
episode.setEpisodeTitle(title);
logger.info("EpisodeTitle: " + title)

// SYNOPSIS
details2 = HtmlUtils.instance().moveTo("<meta name=\"synopsis\"", details)
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
image = HtmlUtils.instance().makeLinkAbsolute("http://www.channel4.com", image);
episode.setIconUrl(image);
logger.info("Icon: " + image)

// TUNE URL
details2 = HtmlUtils.instance().moveTo("<meta property=\"og:url\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String tuneurl = HtmlUtils.instance().extractTo("\"", details2)
tuneurl = HtmlUtils.instance().removeHtml(tuneurl);
tuneurl = HtmlUtils.instance().makeLinkAbsolute("http://www.channel4.com", tuneurl);
episode.setServiceUrl(tuneurl);
logger.info("URL: " + tuneurl)

// CATEGORY
details2 = HtmlUtils.instance().moveTo("<meta name=\"primaryBrandCategory\"", details)
details2 = HtmlUtils.instance().moveTo("content=\"", details2)
String genre = HtmlUtils.instance().extractTo("\"", details2)
genre = HtmlUtils.instance().removeHtml(genre);
episode.setCategory(genre);
logger.info("Category: " + genre)

// CHANNEL
details2 = HtmlUtils.instance().moveTo("<meta property=\"og:site\"", details)
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
episode.setAirTime(time)

return episode;









