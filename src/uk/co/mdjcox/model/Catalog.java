package uk.co.mdjcox.model;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import uk.co.mdjcox.utils.PropertiesFile;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public Collection<Category> getCategories() {
      return categories.values();
    }

    public void setCategories(Map<String, Category> categories) {
        this.categories = categories;
    }
}
