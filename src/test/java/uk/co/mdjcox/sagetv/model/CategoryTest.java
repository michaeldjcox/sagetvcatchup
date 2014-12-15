package uk.co.mdjcox.sagetv.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.*;

/**
 * Category Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Apr 17, 2013</pre>
 */
public class CategoryTest {

  private class MyCategory extends Category {

    protected MyCategory(String sourceId, String id, String shortName, String longName, String serviceUrl,
                         String iconUrl,
                         String parentId) {
      super(sourceId, id, shortName, longName, serviceUrl, iconUrl,
            parentId);    //To change body of overridden methods use File | Settings | File Templates.
    }
  }

  ;

  @Before
  public void before() throws Exception {
  }

  @After
  public void after() throws Exception {
  }

  @Test
  public void testCreateNullSourceId() {
    try {
      Category category = new MyCategory("0", null, "1", "2", "3", "4", "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullId() {
    try {
      Category category = new MyCategory(null, "0", "1", "2", "3", "4", "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullShortName() {
    try {
      Category category = new MyCategory("0", "1", null, "2", "3", "4", "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullLongName() {
    try {
      Category category = new MyCategory("0", "1", "2", null, "3", "4", "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullServiceUrl() {
    try {
      Category category = new MyCategory("0", "1", "2", "3", null, "4", "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

//  @Test
//  public void testCreateNullIconUrl() {
//    try {
//      Category category = new MyCategory("0", "1", "2", "3", "4", null, "5");
//    } catch (NullPointerException e) {
//      return;
//    }
//
//    fail("Should have thrown NullPointerException");
//
//  }

  @Test
  public void testCreateNullParentId() {
    try {
      Category category = new MyCategory("0", "1", "2", "3", "4", "5", null);
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }


  @Test
  public void testCreate() throws Exception {
    MyCategory
        subcat =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertEquals("sourceId", "sourceId", subcat.getSourceId());
    assertEquals("id", "id", subcat.getId());
    assertEquals("shortName", "shortName", subcat.getShortName());
    assertEquals("longName", "longName", subcat.getLongName());
    assertEquals("serviceUrl", "serviceUrl", subcat.getServiceUrl());
    assertEquals("iconUrl", "iconUrl", subcat.getIconUrl());
    assertEquals("parentId", "parentId", subcat.getParentId());
  }

  /**
   * Method: getId()
   */
  @Test
  public void testGetId() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertEquals("getId", "id", category.getId());
  }

  /**
   * Method: setId(String id)
   */
  @Test
  public void testSetId() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    category.setId("id2");
    assertEquals("setId", "id2", category.getId());

    try {
      category.setId(null);
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");
  }

  /**
   * Method: getShortName()
   */
  @Test
  public void testGetShortName() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertEquals("getShortName", "shortName", category.getShortName());
  }

  /**
   * Method: setShortName(String shortName)
   */
  @Test
  public void testSetShortName() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    category.setShortName("shortName2");
    assertEquals("getShortName", "shortName2", category.getShortName());
    try {
      category.setShortName(null);
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");
  }

  /**
   * Method: getLongName()
   */
  @Test
  public void testGetLongName() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertEquals("getLongName", "longName", category.getLongName());
  }

  /**
   * Method: setLongName(String longName)
   */
  @Test
  public void testSetLongName() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    category.setLongName("longName2");
    assertEquals("setLongName", "longName2", category.getLongName());
    try {
      category.setLongName(null);
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");
  }

  /**
   * Method: getServiceUrl()
   */
  @Test
  public void testGetServiceUrl() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertEquals("getServiceUrl", "serviceUrl", category.getServiceUrl());
  }

  /**
   * Method: setServiceUrl(String serviceUrl)
   */
  @Test
  public void testSetServiceUrl() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    category.setServiceUrl("serviceUrl2");
    assertEquals("setServiceUrl", "serviceUrl2", category.getServiceUrl());
    try {
      category.setServiceUrl(null);
    } catch (NullPointerException e) {
      return;
    }
  }

  /**
   * Method: getIconUrl()
   */
  @Test
  public void testGetIconUrl() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertEquals("getIconUrl", "iconUrl", category.getIconUrl());
  }

  /**
   * Method: setIconUrl(String iconUrl)
   */
  @Test
  public void testSetIconUrl() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    category.setIconUrl("iconUrl2");
    assertEquals("setIconUrl", "iconUrl2", category.getIconUrl());

    try {
      category.setIconUrl(null);
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");
  }

  /**
   * Method: getParentId()
   */
  @Test
  public void testGetParentId() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertEquals("getParentId", "parentId", category.getParentId());
  }

  /**
   * Method: setParentId(String parentId)
   */
  @Test
  public void testSetParentId() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    category.setParentId("parentId2");
    assertEquals("setParentId", "parentId2", category.getParentId());
    try {
      category.setParentId(null);
    } catch (NullPointerException e) {
      return;
    }
  }

  /**
   * Method: isSource()
   */
  @Test
  public void testIsSource() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertFalse("This category is not a Source", category.isSource());
  }

  /**
   * Method: isSubCategory()
   */
  @Test
  public void testIsSubCategory() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertFalse("This category is not a SubCategory", category.isSubCategory());
  }

  /**
   * Method: isProgrammeCategory()
   */
  @Test
  public void testIsProgrammeCategory() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertFalse("This category is not a Programme", category.isProgrammeCategory());
  }

  /**
   * Method: isRoot()
   */
  @Test
  public void testIsRoot() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertFalse("This category is not the Root", category.isRoot());
  }

  /**
   * Method: toString()
   */
  @Test
  public void testToString() throws Exception {
    MyCategory
        category =
        new MyCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertEquals("Category toString should be the id", category.getId(), category.toString());
  }
} 
