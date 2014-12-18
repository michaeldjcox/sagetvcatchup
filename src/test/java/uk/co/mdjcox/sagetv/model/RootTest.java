package uk.co.mdjcox.sagetv.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.*;

/** 
* Root Tester. 
* 
* @author <Authors name> 
* @since <pre>Apr 18, 2013</pre> 
* @version 1.0 
*/ 
public class RootTest {


@Before
public void before() throws Exception {
}

@After
public void after() throws Exception { 
}

  /**
   *
   * Method: getId()
   *
   */
  @Test
  public void testGetId() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertEquals("getId", "rootId", category.getId());
  }

  /**
   *
   * Method: setId(String id)
   *
   */
  @Test
  public void testSetId() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
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
   *
   * Method: getShortName()
   *
   */
  @Test
  public void testGetShortName() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertEquals("getShortName", "shortName", category.getShortName());
  }

  /**
   *
   * Method: setShortName(String shortName)
   *
   */
  @Test
  public void testSetShortName() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
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
   *
   * Method: getLongName()
   *
   */
  @Test
  public void testGetLongName() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertEquals("getLongName", "longName", category.getLongName());
  }

  /**
   *
   * Method: setLongName(String longName)
   *
   */
  @Test
  public void testSetLongName() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
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
   *
   * Method: getServiceUrl()
   *
   */
  @Test
  public void testGetServiceUrl() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertEquals("getServiceUrl", "serviceUrl", category.getServiceUrl());
  }

  /**
   *
   * Method: setServiceUrl(String serviceUrl)
   *
   */
  @Test
  public void testSetServiceUrl() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    category.setServiceUrl("serviceUrl2");
    assertEquals("setServiceUrl", "serviceUrl2", category.getServiceUrl());
    try {
      category.setServiceUrl(null);
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");
  }

  /**
   *
   * Method: getIconUrl()
   *
   */
  @Test
  public void testGetIconUrl() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertEquals("getIconUrl", "iconUrl", category.getIconUrl());
  }

  /**
   *
   * Method: setIconUrl(String iconUrl)
   *
   */
  @Test
  public void testSetIconUrl() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    category.setIconUrl("iconUrl2");
    assertEquals("setIconUrl", "iconUrl2", category.getIconUrl());
  }

  /**
   *
   * Method: getParentId()
   *
   */
  @Test
  public void testGetParentId() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertEquals("getParentId", "", category.getParentId());
  }

  /**
   *
   * Method: setParentId(String parentId)
   *
   */
  @Test
  public void testSetParentId() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    category.setParentId("parentId2");
    assertEquals("setParentId", "parentId2", category.getParentId());
    try {
      category.setParentId(null);
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");
  }

  /**
   *
   * Method: isSource()
   *
   */
  @Test
  public void testIsSource() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertFalse("This category is not a Source", category.isSource());
  }

  /**
   *
   * Method: isSubCategory()
   *
   */
  @Test
  public void testIsSubCategory() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertTrue("This category is a SubCategory", category.isSubCategory());
  }

  /**
   *
   * Method: isProgrammeCategory()
   *
   */
  @Test
  public void testIsProgrammeCategory() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertFalse("This category is not a Programme", category.isProgrammeCategory());
  }

  /**
   *
   * Method: isRoot()
   *
   */
  @Test
  public void testIsRoot() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertTrue("This category is the Root", category.isRoot());
  }

  /**
   *
   * Method: toString()
   *
   */
  @Test
  public void testToString() throws Exception {
    Root category = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");
    assertEquals("Category toString should be the id", category.getId(), category.toString());
  }


}

