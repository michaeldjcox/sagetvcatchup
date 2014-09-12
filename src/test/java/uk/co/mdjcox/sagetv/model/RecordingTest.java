package uk.co.mdjcox.sagetv.model;

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
    Recording recording = new Recording(null, null, null, null);
  } catch (NullPointerException e) {
    return;
  }
  fail("Should have thrown a NullPointerException");

}

  @Test
  public void testSetFile() {
    try {
      Recording recording = new Recording("sourceId", "id", "url", System.getProperty("user.dir"));

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
  File file = new File("filename");

  Recording recording = new Recording("sourceId", "id", "url", System.getProperty("user.dir"));

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
  Recording recording = new Recording("sourceId", "id", "url", System.getProperty("user.dir"));

  assertEquals("getSourceId", "sourceId", recording.getSourceId());
}

/** 
* 
* Method: getUrl() 
* 
*/ 
@Test
public void testGetUrl() throws Exception {

  Recording recording = new Recording("sourceId", "id", "url", System.getProperty("user.dir"));

  assertEquals("getUrl", "url", recording.getUrl());
} 

/** 
* 
* Method: getId() 
* 
*/ 
@Test
public void testGetId() throws Exception {

  Recording recording = new Recording("sourceId", "id", "url", System.getProperty("user.dir"));

  assertEquals("getId", "id", recording.getId());
}

/** 
* 
* Method: getFilename() 
* 
*/ 
@Test
public void testGetFilename() throws Exception {

  Recording recording = new Recording("sourceId", "id", "url", System.getProperty("user.dir"));

  File file = new File("filename");

  recording.setFile(file);

  assertEquals("getFileName", System.getProperty("user.dir") + File.separator + "filename", recording.getFilename());
}

  @Test
  public void testGetFilenameNoFile() throws Exception {

    Recording recording = new Recording("sourceId", "id", "url", System.getProperty("user.dir"));

    assertEquals("getFileName", "", recording.getFilename());
  }

/** 
* 
* Method: toString() 
* 
*/ 
@Test
public void testToString() throws Exception {

  Recording recording = new Recording("sourceId", "id", "url", System.getProperty("user.dir"));

  assertEquals("toString", "id", recording.toString());

}


} 
