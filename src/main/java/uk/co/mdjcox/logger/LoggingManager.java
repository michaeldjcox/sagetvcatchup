/**
 * LoggingManager.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.logger;


import java.io.*;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.logging.*;
import java.util.logging.Logger;


/**
 * Provides a logger for the specified class and log file name such that the log
 * files are generated in the installation direcory of the application.
 */
public class LoggingManager {

    public synchronized static void addConsole(LoggerInterface loggerInterface) {
        ConsoleHandler handler2 = new ConsoleHandler();
        // UTF-16 for System.err doesn't seem to work
//        {
//
//            public synchronized void setOutputStream(OutputStream out)
//            throws SecurityException {
//                try {
//                    setEncoding("UTF-16");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//                super.setOutputStream( out);
//            }
//        };
        handler2.setFormatter(new LogRecordFormatter());
        loggerInterface.addHandler(handler2);
    }

    /**
     * Gets a logger instance for the application.
     *
     * @param clazz the main class of the application
     * @param name  the name of the log file to use
     *
     * @return the logger instance
     */
    public synchronized static LoggerInterface getLogger(Class clazz, String name, String logDir) {
        File dir = new File(logDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Logger logger = Logger.getLogger(clazz.getName());
        logger.setUseParentHandlers(false);
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
//        ConsoleHandler handler2 = new ConsoleHandler();
//        handler2.setFormatter(new LogRecordFormatter());
//        logger.addHandler(handler2);
        try {
            FileHandler handler = new FileHandler(logDir + File.separator + name + ".log");
            try {
                if (System.getProperty("logging.unicode", "n").equalsIgnoreCase("y")) {
                    handler.setEncoding("unicode");
                }
            } catch (UnsupportedEncodingException e) {

            }
            handler.setLevel(Level.INFO);
            handler.setFormatter(new LogRecordFormatter());
            logger.addHandler(handler);
        } catch (IOException e) {
            logger.warning("Could not log to file");
        }
        return new uk.co.mdjcox.logger.Logger(logger);
    }

    public static void flushlogs(LoggerInterface loggerInterface) {
        loggerInterface.info("Flushing logs");
        try {
            Handler[] handlers = loggerInterface.getHandlers();
            for (Handler handler : handlers) {
                try {
                    handler.flush();
                } catch (Exception e) {

                }
                try {
                    handler.close();
                } catch (SecurityException e) {

                }
            }
        } catch (Exception e) {
            // We did our best
        }
    }
}
