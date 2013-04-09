package uk.co.mdjcox.logger;

import com.google.inject.Inject;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 26/03/13
 * Time: 08:09
 * To change this template use File | Settings | File Templates.
 */
public class Logger implements LoggerInterface {

    private java.util.logging.Logger logger;

    @Inject
    public Logger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void severe(String msg) {
        logger.severe(msg);
    }

    public void severe(String msg, Throwable exception) {
        logger.log(Level.SEVERE, msg, exception);
    }

    @Override
    public void warning(String msg) {
        logger.warning(msg);
    }

    @Override
    public void warning(String msg, Throwable exception) {
        logger.log(Level.WARNING, msg, exception);
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void addHandler(Handler handler) throws SecurityException {
        logger.addHandler(handler);
    }

    public void removeHandler(Handler handler) throws SecurityException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Handler[] getHandlers() {
        return new Handler[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
