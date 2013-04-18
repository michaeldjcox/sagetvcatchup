package uk.co.mdjcox.model;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 12/02/13
 * Time: 07:37
 * To change this template use File | Settings | File Templates.
 */
public class SubCategory extends Category {

    private ArrayList<String> otherParentIds = new ArrayList<String>();
    private HashMap<String, Category> subCategories = new LinkedHashMap<String, Category>();



    public SubCategory(String id, String categoryShortName, String categoryLongName,
                       String serviceUrl, String iconUrl,
                       String parentId) {
        super(id, categoryShortName, categoryLongName, serviceUrl, iconUrl, parentId);
    }

    public void addSubCategory(Category subCategory) {
        subCategories.put(subCategory.getId(), subCategory);
    }

    public Map<String, Category> getSubCategories() {
        return subCategories;
    }

    public void addOtherParentId(String parentId) {
        otherParentIds.add(parentId);
    }

    public ArrayList<String> getOtherParentIds() {
        return otherParentIds;
    }

}
