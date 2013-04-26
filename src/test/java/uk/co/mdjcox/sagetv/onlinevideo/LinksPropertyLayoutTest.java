package uk.co.mdjcox.sagetv.onlinevideo; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.fail;

/** 
* LinksPropertyLayout Tester. 
* 
* @author <Authors name> 
* @since <pre>Apr 26, 2013</pre> 
* @version 1.0 
*/ 
public class LinksPropertyLayoutTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: getHeadComment() 
* 
*/ 
@Test
public void testGetHeadComment() throws Exception {
  LinksPropertyLayout layout = new LinksPropertyLayout();

  String comment = getExpectedComment("linkHeaderComment");

  String header = layout.getHeadComment();

  checkResult(comment, header);

  assertEquals("Header comment", comment, header);

}

  private void checkResult(String comment, String header) {
    int max = Math.max(comment.length(), header.length());

    System.err.println("MAX=" + max + " expected=" + comment.length() + " found=" + header.length());

    for (int i =0; i< max ; i++) {
      if (comment.charAt(i) != header.charAt(i)) {
        String expected = String.valueOf(comment.charAt(i));
        String actual = String.valueOf(header.charAt(i));

        if (expected.equals("\n")) expected = "LF";
        if (expected.equals("\r")) expected = "CR";
        if (actual.equals("\n")) actual = "LF";
        if (actual.equals("\r")) actual = "CR";
        System.err.println(i + " " + expected + ":" + actual);
      }
    }
  }

  private String getExpectedComment(String commentTag) throws IOException {
    java.net.URL url = getClass().getResource(commentTag + ".txt");
    String filename = url.getFile();
    String comment = "";
    FileReader reader = new FileReader(filename);
    BufferedReader breader = new BufferedReader(reader);
    try {
      out: while (true) {
        String newbit = breader.readLine();
        if (!comment.isEmpty()) {
          comment += System.getProperty("line.separator");
        }
        if (newbit==null) break out;
        comment = comment + newbit;
      }
    } catch (Exception e) {
    } finally {
      breader.close();
      reader.close();
    }
    return comment;
  }

  /**
* 
* Method: getTailComment() 
* 
*/ 
@Test
public void testGetTailComment() throws Exception {
  LinksPropertyLayout layout = new LinksPropertyLayout();

  String comment = "";

  String tail = layout.getTailComment();

  checkResult(comment, tail);

  assertEquals("Tail comment", comment, tail);
}

/** 
* 
* Method: getPrePropComments() 
* 
*/ 
@Test
public void testGetPrePropComments() throws Exception {

  LinksPropertyLayout layout = new LinksPropertyLayout();

  HashMap<String, String> comments = layout.getPrePropComments();

  assertEquals("Pre props", 1, comments.size());

  Map.Entry<String, String> entry = comments.entrySet().iterator().next();

  String comment = getExpectedComment("linkPre1Comment");

  assertEquals("Pre prop #1 key", "CustomSources", entry.getKey());
  assertEquals("Pre prop #1 value", comment, entry.getValue());
} 

/** 
* 
* Method: getPostPropComments() 
* 
*/ 
@Test
public void testGetPostPropComments() throws Exception {
  LinksPropertyLayout layout = new LinksPropertyLayout();

  HashMap<String, String> comments = layout.getPostPropComments();

  assertEquals("Pre props", 2, comments.size());

  Map.Entry<String, String> entry = comments.entrySet().iterator().next();

  String comment = getExpectedComment("linkPost1Comment");

  assertEquals("Pre prop #1 key", "CustomSources", entry.getKey());
  assertEquals("Pre prop #1 value", comment, entry.getValue());
} 

/** 
* 
* Method: getComparator(final Properties props) 
* 
*/ 
@Test
public void testGetComparator() throws Exception { 
fail("Undefined test");
//TODO: Test goes here... 
} 


} 
