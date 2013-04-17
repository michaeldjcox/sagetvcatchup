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
* @since <pre>Apr 17, 2013</pre> 
* @version 1.0 
*/
public class CatalogTest {

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: getCategories() 
* 
*/ 
@Test
public void testGetCategories() throws Exception {
  Catalog catalog = new Catalog();
  List<Category> results = catalog.getCategories();
  assertEquals("Results should be empty", 0, results.size());

  SubCategory cat = new SubCategory("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

  try {
    results.add(cat);
  } catch (Exception e) {
    assertEquals("Should throw UnsupportedOperationException",
                 "java.lang.UnsupportedOperationException", e.getClass().getName());
  }

  Map<String, Category> input = new HashMap<String, Category>();
  input.put(cat.getId(), cat);

  catalog.setCategories(input);

  results = catalog.getCategories();
  assertEquals("Results contain one entry", 1, results.size());

  SubCategory cat2 = new SubCategory("id2", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");

  input.put(cat2.getId(), cat2);

  results = catalog.getCategories();
  assertEquals("Results contain one entry", 1, results.size());

//TODO: Test goes here... 
} 

/** 
* 
* Method: setCategories(Map<String, Category> categories) 
* 
*/ 
@Test
public void testSetCategories() throws Exception { 
//TODO: Test goes here... 
} 


} 
