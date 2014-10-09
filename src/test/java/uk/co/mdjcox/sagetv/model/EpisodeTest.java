package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/** 
* Episode Tester. 
* 
* @author <Authors name> 
* @since <pre>Apr 19, 2013</pre> 
* @version 1.0 
*/ 
public class EpisodeTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
}

  @Test
  public void testCreateNullSourceId() {
    try {
      new Episode(null, "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                    "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                    "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullId() {
    try {
      new Episode("sourceId", null, "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                    "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                    "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullProgrammeTitle() {
    try {
      new Episode("sourceId", "id", null, "seriesTitle", "episodeTitle", "series",
                                    "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                    "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }


  @Test
  public void testCreateNullEpisodeTitle() {
    try {
       new Episode("sourceId", "id", "programmeTitle", "seriesTitle", null, "series",
                                    "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                    "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullSeries() {
    try {
      new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", null,
                                    "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                    "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullEpisode() {
    try {
      new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                    null, "descripton", "iconUrl", "serviceUrl", "airDate",
                                    "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullDescription() {
    try {
      new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                    "episode", null, "iconUrl", "serviceUrl", "airDate",
                                    "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullIconUrl() {
    try {
      new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                    "episode", "descripton", null, "serviceUrl", "airDate",
                                    "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullServiceUrl() {
    try {
      new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                    "episode", "descripton", "iconUrl", null, "airDate",
                                    "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullAirDate() {
    try {
      new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                    "episode", "descripton", "iconUrl", "serviceUrl", null,
                                    "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullAirTime() {
    try {
       new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                    "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                    null, "origAirDate", "origAirTime", "channel", Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullChannel() {
    try {
      new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                    "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                    "airTime", "origAirDate", "origAirTime",  null, Sets.newHashSet("category"));
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullCategory() {
    try {
      new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                    "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                    "airTime", "origAirDate", "origAirTime",  "channel", null);
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreate() throws Exception {
    Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                  "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                  "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    assertEquals("getSourceId", "sourceId", episode.getSourceId());
    assertEquals("getId", "id", episode.getId());
    assertEquals("getProgrammeTitle", "programmeTitle", episode.getProgrammeTitle());
    assertEquals("getSeriesTitle", "seriesTitle", episode.getSeriesTitle());
    assertEquals("getEpisodeTitle", "episodeTitle", episode.getEpisodeTitle());
    assertEquals("getSeries", "series", episode.getSeries());
    assertEquals("getEpisode", "episode", episode.getEpisode());
    assertEquals("getDescription", "descripton", episode.getDescription());
    assertEquals("getIconUrl", "iconUrl", episode.getIconUrl());
    assertEquals("getServiceUrl", "serviceUrl", episode.getServiceUrl());
    assertEquals("getAirDate", "airDate", episode.getAirDate());
    assertEquals("getAirTime", "airTime", episode.getAirTime());
    assertEquals("getOrigAirDate", "origAirDate", episode.getOrigAirDate());
    assertEquals("getOrigAirTime", "origAirTime", episode.getOrigAirTime());
    assertEquals("getChannel", "channel", episode.getChannel());
    assertEquals("getCategory", "category", episode.getGenres().iterator().next());
  }

/** 
* 
* Method: getSourceId() 
* 
*/ 
@Test
public void testGetSourceId() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    assertEquals("getSourceId", "sourceId", episode.getSourceId());

}

/** 
* 
* Method: setSourceId(String sourceId) 
* 
*/ 
@Test
public void testSetSourceId() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setSourceId("sourceId2");
  assertEquals("setSourceId", "sourceId2", episode.getSourceId());

  try {
    episode.setSourceId(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: getId() 
* 
*/ 
@Test
public void testGetId() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getId", "id", episode.getId());
} 

/** 
* 
* Method: setId(String id) 
* 
*/ 
@Test
public void testSetId() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setId("id2");
  assertEquals("setId", "id2", episode.getId());

  try {
    episode.setId(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: getAirDate() 
* 
*/ 
@Test
public void testGetAirDate() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getAirDate", "airDate", episode.getAirDate());
}

/** 
* 
* Method: setAirDate(String airDate) 
* 
*/ 
@Test
public void testSetAirDate() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setAirDate("airDate2");
  assertEquals("setAirDate", "airDate2", episode.getAirDate());

  try {
    episode.setAirDate(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: getAirTime() 
* 
*/ 
@Test
public void testGetAirTime() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getAirTime", "airTime",  episode.getAirTime());
} 

/** 
* 
* Method: setAirTime(String airTime) 
* 
*/ 
@Test
public void testSetAirTime() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "description", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setAirTime("airTime2");
  assertEquals("setAirTime", "airTime2", episode.getAirTime());

  try {
    episode.setAirTime(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: getProgrammeTitle() 
* 
*/ 
@Test
public void testGetProgrammeTitle() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getProgrammeTitle", "programmeTitle", episode.getProgrammeTitle());
} 

/** 
* 
* Method: setProgrammeTitle(String programmeTitle) 
* 
*/ 
@Test
public void testSetProgrammeTitle() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setProgrammeTitle("programmeTitle2");
  assertEquals("testSetProgrammeTitle", "programmeTitle2", episode.getProgrammeTitle());

  try {
    episode.setProgrammeTitle(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: setEpisodeTitle(String episodeTitle) 
* 
*/ 
@Test
public void testSetEpisodeTitle() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setEpisodeTitle("episodeTitle2");
  assertEquals("setEpisodeTitle", "episodeTitle2", episode.getEpisodeTitle());

  try {
    episode.setEpisodeTitle(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: setDescription(String description) 
* 
*/ 
@Test
public void testSetDescription() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setDescription("descripton2");
  assertEquals("setDescription", "descripton2", episode.getDescription());

  try {
    episode.setDescription(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: setIconUrl(String iconUrl) 
* 
*/ 
@Test
public void testSetIconUrl() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setIconUrl("iconUrl2");
  assertEquals("setIconUrl", "iconUrl2", episode.getIconUrl());

  try {
    episode.setIconUrl(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: setServiceUrl(String serviceUrl) 
* 
*/ 
@Test
public void testSetServiceUrl() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setServiceUrl("serviceUrl2");
  assertEquals("setServiceUrl", "serviceUrl2", episode.getServiceUrl());

  try {
    episode.setServiceUrl(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: getIconUrl() 
* 
*/ 
@Test
public void testGetIconUrl() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getIconUrl", "iconUrl", episode.getIconUrl());
} 

/** 
* 
* Method: getEpisodeTitle() 
* 
*/ 
@Test
public void testGetEpisodeTitle() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getEpisodeTitle", "episodeTitle", episode.getEpisodeTitle());
} 

/** 
* 
* Method: getServiceUrl() 
* 
*/ 
@Test
public void testGetServiceUrl() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getServiceUrl", "serviceUrl", episode.getServiceUrl());
} 

/** 
* 
* Method: getDescription() 
* 
*/ 
@Test
public void testGetDescription() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getDescription", "descripton", episode.getDescription());
} 

/** 
* 
* Method: getChannel() 
* 
*/ 
@Test
public void testGetChannel() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getChannel", "channel", episode.getChannel());
} 

/** 
* 
* Method: setChannel(String channel) 
* 
*/ 
@Test
public void testSetChannel() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setChannel("channel2");
  assertEquals("setChannel", "channel2", episode.getChannel());

  try {
    episode.setChannel(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: getCategory() 
* 
*/ 
@Test
public void testGetCategory() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getCategory", "category", episode.getGenres().iterator().next());
} 

/** 
* 
* Method: setCategory(String category) 
* 
*/ 
@Test
public void testSetCategory() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.addGenre("category2");
    Iterator<String> genres = episode.getGenres().iterator();
    assertEquals("addCategory", "category", genres.next());
    assertEquals("addCategory", "category2", genres.next());

}

/** 
* 
* Method: getSeries() 
* 
*/ 
@Test
public void testGetSeries() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getSeries", "series", episode.getSeries());
} 

/** 
* 
* Method: setSeries(String series) 
* 
*/ 
@Test
public void testSetSeries() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setSeries("series2");
  assertEquals("setSeries", "series2", episode.getSeries());

  try {
    episode.setSeries(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: getEpisode() 
* 
*/ 
@Test
public void testGetEpisode() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getEpisode", "episode", episode.getEpisode());
} 

/** 
* 
* Method: setEpisode(String episode) 
* 
*/ 
@Test
public void testSetEpisode() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  episode.setEpisode("episode2");
  assertEquals("setEpisode", "episode2", episode.getEpisode());

  try {
    episode.setEpisode(null);
  } catch (NullPointerException e) {
    return;
  }

  fail("Should have thrown NullPointerException");
} 

/** 
* 
* Method: getPodcastTitle() 
* 
*/ 
@Test
public void testGetPodcastTitle() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "1",
                                "2", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("getPodcastTitle", "programmeTitle - seriesTitle - episodeTitle", episode.getPodcastTitle());

    episode = new Episode("sourceId", "id", "", "seriesTitle", "episodeTitle", "1",
            "2", "descripton", "iconUrl", "serviceUrl", "airDate",
            "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    assertEquals("getPodcastTitle", "seriesTitle - episodeTitle", episode.getPodcastTitle());

    episode = new Episode("sourceId", "id", "", "" , "episodeTitle", "1",
            "2", "descripton", "iconUrl", "serviceUrl", "airDate",
            "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    assertEquals("getPodcastTitle", "Series 1 - episodeTitle", episode.getPodcastTitle());

    episode = new Episode("sourceId", "id", "programmeTitle", "", "episodeTitle", "1",
            "2", "descripton", "iconUrl", "serviceUrl", "airDate",
            "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    assertEquals("getPodcastTitle", "programmeTitle - Series 1 - episodeTitle", episode.getPodcastTitle());

    episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "", "1",
            "2", "descripton", "iconUrl", "serviceUrl", "airDate",
            "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    assertEquals("getPodcastTitle", "programmeTitle - seriesTitle - Episode 2", episode.getPodcastTitle());

    episode = new Episode("sourceId", "id", "programmeTitle", "", "", "",
            "", "descripton", "iconUrl", "serviceUrl", "airDate",
            "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
    assertEquals("getPodcastTitle", "programmeTitle", episode.getPodcastTitle());

 }

/** 
* 
* Method: toString() 
* 
*/ 
@Test
public void testToString() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle", "series",
                                "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                "airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  assertEquals("toString", "id", episode.toString());
} 


} 
