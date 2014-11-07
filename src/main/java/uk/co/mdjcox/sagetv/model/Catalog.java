/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The <code>Catalog</code> class is the repository of media file meta data.
 *
 * It contains an immutable map of entries indexed by their id.
 */
public class Catalog {

    private Map<String, Episode> episodes = new ConcurrentHashMap<String, Episode>();

  /**
   * The <code>Map</code> of categories indexed by <code>id</code>
   */
  private Map<String, Category> categories = new ConcurrentHashMap<String, Category>();

  private String rootId;

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
  public final void setCategories(String rootId, Map<String, Category> categoryMap, Map<String, Episode> episodeMap) {
      categories = new LinkedHashMap<String, Category>();
      categories.putAll(categoryMap);
      episodes.putAll(episodeMap);
    this.rootId = rootId;

  }

  /**
   * Adds a new media file category to the catalog.
   *
   * @param cat the category to be added
   *
   * @throws NullPointerException if a null category is provided
   */
  public final void addCategory(Category cat) {
    checkNotNull(cat);
    this.categories.put(cat.getId(), cat);
      if (cat instanceof Root) {
          rootId = cat.getId();
      }
  }

    public void addEpisode(Episode episode) {
        checkNotNull(episode);
        this.episodes.put(episode.getId(), episode);
    }

    public void addError(String level, String error) {
      Category root = categories.get(rootId);
        if (root != null) {
            root.addError(level, error);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Catalog) {
            Catalog that = (Catalog) obj;
            return this.rootId.equals(that.rootId) && this.categories.equals(that.categories) && this.episodes.equals(that.episodes);
        } else {
            return false;
        }
    }

    public Episode getEpisode(String id) {
        return episodes.get(id);
    }

    public Category getCategory(String id) {
        return categories.get(id);
    }

    public Collection<Episode> getEpisodes() {
        return episodes.values();
    }

    public Collection<ParseError> getErrors() {
        ArrayList<ParseError> errorMap = new ArrayList<ParseError>();
        for (Category cat : categories.values()) {
          for (ParseError err : cat.getErrors()) {
            err.setItem(cat);
            errorMap.add(err);
          }
        }
        for (Episode ep : episodes.values()) {
          for (ParseError err : ep.getErrors()) {
            err.setItem(ep);
            errorMap.add(err);
          }        }
        return errorMap;
    }
}
