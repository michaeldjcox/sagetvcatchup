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

  int max = Math.max(comment.length(), header.length());

  System.err.println("MAX=" + max + " expected=" + comment.length() + " found=" + header.length());

  for (int i =0; i< max ; i++) {
    System.err.println(comment.charAt(i) + ":" + header.charAt(i));
  }


  assertEquals("Header comment", comment, header);



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
          comment += "\r\n";
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
fail("Undefined test");
//TODO: Test goes here... 
} 

/** 
* 
* Method: getPrePropComments() 
* 
*/ 
@Test
public void testGetPrePropComments() throws Exception { 
fail("Undefined test");
//TODO: Test goes here... 
} 

/** 
* 
* Method: getPostPropComments() 
* 
*/ 
@Test
public void testGetPostPropComments() throws Exception { 
fail("Undefined test");
//TODO: Test goes here... 
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
