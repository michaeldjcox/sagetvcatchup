package uk.co.mdjcox.model;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * SubCategory Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Mar 25, 2013</pre>
 */
public class SubCategoryTest {

  private SubCategory subcat;

  @Before
  public void before() throws Exception {
    subcat = new SubCategory("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
  }

  @After
  public void after() throws Exception {
  }


  /**
   * Method: addSubCategory(Category subCategory)
   */
  @Test
  public void testCreate() throws Exception {
    assertEquals("id", "id", subcat.getId());
    assertEquals("shortName", "shortName", subcat.getShortName());
    assertEquals("longName", "longName", subcat.getLongName());
    assertEquals("serviceUrl", "serviceUrl", subcat.getServiceUrl());
    assertEquals("iconUrl", "iconUrl", subcat.getIconUrl());
    assertEquals("parentId", "parentId", subcat.getParentId());
    assertArrayEquals("otherParentIds", new String[]{}, subcat.getOtherParentIds().toArray());
    assertArrayEquals("subCategories keys", new String[]{},
                      subcat.getSubCategories().keySet().toArray());
    assertArrayEquals("subCategories values", new String[]{},
                      subcat.getSubCategories().values().toArray());
  }

  /**
   * Method: addSubCategory(Category subCategory)
   */
  @Test
  public void testAddGetSubCategory() throws Exception {
    SubCategory
        subsubcat =
        new SubCategory("subCatId", "subShortName", "subLongName", "subServiceUrl", "subIconUrl",
                        "id");
    subcat.addSubCategory(subsubcat);
    Map<String, Category> result = subcat.getSubCategories();
    assertEquals("Number of subcategories", 1, result.size());
    assertArrayEquals("Subcategory key set", new String[]{"subCatId"}, result.keySet().toArray());
    assertArrayEquals("Subcategory values", new Category[]{subsubcat}, result.values().toArray());

  }


  /**
   * Method: addOtherParentId(String parentId)
   */
  @Test
  public void testAddGetOtherParentId() throws Exception {
    subcat.addOtherParentId("parentId2");
    ArrayList<String> result = subcat.getOtherParentIds();
    assertEquals("Number of other parentIds #1", 1, result.size());
    assertArrayEquals("Other parents #1", new String[]{"parentId2"}, result.toArray());
    subcat.addOtherParentId("parentId3");
    result = subcat.getOtherParentIds();
    assertEquals("Number of other parentIds #2", 2, result.size());
    assertArrayEquals("Other parents #2", new String[]{"parentId2", "parentId3"}, result.toArray());
  }

  /**
   *
   * Method: isSource()
   *
   */
  @Test
  public void testIsSource() throws Exception {
    assertFalse("This category is not a Source", subcat.isSource());
  }

  /**
   *
   * Method: isSubCategory()
   *
   */
  @Test
  public void testIsSubCategory() throws Exception {
    assertTrue("This category is not a SubCategory", subcat.isSubCategory());
  }

  /**
   *
   * Method: isProgrammeCategory()
   *
   */
  @Test
  public void testIsProgrammeCategory() throws Exception {
    assertFalse("This category is not a Programme", subcat.isProgrammeCategory());
  }

  /**
   *
   * Method: isRoot()
   *
   */
  @Test
  public void testIsRoot() throws Exception {
    assertFalse("This category is not the Root", subcat.isRoot());
  }

  /**
   *
   * Method: toString()
   *
   */
  @Test
  public void testToString() throws Exception {
    assertEquals("Category toString should be the id", subcat.getId(), subcat.toString());
  }
} 
