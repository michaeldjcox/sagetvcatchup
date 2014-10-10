/**
 * PropertiesFile.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.utils;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * A singleton class which loads properties which configure the network encoder.
 */
public class PropertiesFile extends CommentedProperties implements PropertiesInterface {

    @Inject
    public PropertiesFile(String file, boolean throwError) throws Exception {
        super(file, throwError);
    }

    public PropertiesFile() {
    }

    @Override
    public void clearProperty(String token) {
        remove(token);
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        return super.setProperty(key, value);
    }

    @Override
    public void setPropertySequence(String token, ArrayList<String> values) {
        clearProperty(token);
        for (int i=0; i<999; i++) {
            clearProperty(token + "." + i);
        }
        if (values != null) {
            int i= 0;
            for (String value : values) {
                setProperty(token + "." + i, value);
                i++;
            }
        }
    }

    @Override
    public String getString(String token) {
        return getProperty(token);
    }

    @Override
    public String getString(String token, String defaultString) {
        return getProperty(token, defaultString);
    }

    @Override
    public int getInt(String token) {
        return Integer.parseInt(getString(token));
    }

    @Override
    public int getInt(String token, int defaultValue) {
        String value = getString(token);
        if ((value == null) || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(getString(token));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String token) {
        return Boolean.parseBoolean(getString(token));
    }

    @Override
    public boolean getBoolean(String token, boolean defaultValue) {
        String value = getString(token);
        if ((value == null) || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(getString(token));
        } catch (Exception e) {
            return defaultValue;
        }
    }



    @Override
    public ArrayList<String> getPropertySequence(String token) {
        ArrayList<String> starts = new ArrayList<String>();
        String start = getString(token);
        if ((start != null) && (start.trim().length()>0)) {
            starts.add(start);
        }

        int j=0;
        while (true) {
            start = getString(token+"." + j);
            if ((start != null) && start.length()>0) {
                starts.add(start);
                j++;
            } else {
                break;
            }
        }

        if (starts.isEmpty()) {
            return null;
        }
        return starts;
    }

    @Override
    public ArrayList<String> getPropertySequenceAllowBlanks(String token) {
        ArrayList<String> starts = new ArrayList<String>();
        String start = getString(token);
        if ((start != null)) {
            starts.add(start);
        }

        int j=0;
        while (true) {
            start = getString(token+"." + j);
            if ((start != null)) {
                starts.add(start);
                j++;
            } else {
                break;
            }
        }

        if (starts.isEmpty()) {
            return null;
        }
        return starts;
    }

    @Override
    public void clearPropertySequence(String token) {
        clearProperty(token);
        for (int i=0; i<999; i++) {
            clearProperty(token + "." + i);
        }
    }

    @Override
    public Set<String> getPropertiesLike(String regex) {
        LinkedHashSet<String> starts = new LinkedHashSet<String>();
        Set<Object> keys = keySet();
        for (Object key : keys) {
            if (Pattern.matches(regex, key.toString())) {
                starts.add(key.toString());
            }
        }
        return starts;
    }
}
