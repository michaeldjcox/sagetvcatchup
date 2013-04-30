package uk.co.mdjcox.sagetv.onlinevideo;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.mdjcox.sagetv.catchup.CatchupTestModule;
import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.model.Category;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.Root;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.sagetv.model.SubCategory;
import uk.co.mdjcox.utils.PropertiesFile;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

  @Test
  public void testCreate() throws Exception {
    boolean thrown=false;
    try {
      String tmpDir = System.getProperty("java.io.tmpdir", ".");
      CatchupTestModule module = new CatchupTestModule();
      Injector injector = Guice.createInjector(module);
      PublisherFactory publisherFactory = injector.getInstance(PublisherFactory.class);
      publisherFactory.createPublisher(null, tmpDir);
    } catch (com.google.inject.ProvisionException e) {
      thrown = true;
    }

    if (!thrown) {
      fail("Should have thrown com.google.inject.ProvisionException");
    }

    thrown=false;
    try {
      CatchupTestModule module = new CatchupTestModule();
      Injector injector = Guice.createInjector(module);
      PublisherFactory publisherFactory = injector.getInstance(PublisherFactory.class);
      publisherFactory.createPublisher("test", null);
    } catch (com.google.inject.ProvisionException e) {
      thrown = true;
    }

    if (!thrown) {
      fail("Should have thrown com.google.inject.ProvisionException");
    }

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

    // Should also work if there is nothing to do

    publisher.unpublish();

    assertFalse("Link file not should exist after test", linkFile.exists());
    assertFalse("Label file not should exist after test", labelFile.exists());

    linkFile = new File(linkFileName);
    labelFile = new File(labelFileName);

    if (!linkFile.exists()) linkFile.createNewFile();
    if (!labelFile.exists()) labelFile.createNewFile();

    //TODO work out how to test case where delete returns false
    // Test failed deletion
//      try {
//        publisher.unpublish();
//      } catch (Throwable ex) {
//        return;
//      }
//
//    fail("Should have thrown an exception");
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

    class TestCategory extends Category {

      TestCategory(String id, String shortName, String longName, String serviceUrl,
                   String iconUrl, String parentId) {
        super(id, shortName, longName, serviceUrl, iconUrl, parentId);
      }
    };

    TestCategory unknownCat = new TestCategory("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    catalog.addCategory(unknownCat);


    Episode episode = new Episode("sourceId", "episodeId", "programmeTitle", "episodeTitle",
                                  "series", "episode", "description", "iconUrl", "serviceUrl",
                                  "airDate", "airTime", "channel", "category");
    programme.addEpisode(episode);

    publisher.publish(catalog);

    Method method = Publisher.class.getDeclaredMethod("getLinkFile", String.class);
    method.setAccessible(true);
    String linkFileName = (String) method.invoke(publisher, "test");
    PropertiesFile linkPropsFile = new PropertiesFile(linkFileName, true);

    method = Publisher.class.getDeclaredMethod("getLabelFile", String.class);
    method.setAccessible(true);
    String labelFileName = (String) method.invoke(publisher, "test");
    PropertiesFile labelPropsFile = new PropertiesFile(labelFileName, true);

    TreeMap<Object,Object> linkProps = new  TreeMap<Object,Object>(new LinksPropertyLayout().getComparator(linkPropsFile));
    linkProps.putAll(linkPropsFile);

    TreeMap<Object,Object> labelProps = new  TreeMap<Object,Object>(new LabelsPropertyLayout().getComparator(labelPropsFile));
    labelProps.putAll(labelPropsFile);

    assertEquals("Link property count", 8, linkProps.size());
    assertEquals("Label property count", 20, labelProps.size());

    Iterator<Map.Entry<Object, Object>> itr = linkProps.entrySet().iterator();
    Map.Entry<Object, Object> next = itr.next();
    assertEquals("Property 1 key","CustomSources",next.getKey());
    assertEquals("Property 1 val","xPodcastsourceId",next.getValue());
    next = itr.next();
    assertEquals("Property 2 key","xFeedPodcastCustom/subcatId",next.getKey());
    assertEquals("Property 2 val","xPodcastsourceId;xURLNone",next.getValue());
    next = itr.next();
    assertEquals("Property 3 key","subcatId/IsCategory",next.getKey());
    assertEquals("Property 3 val","true",next.getValue());
    next = itr.next();
    assertEquals("Property 4 key","subcatId/CategoryName",next.getKey());
    assertEquals("Property 4 val","xPodcastsubcatId",next.getValue());
    next = itr.next();
    assertEquals("Property 5 key","xFeedPodcastCustom/subcatId2",next.getKey());
    assertEquals("Property 5 val","xPodcastsourceId;xURLNone",next.getValue());
    next = itr.next();
    assertEquals("Property 6 key","subcatId2/IsCategory",next.getKey());
    assertEquals("Property 6 val","true",next.getValue());
    next = itr.next();
    assertEquals("Property 7 key","subcatId2/CategoryName",next.getKey());
    assertEquals("Property 7 val","xPodcastsubcatId2",next.getValue());
    next = itr.next();
    assertEquals("Property 8 key","xFeedPodcastCustom/programmeId",next.getKey());
    assertEquals("Property 8 val","xPodcastsubcatId,xPodcastsubcatId2;podcastUrl",next.getValue());

    itr = labelProps.entrySet().iterator();
    next = itr.next();
    assertEquals("Property 1 key","Source/xPodcastprogrammeId/LongName",next.getKey());
    assertEquals("Property 1 val","programmeLongName",next.getValue());
    next = itr.next();
    assertEquals("Property 2 key","Source/xPodcastprogrammeId/ShortName",next.getKey());
    assertEquals("Property 2 val","programmeShortName",next.getValue());
    next = itr.next();
    assertEquals("Property 3 key","Source/xPodcastprogrammeId/ThumbURL",next.getKey());
    assertEquals("Property 3 val","programmeIconUdl",next.getValue());
    next = itr.next();
    assertEquals("Property 4 key","Source/xPodcastsourceId/LongName",next.getKey());
    assertEquals("Property 4 val","sourceLongName",next.getValue());
    next = itr.next();
    assertEquals("Property 5 key","Source/xPodcastsourceId/ShortName",next.getKey());
    assertEquals("Property 5 val","sourceSourceName",next.getValue());
    next = itr.next();
    assertEquals("Property 6 key","Source/xPodcastsubcatId/LongName",next.getKey());
    assertEquals("Property 6 val","subcatLongName",next.getValue());
    next = itr.next();
    assertEquals("Property 7 key","Source/xPodcastsubcatId/ShortName",next.getKey());
    assertEquals("Property 7 val","subcatShortName",next.getValue());
    next = itr.next();
    assertEquals("Property 8 key","Source/xPodcastsubcatId/ThumbURL",next.getKey());
    assertEquals("Property 8 val","subcatIconUrl",next.getValue());
    next = itr.next();
    assertEquals("Property 9 key","Source/xPodcastsubcatId2/LongName",next.getKey());
    assertEquals("Property 9 val","subcatLongName2",next.getValue());
    next = itr.next();
    assertEquals("Property 10 key","Source/xPodcastsubcatId2/ShortName",next.getKey());
    assertEquals("Property 10 val","subcatShortName2",next.getValue());
    next = itr.next();
    assertEquals("Property 11 key","Source/xPodcastsubcatId2/ThumbURL",next.getKey());
    assertEquals("Property 11 val","subcatIconUrl2",next.getValue());
    next = itr.next();
    assertEquals("Property 12 key","Category/programmeId/LongName",next.getKey());
    assertEquals("Property 12 val","programmeLongName",next.getValue());
    next = itr.next();
    assertEquals("Property 13 key","Category/programmeId/ShortName",next.getKey());
    assertEquals("Property 13 val","programmeShortName",next.getValue());
    next = itr.next();
    assertEquals("Property 14 key","Category/programmeId/ThumbURL",next.getKey());
    assertEquals("Property 14 val","programmeIconUdl",next.getValue());
    next = itr.next();
    assertEquals("Property 15 key","Category/subcatId/LongName",next.getKey());
    assertEquals("Property 15 val","subcatLongName",next.getValue());
    next = itr.next();
    assertEquals("Property 16 key","Category/subcatId/ShortName",next.getKey());
    assertEquals("Property 16 val","subcatShortName",next.getValue());
    next = itr.next();
    assertEquals("Property 17 key","Category/subcatId/ThumbURL",next.getKey());
    assertEquals("Property 17 val","subcatIconUrl",next.getValue());
    next = itr.next();
    assertEquals("Property 18 key","Category/subcatId2/LongName",next.getKey());
    assertEquals("Property 18 val","subcatLongName2",next.getValue());
    next = itr.next();
    assertEquals("Property 19 key","Category/subcatId2/ShortName",next.getKey());
    assertEquals("Property 19 val","subcatShortName2",next.getValue());
    next = itr.next();
    assertEquals("Property 20 key","Category/subcatId2/ThumbURL",next.getKey());
    assertEquals("Property 20 val","subcatIconUrl2",next.getValue());

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

    result =method.invoke(publisher, "");
    assertEquals("getLinkFile()", "/tmp/OnlineVideos/CustomOnlineVideoLinks.properties", result);

  }

  /**
   * Method: getLabelFile(String file)
   */
  @Test
  public void testGetLabelFile() throws Exception {
    Method method = Publisher.class.getDeclaredMethod("getLabelFile", String.class);
    method.setAccessible(true);
    Object result = method.invoke(publisher, "test");
    assertEquals("getLabelFile()", "/tmp/OnlineVideos/CustomOnlineVideoUIText_test.properties", result);

    result = method.invoke(publisher, "");
    assertEquals("getLabelFile()", "/tmp/OnlineVideos/CustomOnlineVideoUIText.properties", result);

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
   * Method: addProgramme(Programme programme, PropertiesFile links, PropertiesFile labels)
   */
  @Test
  public void testAddProgramme() throws Exception {
    Method method = Publisher.class.getDeclaredMethod("addProgramme", Programme.class,
                                                      PropertiesFile.class,
                                                      PropertiesFile.class);
    method.setAccessible(true);
    PropertiesFile linksFile = new PropertiesFile();
    PropertiesFile labelsFile = new PropertiesFile();

    Programme programme = new Programme("callSign", "name", "description", "serviceUrl", "categoryIconUrl", "subcat");
    programme.addOtherParentId("subcat2");
    programme.addOtherParentId("subcat3");
    programme.setPodcastUrl("podcastUrl");

    // TODO this test should probably fail - there should be a name
    Programme programme2 = new Programme("callSign2", "", "", "serviceUrl", "", "");
    programme2.setPodcastUrl("podcastUrl2");
    programme2.addOtherParentId("subcat2");

    method.invoke(publisher, programme, linksFile, labelsFile);
    method.invoke(publisher, programme2, linksFile, labelsFile);

    assertEquals("Property count", 2, linksFile.entrySet().size());

    Iterator<Map.Entry<Object, Object>> itr2 = linksFile.entrySet().iterator();
    Map.Entry<Object, Object> entry2 = itr2.next();
    assertEquals("Property name", "xFeedPodcastCustom/callSign", entry2.getKey());
    assertEquals("Property value", "xPodcastsubcat,xPodcastsubcat2,xPodcastsubcat3;podcastUrl", entry2.getValue());
    entry2 = itr2.next();
    assertEquals("Property name", "xFeedPodcastCustom/callSign2", entry2.getKey());
    assertEquals("Property value", "xPodcastsubcat2;podcastUrl2", entry2.getValue());

    assertEquals("Property count", 6, labelsFile.entrySet().size());
    Iterator<Map.Entry<Object, Object>> itr = labelsFile.entrySet().iterator();
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
        Publisher.class.getDeclaredMethod("addSource", Source.class,
                                          PropertiesFile.class, PropertiesFile.class);
    method.setAccessible(true);
    PropertiesFile links = new PropertiesFile();
    PropertiesFile labels = new PropertiesFile();

    Source source = new Source("category", "name", "description", "serviceUrl", "iconUrl");
    Source source2 = new Source("category2", "name2", "description2", "serviceUrl2", "iconUrl2");
    Source dupe = new Source("category", "name", "description", "serviceUrl", "iconUrl");

    method.invoke(publisher, source, links, labels);
    method.invoke(publisher, source2, links, labels);
    method.invoke(publisher, dupe, links, labels);

    assertEquals("Links Property count", 1, links.entrySet().size());
    assertEquals("Labels count", 4, labels.entrySet().size());

    Iterator<Map.Entry<Object, Object>> itr = links.entrySet().iterator();
    Map.Entry<Object, Object> entry = itr.next();
    assertEquals("Property name", "CustomSources", entry.getKey());
    assertEquals("Property value", "xPodcastcategory,xPodcastcategory2", entry.getValue());

    itr = labels.entrySet().iterator();
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastcategory2/LongName", entry.getKey());
    assertEquals("Property value", "description2", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastcategory2/ShortName", entry.getKey());
    assertEquals("Property value", "name2", entry.getValue());
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
            .getDeclaredMethod("addSubCategory", SubCategory.class, PropertiesFile.class,
                               PropertiesFile.class);
    method.setAccessible(true);
    PropertiesFile linksFile = new PropertiesFile();
    PropertiesFile labelsFile = new PropertiesFile();
    SubCategory subCategory = new SubCategory("subcatId", "subcatTitle", "subCatDescription", "serviceUrl", "iconUrl", "parentId");
    SubCategory subCategory2 = new SubCategory("subcatId2", "subcatTitle2", "subCatDescription2", "serviceUrl2", "", "parentId");

    method.invoke(publisher, subCategory, linksFile, labelsFile);

    method.invoke(publisher, subCategory2, linksFile, labelsFile);

    TreeMap<Object,Object> linkProps = new  TreeMap<Object,Object>(new LinksPropertyLayout().getComparator(linksFile));
    linkProps.putAll(linksFile);

    TreeMap<Object,Object> labelProps = new  TreeMap<Object,Object>(new LabelsPropertyLayout().getComparator(labelsFile));
    labelProps.putAll(labelsFile);

    assertEquals("Links Property count", 6, linkProps.size());
    assertEquals("Labels count", 10, labelProps.size());

    Iterator<Map.Entry<Object, Object>> itr = linkProps.entrySet().iterator();
    Map.Entry<Object, Object> entry;
    entry = itr.next();
    assertEquals("Property name", "xFeedPodcastCustom/subcatId", entry.getKey());
    assertEquals("Property value", "xPodcastparentId;xURLNone", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "subcatId/IsCategory", entry.getKey());
    assertEquals("Property value", "true", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "subcatId/CategoryName", entry.getKey());
    assertEquals("Property value", "xPodcastsubcatId", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "xFeedPodcastCustom/subcatId2", entry.getKey());
    assertEquals("Property value", "xPodcastparentId;xURLNone", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "subcatId2/IsCategory", entry.getKey());
    assertEquals("Property value", "true", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "subcatId2/CategoryName", entry.getKey());
    assertEquals("Property value", "xPodcastsubcatId2", entry.getValue());

    itr = labelProps.entrySet().iterator();
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastsubcatId/LongName", entry.getKey());
    assertEquals("Property value", "subCatDescription", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastsubcatId/ShortName", entry.getKey());
    assertEquals("Property value", "subcatTitle", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastsubcatId/ThumbURL", entry.getKey());
    assertEquals("Property value", "iconUrl", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastsubcatId2/LongName", entry.getKey());
    assertEquals("Property value", "subCatDescription2", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Source/xPodcastsubcatId2/ShortName", entry.getKey());
    assertEquals("Property value", "subcatTitle2", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Category/subcatId/LongName", entry.getKey());
    assertEquals("Property value", "subCatDescription", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Category/subcatId/ShortName", entry.getKey());
    assertEquals("Property value", "subcatTitle", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Category/subcatId/ThumbURL", entry.getKey());
    assertEquals("Property value", "iconUrl", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Category/subcatId2/LongName", entry.getKey());
    assertEquals("Property value", "subCatDescription2", entry.getValue());
    entry = itr.next();
    assertEquals("Property name", "Category/subcatId2/ShortName", entry.getKey());
    assertEquals("Property value", "subcatTitle2", entry.getValue());
  }

} 
