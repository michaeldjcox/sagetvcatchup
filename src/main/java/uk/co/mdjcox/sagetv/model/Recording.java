/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import java.io.File;

import static com.google.inject.internal.util.$Preconditions.checkNotNull;

/**
 * This class represents a recording made of an individual episode.
 */
public class Recording {

  /** The disk file containing the recording */
  private File file;
  /** The episode being recorded */
  private Episode episode;

  /**
   * Constructor of the recording object.
   *
   * @param episode the episode being recorded
   *
   * @throws NullPointerException if a <code>null</code> episode is provided
   */
  public Recording(Episode episode) {
    this.episode = checkNotNull(episode);
  }

  /**
   * Gets the file containing the recording
   *
   * @return the file containing the recording
   */
  public final File getFile() {
    return file;
  }

  /**
   * Sets the file containing the recording
   *
   * @param file the file containing the recording
   *
   * @throws NullPointerException if a <code>null</code> file is provided
   */
  public final void setFile(File file) {
    this.file = checkNotNull(file);
  }

  /**
   * Gets the id of the media source site providing this media file.
   *
   * @return the id of the media source
   */
  public final String getSourceId() {
    return episode.getSourceId();
  }

  /**
   * Gets the URL of the media file on the source site
   *
   * @return the URL of the media file
   */
  public final String getUrl() {
    return episode.getServiceUrl();
  }

  /**
   * Gets the unique id of this episode
   *
   * @return the unique id of this episode
   */
  public final String getId() {
    return episode.getId();
  }

  /**
   * Gets the name of the episode i.e. the episode title
   *
   * @return the title of the episode
   */
  public final String getName() {
    return episode.getEpisodeTitle();
  }

  /**
   * Gets the path of the recording file on disk
   *
   * @return the path of the recording file on disk or empty string if not yet known
   */
  public final String getFilename() {
    if (file != null) {
      return file.getAbsolutePath();
    }
    return "";
  }

  /**
   * Returns a string representation of the recording
   *
   * @return a string representation of the recording
   */
  @Override
  public final String toString() {
    return episode.toString();
  }
}
