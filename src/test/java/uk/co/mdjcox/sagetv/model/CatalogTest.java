package uk.co.mdjcox.sagetv.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/**
 * Catalog Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Apr 17, 2013</pre>
 */
public class CatalogTest {

  @Before
  public void before() throws Exception {
  }

  @After
  public void after() throws Exception {
  }

  @Test
  public void testGetCategoriesEmpty() {
    Catalog catalog = new Catalog();
    Collection<SubCategory> results = catalog.getSubcategories();
    assertEquals("Results should be empty", 0, results.size());
  }

  @Test
  public void testGetCategoriesImmutabilityTest() {
    Catalog catalog = new Catalog();
    Collection<Category> results = catalog.getCategories();

    SubCategory
        cat =
        new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

    try {
      results.add(cat);
    } catch (Exception e) {
      assertEquals("Should throw UnsupportedOperationException",
                   "java.lang.UnsupportedOperationException", e.getClass().getName());
    }
  }

  @Test
  public void testGetCategories() throws Exception {
    Catalog catalog = new Catalog();

    SubCategory
        cat =
        new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

    Map<String, Category> input = new HashMap<String, Category>();
    input.put(cat.getId(), cat);

      Root root = new Root("rootId", "shortName", "longName", "serviceUrl", "iconUrl");

      Map<String, Episode> episodes= new HashMap<String, Episode>();

      catalog.addSubCategory(cat);

    Collection<SubCategory> results = catalog.getSubcategories();
    assertEquals("Results contain one entry", 1, results.size());
  }

  /**
   *
   * Method: addCategory(Category cat)
   *
   */
  @Test
  public void testAddCategory() throws Exception {
    Catalog catalog = new Catalog();
    SubCategory
        cat =
        new SubCategory("sourceId", "id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

      catalog.addSubCategory(cat);

    Collection<SubCategory> cats = catalog.getSubcategories();

    assertEquals("testAddCategory - size", 1, cats.size());

    assertEquals("testAddCategory - added element", cat, cats.iterator().next());
  }
}
