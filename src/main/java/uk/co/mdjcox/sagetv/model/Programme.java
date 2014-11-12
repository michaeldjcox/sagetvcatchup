/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A type of media category that represents all episodes of the same programme
 */
public class Programme extends SubCategory {

  public Programme() {
  }

  /**
   * Constructor for the programme meta data.
   *
   * @param sourceId The media source id of this category
   * @param id         The unique id of this category
   * @param shortName  The short name for this category
   * @param longName   The description of this category
   * @param serviceUrl The URL of the host web site for this category
   * @param iconUrl    The URL of an icon associated with this category
   * @param parentId   The id of any category which includes this category
   * @throws NullPointerException if any of the parameters is <code>null</code>
   */
  public Programme(String sourceId, String id, String shortName, String longName, String serviceUrl, String iconUrl,
                   String parentId) {
    super(sourceId, id, shortName, longName, serviceUrl, iconUrl, parentId);
  }

}
