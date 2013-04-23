package uk.co.mdjcox.sagetv.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertEquals;

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
  Programme subcat = new Programme("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
  assertEquals("Episodes should start from zero", 0, subcat.getEpisodes().size());
}


/** 
* 
* Method: addEpisode(Episode episode) 
* 
*/ 
@Test
public void testAddEpisode() throws Exception {
  Programme subcat = new Programme("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

  Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "channel", "category");
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
    Programme subcat = new Programme("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

    Map<String, Episode> episodes = subcat.getEpisodes();

    assertEquals("Episodes should number 0", 0, episodes.size());

    Episode episode2 = new Episode("sourceId2", "id2", "programmeTitle2", "episodeTitle2", "series2",
                                  "episode2", "descripton2", "iconUrl2", "serviceUrl2", "airDate2",
                                  "airTime2", "channel2", "category2");

    try {
      episodes.put("id2", episode2);
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
  Programme subcat = new Programme("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

  Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "channel", "category");
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
  Programme subcat = new Programme("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

  Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "channel", "category");
  subcat.addEpisode(episode);

  assertEquals("Episodes should number 1", 1, subcat.getEpisodes().size());

  Map<String, Episode> episodes = subcat.getEpisodes();

  assertEquals("Episode found should match that entered", episode,
               episodes.entrySet().iterator().next().getValue());

  assertEquals("Episode key should the episode id", episode.getServiceUrl(), episodes.entrySet().iterator().next().getKey());

}


} 
