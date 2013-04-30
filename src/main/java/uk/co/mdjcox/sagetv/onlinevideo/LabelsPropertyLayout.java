/**
 * LabelsPropertyLayout.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.sagetv.onlinevideo;


import uk.co.mdjcox.utils.PropertiesFileLayout;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;


public class LabelsPropertyLayout extends PropertiesFileLayout {

    private static final String HEADER =
            "# =============================================================================="
            + CR
            + "# This properties file contains custom user definitions for the text displayed"
            + CR
            + "# in the SageTV UI for custom user-added online video menu items."
            + CR + "#" + CR
            + "# To add custom online video links, see the comments near the top of the"
            + CR + "# \"CustomOnlineVideoLinks.properties\" file." + CR + "#"
            + CR
            + "# After adding a new feed in \"CustomOnlineVideoLinks.properties\", select that"
            + CR
            + "# (blank) item in SageTV to download its list of content and automatically set"
            + CR
            + "# its initial icon and title. Aterwards, the icon and/or title can be edited"
            + CR + "# near the end of this file." + CR + "#" + CR
            + "# NOTE: If the same property names are used in this file as are in the default"
            + CR
            + "# properties file (OnlineVideoUIText.properties), the default values will be"
            + CR + "# overwritten inside SageTV!" + CR + "#" + CR + "#" + CR
            + "# To create a language translation file, use the 2-letter language code added to"
            + CR
            + "# the base filename after an underscore. Example: For a German language version"
            + CR + "# of this file, copy the default filename:" + CR
            + "#       CustomOnlineVideoUIText.properties" + CR
            + "# to the _de version: " + CR
            + "#       CustomOnlineVideoUIText_de.properties" + CR + "# " + CR
            + "# Then, change the property values." + CR + "# " + CR
            + "# ==============================================================================" + CR;
    private static final String TAIL = "";
    private static final String PRE1 =
            "# =============================================================================="
            + CR + "# Create the menu items for the Online Services menu:" + CR
            + "# =============================================================================="
            + CR;
    private static final String PRE2 = "" + CR
                                       + "# =============================================================================="
                                       + CR
                                       + "# Create the names for custom subcategories:"
                                       + CR
                                       + "# =============================================================================="
                                       + CR;


    public String getHeadComment() {

        return HEADER;
    }

    public String getTailComment() {
        return TAIL;
    }

    public HashMap<String, String> getPrePropComments() {
        HashMap<String, String> comments = new HashMap<String, String>();
        comments.put("^Source.*", PRE1);
        comments.put("^Category.*", PRE2);
        return comments;
    }

    public HashMap<String, String> getPostPropComments() {
        HashMap<String, String> comments = new HashMap<String, String>();
        return comments;
    }

    public Comparator getComparator(Properties props) {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
              String s1 = (String) o1;
              String s2 = (String) o2;

              String s1part1 = s1.substring(0, s1.indexOf("/"));
              String s1part2 = s1.substring(s1.indexOf("/")+1);

              String s2part1 = s2.substring(0, s2.indexOf("/"));
              String s2part2 = s2.substring(s2.indexOf("/")+1);

              int result =  s2part1.compareTo(s1part1);

              if (result == 0) {
                 return s1part2.compareTo(s2part2);
              } else {
                return result;
              }

            }
        };
    }


}
