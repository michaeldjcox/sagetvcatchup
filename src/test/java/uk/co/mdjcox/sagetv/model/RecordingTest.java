package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import java.io.File;

import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertEquals;

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
    Recording recording = new Recording(null);
  } catch (NullPointerException e) {
    return;
  }
  fail("Should have thrown a NullPointerException");

}

  @Test
  public void testSetFile() {
    try {
      Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle",
                                    "series", "episode", "description", "iconUrl", "serviceUrl",
                                    "airDate","airTime", "channel", Sets.newHashSet("category"));
      Recording recording = new Recording(episode);

      recording.setFile(null);
    } catch (NullPointerException e) {
      return;
    }
    fail("Should have thrown a NullPointerException");

  }

/** 
* 
* Method: getFile() 
* 
*/ 
@Test
public void testSetGetFile() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));
  File file = new File("filename");

  Recording recording = new Recording(episode);

  File file3 = recording.getFile();

  assertEquals("getFile intially null", null, file3);

  recording.setFile(file);

  File file2 = recording.getFile();

  assertEquals("getFile", file, file2);
}


/** 
* 
* Method: getSourceId() 
* 
*/ 
@Test
public void testGetSourceId() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));
  Recording recording = new Recording(episode);

  assertEquals("getSourceId", "sourceId", recording.getSourceId());
}

/** 
* 
* Method: getUrl() 
* 
*/ 
@Test
public void testGetUrl() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));

  Recording recording = new Recording(episode);

  assertEquals("getUrl", "serviceUrl", recording.getUrl());
} 

/** 
* 
* Method: getId() 
* 
*/ 
@Test
public void testGetId() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));

  Recording recording = new Recording(episode);

  assertEquals("getId", "id", recording.getId());
}

/** 
* 
* Method: getName() 
* 
*/ 
@Test
public void testGetName() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));

  Recording recording = new Recording(episode);

  assertEquals("getName", "episodeTitle", recording.getName());
}

/** 
* 
* Method: getFilename() 
* 
*/ 
@Test
public void testGetFilename() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));


  Recording recording = new Recording(episode);

  File file = new File("filename");

  recording.setFile(file);

  assertEquals("getFileName", System.getProperty("user.dir") + File.separator + "filename", recording.getFilename());
}

  @Test
  public void testGetFilenameNoFile() throws Exception {
    Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle",
                                  "series", "episode", "description", "iconUrl", "serviceUrl",
                                  "airDate","airTime", "channel", Sets.newHashSet("category"));


    Recording recording = new Recording(episode);

    assertEquals("getFileName", "", recording.getFilename());
  }

/** 
* 
* Method: toString() 
* 
*/ 
@Test
public void testToString() throws Exception {
  Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle",
                                "series", "episode", "description", "iconUrl", "serviceUrl",
                                "airDate","airTime", "channel", Sets.newHashSet("category"));


  Recording recording = new Recording(episode);

  assertEquals("toString", episode.toString(), recording.toString());

}


} 
