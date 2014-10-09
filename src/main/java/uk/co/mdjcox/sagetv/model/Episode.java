/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class represents an individual playable media file.
 */
public class Episode implements ErrorRecorder {

  /** The id of the media file source providing this file */
  private String sourceId = "";
  /** The unique id for this media file */
  private String id = "";
  /** The title of the programme */
  private String programmeTitle = "";
  /** The title of the episode of the series */
  private String seriesTitle = "";
  /** The title of the episode of the programme */
  private String episodeTitle = "";
  /** The title of the episode of the programme */
  private String podcastTitle = "";
  /** The series this episode belongs to */
  private String series = "";
  /** The episode number within the series */
  private String episode = "";
  /** The description of this episode */
  private String description = "";
  /** The URL of an icon representing this episode */
  private String iconUrl = "";
  /** The URL of the media file */
  private String serviceUrl = "";
  /** The date on which the media file last aired */
  private String airDate = "";
  /** The time at which the media file last aired */
  private String airTime = "";
  /** The date on which the media file originally aired */
  private String origAirDate = "";
  /** The time at which the media file originally aired */
  private String origAirTime = "";
  /** The TV channel on which the media file last aired */
  private String channel = "";
  /** The category of programme this media file falls into */
  private Set<String> genres = new TreeSet<String>();
  /** A list of parsing errors associated with this episode */
  private List<ParseError> errors = new ArrayList<ParseError>();
  /** The metadata URLs used to populate this item */
  private final Set<String> metaUrls = new HashSet<String>();
  /** The "control" podcast URL */
  private String podcastUrl;

  /**
   * Constructor for the episode meta data.
   *
   * @param sourceId The id of the media file source providing this file
   * @param id The unique id for this media file
   * @param programmeTitle The title of the programme
   * @param seriesTitle The title of the programme
   * @param episodeTitle The title of the episode of the programme
   * @param series The series this episode belongs to
   * @param episode The episode number within the series
   * @param description The description of this episode
   * @param iconUrl The URL of an icon representing this episode
   * @param serviceUrl The URL of the media file
   * @param airDate The date on which the media file last aired
   * @param airTime The time at which the media file last aired
   * @param origAirDate The date on which the media file first aired
   * @param origAirTime The time at which the media file first aired
   * @param channel The TV channel on which the media file last aired
   * @param genres The genres of programme this media file falls into
   *
   * @throws NullPointerException if any parameter is <code>null</code>
   */
  public Episode(String sourceId, String id, String programmeTitle, String seriesTitle, String episodeTitle,
                 String series, String episode, String description, String iconUrl,
                 String serviceUrl, String airDate, String airTime,
                 String origAirDate, String origAirTime, String channel,
                 Set genres) {
    this.sourceId = checkNotNull(sourceId);
    this.id = checkNotNull(id);
    this.programmeTitle = checkNotNull(programmeTitle);
    this.seriesTitle = checkNotNull(seriesTitle);
    this.episodeTitle = checkNotNull(episodeTitle);
    this.series = checkNotNull(series);
    this.episode = checkNotNull(episode);
    this.description = checkNotNull(description);
    this.iconUrl = checkNotNull(iconUrl);
    this.serviceUrl = checkNotNull(serviceUrl);
    this.airDate = checkNotNull(airDate);
    this.airTime = checkNotNull(airTime);
    this.origAirDate = checkNotNull(origAirDate);
    this.origAirTime = checkNotNull(origAirTime);
    this.channel = checkNotNull(channel);
    this.genres.addAll(checkNotNull(genres));
    podcastTitle = buildPodcastTitle();
  }

  /**
   * Gets the id of the media source providing this file
   *
   * @return The id of the media file source providing this file
   */
  public final String getSourceId() {
    return sourceId;
  }

  /**
   * Sets the id of the media source providing this file
   *
   * @param sourceId The id of the media source providing this file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setSourceId(String sourceId) {
    this.sourceId = checkNotNull(sourceId);
  }

  /**
   * Gets the unique id of this episode of the programme.
   *
   * @return the unique id of this episode of the programme
   */
  public final String getId() {
    return id;
  }

  /**
   * Sets the id of this media file
   *
   * @param id The unique id of the media file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setId(String id) {
    this.id = checkNotNull(id);
  }

  /**
   * Gets the last air date of this episode of the programme.
   *
   * @return the last air date of this episode of the programme
   */
  public final String getAirDate() {
    return airDate;
  }

  /**
   * Sets the air date string of this media file
   *
   * @param airDate The air date string of the media file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setAirDate(String airDate) {
    this.airDate = checkNotNull(airDate);
  }

  /**
   * Gets the last air time of this episode of the programme.
   *
   * @return the last air time of this episode of the programme
   */
  public final String getAirTime() {
    return airTime;
  }

  /**
   * Sets the air time string of this media file
   *
   * @param airTime The air time string of the media file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setAirTime(String airTime) {
    this.airTime = checkNotNull(airTime);
  }

  /**
   * Gets the original air date of this episode of the programme.
   *
   * @return the last air date of this episode of the programme
   */
  public String getOrigAirDate() {
    return origAirDate;
  }

  /**
   * Sets the original air date string of this media file
   *
   * @param origAirDate The air date string of the media file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public void setOrigAirDate(String origAirDate) {
    this.origAirDate = origAirDate;
  }

  /**
   * Gets the original air time of this episode of the programme.
   *
   * @return the last air time of this episode of the programme
   */
  public String getOrigAirTime() {
    return origAirTime;
  }

  /**
   * Sets the original air time string of this media file
   *
   * @param origAirTime The air time string of the media file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public void setOrigAirTime(String origAirTime) {
    this.origAirTime = origAirTime;
  }

  /**
   * Gets the title of the programme that this episode belongs to.
   *
   * @return the title of the programme that this episode belongs to
   */
  public final String getProgrammeTitle() {
    return programmeTitle;
  }

  /**
   * Sets the programme title of this media file
   *
   * @param programmeTitle The programme title of the media file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setProgrammeTitle(String programmeTitle) {
    this.programmeTitle = checkNotNull(programmeTitle);
    this.podcastTitle = buildPodcastTitle();
  }

  /**
   * Gets the title of the series that this episode belongs to.
   *
   * @return the title of the series that this episode belongs to
   */
  public String getSeriesTitle() {
    return seriesTitle;
  }

  /**
   * Sets the series title of this media file
   *
   * @param seriesTitle The series title of the media file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public void setSeriesTitle(String seriesTitle) {
    this.seriesTitle = seriesTitle;
      this.podcastTitle = buildPodcastTitle();
  }

  /**
   * Sets the episode title of this media file
   *
   * @param episodeTitle The episode title of the media file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setEpisodeTitle(String episodeTitle) {
    this.episodeTitle = checkNotNull(episodeTitle);
      this.podcastTitle = buildPodcastTitle();
  }

  /**
   * Sets the description of this media file
   *
   * @param description The description of the media file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setDescription(String description) {
    this.description = checkNotNull(description);
  }

  /**
   * Sets the URL of an icon representing this media file
   *
   * @param iconUrl The URL of an icon representing this media file
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setIconUrl(String iconUrl) {
    this.iconUrl = checkNotNull(iconUrl);
  }

  /**
   * Sets the URL media file at the source site
   *
   * @param serviceUrl The URL media file at the source site
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setServiceUrl(String serviceUrl) {
    this.serviceUrl = checkNotNull(serviceUrl);
      this.podcastTitle = buildPodcastTitle();
  }

  /**
   * Gets the URL of an icon representing this episode
   *
   * @return the URL of an icon representing this episode
   */
  public final String getIconUrl() {
    return iconUrl;
  }

  /**
   * Gets the title of this episode
   *
   * @return the title of this episode
   */
  public final String getEpisodeTitle() {
    return episodeTitle;
  }

  /**
   * Gets the URL of the media file on the source site
   *
   * @return the URL of the media file on the source site
   */
  public final String getServiceUrl() {
    return serviceUrl;
  }

  /**
   * Gets the description of this episode of the programme
   *
   * @return the description of this episode of the programme
   */
  public final String getDescription() {
    return description;
  }

  /**
   * Gets the channel this episode last aired on
   *
   * @return the channel this episode last aired on
   */
  public final String getChannel() {
    return channel;
  }

  /**
   * Sets the TV channel name this episode aired on
   *
   * @param channel The TV channel name this episode aired on
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setChannel(String channel) {
    this.channel = checkNotNull(channel);
  }

  /**
   * Gets the genres of programme this episode belongs to
   *
   * @return the genres of programme this episode belongs to
   */
  public final Set<String> getGenres() {
    return genres;
  }

  /**
   * Sets the category of programme that this episode belongs to
   *
   * @param genre The category of programme that this episode belongs to
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void addGenre(String genre) {
    String newGenre = checkNotNull(genre);
    genres.add(newGenre);
  }

  /**
   * Gets the series identifier of the series this episode belongs to
   *
   * @return the series identifier of the series this episode belongs to
   */
  public final String getSeries() {
    return series;
  }

  /**
   * Sets the series identifier this episode belongs to
   *
   * @param series The series identifier this episode belongs to
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setSeries(String series) {
    this.series = checkNotNull(series);
      this.podcastTitle = buildPodcastTitle();
  }

  /**
   * Gets the identifier of this episode within the series
   *
   * @return the identifier of this episode within the series
   */
  public final String getEpisode() {
    return episode;
  }

  /**
   * Sets the episode identifier of this episode within the series
   *
   * @param episode The episode identifier of this episode within the series
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setEpisode(String episode) {
    this.episode = checkNotNull(episode);
      this.podcastTitle = buildPodcastTitle();
  }


    /**
     * Builds a title for the podcast which can include programme title, episode title,
     * series and episode identifiers depending on whats available
     *
     * @return a title for this episode of the programme
     */
    public String getPodcastTitle() {
        return podcastTitle;
    }

    /**
   * Builds a title for the podcast which can include programme title, episode title,
   * series and episode identifiers depending on whats available
   *
   * @return a title for this episode of the programme
   */
  private final String buildPodcastTitle() {
    String programmePart = programmeTitle == null ? "" : programmeTitle;

    String seriesPart = seriesTitle == null ? "" : seriesTitle;
    if (seriesPart.equals(programmePart)) {
        seriesPart = "";
    }

    if (seriesPart.isEmpty()) {
        if (!series.isEmpty()) {
            if (series.startsWith("Series") || series.startsWith("series") || !Character
                    .isDigit(series.charAt(0))) {
                seriesPart = series;
            } else {
                seriesPart = "Series " + series;
            }
        }
    }

    String episodePart = episodeTitle == null ? "" : episodeTitle;
    if (episodePart.equals(programmePart) || episodePart.equals(seriesPart)) {
          episodePart = "";
    }
    if (episodePart.isEmpty()) {
        if (!episode.isEmpty()) {

            if (episode.startsWith("Episode") || episode.startsWith("episode") || !Character
                    .isDigit(episode.charAt(0))) {
                episodePart = episode;
            } else {
                episodePart = "Episode " + episode;

            }
        }
    }

    String title = programmePart;
    if (title.isEmpty()) {
        title = seriesPart;
    } else {
        if (!seriesPart.isEmpty()) {
           title = title + " - " + seriesPart;
        }
    }

    if (title.isEmpty()) {
        title = episodePart;
    } else {
        if (!episodePart.isEmpty()) {
            title = title + " - " + episodePart;
        }
    }

    if (title.isEmpty()) {
        return serviceUrl;
    } else {
        return title;
    }
  }

  /**
   * Adds a meta date URL used to populate this category
   * @param metaUrl the URL
   */
  public void addMetaUrl(String metaUrl) {
    metaUrls.add(metaUrl);

  }

  /**
   * Gets the list of meta data URLs used to populate this category
   * @return a set of URLs
   */
  public Set<String> getMetaUrls() {
    return metaUrls;
  }

  /**
   * Returns a list of parsing errors associated with this episode
   *
   * @return list of parsing errors
   */
  @Override
  public List<ParseError> getErrors() {
    return errors;
  }

  /**
   * Adds a parse error to the id
   * @param level a severity level for the error
   * @param message a message indicating the nature of the failure
   */
  @Override
  public void addError(String level, String message) {
    ParseError error = new ParseError(this, level, message);
    errors.add(error);
  }

  /**
   * Indicates if there where parsing errors processing this episode
   *
   * @return <code><true/code> if there were parsing errors
   */
  @Override
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

    /**
     * Gets the URL of the podcast used to control the episode playback
     * @return the control podcast URL
     */
    public String getPodcastUrl() {
        return podcastUrl;
    }

    /**
     * Sets the URL of the podcast used to control the episode playback
     * @param podcastUrl the control podcast URL
     */
    public void setPodcastUrl(String podcastUrl) {
        this.podcastUrl = podcastUrl;
    }

    /**
   * Returns a string representation of the episode - its title.
   *
   * @return the string representation of the episode
   */
  @Override
  public final String toString() {
    return id;
  }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Episode episode1 = (Episode) o;

        if (airDate != null ? !airDate.equals(episode1.airDate) : episode1.airDate != null) return false;
        if (airTime != null ? !airTime.equals(episode1.airTime) : episode1.airTime != null) return false;
        if (origAirDate != null ? !origAirDate.equals(episode1.origAirDate) : episode1.origAirDate != null) return false;
        if (origAirTime != null ? !origAirTime.equals(episode1.origAirTime) : episode1.origAirTime != null) return false;
        if (channel != null ? !channel.equals(episode1.channel) : episode1.channel != null) return false;
        if (description != null ? !description.equals(episode1.description) : episode1.description != null)
            return false;
        if (episode != null ? !episode.equals(episode1.episode) : episode1.episode != null) return false;
        if (episodeTitle != null ? !episodeTitle.equals(episode1.episodeTitle) : episode1.episodeTitle != null)
            return false;
        if (errors != null ? !errors.equals(episode1.errors) : episode1.errors != null) return false;
        if (genres != null ? !genres.equals(episode1.genres) : episode1.genres != null) return false;
        if (iconUrl != null ? !iconUrl.equals(episode1.iconUrl) : episode1.iconUrl != null) return false;
        if (id != null ? !id.equals(episode1.id) : episode1.id != null) return false;
        if (metaUrls != null ? !metaUrls.equals(episode1.metaUrls) : episode1.metaUrls != null) return false;
        if (podcastUrl != null ? !podcastUrl.equals(episode1.podcastUrl) : episode1.podcastUrl != null) return false;
        if (programmeTitle != null ? !programmeTitle.equals(episode1.programmeTitle) : episode1.programmeTitle != null)
            return false;
        if (series != null ? !series.equals(episode1.series) : episode1.series != null) return false;
        if (seriesTitle != null ? !seriesTitle.equals(episode1.seriesTitle) : episode1.seriesTitle != null)
            return false;
        if (serviceUrl != null ? !serviceUrl.equals(episode1.serviceUrl) : episode1.serviceUrl != null) return false;
        if (sourceId != null ? !sourceId.equals(episode1.sourceId) : episode1.sourceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceId != null ? sourceId.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (programmeTitle != null ? programmeTitle.hashCode() : 0);
        result = 31 * result + (seriesTitle != null ? seriesTitle.hashCode() : 0);
        result = 31 * result + (episodeTitle != null ? episodeTitle.hashCode() : 0);
        result = 31 * result + (series != null ? series.hashCode() : 0);
        result = 31 * result + (episode != null ? episode.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        result = 31 * result + (serviceUrl != null ? serviceUrl.hashCode() : 0);
        result = 31 * result + (airDate != null ? airDate.hashCode() : 0);
        result = 31 * result + (airTime != null ? airTime.hashCode() : 0);
        result = 31 * result + (origAirDate != null ? origAirDate.hashCode() : 0);
        result = 31 * result + (origAirTime != null ? origAirTime.hashCode() : 0);
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        result = 31 * result + (genres != null ? genres.hashCode() : 0);
        result = 31 * result + (errors != null ? errors.hashCode() : 0);
        result = 31 * result + (metaUrls != null ? metaUrls.hashCode() : 0);
        result = 31 * result + (podcastUrl != null ? podcastUrl.hashCode() : 0);
        return result;
    }
}
