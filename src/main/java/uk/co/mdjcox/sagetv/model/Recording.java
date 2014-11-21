/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.inject.internal.util.$Preconditions.checkNotNull;

/**
 * This class represents a recording made of an individual episode.
 */
public class Recording {

    /**
     * The directory where the recording will be kept
     */
    private final String recordingDir;
    private final boolean toWatch;
    private final boolean toKeep;
    /**
     * The disk file containing the recording
     */
    private File partialFile;
    private File completedFile;

    /**
     * The process doing the recording
     */
    private Process process;
    private Episode episode;
    private AtomicBoolean stopFlag = new AtomicBoolean(false);
    private boolean failed;
    private boolean stalled = false;

    private String failedReason;
    private Throwable failureException;

    private long startTime = System.currentTimeMillis();
    private long stopTime = System.currentTimeMillis();
    private long lastSize = 0;
    private final AtomicBoolean finishedStreaming = new AtomicBoolean(false);
    private final AtomicBoolean finishedRecording = new AtomicBoolean(false);
    private String progress;
    private boolean completed = false;
    private String percentRecorded = "0.0%";

  /**
     * Constructor of the recording object which details a recording in progress
     * @param episode   the episode to record
     * @param recordingDir the directory where the recording will be kept
     */
    public Recording(Episode episode, String recordingDir, boolean toWatch, boolean toKeep) {
        this.recordingDir = checkNotNull(recordingDir);
        this.episode = checkNotNull(episode);
        this.toWatch = toWatch;
        this.toKeep = toKeep;
      if (toWatch) {
        progress = "In progress";
      } else {
        progress = "Queued";
      }
    }

    public boolean isToWatch() {
        return toWatch;
    }

    public boolean isToKeep() {
        return toKeep;
    }

    /**
     * Gets the name of the episode
     * @return the name of the episode
     */
    public String getName() {
        return episode.getPodcastTitle();
    }

    /**
     * Gets the file containing the recording
     *
     * @return the file containing the recording
     */
    public final File getPartialFile() {
        return partialFile;
    }

    /**
     * Sets the file containing the recording
     *
     * @param file the file containing the recording
     * @throws NullPointerException if a <code>null</code> file is provided
     */
    public final void setPartialFile(File file) {
        this.partialFile = checkNotNull(file);
    }


    public File getCompletedFile() {
        return completedFile;
    }

    public void setCompletedFile(File completedFile) {
        this.completedFile = checkNotNull(completedFile);
    }

    /**
     * Sets the process doing the recording
     *
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
     * Gets the path of the recording file on disk
     *
     * @return the path of the recording file on disk or empty string if not yet known
     */
    public final String getPartialFilename() {
        if (partialFile != null) {
            return partialFile.getAbsolutePath();
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
        return !completed;
    }

    public boolean hasCompletedFile() {
      final boolean completedFileExists = completedFile != null && completedFile.exists();
      completed = completed || completedFileExists;
      return completedFileExists;
    }

    /**
     * Returns a string representation of the recording
     *
     * @return a string representation of the recording
     */
    @Override
    public final String toString() {
        return episode.getId();
    }

    public boolean isStopped() {
        return stopFlag.get();
    }

    public void setStopped() {
      if (stopTime == startTime) {
        stopTime = System.currentTimeMillis();
      }
        stopFlag.set(true);
    }

    public AtomicBoolean getStopFlag() {
        return stopFlag;
    }

    public Episode getEpisode() {
        return episode;
    }

    public boolean isFailed() {
      return failed;
    }

  public String getFailedReason() {
    return failedReason;
  }

  public Throwable getFailureException() {
    return failureException;
  }

  public void setFailed(String reason, Throwable ex) {
    if (stopTime == startTime) {
      stopTime = System.currentTimeMillis();
    }
      failedReason = reason;
      failureException = ex;
      failed = true;
    }

  public long getStartTime() {
    return startTime;
  }

  public long getStopTime() {
    return stopTime;
  }

  public void setCompleted() {
    if (stopTime == startTime) {
      stopTime = System.currentTimeMillis();
    }
    completed=true;
  }

  public void setStalled() {
    stalled = true;
  }

  public boolean isStalled() {
    return stalled;
  }

  public long getLastSize() {
    return lastSize;
  }

  public void setLastSize(long lastSize) {
    this.lastSize = lastSize;
  }

    public boolean hasFinishedStreaming() {
        return finishedStreaming.get();
    }

    public void setFinishedStreaming() {
        finishedStreaming.set(true);
    }

    public boolean hasFinishedRecording() {
        return finishedRecording.get();
    }

    public void setFinishedRecording() {
        finishedRecording.set(true);
    }

  public String getPercentRecorded() {
    return percentRecorded;
  }

  public void setPercentRecorded(String percentRecorded) {
    this.percentRecorded = percentRecorded;
  }

  public void setProgress(String progress) {
    this.progress = progress;
  }

  private String getProgress() {
    return progress;
  }

  public String getRecordingStatus() {
      String status = "Waiting";
      if (isFailed()) {
        status = "Failed";
      } else
      if (isComplete()) {
        status = "Complete";
      } else
      if (isStalled()) {
        status = "Stalled";
      } else
      if (isStopped()) {
        status = "Stopped";
      } else
      if (isInProgress()) {
        status = getProgress();
        if (status == null || status.isEmpty()) {
          status = "In progress";
        }

      }
      return status;
  }

  public boolean isComplete() {
    hasCompletedFile();
    return completed;
  }
}
