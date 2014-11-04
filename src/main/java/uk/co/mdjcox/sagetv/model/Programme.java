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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A type of media category that represents all episodes of the same programme
 */
public class Programme extends SubCategory {

  /**
   * A set containing all episodes of this programme keyed by id
   */
  private Set<String> episodes = new HashSet<String>();

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

        Programme programme = (Programme) o;

        return !(episodes != null ? !episodes.equals(programme.episodes) : programme.episodes != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (episodes != null ? episodes.hashCode() : 0);
        return result;
    }

  public void addAllEpisodes(Set<String> episodes) {
    this.episodes.addAll(episodes);
  }
}
