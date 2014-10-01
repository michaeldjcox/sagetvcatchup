package uk.co.mdjcox.utils;

import uk.co.mdjcox.sagetv.model.Recording;

import java.io.File;
import java.util.Set;

/**
 * Created by michael on 24/09/14.
 */
public interface SageUtilsInterface {
    String getSageTVProperty(String property, String defaultValue) throws Exception;

    String[] findTitlesWithName(String regex);

    Object[] findAiringsByText(String name);

    String printAiring(Object airing);

    Object findShowForAiring(Object airing);

    String printShow(Object show);

    Object addAiringToSageTV(Recording recording);

    File[] getRecordingDirectories();

    void setClientProperty(String name, String value);
}
