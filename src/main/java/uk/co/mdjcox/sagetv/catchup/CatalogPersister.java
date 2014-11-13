package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.LoggerInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by michael on 02/10/14.
 */
@Singleton
public class CatalogPersister implements CatalogPublisher {

    private final XStream xstream;
    private LoggerInterface logger;
    private String fileName;
    private String emptyFileName;

    @Inject
    private CatalogPersister(LoggerInterface logger, CatchupContextInterface context) {
        this.logger = logger;
      fileName = context.getCatalogFileName();
      emptyFileName = context.getDefaultCatalogFileName();
      xstream = new XStream(new PureJavaReflectionProvider());
      xstream.addImplicitCollection(Episode.class, "genres", "genre", String.class);
      xstream.addImplicitCollection(Episode.class, "metaUrls", "metaUrl", String.class);
      xstream.addImplicitCollection(Episode.class, "errors", "error", ParseError.class);
      xstream.addImplicitCollection(Category.class, "metaUrls", "metaUrl", String.class);
      xstream.addImplicitCollection(Category.class, "errors", "error", ParseError.class);
      xstream.addImplicitCollection(SubCategory.class, "episodes", "episode", String.class);
      xstream.addImplicitCollection(SubCategory.class, "otherParentIds", "otherParentId", String.class);
      xstream.addImplicitCollection(SubCategory.class, "subCategories", "subCategory", String.class);
      xstream.addImplicitMap(Catalog.class, "episodes", "episode", Episode.class, "id");
      xstream.addImplicitMap(Catalog.class, "programmes", Programme.class, "id");
      xstream.addImplicitMap(Catalog.class, "subCategories", SubCategory.class, "id");
      xstream.addImplicitMap(Catalog.class, "sources", Source.class, "id");

      xstream.alias("programme", Programme.class);
      xstream.alias("episode", Episode.class);
      xstream.alias("subcategory", SubCategory.class);
      xstream.alias("category", Category.class);
      xstream.alias("catalog", Catalog.class);
      xstream.alias("root", Root.class);
      xstream.alias("source", Source.class);
      xstream.alias("error", ParseError.class);
      xstream.addDefaultImplementation(CopyOnWriteArraySet.class, Set.class);
      xstream.addDefaultImplementation(CopyOnWriteArrayList.class, List.class);
      xstream.addDefaultImplementation(ConcurrentHashMap.class, Map.class);
    }

  @Override
  public void publish(Catalog catalog) {
    publish(catalog, fileName);
  }

    public void publish(Catalog catalog, String fileName) {
        try {
            logger.info("Persisting new catalog to " + fileName);
          FileOutputStream stream = new FileOutputStream(fileName);
            xstream.toXML(catalog, stream);
            logger.info("Persisted new catalog to " + fileName);
        } catch (Exception e) {
            logger.error("Failed to persist catalog to " + fileName, e);
        }
    }

    public String parseIntoXML(Object catalog) {
        return xstream.toXML(catalog);
    }

    public Catalog parseIntoCatalog(String xml) {
      return (Catalog)xstream.fromXML(xml);
    }

    public Catalog load() {
      return load(fileName);
    }

    Catalog load(String fileName) {
        try {
          logger.info("Loading previous catalog " + fileName);
          Catalog catalog = (Catalog)xstream.fromXML(new File(fileName));
          logger.info("Done loading previous catalog");
          return catalog;
        } catch (Exception e) {
            logger.warn("Failed to load previous catalog - starting afresh");
        }

      try {
        logger.info("Loading default catalog " + emptyFileName);
        Catalog catalog = (Catalog)xstream.fromXML(new File(emptyFileName));
        logger.info("Done loading empty catalog");
        return catalog;
      } catch (Exception e) {
        logger.error("Failed to load default catalog", e);
      }

        return new Catalog();
    }




}

