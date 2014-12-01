package uk.co.mdjcox.sagetv.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by michael on 24/09/14.
 */
public interface SageUtilsInterface extends LoggerInterface {
    String getSageTVProperty(String property, String defaultValue) throws Exception;

    String[] findTitlesWithName(String regex);

    Object[] findAiringsByText(String name);

    String printAiring(Object airing);

    Object findShowForAiring(Object airing);

    String printShow(Object show);

    void addRecordingToSageTV(String recordingFile, String programmeTitle, String episodeTitle, String description,
                              List<String> category, String origAirDate, String origAirTime, String airDate, String airTime,
                              int seriesNumber, int episodeNumber, int duration);

    File[] getRecordingDirectories();

    void setClientProperty(String name, String value);

   Map<String, Integer> getChannels();
}
