package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.Sets;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/** 
* Programme Tester. 
* 
* @author <Authors name> 
* @since <pre>Apr 18, 2013</pre> 
* @version 1.0 
*/ 
public class ProgrammeTest {


@Before
public void before() throws Exception {
}

@After
public void after() throws Exception { 
} 

@Test
public void testEpisodesStartAtZero() {
  Programme subcat = new Programme("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
  assertEquals("Episodes should start from zero", 0, subcat.getEpisodes().size());
}


/** 
* 
* Method: addEpisode(Episode episode) 
* 
*/ 
@Test
public void testAddEpisode() throws Exception {
  Programme subcat = new Programme("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  subcat.addEpisode(episode);

  assertEquals("Episodes should number 1", 1, subcat.getEpisodes().size());

  try {
    subcat.addEpisode(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");

}

  /**
   *
   * Method: addEpisode(Episode episode)
   *
   */
  @Test
  public void testGetEpisodesImmutabilityTest() throws Exception {
    Programme subcat = new Programme("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

    Set<String> episodes = subcat.getEpisodes();

    assertEquals("Episodes should number 0", 0, episodes.size());

    Episode episode2 = new Episode("sourceId2", "id2", "programmeTitle2", "seriesTitle2", "episodeTitle2", "series2",
                                  "episode2", "descripton2", "iconUrl2", "serviceUrl2", "airDate2",
                                  "airTime2", "origAirDate2", "origAirTime2", "channel2", Sets.newHashSet("category2"));

    try {
      episodes.add("id2");
    } catch (UnsupportedOperationException e) {
      return;
    }

    fail("Should have thrown UnsupportedOperationException");

  }

  /**
* 
* Method: removeEpisode(Episode episode) 
* 
*/ 
@Test
public void testRemoveEpisode() throws Exception {
  Programme subcat = new Programme("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  subcat.addEpisode(episode);

  assertEquals("Episodes should number 1", 1, subcat.getEpisodes().size());

  subcat.removeEpisode(episode);

  assertEquals("Episodes should number 0", 0, subcat.getEpisodes().size());

  try {
    subcat.removeEpisode(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");

}

/** 
* 
* Method: getEpisodes() 
* 
*/ 
@Test
public void testGetEpisodes() throws Exception {
  Programme subcat = new Programme("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  subcat.addEpisode(episode);

  assertEquals("Episodes should number 1", 1, subcat.getEpisodes().size());

  Set<String> episodes = subcat.getEpisodes();

  assertEquals("Episode key should the episode id", episode.getId(), episodes.iterator().next());

}

  /**
   *
   * Method: getPodcastUrl()
   *
   */
  @Test
  public void testGetPodcastUrl() throws Exception {
    Programme subcat = new Programme("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    subcat.setPodcastUrl("podcastUrl");
    assertEquals("getPodcastUrl", "podcastUrl", subcat.getPodcastUrl());
  }

  /**
   *
   * Method: setPodcastUrl(String podcastUrl)
   *
   */
  @Test
  public void testSetPodcastUrl() throws Exception {
    Programme subcat = new Programme("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    subcat.setPodcastUrl("podcastUrl");

    assertEquals("setPodcastUrl", "podcastUrl", subcat.getPodcastUrl());

    try {
      subcat.setPodcastUrl(null);
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");
  }

} 
