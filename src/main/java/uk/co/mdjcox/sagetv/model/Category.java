/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract base class of all types of media categories.
 */
public abstract class Category {

  /** The unique id of this category */
  private String id="";
  /** The short name for this category */
  private String shortName="";
  /** The description of this category */
  private String longName="";
  /** The URL of the host web site for this category */
  private String serviceUrl="";
  /** The URL of an icon associated with this category */
  private String iconUrl="";
  /** The id of any category which includes this category */
  private String parentId="";

  /**
   * Default constructor called by concrete subclasses.
   *
   * @param id The unique id of this category
   * @param shortName The short name for this category
   * @param longName The description of this category
   * @param serviceUrl The URL of the host web site for this category
   * @param iconUrl The URL of an icon associated with this category
   * @param parentId The id of any category which includes this category
   *
   * @throws NullPointerException if any of the parameters is <code>null</code>
   */
  protected Category(String id, String shortName, String longName, String serviceUrl,
                     String iconUrl, String parentId) {
    this.id = checkNotNull(id);
    this.shortName = checkNotNull(shortName);
    this.longName = checkNotNull(longName);
    this.serviceUrl = checkNotNull(serviceUrl);
    this.iconUrl = checkNotNull(iconUrl);
    this.parentId = checkNotNull(parentId);
  }

  /**
   * Gets the unique id of this category.
   *
   * @return the unique id of this category
   */
  public final String getId() {
    return id;
  }

  /**
   * Sets the unique id of this category.
   *
   * @param id the unique id for this category
   *
   * @throws NullPointerException if the id provided is <code>null</code>
   */
  public final void setId(String id) {
    this.id = checkNotNull(id);
  }

  /**
   * Gets the short name  of this category.
   *
   * @return the short name of this category
   */
  public final String getShortName() {
    return shortName;
  }

  /**
   * Sets the short name of the media category
   *
   * @param shortName The short name of the media category
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setShortName(String shortName) {
    this.shortName = checkNotNull(shortName);
  }

  /**
   * Gets the long name or description of this category.
   *
   * @return the long name of this category
   */
  public final String getLongName() {
    return longName;
  }

  /**
   * Sets the long name of the media category
   *
   * @param longName The long name of the media category
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setLongName(String longName) {
    this.longName = checkNotNull(longName);
  }

  /**
   * Gets the URL of the media category on the source site.
   *
   * @return the URL at which the media category can be found
   */
  public final String getServiceUrl() {
    return serviceUrl;
  }

  /**
   * Sets the URL of the media category on the source site
   *
   * @param serviceUrl The URL of the media category on the source site
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setServiceUrl(String serviceUrl) {
    this.serviceUrl = checkNotNull(serviceUrl);
  }

  /**
   * Gets the URL of an icon representing this media category.
   *
   * @return the URL of an icon representing this media category
   */
  public final String getIconUrl() {
    return iconUrl;
  }

  /**
   * Sets the URL of an icon representing this episode
   *
   * @param iconUrl The URL of an icon representing this episode
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setIconUrl(String iconUrl) {
    this.iconUrl = checkNotNull(iconUrl);
  }

  /**
   * Gets the unique id of the media category which contains this one.
   *
   * @return the unique id of the parent media category
   */
  public final String getParentId() {
    return parentId;
  }

  /**
   * Sets the unique id of media category which contains this one
   *
   * @param parentId The unique id of media category which contains this one
   *
   * @throws NullPointerException if a <code>null</code> value is provided
   */
  public final void setParentId(String parentId) {
    this.parentId = checkNotNull(parentId);
  }

  /**
   * Indicates if this media category represents a source of media file e.g BBC Iplayer.
   *
   * @return <code>true</code> if this category represents a source of media files
   */
  public final boolean isSource() {
    return (this instanceof Source);
  }

  /**
   * Indicates if this media category represents a category of media files.
   *
   * @return <code>true</code> if this category represents a category of media files
   */
  public final boolean isSubCategory() {
    return (this instanceof SubCategory);
  }

  /**
   * Indicates if this media category represents a particular TV programme which may have many episodes.
   *
   * @return <code>true</code> if this category represents a specific TV programme
   */
  public final boolean isProgrammeCategory() {
    return (this instanceof Programme);
  }

  /**
   * Indicates if this media category is the root of the media tree..
   *
   * @return <code>true</code> if this category is the root of the media tree
   */
  public final boolean isRoot() {
    return (this instanceof Root);
  }

  /**
   * Returns a string representation of the category - the unique id.
   *
   * @return the string representation of the category
   */
  @Override
  public final String toString() {
    return id;
  }
}
