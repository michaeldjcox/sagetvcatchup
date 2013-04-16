package uk.co.mdjcox.model;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 15/04/13
 * Time: 07:33
 * To change this template use File | Settings | File Templates.
 */
public class Recording {
    private File file;
    private Episode episode;

    public Recording(Episode episode) {
        this.episode = episode;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getSourceId() {
        return episode.getSourceId();
    }

    public String getUrl() {
        return episode.getServiceUrl();
    }

    public String getId() {
        return episode.getId();
    }

    public String getName() {
        return episode.getEpisodeTitle();
    }

    public String getFilename() {
        if (file != null) {
            return file.getAbsolutePath();
        }
        return "";
    }

    @Override
    public String toString() {
        return episode.toString();
    }
}
