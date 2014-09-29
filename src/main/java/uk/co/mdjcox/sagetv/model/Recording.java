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
    private final boolean watchOnly;
    /**
     * The disk file containing the recording
     */
    private File partialFile;
    private File completedFile;
    private File savedFile;
    /**
     * The process doing the recording
     */
    private Process process;
    private Episode episode;
    private AtomicBoolean stopFlag = new AtomicBoolean(false);
    private boolean isC;

    /**
     * Constructor of the recording object which details a recording in progress
     * @param episode   the episode to record
     * @param recordingDir the directory where the recording will be kept
     */
    public Recording(Episode episode, String recordingDir, boolean watchOnly) {
        this.recordingDir = checkNotNull(recordingDir);
        this.episode = checkNotNull(episode);
        this.watchOnly = watchOnly;
    }

    public boolean isWatchOnly() {
        return watchOnly;
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

    public File getSavedFile() {
        return savedFile;
    }

    public void setSavedFile(File savedFile) {
        this.savedFile = checkNotNull(savedFile);
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
        return !isComplete();
    }

    public boolean isComplete() {
        System.err.println("compltedFile=" + completedFile + " exists=" + (completedFile == null ? false : completedFile.exists()));
        return completedFile != null && completedFile.exists();
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
        stopFlag.set(true);
    }

    public AtomicBoolean getStopFlag() {
        return stopFlag;
    }

    public Episode getEpisode() {
        return episode;
    }

}
