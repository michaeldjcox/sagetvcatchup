package uk.co.mdjcox.model; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/** 
* Root Tester. 
* 
* @author <Authors name> 
* @since <pre>Apr 18, 2013</pre> 
* @version 1.0 
*/ 
public class RootTest {

  private Root category;


@Before
public void before() throws Exception {
  category = new Root("shortName", "longName", "serviceUrl", "iconUrl");

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
    assertEquals("getId", "", category.getId());
  }

  /**
   *
   * Method: setId(String id)
   *
   */
  @Test
  public void testSetId() throws Exception {
    category.setId("id2");
    assertEquals("setId", "id2", category.getId());
  }

  /**
   *
   * Method: getShortName()
   *
   */
  @Test
  public void testGetShortName() throws Exception {
    assertEquals("getShortName", "shortName", category.getShortName());
  }

  /**
   *
   * Method: setShortName(String shortName)
   *
   */
  @Test
  public void testSetShortName() throws Exception {
    category.setShortName("shortName2");
    assertEquals("getShortName", "shortName2", category.getShortName());
  }

  /**
   *
   * Method: getLongName()
   *
   */
  @Test
  public void testGetLongName() throws Exception {
    assertEquals("getLongName", "longName", category.getLongName());
  }

  /**
   *
   * Method: setLongName(String longName)
   *
   */
  @Test
  public void testSetLongName() throws Exception {
    category.setLongName("longName2");
    assertEquals("setLongName", "longName2", category.getLongName());
  }

  /**
   *
   * Method: getServiceUrl()
   *
   */
  @Test
  public void testGetServiceUrl() throws Exception {
    assertEquals("getServiceUrl", "serviceUrl", category.getServiceUrl());
  }

  /**
   *
   * Method: setServiceUrl(String serviceUrl)
   *
   */
  @Test
  public void testSetServiceUrl() throws Exception {
    category.setServiceUrl("serviceUrl2");
    assertEquals("setServiceUrl", "serviceUrl2", category.getServiceUrl());
  }

  /**
   *
   * Method: getIconUrl()
   *
   */
  @Test
  public void testGetIconUrl() throws Exception {
    assertEquals("getIconUrl", "iconUrl", category.getIconUrl());
  }

  /**
   *
   * Method: setIconUrl(String iconUrl)
   *
   */
  @Test
  public void testSetIconUrl() throws Exception {
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
    assertEquals("getParentId", "", category.getParentId());
  }

  /**
   *
   * Method: setParentId(String parentId)
   *
   */
  @Test
  public void testSetParentId() throws Exception {
    category.setParentId("parentId2");
    assertEquals("setParentId", "parentId2", category.getParentId());
  }

  /**
   *
   * Method: isSource()
   *
   */
  @Test
  public void testIsSource() throws Exception {
    assertFalse("This category is not a Source", category.isSource());
  }

  /**
   *
   * Method: isSubCategory()
   *
   */
  @Test
  public void testIsSubCategory() throws Exception {
    assertTrue("This category is not a SubCategory", category.isSubCategory());
  }

  /**
   *
   * Method: isProgrammeCategory()
   *
   */
  @Test
  public void testIsProgrammeCategory() throws Exception {
    assertFalse("This category is not a Programme", category.isProgrammeCategory());
  }

  /**
   *
   * Method: isRoot()
   *
   */
  @Test
  public void testIsRoot() throws Exception {
    assertTrue("This category is the Root", category.isRoot());
  }

  /**
   *
   * Method: toString()
   *
   */
  @Test
  public void testToString() throws Exception {
    assertEquals("Category toString should be the id", category.getId(), category.toString());
  }


}

