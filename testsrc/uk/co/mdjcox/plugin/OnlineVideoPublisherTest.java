package uk.co.mdjcox.plugin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.logger.LoggingManager;
import uk.co.mdjcox.utils.HtmlUtils;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * OnlineVideoPublisher Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Mar 25, 2013</pre>
 */
public class OnlineVideoPublisherTest {

    private OnlineVideoPublisher publisher;
    @Mock
    private PropertiesInterface properties;


    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
        LoggerInterface logger = LoggingManager.getLogger(this.getClass(), "test", "logs");
        HtmlUtilsInterface htmlUtils = HtmlUtils.instance();
        publisher = new OnlineVideoPublisher(logger, properties, htmlUtils);
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
//TODO: Test goes here... 
/* 
try { 
   Method method = OnlineVideoPublisher.getClass().getMethod("getLinkFile", String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: getLabelFile(String file)
     */
    @Test
    public void testGetLabelFile() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = OnlineVideoPublisher.getClass().getMethod("getLabelFile", String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: getRoot()
     */
    @Test
    public void testGetRoot() throws Exception {
        when(properties.getProperty("STV", "C:\\Program Files\\SageTV\\SageTV\\STVs\\SageTV7\\SageTV7.xml")).thenReturn("./test.xml");
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
//TODO: Test goes here... 
/* 
try { 
   Method method = OnlineVideoPublisher.getClass().getMethod("addPodcast", String.class, String.class, String.class, PropertiesFile.class, ArrayList<String>.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: addCategory(String callSign, String name, String description, String categoryIconUrl, PropertiesFile labels)
     */
    @Test
    public void testAddCategory() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = OnlineVideoPublisher.getClass().getMethod("addCategory", String.class, String.class, String.class, String.class, PropertiesFile.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: addSource(String category, String categoryShortName, String categoryLongName, PropertiesFile links, PropertiesFile labels)
     */
    @Test
    public void testAddSource() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = OnlineVideoPublisher.getClass().getMethod("addSource", String.class, String.class, String.class, PropertiesFile.class, PropertiesFile.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: addSubCategory(String parentId, String subCatId, String subCatTitle, String subCatDescription, String iconUrl, PropertiesFile links, PropertiesFile labels)
     */
    @Test
    public void testAddSubCategory() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = OnlineVideoPublisher.getClass().getMethod("addSubCategory", String.class, String.class, String.class, String.class, String.class, PropertiesFile.class, PropertiesFile.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

} 
