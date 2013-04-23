/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.ImmutableList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The <code>Catalog</code> class is the repository of media file meta data.
 *
 * It contains an immutable map of entries indexed by their id.
 */
public class Catalog {

  /**
   * The <code>Map</code> of categories indexed by <code>id</code>
   */
  private Map<String, Category> categories = new LinkedHashMap<String, Category>();

  /**
   * Default constructor
   */
  public Catalog() {
  }

  /** Returns a list of categories
   *
   * @return  A list of the meta data
   */
  public final List<Category> getCategories() {
    return ImmutableList.copyOf(categories.values());
  }

  /** Allows the media catalog to be initialised with data
   *
   * @param categoryMap A map of all metadata indexed by id
   */
  public final void setCategories(Map<String, Category> categoryMap) {
    this.categories = new LinkedHashMap<String, Category>(checkNotNull(categoryMap));
  }
}
