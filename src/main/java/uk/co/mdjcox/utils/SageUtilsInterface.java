package uk.co.mdjcox.utils;

/**
 * Created by michael on 24/09/14.
 */
public interface SageUtilsInterface {
    String getSageTVProperty(String property, String defaultValue) throws Exception;

    int findShowWithName(String regex);
}
