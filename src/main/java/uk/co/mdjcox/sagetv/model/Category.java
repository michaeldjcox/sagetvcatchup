/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract base class of all types of media categories.
 */
public abstract class Category implements ErrorRecorder {

    /** The unique id of this category */
    private String sourceId="";
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
    /** A list of parsing errors associated with this episode */
    private List<ParseError> errors = new CopyOnWriteArrayList<ParseError>();
    /** The metadata URLs used to populate this item */
    private final Set<String> metaUrls = new CopyOnWriteArraySet<String>();
    /** The URL of the podcast listing these episodes */
    private String podcastUrl="";
    /** The type of category */
    private String type="";

  protected Category() {
  }

  /**
     * Default constructor called by concrete subclasses.
     *
     * @param sourceId The media source id of this category
     * @param id The unique id of this category
     * @param shortName The short name for this category
     * @param longName The description of this category
     * @param serviceUrl The URL of the host web site for this category
     * @param iconUrl The URL of an icon associated with this category
     * @param parentId The id of any category which includes this category
     *
     * @throws NullPointerException if any of the parameters is <code>null</code>
     */
    protected Category(String sourceId, String id, String shortName, String longName, String serviceUrl,
                       String iconUrl, String parentId) {
        this.sourceId = checkNotNull(sourceId);
        this.id = checkNotNull(id);
        this.shortName = checkNotNull(shortName);
        this.longName = checkNotNull(longName);
        this.serviceUrl = checkNotNull(serviceUrl);
        this.iconUrl = iconUrl;
        this.parentId = checkNotNull(parentId);
        this.type = getClass().getSimpleName();
    }

    /**
     * Gets the id of the media source providing this file
     *
     * @return The id of the media file source providing this file
     */
    public final String getSourceId() {
        return sourceId;
    }

    /**
     * Sets the id of the media source providing this file
     *
     * @return The id of the media file source providing this file
     */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
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
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
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
     * Adds a meta date URL used to populate this category
     * @param metaUrl the URL
     */
    public void addMetaUrl(String metaUrl) {
        metaUrls.add(metaUrl);

    }

    /**
     * Gets the list of meta data URLs used to populate this category
     * @return a set of URLs
     */
    public Set<String> getMetaUrls() {
        return metaUrls;
    }

    /**
     * Returns a list of parsing errors associated with this episode
     *
     * @return list of parsing errors
     */
    @Override
    public List<ParseError> getErrors() {
        return errors;
    }

    /**
     * Adds a parse error to the id
     * @param level a severity level for the error
     * @param message a message indicating the nature of the failure
     */
    @Override
    public void addError(String level, String message) {
        ParseError error = new ParseError(level, message);
        errors.add(error);
    }

    /**
     * Indicates if there where parsing errors processing this episode
     *
     * @return <code><true/code> if there were parsing errors
     */
    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Gets the podcast URL that will return this list of episodes.
     *
     * @return the podcast URL
     */
    public String getPodcastUrl() {
        return podcastUrl;
    }

    /**
     * Sets the podcast URL that will return the list of episodes
     *
     * @param podcastUrl the podcast URL
     *
     * @throws NullPointerException if the podcastUrl is <code>null</code>
     */
    public void setPodcastUrl(String podcastUrl) {
        this.podcastUrl = checkNotNull(podcastUrl);
    }

    public boolean hasEpisodes() {
      return false;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (errors != null ? !errors.equals(category.errors) : category.errors != null) return false;
        if (iconUrl != null ? !iconUrl.equals(category.iconUrl) : category.iconUrl != null) return false;
        if (id != null ? !id.equals(category.id) : category.id != null) return false;
        if (longName != null ? !longName.equals(category.longName) : category.longName != null) return false;
        if (metaUrls != null ? !metaUrls.equals(category.metaUrls) : category.metaUrls != null) return false;
        if (parentId != null ? !parentId.equals(category.parentId) : category.parentId != null) return false;
        if (podcastUrl != null ? !podcastUrl.equals(category.podcastUrl) : category.podcastUrl != null) return false;
        if (serviceUrl != null ? !serviceUrl.equals(category.serviceUrl) : category.serviceUrl != null) return false;
        if (shortName != null ? !shortName.equals(category.shortName) : category.shortName != null) return false;
        if (sourceId != null ? !sourceId.equals(category.sourceId) : category.sourceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceId != null ? sourceId.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (shortName != null ? shortName.hashCode() : 0);
        result = 31 * result + (longName != null ? longName.hashCode() : 0);
        result = 31 * result + (serviceUrl != null ? serviceUrl.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (errors != null ? errors.hashCode() : 0);
        result = 31 * result + (metaUrls != null ? metaUrls.hashCode() : 0);
        result = 31 * result + (podcastUrl != null ? podcastUrl.hashCode() : 0);
        return result;
    }

    public String getType() {
        return type;
    }
}