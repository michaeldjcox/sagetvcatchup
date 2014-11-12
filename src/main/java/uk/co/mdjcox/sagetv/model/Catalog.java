/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The <code>Catalog</code> class is the repository of media file meta data.
 *
 * It contains an immutable map of entries indexed by their id.
 */
public class Catalog {

  /**
   * The Content root
   */
  private Root root = new Root("", "", "", "", "");

  /**
   * The <code>Map</code> of sources indexed by <code>id</code>
   */
  private Map<String, Source> sources = new ConcurrentHashMap<String, Source>();

  /**
   * The <code>Map</code> of categories indexed by <code>id</code>
   */
  private Map<String, SubCategory> subCategories = new ConcurrentHashMap<String, SubCategory>();

  /**
   * The <code>Map</code> of programmes indexed by <code>id</code>
   */
  private Map<String, Programme> programmes = new ConcurrentHashMap<String, Programme>();

  /**
   * The <code>Map</code> of episodes indexed by <code>id</code>
   */
  private Map<String, Episode> episodes = new ConcurrentHashMap<String, Episode>();

  /**
   * Default constructor
   */
  public Catalog() {
  }

  public Root getRoot() {
    return root;
  }

  public Source getSource(String id) {
    return sources.get(id);
  }

  public Collection<Source> getSources() {
    return sources.values();
  }

  public SubCategory getSubcategory(String id) {
    return subCategories.get(id);
  }

  public Collection<SubCategory> getSubcategories() {
    return subCategories.values();
  }

  public Programme getProgramme(String id) {
    return programmes.get(id);
  }

  public Collection<Programme> getProgrammes() {
    return programmes.values();
  }




  /** Returns a list of categories
   *
   * @return  A list of the meta data
   */
//  public final List<Category> getCategories() {
//    return ImmutableList.copyOf(categories.values());
//  }

  public Episode getEpisode(String id) {
    return episodes.get(id);
  }

  public Collection<Episode> getEpisodes() {
    return episodes.values();
  }


  /**
   * Adds the media content root
   * @param root the root category
   */
  public final void addRoot(Root root) {
    this.root = root;
  }

  /**
   * Adds a new media file source to the catalog.
   *
   * @param source the source to be added
   *
   * @throws NullPointerException if a null sub category is provided
   */
  public final void addSource(Source source) {
    checkNotNull(source);
    this.sources.put(source.getId(), source);
  }

  /**
   * Adds a new media file sub category to the catalog.
   *
   * @param cat the sub category to be added
   *
   * @throws NullPointerException if a null sub category is provided
   */
  public final void addSubCategory(SubCategory cat) {
    checkNotNull(cat);
    this.subCategories.put(cat.getId(), cat);
  }

  /**
   * Adds a new media file programme to the catalog.
   *
   * @param programme the programme to be added
   *
   * @throws NullPointerException if a null sub category is provided
   */
  public final void addProgramme(Programme programme) {
    checkNotNull(programme);
    this.programmes.put(programme.getId(), programme);
  }

  /**
   * Adds a new media file episode to the catalog.
   *
   * @param episode the episode to be added
   *
   * @throws NullPointerException if a null episode is provided
   */
    public void addEpisode(Episode episode) {
        checkNotNull(episode);
        this.episodes.put(episode.getId(), episode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Catalog) {
            Catalog that = (Catalog) obj;
            return root.equals(that.root) &&
                    this.sources.equals(that.sources) &&
                    this.subCategories.equals(that.subCategories) &&
                    this.programmes.equals(that.programmes) &&
                    this.episodes.equals(that.episodes);
        } else {
            return false;
        }
    }


    public Collection<ParseError> getErrors() {
        ArrayList<ParseError> errorMap = new ArrayList<ParseError>();
      for (ParseError err : root.getErrors()) {
        err.setItem(root);
        errorMap.add(err);
      }
      for (Source cat : sources.values()) {
        for (ParseError err : cat.getErrors()) {
          err.setItem(cat);
          errorMap.add(err);
        }
      }
      for (SubCategory cat : subCategories.values()) {
        for (ParseError err : cat.getErrors()) {
          err.setItem(cat);
          errorMap.add(err);
        }
      }
      for (Programme cat : programmes.values()) {
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

  public int getNumberSources() {
    int size = sources.size();
    if (sources.containsKey("status")) {
      size--;
    }
    if (sources.containsKey("search")) {
      size--;
    }

    return size;
  }

  public int getNumberSubCategories() {
    return subCategories.size();
  }

  public int getNumberProgrammes() {
    return programmes.size();
  }

  public int getNumberEpisodes() {
    return episodes.size();
  }

  public String getStatsSummary() {
    return getNumberSources() + " sources " + getNumberProgrammes() + " programmes " + getNumberEpisodes() + " episodes";
  }

  public String getErrorSummary() {
    Collection<ParseError> errorList = getErrors();
    HashMap<String, Integer> errorSum = new HashMap<String, Integer>();
    for (ParseError error : errorList) {
      Integer count = errorSum.get(error.getLevel());
      if (count == null) {
        errorSum.put(error.getLevel(), 1);
      } else {
        errorSum.put(error.getLevel(), count + 1);
      }
    }

    String errorSummary = "(";
    for (Map.Entry<String, Integer> entry : errorSum.entrySet()) {
      errorSummary += entry.getValue() + " " + entry.getKey() + " ";
    }
    errorSummary += ")";
    errorSummary = errorSummary.replace(" )", ")");

    if (errorSummary.equals("()")) {
      errorSummary = "";
    }
    return errorSummary;
  }

  public Collection<Category> getCategories() {
    Collection<Category> categories = new ArrayList<Category>();
    categories.add(root);
    categories.addAll(sources.values());
    categories.addAll(subCategories.values());
    categories.addAll(programmes.values());
    return categories;
  }

  public Category getCategory(String id) {
    if (id.equals(root.getId())) {
      return root;
    }
    Category cat = sources.get(id);
    if (cat == null) {
      cat = subCategories.get(id);
    }
    if (cat == null) {
      cat = programmes.get(id);
    }
    return cat;
  }
}
