/**
 * PropertiesFileLayout.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.sagetv.utils;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;


public abstract class PropertiesFileLayout {

    protected static final String CR = System.getProperty("line.separator");

    protected Properties properties;

    public abstract String getHeadComment();

    public abstract String getTailComment();

    public String getOtherComment() {
        return null;
    };

    public abstract HashMap<String, String> getPrePropComments();

    public abstract HashMap<String, String> getPostPropComments();

    public Comparator getComparator(Properties props) {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
                String s1 = (String) o1;
                String s2 = (String) o2;
                return s1.compareTo(s2);
            }
        };
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
