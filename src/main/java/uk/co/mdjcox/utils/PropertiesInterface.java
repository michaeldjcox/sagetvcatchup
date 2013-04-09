package uk.co.mdjcox.utils;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 26/03/13
 * Time: 07:26
 * To change this template use File | Settings | File Templates.
 */
public interface PropertiesInterface {
    void clearProperty(String token);

    void setPropertySequence(String token, ArrayList<String> values);

    String getString(String token);

    String getString(String token, String defaultString);

    int getInt(String token);

    int getInt(String token, int defaultValue);

    boolean getBoolean(String token);

    boolean getBoolean(String token, boolean defaultValue);

    ArrayList<String> getPropertySequence(String token);

    ArrayList<String> getPropertySequenceAllowBlanks(String token);

    void clearPropertySequence(String token);

    Set<String> getPropertiesLike(String regex);

    void refresh(boolean throwError) throws Exception;

    String getProperty(String s, String s1);
}
