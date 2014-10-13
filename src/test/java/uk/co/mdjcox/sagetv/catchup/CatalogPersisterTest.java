package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.model.CatalogTestHelper;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by michael on 02/10/14.
 */
public class CatalogPersisterTest {

    private CatchupTestModule module = new CatchupTestModule();
    private Injector injector = Guice.createInjector(module);
    private CatchupContextInterface context;

    @Before
    public void before() throws Exception {
        context = injector.getInstance(CatchupContextInterface.class);

       String catalogFileName = context.getCatalogFileName();

      File catalogFile = new File(catalogFileName);
      if (catalogFile.exists()) {
        catalogFile.delete();
      }
    }

    @Test
    public void testCatalogFileWriteRead() {
        Catalog catalog = CatalogTestHelper.getTestCatalog();

        CatalogPersister persister = injector.getInstance(CatalogPersister.class);

        persister.publish(catalog);

        Catalog catalog2 = persister.load();

        assertEquals("Persisted is same as saved", catalog, catalog2);


    }


}
