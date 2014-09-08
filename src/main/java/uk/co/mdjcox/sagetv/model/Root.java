/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

/**
 * Special category representing the root of the tree of categories and media files
 */
public class Root extends SubCategory {

  /**
   * Constructs a new root category.
   *
   * This category as empty string as id and no parent category.
   *
   * @param shortName The short name for this category
   * @param longName The description of this category
   * @param serviceUrl The URL of the host web site for this category
   * @param iconUrl The URL of an icon associated with this category
   */
    public Root(String shortName, String longName, String serviceUrl, String iconUrl) {
        super("", "", shortName, longName, serviceUrl, iconUrl, "");
    }
}
