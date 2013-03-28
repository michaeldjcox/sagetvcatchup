/**
 * CustomOnlineLinksPropertyLayout.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.utils;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;


public class CustomOnlineLinksPropertyLayout extends PropertiesFileLayout {

    private final static String HEADER =
            "# =============================================================================="
            + CR
            + "# This properties file contains custom user definitions for the list of"
            + CR
            + "# available online videos and the links to get those videos."
            + CR + "#" + CR
            + "# NOTE: If the same property names are used in this file as are in the default"
            + CR
            + "# properties file (OnlineVideoLinks.properties), the default values will be"
            + CR + "# overwritten!" + CR + "#" + CR
            + "# =============================================================================="
            + CR + "" + CR
            + "# =============================================================================="
            + CR + "# To add custom podcast links:" + CR + "#" + CR
            + "#  Section 1) Add a new xPodcast<CategoryName> to the CustomSources"
            + CR + "#             line. (Optional) " + CR + "#" + CR
            + "#  Section 2) Define the FeedName's URL on its feed definition line:"
            + CR
            + "#             xFeedPodcastCustom/<FeedName>=Source1,Source2,...,SourceN;FeedURL    "
            + CR + "#" + CR
            + "# After adding a new feed, reload the Online Services menu in SageTV, navigate"
            + CR
            + "# to the category containing the new link, then select that item to download"
            + CR
            + "# its list of content and automatically set its initial icon and title."
            + CR
            + "# Aterwards, the icon and/or title can be edited near the end of the"
            + CR + "# \"CustomOnlineVideoUIText.properties\" file." + CR + "#"
            + CR
            + "# Note: Any source or category item that has no title defined in the "
            + CR
            + "# \t\t\"CustomOnlineVideoUIText.properties\" file will show its property name in"
            + CR + "#\t\tSageTV.  " + CR + "#" + CR
            + "# =============================================================================="
            + CR + "" + CR;

    private static final String TAIL = "";

    final static String PRE1 =
            "# =============================================================================="
            + CR + "#" + CR + "# 1" + CR + "#" + CR
            + "# To add additional items on the Online Services menu, define the custom source"
            + CR
            + "# categories here. These items will be listed after the default items on the"
            + CR + "# Online Services menu in the order they are listed here."
            + CR + "#" + CR
            + "# NOTE: Do not use the property named \"Sources\", unless you wish to override the"
            + CR + "# default list of online source categories. " + CR + "#"
            + CR + "# NOTE: Podcast sources must start with \"xPodcast\"." + CR
            + "#" + CR + "# Format:" + CR
            + "# Separate each item with a comma (,)" + CR
            + "# CustomSources=xPodcastCategeoryName1,xPodcastCategeoryName2,xPodcastCategeoryName3, ..."
            + CR + "#" + CR
            + "# =============================================================================="
            + CR;

    final static String PRE2 = "" + CR + "#" + CR
                               + "# =============================================================================="
                               + CR + "#" + CR + "# 2" + CR + "#" + CR
                               + "# Custom Podcast feeds added by users" + CR
                               + "#" + CR + "# Property line format:" + CR + "#"
                               + CR
                               + "# xFeedPodcastCustom/FeedName=Source1,Source2,...,SourceN;FeedURL "
                               + CR + "#" + CR
                               + "#\txFeedPodcastCustom - The required property prefix."
                               + CR + "#" + CR
                               + "#\tFeedName - The unique code name for this feed. To use a Google or YouTube"
                               + CR
                               + "#\t\t\t   Video link as a podcast feed, end the FeedName with one of these"
                               + CR
                               + "# \t\t\t   suffixes, including the underscore '_':"
                               + CR
                               + "#\t\t\t\t\t_GOO - Google Video style link "
                               + CR
                               + "#\t\t\t\t\t_YTV - YouTube Video style link "
                               + CR
                               + "#\t\t\t\t\t_YTC - YouTube Channel style link "
                               + CR + "#" + CR
                               + "#\t\t\t   The feed list is sorted alphanumerically, based on this FeedName."
                               + CR + "#" + CR
                               + "#\tSource1,Source2,...,SourceN - " + CR
                               + "#\t\t\t\t\tComma-separated list of source categoriess to list this feed"
                               + CR
                               + "#\t\t\t\t\tunder, and other Flags. The source categories must come"
                               + CR
                               + "#                   from the 'Sources' property at the top of this file. The"
                               + CR + "#\t\t\t\t\tvalid Flags are listed below."
                               + CR
                               + "#                   \t** Notes: All of the source categories listed here must"
                               + CR
                               + "#                   \t** start with \"xPodcast\"; see list below."
                               + CR
                               + "#                   \t** All of the flags listed here must start with"
                               + CR
                               + "#\t\t\t\t\t\t** \"xFlag\"; see list below."
                               + CR + "#" + CR
                               + "#\t; - Separate the Source list from the feed's URL using a semicolon "
                               + CR + "#" + CR
                               + "#\tFeedURL - The URL of the feed." + CR + "#"
                               + CR
                               + "# ============================================================"
                               + CR + "#" + CR
                               + "# Valid predefined Source category names:"
                               + CR + "#" + CR
                               + "#    xPodcastNews           xPodcastComedy  xPodcastSports    xPodcastScienceTech"
                               + CR
                               + "#    xPodcastEntertainment  xPodcastHD      xPodcastFamily    xPodcastHomeGarden"
                               + CR + "#" + CR
                               + "#    ... plus any defined at the top of this file or defined as subcategories."
                               + CR + "#" + CR
                               + "# ============================================================"
                               + CR + "#" + CR
                               + "# Valid predefined Flag names:" + CR + "#"
                               + CR
                               + "#    xFlagTitleNone - Display the feed's icon w/o a title overlay."
                               + CR
                               + "#\t xFlagTitleShow - Display the feed's icon with a title overlay. (default) "
                               + CR + "#" + CR
                               + "# ============================================================"
                               + CR + "#" + CR + "# Subcategory definitions"
                               + CR + "#" + CR
                               + "#\tTo create a subcategory, use the following three lines as examples:"
                               + CR + "#" + CR
                               + "#       xFeedPodcastCustom/NameOfSubCat=xPodcastCustom;xURLNone"
                               + CR + "#       NameOfSubCat/IsCategory=true"
                               + CR
                               + "#       NameOfSubCat/CategoryName=xPodcastNameOfSubCat"
                               + CR + "#" + CR
                               + "#   In this example, \"NameOfSubCat\" is the FeedName of the subcategory, which"
                               + CR
                               + "#   is used to sort the subcategory into its parent category - \"xPodcastCustom\""
                               + CR + "#   in this example." + CR + "#" + CR
                               + "#\t\"xPodcastNameOfSubCat\" is now the text used to assign feeds to this"
                               + CR + "#\tsubcategory." + CR + "#" + CR
                               + "#\tBe sure to define the subcategory names as shown in the examples in "
                               + CR
                               + "#\t\"CustomOnlineVideoUIText.properties\", both the \"Category\" and \"Source\""
                               + CR + "#   names, as follows:" + CR + "#" + CR
                               + "#       Category/NameOfSubCat/LongName=<Full Name to display in feed list>"
                               + CR
                               + "#       Category/NameOfSubCat/ShortName=<Short Name of subcategory>"
                               + CR
                               + "#       Source/xPodcastNameOfSubCat/LongName=<Full Name to display as menu title>"
                               + CR
                               + "#       Source/xPodcastNameOfSubCat/ShortName=<Short Name to display in menu title>"
                               + CR + "#" + CR
                               + "# ============================================================"
                               + CR + "#" + CR;

    public String getHeadComment() {
        return HEADER;
    }

    public String getTailComment() {
        return TAIL;
    }

    public HashMap<String, String> getPrePropComments() {
        HashMap<String, String> comments = new HashMap<String, String>();
        comments.put("CustomSources", PRE1);
        return comments;
    }

    public HashMap<String, String> getPostPropComments() {
        HashMap<String, String> comments = new HashMap<String, String>();
        comments.put("CustomSources", PRE2);
        comments.put(".*\\/CategoryName", "");
        return comments;
    }

    public Comparator getComparator(final Properties props) {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
                String s1 = (String) o1;
                String s2 = (String) o2;

                boolean s1IsNone = props.getProperty(s1).endsWith("xURLNone");
                boolean s2IsNone = props.getProperty(s2).endsWith("xURLNone");

                boolean s1IsSpec = s1.contains("IsCategory") || s1.contains("CategoryName");
                boolean s2IsSpec = s2.contains("IsCategory") || s2.contains("CategoryName");

                boolean s1IsCatRelated = s1IsNone || s1IsSpec;
                boolean s2IsCatRelated = s2IsNone || s2IsSpec;

                if (s1.equals("CustomSources") && !s2.equals("CustomSources")) {
                    return -1;
                } else
                if (s2.equals("CustomSources") && !s1.equals("CustomSources")) {
                    return 1;
                } else
                if (s1IsCatRelated && !s2IsCatRelated) {
                    return -1;
                }
                else
                if (!s1IsCatRelated && s2IsCatRelated) {
                    return 1;        
                } else
                if (s1IsCatRelated && s2IsCatRelated) {
                    String subcats1 = s1IsSpec ? s1.substring(0, s1.indexOf("/")) : s1.substring(s1.indexOf("/")+1);
                    String subcats2 = s2IsSpec ? s2.substring(0, s2.indexOf("/")) : s2.substring(s2.indexOf("/")+1);
                    if (subcats1.equals(subcats2)) {
                        if (s1.startsWith("xFeed") && !s2.startsWith("xFeed")) {
                            return -1;
                        } else
                        if (!s1.startsWith("xFeed") && s2.startsWith("xFeed")) {
                            return 1;
                        } else
                        if (s1.endsWith("CategoryName") && s2.endsWith("IsCategory")) {
                            return 1;
                        } else
                        if (s1.endsWith("IsCategory") && s2.endsWith("CategoryName")) {
                            return -1;
                        }
                    } else {
                        return subcats1.compareTo(subcats2);
                    }
                }

                return s1.compareTo(s2);

            }
        };
    }
}
