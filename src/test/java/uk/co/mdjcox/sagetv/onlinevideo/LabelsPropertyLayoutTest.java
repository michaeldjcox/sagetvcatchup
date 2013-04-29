package uk.co.mdjcox.sagetv.onlinevideo; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.fail;

/** 
* LabelsPropertyLayout Tester. 
* 
* @author <Authors name> 
* @since <pre>Apr 26, 2013</pre> 
* @version 1.0 
*/ 
public class LabelsPropertyLayoutTest { 

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
  LabelsPropertyLayout layout = new LabelsPropertyLayout();

  String comment = getExpectedComment("labelsHeaderComment");

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
  LabelsPropertyLayout layout = new LabelsPropertyLayout();

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

  LabelsPropertyLayout layout = new LabelsPropertyLayout();

  HashMap<String, String> comments = layout.getPrePropComments();

  assertEquals("Pre props", 2, comments.size());

  Iterator<Map.Entry<String,String>> itr = comments.entrySet().iterator();
  Map.Entry<String, String> entry = itr.next();

  String comment = getExpectedComment("labelsPre1Comment");

  assertEquals("Pre prop #1 key", "^Source.*", entry.getKey());
  assertEquals("Pre prop #1 value", comment, entry.getValue());

  entry = itr.next();

  comment = getExpectedComment("labelsPre2Comment");

//  checkResult(comment, entry.getValue());

  assertEquals("Pre prop #2 key", "^Category.*", entry.getKey());
  assertEquals("Pre prop #2 value", comment, entry.getValue());

} 

/** 
* 
* Method: getPostPropComments() 
* 
*/ 
@Test
public void testGetPostPropComments() throws Exception {
  LabelsPropertyLayout layout = new LabelsPropertyLayout();

  HashMap<String, String> comments = layout.getPostPropComments();

  assertEquals("Post props", 0, comments.size());

} 

/** 
* 
* Method: getComparator(Properties props) 
* 
*/ 
@Test
public void testGetComparator() throws Exception {
  LabelsPropertyLayout layout = new LabelsPropertyLayout();
  Properties test = new Properties();
  test.put("Source/xPodcastCustom/LongName","Custom Test Links");
  test.put("Source/xPodcastCustom/ShortName","Custom Test Links");
  test.put("Source/xPodcastPhotoshop/LongName","Photoshop");
  test.put("Source/xPodcastPhotoshop/ShortName","Photoshop");
  test.put("Category/AACustomSubCat/LongName","Subcategory 1 list name");
  test.put("Category/AACustomSubCat/ShortName","Subcat 1 name");
  test.put("Source/xPodcastAACustomSubCat/LongName","Subcategory 1 menu title");
  test.put("Source/xPodcastAACustomSubCat/ShortName","Subcat 1 title");
  test.put("Category/AACustomSubCat2/LongName","Subcategory 2 list name");
  test.put("Category/AACustomSubCat2/ShortName","Subcat 2 name");
  test.put("Source/xPodcastAACustomSubCat2/LongName","Subcategory 2 menu title");
  test.put("Source/xPodcastAACustomSubCat2/ShortName","Subcat 2 title");
  test.put("Category/ZZCustomSubCat/LongName","ZZ Subcategory at end of list");
  test.put("Category/ZZCustomSubCat/ShortName","ZZ Subcat at list end");
  test.put("Source/xPodcastZZCustomSubCat/LongName","ZZ Subcategory menu title");
  test.put("Source/xPodcastZZCustomSubCat/ShortName","ZZ Subcat title");
  test.put("Category/RusselBrownShow/ThumbURL","http://rss.adobe.com/www/special/rbrown/BannerArtjpg.jpg");
  test.put("Category/RusselBrownShow/LongName","The Russell Brown Show");
  test.put("Category/RusselBrownShow/ShortName","The Russell Brown Show");
  test.put("Category/PhotoshopQuickTips/ThumbURL","http://libsyn.com/podcasts/photoshopquicktips/images/quicktipslarge.jpg");
  test.put("Category/PhotoshopQuickTips/LongName","Photoshop Quicktips");
  test.put("Category/PhotoshopQuickTips/ShortName","Photoshop Quicktips");
  test.put("Category/PhotoshopPixelPerfect/ThumbURL","http://revision3.com/static/images/shows/pixelperfect/pixelperfect.jpg");
  test.put("Category/PhotoshopPixelPerfect/LongName","PixelPerfect (HD Quicktime)");
  test.put("Category/PhotoshopPixelPerfect/ShortName","PixelPerfect (HD Quicktime)");
  test.put("Category/PhotoshopUserTV/ThumbURL","http://photoshopusertv.com/wp-images/photoshopusertv/psutv-144.jpg");
  test.put("Category/PhotoshopUserTV/LongName","Photoshop User TV");
  test.put("Category/PhotoshopUserTV/ShortName","Photoshop User TV");
  test.put("Category/PhotoshopOnline/ThumbURL","http://www.feedburner.com/fb/images/pub/fb_pwrd.gif");
  test.put("Category/PhotoshopOnline/LongName","Photoshop Online");
  test.put("Category/PhotoshopOnline/ShortName","Photoshop Online");
  test.put("Category/DrPhotoshop/LongName","Dr. Photoshop News");
  test.put("Category/DrPhotoshop/ShortName","Dr. Photoshop News");
  test.put("Category/Lost/ThumbURL","http://a.abc.com/primetime/lost/images/podcast/300x300_lost_20051109.jpg");
  test.put("Category/Lost/LongName","The Official LOST Podcast");
  test.put("Category/Lost/ShortName","The Official LOST Podcast");
  test.put("Category/ShowtimeTudors/LongName","Showtime: The Tudors");
  test.put("Category/ShowtimeTudors/ShortName","Showtime: The Tudors");
  test.put("Category/ShowtimeDexter/LongName","Showtime: Dexter");
  test.put("Category/ShowtimeDexter/ShortName","Showtime: Dexter");
  test.put("Category/ShowtimeLWword/LongName","Showtime: The L Word");
  test.put("Category/ShowtimeLWword/ShortName","Showtime: The L Word");
  test.put("Category/ShowtimeWeeds/LongName","Showtime: Weeds");
  test.put("Category/ShowtimeWeeds/ShortName","Showtime: Weeds");
  test.put("Category/YouTubeCBSTest_YTV/ThumbURL","http://www.youtube.com/img/pic_youtubelogo_123x63.gif");
  test.put("Category/YouTubeCBSTest_YTV/LongName","Playlists of cbs");
  test.put("Category/YouTubeCBSTest_YTV/ShortName","Playlists of cbs");
  test.put("Category/YouTubeCBSSTPlaylist6_YTV/ThumbURL","http://www.youtube.com/img/pic_youtubelogo_123x63.gif");
  test.put("Category/YouTubeCBSSTPlaylist6_YTV/LongName","Star Trek");
  test.put("Category/YouTubeCBSSTPlaylist6_YTV/ShortName","Star Trek");
  test.put("Category/YouTubeCBSSTPlaylist8_YTV/ThumbURL","http://www.youtube.com/img/pic_youtubelogo_123x63.gif");
  test.put("Category/YouTubeCBSSTPlaylist8_YTV/LongName","Star Trek");
  test.put("Category/YouTubeCBSSTPlaylist8_YTV/ShortName","Star Trek");

  Comparator comp = layout.getComparator(test);
  assertNotNull("getComparator() should return something", comp);

  TreeMap<Object, Object> test2 = new TreeMap<Object,Object>(comp);
  test2.putAll(test);


  Iterator<Map.Entry<Object,Object>> itr = test2.entrySet().iterator();
  Map.Entry<Object,Object> entry = null;

  entry=itr.next(); assertEquals("Item out of order", "Source/xPodcastCustom/LongName=Custom Test Links", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Source/xPodcastCustom/ShortName=Custom Test Links", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Source/xPodcastPhotoshop/LongName=Photoshop", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Source/xPodcastPhotoshop/ShortName=Photoshop", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Source/xPodcastAACustomSubCat/LongName=Subcategory 1 menu title", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Source/xPodcastAACustomSubCat/ShortName=Subcat 1 title", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Source/xPodcastAACustomSubCat2/LongName=Subcategory 2 menu title", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Source/xPodcastAACustomSubCat2/ShortName=Subcat 2 title", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Source/xPodcastZZCustomSubCat/LongName=ZZ Subcategory menu title", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Source/xPodcastZZCustomSubCat/ShortName=ZZ Subcat title", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/AACustomSubCat/LongName=Subcategory 1 list name", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/AACustomSubCat/ShortName=Subcat 1 name", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/AACustomSubCat2/LongName=Subcategory 2 list name", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/AACustomSubCat2/ShortName=Subcat 2 name", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/ZZCustomSubCat/LongName=ZZ Subcategory at end of list", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/ZZCustomSubCat/ShortName=ZZ Subcat at list end", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/RusselBrownShow/ThumbURL=http://rss.adobe.com/www/special/rbrown/BannerArtjpg.jpg", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/RusselBrownShow/LongName=The Russell Brown Show", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/RusselBrownShow/ShortName=The Russell Brown Show", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopQuickTips/ThumbURL=http://libsyn.com/podcasts/photoshopquicktips/images/quicktipslarge.jpg", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopQuickTips/LongName=Photoshop Quicktips", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopQuickTips/ShortName=Photoshop Quicktips", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopPixelPerfect/ThumbURL=http://revision3.com/static/images/shows/pixelperfect/pixelperfect.jpg", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopPixelPerfect/LongName=PixelPerfect (HD Quicktime)", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopPixelPerfect/ShortName=PixelPerfect (HD Quicktime)", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopUserTV/ThumbURL=http://photoshopusertv.com/wp-images/photoshopusertv/psutv-144.jpg", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopUserTV/LongName=Photoshop User TV", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopUserTV/ShortName=Photoshop User TV", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopOnline/ThumbURL=http://www.feedburner.com/fb/images/pub/fb_pwrd.gif", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopOnline/LongName=Photoshop Online", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/PhotoshopOnline/ShortName=Photoshop Online", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/DrPhotoshop/LongName=Dr. Photoshop News", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/DrPhotoshop/ShortName=Dr. Photoshop News", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/Lost/ThumbURL=http://a.abc.com/primetime/lost/images/podcast/300x300_lost_20051109.jpg", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/Lost/LongName=The Official LOST Podcast", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/Lost/ShortName=The Official LOST Podcast", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/ShowtimeTudors/LongName=Showtime: The Tudors", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/ShowtimeTudors/ShortName=Showtime: The Tudors", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/ShowtimeDexter/LongName=Showtime: Dexter", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/ShowtimeDexter/ShortName=Showtime: Dexter", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/ShowtimeLWword/LongName=Showtime: The L Word", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/ShowtimeLWword/ShortName=Showtime: The L Word", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/ShowtimeWeeds/LongName=Showtime: Weeds", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/ShowtimeWeeds/ShortName=Showtime: Weeds", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/YouTubeCBSTest_YTV/ThumbURL=http://www.youtube.com/img/pic_youtubelogo_123x63.gif", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/YouTubeCBSTest_YTV/LongName=Playlists of cbs", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/YouTubeCBSTest_YTV/ShortName=Playlists of cbs", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/YouTubeCBSSTPlaylist6_YTV/ThumbURL=http://www.youtube.com/img/pic_youtubelogo_123x63.gif", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/YouTubeCBSSTPlaylist6_YTV/LongName=Star Trek", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/YouTubeCBSSTPlaylist6_YTV/ShortName=Star Trek", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/YouTubeCBSSTPlaylist8_YTV/ThumbURL=http://www.youtube.com/img/pic_youtubelogo_123x63.gif", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/YouTubeCBSSTPlaylist8_YTV/LongName=Star Trek", entry.getKey() + "=" + entry.getValue());
  entry=itr.next(); assertEquals("Item out of order", "Category/YouTubeCBSSTPlaylist8_YTV/ShortName=Star Trek", entry.getKey() + "=" + entry.getValue());


} 


} 
