package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.sagetv.model.Catalog;

/**
 * Created by michael on 19/09/14.
 */
public interface CatalogPublisher {
    void publish(Catalog catalog);
}
