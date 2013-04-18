package uk.co.mdjcox.model;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

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
    List<Category> results = catalog.getCategories();
    assertEquals("Results should be empty", 0, results.size());
  }

  @Test
  public void testGetCategoriesImmutabilityTest() {
    Catalog catalog = new Catalog();
    List<Category> results = catalog.getCategories();

    SubCategory
        cat =
        new SubCategory("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

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
        new SubCategory("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

    Map<String, Category> input = new HashMap<String, Category>();
    input.put(cat.getId(), cat);

    catalog.setCategories(input);

    List<Category> results = catalog.getCategories();
    assertEquals("Results contain one entry", 1, results.size());
  }

  @Test
  public void testSetCategories() throws Exception {

    // Test the simple came of inserting one entry
    Catalog catalog = new Catalog();
    SubCategory
        cat =
        new SubCategory("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

    Map<String, Category> input = new HashMap<String, Category>();
    input.put(cat.getId(), cat);

    catalog.setCategories(input);

    List<Category> results = catalog.getCategories();
    assertEquals("Results contain one entry", 1, results.size());
  }

  @Test
  public void testSetCategoriesImmutability() throws Exception {

    Catalog catalog = new Catalog();
    SubCategory
        cat =
        new SubCategory("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

    Map<String, Category> input = new HashMap<String, Category>();
    input.put(cat.getId(), cat);

    catalog.setCategories(input);

    List<Category> results = catalog.getCategories();
    assertEquals("Results contain one entry", 1, results.size());

    SubCategory
        cat2 =
        new SubCategory("id2", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

    input.put(cat2.getId(), cat2);

    results = catalog.getCategories();
    assertEquals("Results should still contain one entry", 1, results.size());
  }
}
