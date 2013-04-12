package uk.co.mdjcox.model;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 01/02/13
 * Time: 18:24
 * To change this template use File | Settings | File Templates.
 */
public class Episode {
    private String id;
    private String programmeTitle;
    private String episodeTitle;
    private String series;
    private String episode;
    private String description;
    private String iconUrl;
    private String serviceUrl;
    private String airDate;
    private String airTime;
    private String channel;
    private String category;

    public Episode(String id, String programmeTitle, String episodeTitle, String series, String episode, String description, String iconUrl, String serviceUrl, String airDate, String airTime, String channel, String category) {
        this.id = id;
        this.programmeTitle = programmeTitle;
        this.episodeTitle = episodeTitle;
        this.series = series;
        this.episode = episode;
        this.description = description;
        this.iconUrl = iconUrl;
        this.serviceUrl = serviceUrl;
        this.airDate = airDate;
        this.airTime = airTime;
        this.channel = channel;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAirDate() {
        return airDate;
    }

    public void setAirDate(String airDate) {
        this.airDate = airDate;
    }

    public String getAirTime() {
        return airTime;
    }

    public void setAirTime(String airTime) {
        this.airTime = airTime;
    }

    public String getProgrammeTitle() {
        return programmeTitle;
    }

    public void setProgrammeTitle(String programmeTitle) {
        this.programmeTitle = programmeTitle;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
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
        this.channel = channel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
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
