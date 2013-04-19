package uk.co.mdjcox.model;

import com.google.common.collect.ImmutableList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 26/02/13
 * Time: 07:41
 * To change this template use File | Settings | File Templates.
 */
public class Catalog {

    private Map<String, Category> categories = new LinkedHashMap<String, Category>();

    public Catalog()  {
    }

    public final List<Category> getCategories() {
      return ImmutableList.copyOf(categories.values());
    }

    public final void setCategories(Map<String, Category> categoryMap) {
       this.categories = new LinkedHashMap<String, Category>(checkNotNull(categoryMap));
    }
}
