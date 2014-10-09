package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.HtmlBuilder;
import uk.co.mdjcox.utils.PropertiesInterface;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by michael on 02/10/14.
 */
public class CatalogPersisterTest {

    private CatchupTestModule module = new CatchupTestModule();
    private Injector injector = Guice.createInjector(module);
    private PropertiesInterface props;

    @Before
    public void before() throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir", ".");
        File catalogFileName = new File(tmpDir + File.separator + "testcatalog.xml");
        if (catalogFileName.exists()) {
            catalogFileName.delete();
        }


        props = injector.getInstance(PropertiesInterface.class);
        String defaultFileName = System.getProperty("java.io.tmpdir", ".") + File.separator + "sagetvcatchup.xml";

        when(props.getString("catalogFileName", defaultFileName)).thenReturn(catalogFileName.getAbsolutePath());

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
