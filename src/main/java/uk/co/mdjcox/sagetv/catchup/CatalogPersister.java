package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.model.Catalog;

import java.io.*;

/**
 * Created by michael on 02/10/14.
 */
@Singleton
public class CatalogPersister implements CatalogPublisher {

    private final XStream xstream;
    private Logger logger;
    private String fileName;
    private String emptyFileName;

    @Inject
    private CatalogPersister(Logger logger, CatchupContextInterface context) {
        this.logger = logger;
      fileName = context.getCatalogFileName();
      emptyFileName = context.getDefaultCatalogFileName();
      xstream = new XStream();
    }

  @Override
  public void publish(Catalog catalog) {
    publish(catalog, fileName);
  }

    public void publish(Catalog catalog, String fileName) {
        PrintWriter writer = null;
        try {
          FileOutputStream stream = new FileOutputStream(fileName);
            xstream.toXML(catalog, stream);
        } catch (Exception e) {
            logger.error("Failed to persist catalog", e);
        }
    }

    public String parseIntoXML(Object catalog) {
        return xstream.toXML(catalog);
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

