package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import java.io.File;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/** 
* Recording Tester. 
* 
* @author <Authors name> 
* @since <pre>Apr 19, 2013</pre> 
* @version 1.0 
*/ 
public class RecordingTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 


@Test
public void testCreate() {
  try {
    new Recording(null, null, true);
  } catch (NullPointerException e) {
    return;
  }
  fail("Should have thrown a NullPointerException");

}

  @Test
  public void testSetPartialFile() {
    try {
      Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle",
                                    "series", "episode", "description", "iconUrl", "serviceUrl",
                                    "airDate","airTime", "channel", Sets.newHashSet("category"));
      Recording recording = new Recording(episode, System.getProperty("user.dir"), true);

      recording.setPartialFile(null);
    } catch (NullPointerException e) {
      return;
    }
    fail("Should have thrown a NullPointerException");

  }

/** 
* 
* Method: getPartialFile()
* 
*/ 
@Test
public void testSetGetPartialFile() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));
  File file = new File("filename");

  Recording recording = new Recording(episode, System.getProperty("user.dir"), true);

  File file3 = recording.getPartialFile();

  assertEquals("getPartialFile intially null", null, file3);

  recording.setPartialFile(file);

  File file2 = recording.getPartialFile();

  assertEquals("getPartialFile", file, file2);
}


/** 
* 
* Method: getSourceId() 
* 
*/ 
@Test
public void testGetSourceId() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));
  Recording recording = new Recording(episode, System.getProperty("user.dir"), true);

  assertEquals("getSourceId", "sourceId", recording.getSourceId());
}

/** 
* 
* Method: getUrl() 
* 
*/ 
@Test
public void testGetUrl() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));

  Recording recording = new Recording(episode, System.getProperty("user.dir"), true);

  assertEquals("getUrl", "serviceUrl", recording.getUrl());
} 

/** 
* 
* Method: getId() 
* 
*/ 
@Test
public void testGetId() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));

  Recording recording = new Recording(episode, System.getProperty("user.dir"), true);

  assertEquals("getId", "id", recording.getId());
}

/** 
* 
* Method: getName()
*
*/
@Test
public void testGetName() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));

  Recording recording = new Recording(episode, System.getProperty("user.dir"), true);

  assertEquals("getName", episode.getPodcastTitle(), recording.getName());
}

/**
*
* Method: getPartialFilename()
* 
*/ 
@Test
public void testGetPartialFilename() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));


  Recording recording = new Recording(episode, System.getProperty("user.dir"), true);

  File file = new File("filename");

  recording.setPartialFile(file);

  assertEquals("getPartialFileName", System.getProperty("user.dir") + File.separator + "filename", recording.getPartialFilename());
}

  @Test
  public void testGetPartialFilenameNoFile() throws Exception {
    Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle",
                                  "series", "episode", "description", "iconUrl", "serviceUrl",
                                  "airDate","airTime", "channel", Sets.newHashSet("category"));


    Recording recording = new Recording(episode, System.getProperty("user.dir"), true);

    assertEquals("getPartialFileName", "", recording.getPartialFilename());
  }

/** 
* 
* Method: toString() 
* 
*/ 
@Test
public void testToString() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));


  Recording recording = new Recording(episode, System.getProperty("user.dir"), true);

  assertEquals("toString", episode.toString(), recording.toString());

}


} 
