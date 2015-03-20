package uk.co.mdjcox.sagetv.catchup;

/**
 * Created by michael on 13/03/15.
 */
public interface ProgressTracker {
    String getProgress();

    void setProgress(String progress);
}
