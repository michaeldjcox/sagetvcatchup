package Demand5

String details = GET_WEB_PAGE(url);

//programmeDetails = MOVE_TO("<meta property=\"fb:app_id", programmeDetails);
//programmeDetails = EXTRACT_TO("<body class=\"episodes_show_page\">", programmeDetails);

episode.addMetaUrl(url);

// PROGRAMME TITLE
details2 = MOVE_TO("property=\"og:title\"", details)
details2 = MOVE_TO("content=\"", details2)
String jointTitle = EXTRACT_TO("\"", details2)
jointTitle.replace(" | ", "|")
String[] result = jointTitle.split("\\|")

seriesTitle = REMOVE_HTML_TAGS(result[1]);
episode.setProgrammeTitle(seriesTitle);
LOG_INFO("SeriesTitle: " + seriesTitle)

// EPISODE TITLE
//details2 = MOVE_TO("<meta name=\"episodeTitle\"", programmeDetails)
//details2 = MOVE_TO("content=\"", details2)
//String title = EXTRACT_TO("\"", details2)
title = REMOVE_HTML_TAGS(result[0]);
if (title != null) {
    episode.setEpisodeTitle(title);
    LOG_INFO("EpisodeTitle: " + title)
} else {
    LOG_ERROR(episode, "Episode title not found");
}

// SERIES TITLE

// No series title?
//LOG_WARNING(episode, "Series title not found");

// SYNOPSIS
details2 = MOVE_TO("<meta property=\"og:description\"", details)
details2 = MOVE_TO("content=\"", details2)
String desc = EXTRACT_TO("\"", details2)
desc = REMOVE_HTML_TAGS(desc);
if (desc != null) {
    episode.setDescription(desc);
    LOG_INFO("Synopsis: " + desc);
} else {
    LOG_ERROR(episode, "Episode description not found");
}

// IMAGE URL
details2 = MOVE_TO("<meta property=\"og:image\"", details)
details2 = MOVE_TO("content=\"", details2)
String image = EXTRACT_TO("\"", details2)
image = REMOVE_HTML_TAGS(image);
image = MAKE_LINK_ABSOLUTE("http://www.channel5.com", image);

if (image != null) {
    episode.setIconUrl(image);
    LOG_INFO("Icon: " + image);
} else {
    LOG_WARNING(episode, "Episode image not found");
}

// TUNE URL
details2 = MOVE_TO("<meta property=\"og:url\"", details)
details2 = MOVE_TO("content=\"", details2)
String tuneurl = EXTRACT_TO("\"", details2)
tuneurl = REMOVE_HTML_TAGS(tuneurl);
tuneurl = MAKE_LINK_ABSOLUTE("http://www.channel5.com", tuneurl);
if (tuneurl != null) {
    episode.setServiceUrl(tuneurl);
    LOG_INFO("URL: " + tuneurl);
} else {
    LOG_ERROR(episode, "Tuning URL not found");
}

// CHANNEL
details2 = MOVE_TO("<meta property=\"og:site_name\"", details)
details2 = MOVE_TO("content=\"", details2)
String channel = EXTRACT_TO("\"", details2)
channel = REMOVE_HTML_TAGS(channel);
if (channel == null) {
    LOG_WARNING(episode, "Channel not found - defaulting to 'Five'");
    channel = "Five";
}
episode.setChannel(channel);
LOG_INFO("Channel: " + channel)

// CATEGORY
details2 = MOVE_TO("genre=", details)
String genre = EXTRACT_TO("\'", details2)
genre = REMOVE_HTML_TAGS(genre);
if (genre != null) {
    episode.addGenre(genre);
    LOG_INFO("Category: " + genre)
}

if (episode.getGenres().size() == 0) {
    LOG_WARNING(episode, "Genres not found - defaulting to 'Other'");
    episode.addGenre("Other");
}

// SERIES
details2 = MOVE_TO("<h3 class=\"episode_header\">", details)
details2 = MOVE_TO(">Series ", details2)
String seriesNumber = EXTRACT_TO(" ", details2)
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

// EPISODE
details2 = MOVE_TO("- Episode ", details2)
String episodeNo = EXTRACT_TO(":", details2)
episodeNo = REMOVE_HTML_TAGS(episodeNo);

if (episodeNo != null) {
    if (!episodeNo.matches("[0-9]*")) {
        LOG_WARNING(episode, "Episode number '" + episodeNo + "' is not a number");
    }
    LOG_INFO("Episode: " + episodeNo);
    episode.setEpisode(episodeNo);

    if (title.startsWith("Episode " + episodeNo + ": ")) {
        title = title.replace("Episode " + episodeNo + ": ", "");
        episode.setEpisodeTitle(title);
    }
} else {
    LOG_WARNING(episode, "Episode number not found");
}

// AIRING DATE
details2 = MOVE_TO("First broadcast at ", details)
String time = EXTRACT_TO(" ", details2)
time = REMOVE_HTML_TAGS(time);

details2 = MOVE_TO(" ", details2);

String date = EXTRACT_TO("</", details2)
date = date.trim();


if (date != null) {
    LOG_INFO("Date: " + date)

    String newDate = FIX_DATE("dd MMM yyyy", date);
    if (newDate == null) {
        LOG_ERROR(episode, "Failed to parse air date " + date);
        newDate = date;
    }

    episode.setAirDate(newDate);
    episode.setOrigAirDate(newDate);
} else {
    LOG_WARNING(episode, "Original air date not found");
}

if (time != null) {
    LOG_INFO("Time: " + time)

    String newTime = FIX_TIME("HH:mm", time);
    if (newTime == null) {
        LOG_ERROR(episode, "Failed to parse air time " + time);
        newTime = time;
    }

    episode.setAirTime(newTime);
    episode.setOrigAirTime(newTime);
} else {
    LOG_WARNING(episode, "Original air time not found");
}

episode.setId(MAKE_ID(episode.getPodcastTitle()))

return episode;








