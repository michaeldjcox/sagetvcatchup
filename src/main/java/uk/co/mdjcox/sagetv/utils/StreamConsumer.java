package uk.co.mdjcox.sagetv.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
* Created by michael on 29/10/14.
*/
class StreamConsumer extends Thread {
    private InputStream is;
    private LoggerInterface logger;
    private String type;
    private ArrayList<String> output;
    private static int counter = 0;

    /** No access to default constructor */
    StreamConsumer() {
    }

    public StreamConsumer(InputStream is, String type, LoggerInterface logger, ArrayList<String> output) {
        this.is = is;
        this.logger = logger;
        this.type = type;
        if (type != null) {
          setName(type + "-" + (counter++));
        }
        this.output = output;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (logger != null) {
                  if (type != null) {
                    logger.info(line);
                  }
                }
                if (output != null) {
                    output.add(line);
                }
            }
        } catch (IOException ioe) {
            logger.error("Stream consumer terminated with exception", ioe);
        } finally {
//                logger.info("Stream consumer " + type + " terminating ");
        }
    }

    public ArrayList<String> getOutput() {
        return output;
    }
}
