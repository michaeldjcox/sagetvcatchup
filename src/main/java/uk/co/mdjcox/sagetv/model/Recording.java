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
    /**
     * The disk file containing the recording
     */
    private File file;
    /**
     * The process doing the recording
     */
    private Process process;
    private String sourceId;
    private String serviceUrl;
    private String id;
    private String name;
    private AtomicBoolean stopFlag = new AtomicBoolean(false);

    /**
     * Constructor of the recordin gobject which details a recording in progress
     *  @param sourceId     the top level source id of the recording
     * @param id           the id of the episode being recorded
     * @param serviceUrl   the service URL of the recording
     * @param recordingDir the directory where the recording will be kept
     */
    public Recording(String sourceId, String id, String name, String serviceUrl, String recordingDir) {
        this.recordingDir = checkNotNull(recordingDir);
        this.id = checkNotNull(id);
        this.serviceUrl = checkNotNull(serviceUrl);
        this.sourceId = checkNotNull(sourceId);
        this.name = checkNotNull(name);
    }

    /**
     * Gets the name of the episode
     * @return the name of the episode
     */
    public String getName() {
        return name;
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
     * @throws NullPointerException if a <code>null</code> file is provided
     */
    public final void setFile(File file) {
        this.file = checkNotNull(file);
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
        return sourceId;
    }

    /**
     * Gets the URL of the media file on the source site
     *
     * @return the URL of the media file
     */
    public final String getUrl() {
        return serviceUrl;
    }

    /**
     * Gets the unique id of this episode
     *
     * @return the unique id of this episode
     */
    public final String getId() {
        return id;
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
        return id;
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
}
