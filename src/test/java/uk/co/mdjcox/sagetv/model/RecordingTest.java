package uk.co.mdjcox.sagetv.model;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.catchup.CatchupTestModule;

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

  private CatchupTestModule module = new CatchupTestModule();
  private Injector injector = Guice.createInjector(module);
  private CatchupContextInterface context;

  @Before
public void before() throws Exception {
    context = injector.getInstance(CatchupContextInterface.class);
  }

@After
public void after() throws Exception { 
} 


@Test
public void testCreate() {
  try {
    new Recording(null, null, true, false);
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
                                    "airDate","airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
      Recording recording = new Recording(episode, context.getRecordingDir(), true, false);

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
                                "airDate","airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  File file = new File("filename");

  Recording recording = new Recording(episode, context.getRecordingDir(), true, false);

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
                                "airDate","airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));
  Recording recording = new Recording(episode, context.getRecordingDir(), true, false);

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
                                "airDate","airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));

  Recording recording = new Recording(episode, context.getRecordingDir(), true, false);

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
                                "airDate","airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));

  Recording recording = new Recording(episode, context.getRecordingDir(), true, false);

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
                                "airDate","airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));

  Recording recording = new Recording(episode, context.getRecordingDir(), true, false);

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
                                "airDate","airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));


  Recording recording = new Recording(episode, context.getRecordingDir(), true, false);

  File file = new File(context.getRecordingDir() + File.separator + "filename");

  recording.setPartialFile(file);

  assertEquals("getPartialFileName", context.getRecordingDir() + File.separator + "filename", recording.getPartialFilename());
}

  @Test
  public void testGetPartialFilenameNoFile() throws Exception {
    Episode episode = new Episode("sourceId", "id", "programmeTitle", "seriesTitle", "episodeTitle",
                                  "series", "episode", "description", "iconUrl", "serviceUrl",
                                  "airDate","airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));


    Recording recording = new Recording(episode, context.getRecordingDir(), true, false);

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
                                "airDate","airTime", "origAirDate", "origAirTime",  "channel", Sets.newHashSet("category"));


  Recording recording = new Recording(episode, context.getRecordingDir(), true, false);

  assertEquals("toString", episode.toString(), recording.toString());

}


} 
