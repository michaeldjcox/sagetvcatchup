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

  /** The directory where the recording will be kept */
  private final String recordingDir;
  /** The disk file containing the recording */
  private File file;
  /** The process doing the recording */
  private Process process;
  /** The episode being recorded */
  private Episode episode;

  /**
   * Constructor of the recording object.
   *
   * @param recordingDir the directory where the recording will be kept
   * @param episode the episode being recorded
   *
   * @throws NullPointerException if a <code>null</code> episode is provided
   */
  public Recording(Episode episode, String recordingDir) {
    this.episode = checkNotNull(episode);
    this.recordingDir = checkNotNull(recordingDir);
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
   * Sets the process doing the recording
   * @param process the process
   */
  public void setProcess(Process process) {
    this.process = process;
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
   * Gets the recording directory where the recording will be placed
   *
   * @return the recording dir
   */
  public String getRecordingDir() {
    return recordingDir;
  }

  /**
   * Indicates if the recording is in progress
   *
   * @return <code>true</code> if the recording is in progress
   */
  public boolean isInProgress() {
    try {
      if (process != null) {
        int exitValue = process.exitValue();
        return false;
      }
    } catch (IllegalThreadStateException e) {
      return true;
    }
    return false;
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
