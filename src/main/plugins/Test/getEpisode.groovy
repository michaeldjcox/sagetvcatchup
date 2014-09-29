package Test


String pid = REPLACE_LINK_PREFIX(url, "http://www.bbc.co.uk/iplayer/episode/", "");
pid = REPLACE_LINK_TARGET(pid, "");
String metaurl = "http://www.bbc.co.uk/programmes/" + pid + ".xml";
String programmeUrl = REPLACE_LINK_PREFIX(url, "http://www.bbc.co.uk/iplayer/episode/", "http://www.bbc.co.uk/iplayer/playlist/");

episode.addMetaUrl(url);
episode.addMetaUrl(metaurl);
episode.addMetaUrl(programmeUrl);

String metadetails = GET_WEB_PAGE(metaurl);
String programmeDetails = GET_WEB_PAGE(programmeUrl);

// EPISODE TITLE
String details = MOVE_TO("<title>", metadetails)
String title = EXTRACT_TO("<", details)
title = REMOVE_HTML_TAGS(title);

if (title == null) {
    details = MOVE_TO("<display_title>", metadetails)
    details = MOVE_TO("<title>", details)
    title = EXTRACT_TO("<", details)
    title = REMOVE_HTML_TAGS(title);

    details = MOVE_TO("<display_title>", metadetails)
    details = MOVE_TO("<subtitle>", details)
    String subtitle = EXTRACT_TO("<", details)
    subtitle = REMOVE_HTML_TAGS(subtitle);

    if (subtitle != null) {
        title=subtitle;
    }   
}

if (title != null) {
    episode.setEpisodeTitle(title);
    LOG_INFO("EpisodeTitle: " + title)
} else {
    LOG_ERROR(episode, "Episode title not found");
}

// SERIES TITLE

details = MOVE_TO("<programme type=\"series\">", metadetails);

if (details == null) {
    details = MOVE_TO("<programme type=\"brand\">", metadetails);
}

details = MOVE_TO("<title>", details);
String seriesTitle = EXTRACT_TO("<", details)
seriesTitle = REMOVE_HTML_TAGS(seriesTitle);

if (seriesTitle != null) {
    episode.setSeriesTitle(seriesTitle);
    LOG_INFO("SeriesTitle: " + seriesTitle)
} else {
    LOG_WARNING(episode, "Series title not found");
}

// SYNOPSIS
details = MOVE_TO("<long_synopsis>", metadetails)
String desc = EXTRACT_TO("<", details)
desc = REMOVE_HTML_TAGS(desc);

if (desc == null) {
    details = MOVE_TO("<medium_synopsis>", metadetails)
    desc = EXTRACT_TO("<", details)
    desc = REMOVE_HTML_TAGS(desc);
}

if (desc == null) {
    details = MOVE_TO("<short_synopsis>", metadetails)
    desc = EXTRACT_TO("<", details)
    desc = REMOVE_HTML_TAGS(desc);
}

if (desc != null) {
    episode.setDescription(desc);
    LOG_INFO("Synopsis: " + desc);
} else {
    LOG_ERROR(episode, "Episode description not found" );
}

// IMAGE URL
details = MOVE_TO("<image>", metadetails);
details = MOVE_TO("<pid>", details);
String image = EXTRACT_TO("<", details);
image = REMOVE_HTML_TAGS(image);
image = "http://ichef.bbci.co.uk/images/ic/272x153/" + image + ".jpg";

if (image != null) {
    episode.setIconUrl(image);
    LOG_INFO("Icon: " + image);
} else {
    LOG_WARNING(episode, "Episode image not found" );
}

// TUNE URL
details = MOVE_TO("<link rel=\"alternate\"", programmeDetails)
details = MOVE_TO("href=\"", details)
String tuneurl = EXTRACT_TO("\"", details)
tuneurl = REMOVE_HTML_TAGS(tuneurl);
tuneurl = MAKE_LINK_ABSOLUTE("http://www.bbc.co.uk", tuneurl);

if (tuneurl != null) {
    episode.setServiceUrl(tuneurl);
    LOG_INFO("URL: " + tuneurl);
} else {
    LOG_ERROR(episode, "Tuning URL not found" );
}

// CHANNEL
details=MOVE_TO("<service id=", programmeDetails);
details=MOVE_TO(">", details);
String channel1 = EXTRACT_TO("<", details)

details = MOVE_TO("<service type=", metadetails)
details = MOVE_TO("<title>", details)
String channel2 = EXTRACT_TO("<", details)
channel2 = REMOVE_HTML_TAGS(channel2);

String channel;
if (channel1 == null) {
    channel = channel2;
} else
if (channel2 == null) {
    channel = channel1;
} else {
    if (channel1.equals("BBC")) {
        channel = channel2;
    } else {
        channel = channel1;
    }
}

if (channel == null) {
    LOG_WARNING(episode, "Channel not found - defaulting to 'BBC'" );
    channel="BBC";
}
episode.setChannel(channel);
LOG_INFO("Channel: " + channel)

// CATEGORY
details = MOVE_TO("<categories>", metadetails);
String genres = EXTRACT_TO("</categories>", details);

while (genres != null) {
    genres = MOVE_TO("<category type=\"genre", genres);
    genres = MOVE_TO("<title>", genres);
    genre = EXTRACT_TO("</title>", genres);
    genre = REMOVE_HTML_TAGS(genre);
    if (genre != null && !genre.isEmpty()) {
        genre = genre.replace(" & ", " and ");
        genre = genre.replace("&", " and ");
        genre = genre.replace("_", " ");
        LOG_INFO("Genre: " + genre);
        episode.addGenre(genre);
    }
}

if (episode.getGenres().size() == 0) {
        LOG_WARNING(episode, "Genres not found - defaulting to 'Other'" );
        episode.addGenre("Other");
}

// SERIES
details = MOVE_TO("<programme type=\"series\">", metadetails);

if (details == null) {
    details = MOVE_TO("<programme type=\"brand\">", metadetails);
}

details = MOVE_TO("<position", details);

if (details == null || !details.startsWith("/>")) {
    details = MOVE_TO(">", details);
    String seriesNumber = EXTRACT_TO("<", details)
    seriesNumber = REMOVE_HTML_TAGS(seriesNumber);

    if (seriesNumber != null) {
        if (!seriesNumber.matches("[0-9]*")) {
            LOG_WARNING(episode, "Series number '" + seriesNumber + "' is not a number");
        }
        LOG_INFO("Series: " + seriesNumber)
        episode.setSeries(seriesNumber);
    } else {
        LOG_WARNING(episode, "Series number not found");
    }
}

// EPISODE
details = MOVE_TO("<position", metadetails);
if (details == null || !details.startsWith("/>")) {
    details = MOVE_TO(">", details);
    String episodeNo = EXTRACT_TO("</position", details)
    episodeNo = REMOVE_HTML_TAGS(episodeNo);

    if (episodeNo != null) {
        if (!episodeNo.matches("[0-9]*")) {
            LOG_WARNING(episode, "Episode number '" + episodeNo + "' is not a number");
        }
        LOG_INFO("Episode: " + episodeNo);
        episode.setEpisode(episodeNo);
    } else {
        LOG_WARNING(episode, "Episode number not found");
    }
}

//AIRING DATE AND TIME
details = MOVE_TO("<first_broadcast_date", metadetails)

// If empty tag is present then there is no data to parse otherwise...
if (details == null || !details.startsWith("/>")) {
    details = MOVE_TO(">", details);
    details = EXTRACT_TO("</first_broadcast_date>", details)

    String time=null;
    String date = null;

    if (details != null) {
        date = EXTRACT_TO("T", details);
        date = REMOVE_HTML_TAGS(date);
        time = MOVE_TO("T", details);
        String time2 = EXTRACT_TO("+", time);
        if (time2 == null) {
            time2 = EXTRACT_TO("Z", time);
        }
        time = REMOVE_HTML_TAGS(time2);
    }

    if (date != null) {
        LOG_INFO("Date: " + date)
        episode.setAirDate(date)
    } else {
        LOG_WARNING(episode, "Original air date not found");
    }


    if (time != null) {
        LOG_INFO("Time: " + time)
        episode.setAirTime(time);
    } else {
        LOG_WARNING(episode, "Original air time not found");
    }

}

episode.setId(MAKE_ID(episode.getPodcastTitle()))

return episode;
