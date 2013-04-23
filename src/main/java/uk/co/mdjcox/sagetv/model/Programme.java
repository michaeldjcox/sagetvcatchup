/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A type of media category that represents all episodes of the same programme
 */
public class Programme extends SubCategory {

  /**
   * A map containing all episodes of this programme keyed by id
   */
  private Map<String, Episode> episodes = new HashMap<String, Episode>();

  /**
   * Constructor for the programme meta data.
   *
   * @param id         The unique id of this category
   * @param shortName  The short name for this category
   * @param longName   The description of this category
   * @param serviceUrl The URL of the host web site for this category
   * @param iconUrl    The URL of an icon associated with this category
   * @param parentId   The id of any category which includes this category
   * @throws NullPointerException if any of the parameters is <code>null</code>
   */
  public Programme(String id, String shortName, String longName, String serviceUrl, String iconUrl,
                   String parentId) {
    super(id, shortName, longName, serviceUrl, iconUrl, parentId);
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
    episodes.put(episode.getServiceUrl(), episode);
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
    episodes.remove(episode.getServiceUrl());
  }

  /**
   * Gets an immutable list of the episodes of the programme.
   *
   * @return an immutable list of the episodes of the programme
   */
  public final Map<String, Episode> getEpisodes() {
    return ImmutableMap.copyOf(episodes);
  }
}
