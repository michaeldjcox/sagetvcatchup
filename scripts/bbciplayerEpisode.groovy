
import uk.co.mdjcox.utils.HtmlUtils
import uk.co.mdjcox.utils.DownloadUtils

String details = DownloadUtils.instance().downloadFileString(url);

String pid = HtmlUtils.instance().moveTo("pid=", details)
pid = HtmlUtils.instance().extractTo("&", pid)

metadetails = DownloadUtils.instance().downloadFileString("http://www.bbc.co.uk/iplayer/playlist/" +pid)

// PROGRAMME TITLE
details2 = HtmlUtils.instance().moveTo("<passionSite ", metadetails)
details2 = HtmlUtils.instance().moveTo(">", details2)
String seriesTitle = HtmlUtils.instance().extractTo("<", details2)
seriesTitle = HtmlUtils.instance().removeHtml(seriesTitle);
episode.setProgrammeTitle(seriesTitle);
logger.info("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
details2 = HtmlUtils.instance().moveTo("<title>", metadetails)
String title = HtmlUtils.instance().extractTo("<", details2)
title = HtmlUtils.instance().removeHtml(title);
episode.setEpisodeTitle(title);
logger.info("EpisodeTitle: " + title)

// SYNOPSIS
details2 = HtmlUtils.instance().moveTo("<summary>", metadetails)
String desc = HtmlUtils.instance().extractTo("<", details2)
desc = HtmlUtils.instance().removeHtml(desc);
episode.setDescription(desc);
logger.info("Synopsis: " + desc)

// IMAGE URL
details2 = HtmlUtils.instance().moveTo("<link rel=\"holding\"", metadetails)
details2 = HtmlUtils.instance().moveTo("href=\"", details2)
String image = HtmlUtils.instance().extractTo("\"", details2)
image = HtmlUtils.instance().removeHtml(image);
image = HtmlUtils.instance().makeLinkAbsolute("http://www.bbc.co.uk", image);
episode.setIconUrl(image);
logger.info("Icon: " + image)

// TUNE URL
details2 = HtmlUtils.instance().moveTo("<link rel=\"alternate\"", metadetails)
details2 = HtmlUtils.instance().moveTo("href=\"", details2)
String tuneurl = HtmlUtils.instance().extractTo("\"", details2)
tuneurl = HtmlUtils.instance().removeHtml(tuneurl);
tuneurl = HtmlUtils.instance().makeLinkAbsolute("http://www.bbc.co.uk", tuneurl);
episode.setServiceUrl(tuneurl);
logger.info("URL: " + tuneurl)

// CATEGORY
details2 = HtmlUtils.instance().moveTo("<ul class=\"categories\"", details)
details2 = HtmlUtils.instance().moveTo("<a href=\"/iplayer/tv/categories/", details2)

String genre = HtmlUtils.instance().extractTo("\"", details2)
genre = HtmlUtils.instance().removeHtml(genre)
if (genre == null) genre="Other"
genre = genre.replace("&", " and ")
genre = genre.replace("_", " ")
episode.setCategory(genre);
logger.info("Category: " + genre)

// CHANNEL
details2 = HtmlUtils.instance().moveTo("<service id=", metadetails)
details2 = HtmlUtils.instance().moveTo(">", details2)
String channel = HtmlUtils.instance().extractTo("<", details2)
channel = HtmlUtils.instance().removeHtml(channel);
episode.setChannel(channel);
logger.info("Channel: " + channel)

// SERIES
details2 = HtmlUtils.instance().moveTo("<programmeSeries id=", details)
details2 = HtmlUtils.instance().moveTo(">", details2)
String seriesNumber = HtmlUtils.instance().extractTo("<", details2)
seriesNumber = HtmlUtils.instance().removeHtml(seriesNumber);
episode.setSeries(seriesNumber);
logger.info("Series: " + seriesNumber)

// EPISODE
details2 = HtmlUtils.instance().moveTo("<div class=\"field-episode-number\"", details)
details2 = HtmlUtils.instance().moveTo("<div class=\"field-item even\">", details2)
String episodeNo = HtmlUtils.instance().extractTo("<", details2)
episodeNo = HtmlUtils.instance().removeHtml(episodeNo);
episode.setEpisode(episodeNo);
logger.info("Episode: " + episodeNo)

//AIRING DATE
details2 = HtmlUtils.instance().moveTo("<li class=\"last_broadcast_date\">", details)
details2 = HtmlUtils.instance().moveTo("<span>", details2)
details2 = HtmlUtils.instance().extractTo("<", details2)
//  <span>BBC Two, 9:15AM Sun, 10 Mar 2013</span>

details3 = HtmlUtils.instance().moveTo(", ", details2);
String date = HtmlUtils.instance().moveTo(" ", details3);
date = HtmlUtils.instance().removeHtml(date);
if (date == null) date = ""
logger.info("Date: " + date)
episode.setAirDate(date)

time = HtmlUtils.instance().extractTo(" ", details3);
time = HtmlUtils.instance().removeHtml(time);
if (time == null) time = ""
logger.info("Time: " + time)
episode.setAirTime(time)

return episode;
