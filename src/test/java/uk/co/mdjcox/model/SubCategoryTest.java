package uk.co.mdjcox.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

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


    /**
     * Method: addSubCategory(Category subCategory)
     */
    @Test
    public void testCreate() throws Exception {
        SubCategory subcat = new SubCategory("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
        assertEquals("id", "id", subcat.getId());
        assertEquals("shortName", "shortName", subcat.getShortName());
        assertEquals("longName", "longName", subcat.getLongName());
        assertEquals("serviceUrl", "serviceUrl", subcat.getServiceUrl());
        assertEquals("iconUrl", "iconUrl", subcat.getIconUrl());
        assertEquals("parentId", "parentId", subcat.getParentId());
        assertArrayEquals("otherParentIds", new String[] {}, subcat.getOtherParentIds().toArray());
        assertArrayEquals("subCategories keys", new String[] {}, subcat.getSubCategories().keySet().toArray());
        assertArrayEquals("subCategories values", new String[] {}, subcat.getSubCategories().values().toArray());
        assertEquals("podcastUrl", "http://localhost:8081/id", subcat.getPodcastUrl());
    }

    /**
     * Method: addSubCategory(Category subCategory)
     */
    @Test
    public void testAddGetSubCategory() throws Exception {
        SubCategory cat = new SubCategory("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
        SubCategory subcat = new SubCategory("subCatId", "subShortName", "subLongName", "subServiceUrl", "subIconUrl", "id");
        cat.addSubCategory(subcat);
        Map<String, Category> result = cat.getSubCategories();
        assertEquals("Number of subcategories", 1, result.size());
        assertArrayEquals("Subcategory key set", new String[]{"subCatId"}, result.keySet().toArray());
        assertArrayEquals("Subcategory values", new Category[]{subcat}, result.values().toArray());

    }


    /**
     * Method: addOtherParentId(String parentId)
     */
    @Test
    public void testAddGetOtherParentId() throws Exception {
        SubCategory cat = new SubCategory("id", "shortName", "longName", "serviceUrl", "iconUrl", "parentId");
        cat.addOtherParentId("parentId2");
        ArrayList<String> result = cat.getOtherParentIds();
        assertEquals("Number of other parentIds #1", 1, result.size());
        assertArrayEquals("Other parents #1", new String[]{"parentId2"}, result.toArray());
        cat.addOtherParentId("parentId3");
        result = cat.getOtherParentIds();
        assertEquals("Number of other parentIds #2", 2, result.size());
        assertArrayEquals("Other parents #2", new String[]{"parentId2", "parentId3"}, result.toArray());
    }


    /**
     * Method: clone()
     */
    @Test
    public void testClone() throws Exception {
//TODO: Test goes here... 
    }


} 
