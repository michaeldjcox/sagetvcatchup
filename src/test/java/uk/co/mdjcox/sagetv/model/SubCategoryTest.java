package uk.co.mdjcox.sagetv.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
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

  @Before
  public void before() throws Exception {
  }

  @After
  public void after() throws Exception {
  }


  @Test
  public void testCreateNullSourceId() {
    try {
      SubCategory category = new SubCategory(null, "0", "1", "2", "3", "4", "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullId() {
    try {
      SubCategory category = new SubCategory("0", null, "1", "2", "3", "4", "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullShortName() {
    try {
      SubCategory category = new SubCategory("0", "1", null, "2", "3", "4", "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullLongName() {
    try {
      SubCategory category = new SubCategory("0", "1", "2", null, "3", "4", "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullServiceUrl() {
    try {
      SubCategory category = new SubCategory("0", "1", "2", "3", null, "4", "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullIconUrl() {
    try {
      SubCategory category = new SubCategory("0", "1", "2", "3", "4", null, "5");
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testCreateNullParentId() {
    try {
      SubCategory category = new SubCategory("0", "1", "2", "3", "4", "5", null);
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NullPointerException");

  }

  /**
   * Method: addSubCategory(Category subCategory)
   */
  @Test
  public void testCreate() throws Exception {
    SubCategory subcat = new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertEquals("sourceId", "sourceId", subcat.getSourceId());
    assertEquals("id", "id", subcat.getId());
    assertEquals("shortName", "shortName", subcat.getShortName());
    assertEquals("longName", "longName", subcat.getLongName());
    assertEquals("serviceUrl", "serviceUrl", subcat.getServiceUrl());
    assertEquals("iconUrl", "iconUrl", subcat.getIconUrl());
    assertEquals("parentId", "parentId", subcat.getParentId());
    assertArrayEquals("otherParentIds", new String[]{}, subcat.getOtherParentIds().toArray());
    assertArrayEquals("subCategories keys", new String[]{},
                      subcat.getSubCategories().toArray());
  }

  /**
   * Method: addSubCategory(Category subCategory)
   */
  @Test
  public void testAddGetSubCategory() throws Exception {
    SubCategory subcat = new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    SubCategory
        subsubcat =
        new SubCategory("sourceId", "subCatId", "subShortName", "subLongName", "subServiceUrl", "subIconUrl",
                        "id");
    subcat.addSubCategory(subsubcat);
    Set<String> result = subcat.getSubCategories();
    assertEquals("Number of subcategories", 1, result.size());
    assertArrayEquals("Subcategory key set", new String[]{"subCatId"}, result.toArray());

    try {
      subcat.addSubCategory(null);
    } catch (NullPointerException e) {
      return;
    }
    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testGetSubCategoriesImmutable() throws Exception {
    SubCategory subcat = new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    SubCategory
        subsubcat =
        new SubCategory("sourceId", "subCatId", "subShortName", "subLongName", "subServiceUrl", "subIconUrl",
                        "id");
    subcat.addSubCategory(subsubcat);
    Set<String> result = subcat.getSubCategories();
    assertEquals("Number of subcategories", 1, result.size());
    assertArrayEquals("Subcategory key set", new String[]{"subCatId"}, result.toArray());

    SubCategory
        subsubcat2 =
        new SubCategory("sourceId", "subCatId2", "subShortName2", "subLongName2", "subServiceUrl2", "subIconUrl2",
                        "id2");

    try {
      result.add("test");
    } catch (UnsupportedOperationException e) {
      return;
    }

    fail("Should have thrown UnsupportedOperationException");
  }

  /**
   * Method: addOtherParentId(String parentId)
   */
  @Test
  public void testAddGetOtherParentId() throws Exception {
    SubCategory subcat = new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    subcat.addOtherParentId("parentId2");
    Set<String> result = subcat.getOtherParentIds();
    assertEquals("Number of other parentIds #1", 1, result.size());
    assertArrayEquals("Other parents #1", new String[]{"parentId2"}, result.toArray());
    subcat.addOtherParentId("parentId3");
    result = subcat.getOtherParentIds();
    assertEquals("Number of other parentIds #2", 2, result.size());
    assertArrayEquals("Other parents #2", new String[]{"parentId2", "parentId3"}, result.toArray());
    try {
      subcat.addOtherParentId(null);
    } catch (NullPointerException e) {
      return;
    }
    fail("Should have thrown NullPointerException");

  }

  @Test
  public void testGetOtherParentIdImmutable() throws Exception {
    SubCategory subcat = new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    subcat.addOtherParentId("parentId2");
    Set<String> result = subcat.getOtherParentIds();
    assertEquals("Number of other parentIds #1", 1, result.size());
    assertArrayEquals("Other parents #1", new String[]{"parentId2"}, result.toArray());

    try {
      result.add("parentId3");
    } catch (UnsupportedOperationException e) {
      return;
    }

    fail("Should have thrown UnsupportedOperationException");
  }

  /**
   *
   * Method: isSource()
   *
   */
  @Test
  public void testIsSource() throws Exception {
    SubCategory subcat = new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertFalse("This category is not a Source", subcat.isSource());
  }

  /**
   *
   * Method: isSubCategory()
   *
   */
  @Test
  public void testIsSubCategory() throws Exception {
    SubCategory subcat = new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertTrue("This category is not a SubCategory", subcat.isSubCategory());
  }

  /**
   *
   * Method: isProgrammeCategory()
   *
   */
  @Test
  public void testIsProgrammeCategory() throws Exception {
    SubCategory subcat = new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertFalse("This category is not a Programme", subcat.isProgrammeCategory());
  }

  /**
   *
   * Method: isRoot()
   *
   */
  @Test
  public void testIsRoot() throws Exception {
    SubCategory subcat = new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertFalse("This category is not the Root", subcat.isRoot());
  }

  /**
   *
   * Method: toString()
   *
   */
  @Test
  public void testToString() throws Exception {
    SubCategory subcat = new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
    assertEquals("Category toString should be the id", subcat.getId(), subcat.toString());
  }
} 
