package uk.co.mdjcox.sagetv.onlinevideo;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.mdjcox.sagetv.catchup.CatchupTestModule;
import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.Root;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.sagetv.model.SubCategory;
import uk.co.mdjcox.utils.PropertiesFile;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Publisher Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Mar 25, 2013</pre>
 */
public class PublisherTest {

  private Publisher publisher;

  @Before
  public void before() throws Exception {
    String tmpDir = System.getProperty("java.io.tmpdir", ".");
    File tmpDirFle = new File(tmpDir + File.separator + "OnlineVideos");
    if (tmpDirFle.exists()) {
      tmpDirFle.delete();
    }

    tmpDirFle.mkdir();

    CatchupTestModule module = new CatchupTestModule();
    Injector injector = Guice.createInjector(module);
    PublisherFactory publisherFactory = injector.getInstance(PublisherFactory.class);
    publisher = publisherFactory.createPublisher("test", tmpDir);

  }

  @After
  public void after() throws Exception {
  }

  /**
   * Method: unpublish(String file)
   */
  @Test
  public void testUnpublish() throws Exception {
    Method method = Publisher.class.getDeclaredMethod("getLinkFile", String.class);
    method.setAccessible(true);
    String linkFileName = (String) method.invoke(publisher, "test");

    method = Publisher.class.getDeclaredMethod("getLabelFile", String.class);
    method.setAccessible(true);
    String labelFileName = (String) method.invoke(publisher, "test");

    File linkFile = new File(linkFileName);
    File labelFile = new File(labelFileName);

    if (!linkFile.exists()) linkFile.createNewFile();
    if (!labelFile.exists()) labelFile.createNewFile();

    assertTrue("Link file should exist before test", linkFile.exists());
    assertTrue("Label file should exist before test", labelFile.exists());

    publisher.unpublish();

    assertFalse("Link file not should exist after test", linkFile.exists());
    assertFalse("Label file not should exist after test", labelFile.exists());
  }

  /**
   * Method: publish(Catalog catalog)
   */
  @Test
  public void testPublish() throws Exception {

    Catalog catalog = new Catalog();
    Root root = new Root("rootShortName", "rootLongName", "rootServiceUrl", "rootIconUrl");
    catalog.addCategory(root);
    Source source = new Source("sourceId", "sourceSourceName", "sourceLongName",
                               "sourceServiceUrl", "sourceIconUrl");
    catalog.addCategory(source);
    SubCategory subcat = new SubCategory("subcatId", "subcatShortName", "subcatLongName",
                                         "subcatServiceUrl", "subcatIconUrl", "sourceId");
    catalog.addCategory(subcat);
    SubCategory subcat2 = new SubCategory("subcatId2", "subcatShortName2", "subcatLongName2",
                                          "subcatServiceUrl2", "subcatIconUrl2", "sourceId");
    catalog.addCategory(subcat2);

    Programme programme = new Programme("programmeId", "programmeShortName", "programmeLongName",
                                        "programmeServiceUrl", "programmeIconUdl", "subcatId");
    programme.setPodcastUrl("podcastUrl");
    programme.addOtherParentId("subcatId2");
    catalog.addCategory(programme);

    Episode episode = new Episode("sourceId", "episodeId", "programmeTitle", "episodeTitle",
                                  "series", "episode", "description", "iconUrl", "serviceUrl",
                                  "airDate", "airTime", "channel", "category");
    programme.addEpisode(episode);

    publisher.publish(catalog);

    Method method = Publisher.class.getDeclaredMethod("getLinkFile", String.class);
    method.setAccessible(true);
    String linkFileName = (String) method.invoke(publisher, "test");
    PropertiesFile linkProps = new PropertiesFile(linkFileName, true);

    method = Publisher.class.getDeclaredMethod("getLabelFile", String.class);
    method.setAccessible(true);
    String labelFileName = (String) method.invoke(publisher, "test");
    PropertiesFile labelProps = new PropertiesFile(labelFileName, true);

    assertEquals("Link property count", 8, linkProps.size());
    assertEquals("Label property count", 20, labelProps.size());

    Iterator<Map.Entry<Object, Object>> itr = linkProps.entrySet().iterator();
    Map.Entry<Object, Object> next = itr.next();
    assertEquals("Property 1 key","subcatId2/IsCategory",next.getKey());
    assertEquals("Property 1 val","true",next.getValue());
    next = itr.next();
    assertEquals("Property 2 key","subcatId/IsCategory",next.getKey());
    assertEquals("Property 2 val","true",next.getValue());
    next = itr.next();
    assertEquals("Property 3 key","subcatId/CategoryName",next.getKey());
    assertEquals("Property 3 val","xPodcastsubcatId",next.getValue());
    next = itr.next();
    assertEquals("Property 4 key","CustomSources",next.getKey());
    assertEquals("Property 4 val","xPodcastsourceId",next.getValue());
    next = itr.next();
    assertEquals("Property 5 key","xFeedPodcastCustom/subcatId",next.getKey());
    assertEquals("Property 5 val","xPodcastsourceId;xURLNone",next.getValue());
    next = itr.next();
    assertEquals("Property 6 key","xFeedPodcastCustom/programmeId",next.getKey());
    assertEquals("Property 6 val","xPodcastsubcatId,xPodcastsubcatId2;podcastUrl",next.getValue());
    next = itr.next();
    assertEquals("Property 7 key","subcatId2/CategoryName",next.getKey());
    assertEquals("Property 7 val","xPodcastsubcatId2",next.getValue());
    next = itr.next();
    assertEquals("Property 8 key","xFeedPodcastCustom/subcatId2",next.getKey());
    assertEquals("Property 8 val","xPodcastsourceId;xURLNone",next.getValue());


    itr = labelProps.entrySet().iterator();
    next = itr.next();
    assertEquals("Property 1 key","Source/xPodcastsubcatId2/ShortName",next.getKey());
    assertEquals("Property 1 val","subcatShortName2",next.getValue());
    next = itr.next();
    assertEquals("Property 2 key","Source/xPodcastsubcatId2/ThumbURL",next.getKey());
    assertEquals("Property 2 val","subcatIconUrl2",next.getValue());
    next = itr.next();
    assertEquals("Property 3 key","Category/subcatId2/ShortName",next.getKey());
    assertEquals("Property 3 val","subcatShortName2",next.getValue());
    next = itr.next();
    assertEquals("Property 4 key","Source/xPodcastprogrammeId/ThumbURL",next.getKey());
    assertEquals("Property 4 val","programmeIconUdl",next.getValue());
    next = itr.next();
    assertEquals("Property 5 key","Category/subcatId2/ThumbURL",next.getKey());
    assertEquals("Property 5 val","subcatIconUrl2",next.getValue());
    next = itr.next();
    assertEquals("Property 6 key","Source/xPodcastsubcatId/LongName",next.getKey());
    assertEquals("Property 6 val","subcatLongName",next.getValue());
    next = itr.next();
    assertEquals("Property 7 key","Source/xPodcastsourceId/ShortName",next.getKey());
    assertEquals("Property 7 val","sourceSourceName",next.getValue());
    next = itr.next();
    assertEquals("Property 8 key","Source/xPodcastprogrammeId/ShortName",next.getKey());
    assertEquals("Property 8 val","programmeShortName",next.getValue());
    next = itr.next();
    assertEquals("Property 9 key","Source/xPodcastsourceId/LongName",next.getKey());
    assertEquals("Property 9 val","sourceLongName",next.getValue());
    next = itr.next();
    assertEquals("Property 10 key","Source/xPodcastsubcatId2/LongName",next.getKey());
    assertEquals("Property 10 val","subcatLongName2",next.getValue());
    next = itr.next();
    assertEquals("Property 11 key","Category/programmeId/ShortName",next.getKey());
    assertEquals("Property 11 val","programmeShortName",next.getValue());
    next = itr.next();
    assertEquals("Property 12 key","Category/programmeId/LongName",next.getKey());
    assertEquals("Property 12 val","programmeLongName",next.getValue());
    next = itr.next();
    assertEquals("Property 13 key","Source/xPodcastprogrammeId/LongName",next.getKey());
    assertEquals("Property 13 val","programmeLongName",next.getValue());
    next = itr.next();
    assertEquals("Property 14 key","Category/subcatId2/LongName",next.getKey());
    assertEquals("Property 14 val","subcatLongName2",next.getValue());
    next = itr.next();
    assertEquals("Property 15 key","Source/xPodcastsubcatId/ShortName",next.getKey());
    assertEquals("Property 15 val","subcatShortName",next.getValue());
    next = itr.next();
    assertEquals("Property 16 key","Source/xPodcastsubcatId/ThumbURL",next.getKey());
    assertEquals("Property 16 val","subcatIconUrl",next.getValue());
    next = itr.next();
    assertEquals("Property 17 key","Category/subcatId/ShortName",next.getKey());
    assertEquals("Property 17 val","subcatShortName",next.getValue());
    next = itr.next();
    assertEquals("Property 18 key","Category/subcatId/LongName",next.getKey());
    assertEquals("Property 18 val","subcatLongName",next.getValue());
    next = itr.next();
    assertEquals("Property 19 key","Category/programmeId/ThumbURL",next.getKey());
    assertEquals("Property 19 val","programmeIconUdl",next.getValue());
    next = itr.next();
    assertEquals("Property 20 key","Category/subcatId/ThumbURL",next.getKey());
    assertEquals("Property 20 val","subcatIconUrl",next.getValue());

  }


  /**
   * Method: getLinkFile(String file)
   */
  @Test
  public void testGetLinkFile() throws Exception {
    Method method = Publisher.class.getDeclaredMethod("getLinkFile", String.class);
    method.setAccessible(true);
    Object result = method.invoke(publisher, "test");
    assertEquals("getLinkFile()", "/tmp/OnlineVideos/CustomOnlineVideoLinks_test.properties", result);
  }

  /**
   * Method: getLabelFile(String file)
   */
  @Test
  public void testGetLabelFile() throws Exception {
    Method method = Publisher.class.getDeclaredMethod("getLabelFile", String.class);
    method.setAccessible(true);
    Object result = method.invoke(publisher, "test");
    assertEquals("getLinkFile()", "/tmp/OnlineVideos/CustomOnlineVideoUIText_test.properties", result);
  }

  /**
   * Method: getRoot()
   */
  @Test
  public void testGetRoot() throws Exception {
    Method method = publisher.getClass().getDeclaredMethod("getRoot");
    method.setAccessible(true);
    Object result = method.invoke(publisher);
    assertNotNull("getRoot should return a value", result);
    assertTrue("getRoot should return a String", (result instanceof String));
    assertEquals("getRoot", "/tmp/OnlineVideos", result);

  }

  /**
   * Method: addPodcast(String category, String subCat, String url, PropertiesFile links,
   * ArrayList<String> otherSubCats)
   */
  @Test
  public void testAddPodcast() throws Exception {
    Method method = Publisher.class.getDeclaredMethod("addPodcast", String.class, String.class,
                                                      String.class, PropertiesFile.class,
                                                      List.class);
    method.setAccessible(true);
    PropertiesFile file = new PropertiesFile();
    ArrayList<String> subcats = new ArrayList<String>();
    subcats.add("subcat2");
    method.invoke(publisher, "cat", "subcat", "url", file, subcats);
    assertEquals("Property count", 1, file.entrySet().size());
    assertEquals("Property name", "xFeedPodcastCustom/cat",
                 file.entrySet().iterator().next().getKey());
    assertEquals("Property value", "xPodcastsubcat,xPodcastsubcat2;url",
                 file.entrySet().iterator().next().getValue());
  }

  /**
   * Method: addCategory(String callSign, String name, String description, String categoryIconUrl,
   * PropertiesFile labels)
   */
  @Test
  public void testAddCategory() throws Exception {
    Method
        method =
        Publisher.class.getDeclaredMethod("addCategory", String.class, String.class, String.class,
                                          String.class, PropertiesFile.class);
    method.setAccessible(true);
    PropertiesFile file = new PropertiesFile();
    method.invoke(publisher, "callSign", "name", "description", "categoryIconUrl", file);
    assertEquals("Property count", 6, file.entrySet().size());
    Iterator<Map.Entry<Object, Object>> itr = file.entrySet().iterator();
    Map.Entry<Object, Object> entry = itr.next();
    assertEquals("Property name", "Category/callSign/ShortName", entry.getKey());
    assertEquals("Property value", "name", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastcallSign/ShortName", entry.getKey());
    assertEquals("Property value", "name", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Category/callSign/ThumbURL", entry.getKey());
    assertEquals("Property value", "categoryIconUrl", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Category/callSign/LongName", entry.getKey());
    assertEquals("Property value", "description", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastcallSign/ThumbURL", entry.getKey());
    assertEquals("Property value", "categoryIconUrl", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastcallSign/LongName", entry.getKey());
    assertEquals("Property value", "description", entry.getValue());

  }

  /**
   * Method: addSource(String category, String categoryShortName, String categoryLongName,
   * PropertiesFile links, PropertiesFile labels)
   */
  @Test
  public void testAddSource() throws Exception {

    Method
        method =
        Publisher.class.getDeclaredMethod("addSource", String.class, String.class, String.class,
                                          PropertiesFile.class, PropertiesFile.class);
    method.setAccessible(true);
    PropertiesFile links = new PropertiesFile();
    PropertiesFile labels = new PropertiesFile();
    method.invoke(publisher, "category", "name", "description", links, labels);
    assertEquals("Links Property count", 1, links.entrySet().size());
    assertEquals("Labels count", 2, labels.entrySet().size());

    Iterator<Map.Entry<Object, Object>> itr = links.entrySet().iterator();
    Map.Entry<Object, Object> entry = itr.next();
    assertEquals("Property name", "CustomSources", entry.getKey());
    assertEquals("Property value", "xPodcastcategory", entry.getValue());

    itr = labels.entrySet().iterator();
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastcategory/ShortName", entry.getKey());
    assertEquals("Property value", "name", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastcategory/LongName", entry.getKey());
    assertEquals("Property value", "description", entry.getValue());
  }

  /**
   * Method: addSubCategory(String parentId, String subCatId, String subCatTitle, String
   * subCatDescription, String iconUrl, PropertiesFile links, PropertiesFile labels)
   */
  @Test
  public void testAddSubCategory() throws Exception {
    Method
        method =
        Publisher.class
            .getDeclaredMethod("addSubCategory", String.class, String.class, String.class,
                               String.class, String.class, PropertiesFile.class,
                               PropertiesFile.class);
    method.setAccessible(true);
    PropertiesFile links = new PropertiesFile();
    PropertiesFile labels = new PropertiesFile();
    method.invoke(publisher, "parentId", "subcatId", "subcatTitle", "subCatDescription", "iconUrl",
                  links, labels);
    assertEquals("Links Property count", 3, links.entrySet().size());
    assertEquals("Labels count", 6, labels.entrySet().size());

    Iterator<Map.Entry<Object, Object>> itr = links.entrySet().iterator();
    Map.Entry<Object, Object> entry = itr.next();
    assertEquals("Property name", "subcatId/IsCategory", entry.getKey());
    assertEquals("Property value", "true", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "subcatId/CategoryName", entry.getKey());
    assertEquals("Property value", "xPodcastsubcatId", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "xFeedPodcastCustom/subcatId", entry.getKey());
    assertEquals("Property value", "xPodcastparentId;xURLNone", entry.getValue());

    itr = labels.entrySet().iterator();
    entry = itr.next();
    assertEquals("Property name", "Category/subcatId/ThumbURL", entry.getKey());
    assertEquals("Property value", "iconUrl", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Category/subcatId/ShortName", entry.getKey());
    assertEquals("Property value", "subcatTitle", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastsubcatId/ShortName", entry.getKey());
    assertEquals("Property value", "subcatTitle", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastsubcatId/ThumbURL", entry.getKey());
    assertEquals("Property value", "iconUrl", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Category/subcatId/LongName", entry.getKey());
    assertEquals("Property value", "subCatDescription", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastsubcatId/LongName", entry.getKey());
    assertEquals("Property value", "subCatDescription", entry.getValue());
  }

} 
