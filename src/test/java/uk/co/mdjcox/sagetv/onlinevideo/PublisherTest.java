package uk.co.mdjcox.sagetv.onlinevideo;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.mdjcox.sagetv.catchup.CatchupTestModule;
import uk.co.mdjcox.utils.PropertiesFile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        CatchupTestModule module = new CatchupTestModule();
        Injector injector = Guice.createInjector(module);
        PublisherFactory publisherFactory =  injector.getInstance(PublisherFactory.class);
        publisher = publisherFactory.createPublisher("test", ".");

    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: unpublish(String file)
     */
    @Test
    public void testUnpublish() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: publish(Catalog catalog)
     */
    @Test
    public void testPublish() throws Exception {
//TODO: Test goes here... 
    }


    /**
     * Method: getLinkFile(String file)
     */
    @Test
    public void testGetLinkFile() throws Exception {
        Method method = Publisher.class.getDeclaredMethod("getLinkFile", String.class);
        method.setAccessible(true);
        Object result = method.invoke(publisher, "test");
        assertEquals("getLinkFile()", "./OnlineVideos/CustomOnlineVideoLinks_test.properties", result);
    }

    /**
     * Method: getLabelFile(String file)
     */
    @Test
    public void testGetLabelFile() throws Exception {
      Method method = Publisher.class.getDeclaredMethod("getLabelFile", String.class);
      method.setAccessible(true);
      Object result = method.invoke(publisher, "test");
      assertEquals("getLinkFile()", "./OnlineVideos/CustomOnlineVideoUIText_test.properties", result);
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
        assertEquals("getRoot", "./OnlineVideos", result);

    }

    /**
     * Method: addPodcast(String category, String subCat, String url, PropertiesFile links, ArrayList<String> otherSubCats)
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
      assertEquals("Property name", "xFeedPodcastCustom/cat", file.entrySet().iterator().next().getKey());
      assertEquals("Property value", "xPodcastsubcat,xPodcastsubcat2;url", file.entrySet().iterator().next().getValue());
    }

    /**
     * Method: addCategory(String callSign, String name, String description, String categoryIconUrl, PropertiesFile labels)
     */
    @Test
    public void testAddCategory() throws Exception {
      Method method = Publisher.class.getDeclaredMethod("addCategory",  String.class, String.class, String.class, String.class, PropertiesFile.class);
      method.setAccessible(true);
      PropertiesFile file = new PropertiesFile();
      method.invoke(publisher, "callSign", "name", "description", "categoryIconUrl", file);
      assertEquals("Property count", 6, file.entrySet().size());
      Iterator<Map.Entry<Object,Object>> itr = file.entrySet().iterator();
      Map.Entry<Object,Object> entry = itr.next();
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
     * Method: addSource(String category, String categoryShortName, String categoryLongName, PropertiesFile links, PropertiesFile labels)
     */
    @Test
    public void testAddSource() throws Exception {

      Method method = Publisher.class.getDeclaredMethod("addSource", String.class, String.class, String.class, PropertiesFile.class, PropertiesFile.class);
      method.setAccessible(true);
      PropertiesFile links = new PropertiesFile();
      PropertiesFile labels = new PropertiesFile();
      method.invoke(publisher, "category", "name", "description", links, labels);
      assertEquals("Links Property count", 1, links.entrySet().size());
      assertEquals("Labels count", 2, labels.entrySet().size());

      Iterator<Map.Entry<Object,Object>> itr = links.entrySet().iterator();
      Map.Entry<Object,Object> entry = itr.next();
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
     * Method: addSubCategory(String parentId, String subCatId, String subCatTitle, String subCatDescription, String iconUrl, PropertiesFile links, PropertiesFile labels)
     */
    @Test
    public void testAddSubCategory() throws Exception {
      Method method = Publisher.class.getDeclaredMethod("addSubCategory", String.class, String.class, String.class, String.class, String.class, PropertiesFile.class, PropertiesFile.class);
      method.setAccessible(true);
      PropertiesFile links = new PropertiesFile();
      PropertiesFile labels = new PropertiesFile();
      method.invoke(publisher, "parentId", "subcatId", "subcatTitle", "subCatDescription", "iconUrl", links, labels);
      assertEquals("Links Property count", 3, links.entrySet().size());
      assertEquals("Labels count", 6, labels.entrySet().size());

      Iterator<Map.Entry<Object,Object>> itr = links.entrySet().iterator();
      Map.Entry<Object,Object> entry = itr.next();
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
