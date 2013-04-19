package uk.co.mdjcox.model;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 01/02/13
 * Time: 18:24
 * To change this template use File | Settings | File Templates.
 */
public class Episode {
    private String sourceId="";
    private String id="";
    private String programmeTitle="";
    private String episodeTitle="";
    private String series="";
    private String episode="";
    private String description="";
    private String iconUrl="";
    private String serviceUrl="";
    private String airDate="";
    private String airTime="";
    private String channel="";
    private String category="";

    public Episode(String sourceId, String id, String programmeTitle, String episodeTitle, String series, String episode, String description, String iconUrl, String serviceUrl, String airDate, String airTime, String channel, String category) {
        this.sourceId = checkNotNull(sourceId);
        this.id = checkNotNull(id);
        this.programmeTitle = checkNotNull(programmeTitle);
        this.episodeTitle = checkNotNull(episodeTitle);
        this.series = checkNotNull(series);
        this.episode = checkNotNull(episode);
        this.description = checkNotNull(description);
        this.iconUrl = checkNotNull(iconUrl);
        this.serviceUrl = checkNotNull(serviceUrl);
        this.airDate = checkNotNull(airDate);
        this.airTime = checkNotNull(airTime);
        this.channel = checkNotNull(channel);
        this.category = checkNotNull(category);
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = checkNotNull(sourceId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = checkNotNull(id);
    }

    public String getAirDate() {
        return airDate;
    }

    public void setAirDate(String airDate) {
        this.airDate = checkNotNull(airDate);
    }

    public String getAirTime() {
        return airTime;
    }

    public void setAirTime(String airTime) {
        this.airTime = checkNotNull(airTime);
    }

    public String getProgrammeTitle() {
        return programmeTitle;
    }

    public void setProgrammeTitle(String programmeTitle) {
        this.programmeTitle = checkNotNull(programmeTitle);
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = checkNotNull(episodeTitle);
    }

    public void setDescription(String description) {
        this.description = checkNotNull(description);
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = checkNotNull(iconUrl);
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = checkNotNull(serviceUrl);
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = checkNotNull(channel);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = checkNotNull(category);
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = checkNotNull(series);
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = checkNotNull(episode);
    }

    public String getPodcastTitle() {
        String episodeTitle = getEpisodeTitle();
        String programmeTitle = getProgrammeTitle();
        String series = getSeries();
        String episodeNr = getEpisode();
        String title = episodeTitle;
        if (title == null || title.isEmpty()) {
            title = programmeTitle;
        }
        if (series != null && !series.isEmpty()) {
            if (series.startsWith("Series") || series.startsWith("series") || !Character.isDigit(series.charAt(0))) {
                title += " - " + series;
            } else {
                title += " - Series " + series;
            }
        }
        if (episodeNr != null && !episodeNr.isEmpty()) {

            if (episodeNr.startsWith("Episode") || episodeNr.startsWith("episode") || !Character.isDigit(episodeNr.charAt(0))) {
                title += " - " + episodeNr;
            } else {
                title += " - Episode " + episodeNr;

            }
        }
       return title;
    }

    @Override
    public String toString() {
        return episodeTitle;
    }
}
