package uk.co.mdjcox.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

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
        subCategories.put(subCategory.getId(), checkNotNull(subCategory));
    }

    public Map<String, Category> getSubCategories() {
        return ImmutableMap.copyOf(subCategories);
    }

    public void addOtherParentId(String parentId) {
        otherParentIds.add(checkNotNull(parentId));
    }

    public List<String> getOtherParentIds() {
        return ImmutableList.copyOf(otherParentIds);
    }

}
