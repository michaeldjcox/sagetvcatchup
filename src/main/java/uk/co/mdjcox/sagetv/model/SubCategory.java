/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <code>Subcategory</code> represents categories of media files in between {@link Source} and
 * {@link Programme}.
 *
 * Subcategories can have many parent categories and many further subcategories.
 */
public class SubCategory extends Category {

  /** List of {@link Category} ids that are parents of this category. */
  private final Set<String> otherParentIds = new CopyOnWriteArraySet<String>();
  /** Set of child {@link Category} ids. */
  private final Set<String> subCategories = new CopyOnWriteArraySet<String>();
  /**
   * A set containing all episodes of this programme keyed by id
   */
  private Set<String> episodes = new CopyOnWriteArraySet<String>();

  public SubCategory() {
  }

  /**
   * Constructs a subcategory.
   *
   * @param sourceId The media source id of this category
   * @param id The unique id of this category
   * @param shortName The short name for this category
   * @param longName The description of this category
   * @param serviceUrl The URL of the host web site for this category
   * @param iconUrl The URL of an icon associated with this category
   * @param parentId The id of any category which includes this category
   */
  public SubCategory(String sourceId, String id, String shortName, String longName,
                     String serviceUrl, String iconUrl,
                     String parentId) {
    super(sourceId, id, shortName, longName, serviceUrl, iconUrl, parentId);
  }

  /**
   * Adds a new child subcategory of this subcategory.
   *
   * @param subCategory The child subcategory to add
   */
  public void addSubCategory(Category subCategory) {
    subCategories.add(subCategory.getId());
  }

  /**
   * Gets an immutable map of the child subcategories indexed by id
   *
   * @return map of child subcategories
   */
  public final Set<String> getSubCategories() {
    return ImmutableSet.copyOf(subCategories);
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

  /**
   * Adds an individual episode of the programme with the programme meta data.
   *
   * @param episode The episode of the programme
   *
   * @throws NullPointerException if the episode provided is <code>null</code>
   */
  public final void addEpisode(Episode episode) {
    checkNotNull(episode);
    episodes.add(episode.getId());
  }

  /**
   * Removes an individual episode of the programme
   *
   * @param episode The episode of the programme
   *
   * @throws NullPointerException if the episode provided is <code>null</code>
   */
  public final void removeEpisode(Episode episode) {
    checkNotNull(episode);
    episodes.remove(episode.getId());
  }

  /**
   * Gets an immutable list of the episodes of the programme.
   *
   * @return an immutable list of the episodes of the programme
   */
  public final Set<String> getEpisodes() {
    return ImmutableSet.copyOf(episodes);
  }

  public void addAllEpisodes(Set<String> episodes) {
    this.episodes.addAll(episodes);
  }

  /**
   * Indicates if the programme has any episodes
   *
   * @return <code>true</code> if the programme has episodes
   */
  public final boolean hasEpisodes() {
    return !episodes.isEmpty();
  }

  @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SubCategory that = (SubCategory) o;

    if (episodes != null ? !episodes.equals(that.episodes) : that.episodes != null)
      return false;
    if (otherParentIds != null ? !otherParentIds.equals(that.otherParentIds) : that.otherParentIds != null)
            return false;
        if (subCategories != null ? !subCategories.equals(that.subCategories) : that.subCategories != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (otherParentIds != null ? otherParentIds.hashCode() : 0);
        result = 31 * result + (subCategories != null ? subCategories.hashCode() : 0);
        result = 31 * result + (episodes != null ? episodes.hashCode() : 0);
        return result;
    }
}
