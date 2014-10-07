package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.*;

/**
 * Created by michael on 02/10/14.
 */
@Singleton
public class CatalogPersister implements CatalogPublisher {

    private final XStream xstream;
    private Logger logger;
    private String fileName;

    @Inject
    private CatalogPersister(Logger logger, PropertiesInterface props) {
        this.logger = logger;
        String defaultFileName = System.getProperty("java.io.tmpdir", ".") + File.separator + "sagetvcatchup.xml";
        fileName = props.getString("catalogFileName", defaultFileName);
        xstream = new XStream();
    }

    @Override
    public void publish(Catalog catalog) {
        PrintWriter writer = null;
        try {
            String xml = parseIntoXML(catalog);
            writer = writeCatalog(writer, xml);
        } catch (Exception e) {
            logger.error("Failed to persist catalog", e);
        }
    }

    public String parseIntoXML(Object catalog) {
        return xstream.toXML(catalog);
    }

    PrintWriter writeCatalog(PrintWriter writer, String xml) throws FileNotFoundException {
        try {
            writer = new PrintWriter(fileName);
            writer.write(xml);
            writer.flush();
            return writer;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    public Catalog load() {
        try {
            String xml = readCatalog();
            return parseXMLIntoCatalog(xml);
        } catch (Exception e) {
            logger.error("Failed to load catalog", e);
        }
        return new Catalog();
    }

    Catalog parseXMLIntoCatalog(String xml) {
        return (Catalog)xstream.fromXML(xml);
    }

    String readCatalog() throws Exception {
        File file = new File(fileName);
        FileReader reader = null;
        BufferedReader breader = null;
        try {
            reader = new FileReader(file);
            breader = new BufferedReader(reader);
            String result = "";
            String line = "";
            while ((line = breader.readLine()) != null) {
                result+=line;
                result+="\n";
            }
            return result;
        } finally {
            if (breader != null) {
                try {
                    breader.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

}
