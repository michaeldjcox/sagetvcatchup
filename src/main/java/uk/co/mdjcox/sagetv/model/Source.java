/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

/**
 * Special category representing the all media files from one source.
 */
public class Source extends SubCategory {

  /**
   * Constructs a new source category.
   *
   * This category always as the root as parent category.
   *
   * @param id The unique id of this category
   * @param shortName The short name for this category
   * @param longName The description of this category
   * @param serviceUrl The URL of the host web site for this category
   * @param iconUrl The URL of an icon associated with this category
   */
    public Source(String id, String shortName, String longName,
                  String serviceUrl, String iconUrl) {
        super(id, shortName, longName, serviceUrl, iconUrl, "");
    }
}
