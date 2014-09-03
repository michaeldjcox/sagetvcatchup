/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <code>Subcategory</code> represents categories of media files in between {@link Source} and
 * {@link Programme}.
 *
 * Subcategories can have many parent categories and many further subcategories.
 */
public class SubCategory extends Category {

  /** List of {@link Category} ids that are parents of this category. */
  private Set<String> otherParentIds = new LinkedHashSet<String>();
  /** Map of child {@link Category} indexed by their ids. */
  private Map<String, Category> subCategories = new TreeMap<String, Category>();

  /**
   * Constructs a subcategory.
   *
   * @param id The unique id of this category
   * @param shortName The short name for this category
   * @param longName The description of this category
   * @param serviceUrl The URL of the host web site for this category
   * @param iconUrl The URL of an icon associated with this category
   * @param parentId The id of any category which includes this category
   */
  public SubCategory(String id, String shortName, String longName,
                     String serviceUrl, String iconUrl,
                     String parentId) {
    super(id, shortName, longName, serviceUrl, iconUrl, parentId);
  }

  /**
   * Adds a new child subcategory of this subcategory.
   *
   * @param subCategory The child subcategory to add
   */
  public final void addSubCategory(Category subCategory) {
    subCategories.put(subCategory.getId(), checkNotNull(subCategory));
  }

  /**
   * Gets an immutable map of the child subcategories indexed by id
   *
   * @return map of child subcategories
   */
  public final Map<String, Category> getSubCategories() {
    return ImmutableMap.copyOf(subCategories);
  }

  /**
   * Clears the map of child subcategories
   */
  public final void clearSubCategories() {
    subCategories.clear();
  }

  /**
   * Adds an additional category id  which represents a parent category.
   *
   * @param parentId the id of the additional parent category
   */
  public final void addOtherParentId(String parentId) {
    otherParentIds.add(checkNotNull(parentId));
  }

  /**
   * Gets a list of the ids of other parent categories.
   *
   * @return the list of other parent ids
   */
  public final Set<String> getOtherParentIds() {
    return ImmutableSet.copyOf(otherParentIds);
  }

  /**
   * Indicates if the programme has any subcategories
   *
   * @return <code>true</code> if the category has subcategories
   */
  public final boolean hasSubCategories() {
    return !subCategories.isEmpty();
  }
}
