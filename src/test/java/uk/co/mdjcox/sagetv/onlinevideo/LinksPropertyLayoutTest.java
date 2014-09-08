package uk.co.mdjcox.sagetv.onlinevideo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

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

//  checkResult(comment, header);

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
      boolean doneNothing = true;
      out: while (true) {
        String newbit = breader.readLine();
        if (!doneNothing) {
          comment += System.getProperty("line.separator");
        }
        if (newbit==null) break out;
        comment = comment + newbit;
        doneNothing = false;
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

//  checkResult(comment, tail);

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

  assertEquals("Post props", 2, comments.size());

  Iterator itr = comments.entrySet().iterator();

  Map.Entry<String, String> entry = (Map.Entry<String, String>)itr.next();

  String comment = getExpectedComment("linkPost1Comment");

  assertEquals("Pre prop #1 key", "CustomSources", entry.getKey());
  assertEquals("Pre prop #1 value", comment, entry.getValue());

  entry = (Map.Entry<String, String>)itr.next();

  assertEquals("Pre prop #2 key", ".*\\/CategoryName", entry.getKey());
  assertEquals("Pre prop #2 value", "", entry.getValue());
}

/** 
* 
* Method: getComparator(final Properties props) 
* 
*/ 
@Test
public void testGetComparator() throws Exception {
  LinksPropertyLayout layout = new LinksPropertyLayout();
  Properties test = new Properties();

  test.put("CustomSources","xPodcastPhotoshop,xPodcastCustom");
  test.put("xFeedPodcastCustom/AACustomSubCat","xPodcastCustom;xURLNone");
  test.put("AACustomSubCat/IsCategory","true");
  test.put("AACustomSubCat/CategoryName","xPodcastAACustomSubCat");
  test.put("xFeedPodcastCustom/AACustomSubCat2","xPodcastAACustomSubCat;xURLNone");
  test.put("AACustomSubCat2/IsCategory","true");
  test.put("AACustomSubCat2/CategoryName","xPodcastAACustomSubCat2");
  test.put("xFeedPodcastCustom/ZZCustomSubCat","xPodcastCustom;xURLNone");
  test.put("ZZCustomSubCat/IsCategory","true");
  test.put("ZZCustomSubCat/CategoryName","xPodcastZZCustomSubCat");
  test.put("xFeedPodcastCustom/RusselBrownShow","xPodcastPhotoshop,xPodcastAACustomSubCat;http://rss.adobe.com/www/special/rbrown_show.rss");
  test.put("xFeedPodcastCustom/PhotoshopQuickTips","xPodcastPhotoshop,xPodcastAACustomSubCat;http://photoshopquicktips.libsyn.com/rss");
  test.put("xFeedPodcastCustom/PhotoshopPixelPerfect","xPodcastPhotoshop,xPodcastAACustomSubCat2;http://revision3.com/pixelperfect/feed/quicktime-high-definition");
  test.put("xFeedPodcastCustom/PhotoshopUserTV","xPodcastPhotoshop,xPodcastAACustomSubCat2;http://www.photoshopusertv.com/feed");
  test.put("xFeedPodcastCustom/PhotoshopOnline","xPodcastPhotoshop,xPodcastZZCustomSubCat;http://feeds.feedburner.com/photoshoponline");
  test.put("xFeedPodcastCustom/DrPhotoshop","xPodcastPhotoshop,xPodcastZZCustomSubCat;http://dr-photoshop.com/rss");
  test.put("xFeedPodcastCustom/Lost","xPodcastCustom;http://abc.go.com/primetime/lost/podcastRSS?feedPublishKey");
  test.put("xFeedPodcastCustom/ShowtimeTudors","xPodcastCustom;http://www.sho.com/site/tudors/xml/podcasts.xml");
  test.put("xFeedPodcastCustom/ShowtimeDexter","xPodcastCustom;http://www.sho.com/site/dexter/xml/podcasts.xml");
  test.put("xFeedPodcastCustom/ShowtimeLWword","xPodcastCustom;http://www.sho.com/site/lword/xml/podcasts.xml");
  test.put("xFeedPodcastCustom/ShowtimeWeeds","xPodcastCustom;http://www.sho.com/site/weeds/xml/podcasts.xml");

  Comparator comp = layout.getComparator(test);
  assertNotNull("getComparator() should return something", comp);

  TreeMap<Object, Object> test2 = new TreeMap<Object,Object>(comp);
  test2.putAll(test);


  Iterator<Map.Entry<Object,Object>> itr = test2.entrySet().iterator();
  Map.Entry<Object,Object> entry = null;
  entry=itr.next(); assertEquals("Item out of order", "CustomSources=xPodcastPhotoshop,xPodcastCustom", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/AACustomSubCat=xPodcastCustom;xURLNone", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "AACustomSubCat/IsCategory=true", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "AACustomSubCat/CategoryName=xPodcastAACustomSubCat", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/AACustomSubCat2=xPodcastAACustomSubCat;xURLNone", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "AACustomSubCat2/IsCategory=true", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "AACustomSubCat2/CategoryName=xPodcastAACustomSubCat2", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/ZZCustomSubCat=xPodcastCustom;xURLNone", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "ZZCustomSubCat/IsCategory=true", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "ZZCustomSubCat/CategoryName=xPodcastZZCustomSubCat", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/DrPhotoshop=xPodcastPhotoshop,xPodcastZZCustomSubCat;http://dr-photoshop.com/rss", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/Lost=xPodcastCustom;http://abc.go.com/primetime/lost/podcastRSS?feedPublishKey", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/PhotoshopOnline=xPodcastPhotoshop,xPodcastZZCustomSubCat;http://feeds.feedburner.com/photoshoponline", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/PhotoshopPixelPerfect=xPodcastPhotoshop,xPodcastAACustomSubCat2;http://revision3.com/pixelperfect/feed/quicktime-high-definition", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/PhotoshopQuickTips=xPodcastPhotoshop,xPodcastAACustomSubCat;http://photoshopquicktips.libsyn.com/rss", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/PhotoshopUserTV=xPodcastPhotoshop,xPodcastAACustomSubCat2;http://www.photoshopusertv.com/feed", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/RusselBrownShow=xPodcastPhotoshop,xPodcastAACustomSubCat;http://rss.adobe.com/www/special/rbrown_show.rss", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/ShowtimeDexter=xPodcastCustom;http://www.sho.com/site/dexter/xml/podcasts.xml", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/ShowtimeLWword=xPodcastCustom;http://www.sho.com/site/lword/xml/podcasts.xml", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/ShowtimeTudors=xPodcastCustom;http://www.sho.com/site/tudors/xml/podcasts.xml", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "xFeedPodcastCustom/ShowtimeWeeds=xPodcastCustom;http://www.sho.com/site/weeds/xml/podcasts.xml", entry.getKey() + "=" + entry.getValue());





}


} 
