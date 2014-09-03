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
  /** The title of the episode of the programme */
  private String episodeTitle = "";
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
  /** The TV channel on which the media file last aired */
  private String channel = "";
  /** The category of programme this media file falls into */
  private Set<String> genres = new TreeSet<String>();
  /** A list of parsing errors associated with this episode */
  private List<ParseError> errors = new ArrayList<ParseError>();

  /**
   * Constructor for the episode meta data.
   *
   * @param sourceId The id of the media file source providing this file
   * @param id The unique id for this media file
   * @param programmeTitle The title of the programme
   * @param episodeTitle The title of the episode of the programme
   * @param series The series this episode belongs to
   * @param episode The episode number within the series
   * @param description The description of this episode
   * @param iconUrl The URL of an icon representing this episode
   * @param serviceUrl The URL of the media file
   * @param airDate The date on which the media file last aired
   * @param airTime The time at which the media file last aired
   * @param channel The TV channel on which the media file last aired
   * @param genres The genres of programme this media file falls into
   *
   * @throws NullPointerException if any parameter is <code>null</code>
   */
  public Episode(String sourceId, String id, String programmeTitle, String episodeTitle,
                 String series, String episode, String description, String iconUrl,
                 String serviceUrl, String airDate, String airTime, String channel,
                 Set genres) {
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
    this.genres.addAll(checkNotNull(genres));
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
  }

  /**
   * Gets a title for the podcast which can include programme title, episode title,
   * series and episode identifiers depending on whats available
   *
   * @return a title for this episode of the programme
   */
  public final String getPodcastTitle() {
    String title = episodeTitle;
    if (title.isEmpty()) {
      title = programmeTitle;
    }
    if (!series.isEmpty()) {
      if (series.startsWith("Series") || series.startsWith("series") || !Character
          .isDigit(series.charAt(0))) {
        title += " - " + series;
      } else {
        title += " - Series " + series;
      }
    }
    if (!episode.isEmpty()) {

      if (episode.startsWith("Episode") || episode.startsWith("episode") || !Character
          .isDigit(episode.charAt(0))) {
        title += " - " + episode;
      } else {
        title += " - Episode " + episode;

      }
    }
    return title;
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
   * Adds a parse error to the episode
   *
   * @param level a severity level for the error
   * @param plugin the plugin name e.g. iplayer
   * @param programme the programme name affected
   * @param episode the episode name affected
   * @param sourceUrl the source URL from which the information could not be parsed
   * @param message a message indicating the nature of the failure
   */
  @Override
  public void addError(String level, String plugin, String programme, String episode, String sourceUrl, String message) {
    ParseError error = new ParseError(level, plugin, programme, episode, sourceUrl, message);
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
   * Returns a string representation of the episode - its title.
   *
   * @return the string representation of the episode
   */
  @Override
  public final String toString() {
    return episodeTitle;
  }

}
