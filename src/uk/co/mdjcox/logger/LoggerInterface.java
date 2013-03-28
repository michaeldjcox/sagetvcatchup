package uk.co.mdjcox.logger;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 26/03/13
 * Time: 08:08
 * To change this template use File | Settings | File Templates.
 */
public interface LoggerInterface {
    void severe(String msg);

    void severe(String msg, Throwable thrown);

    void warning(String msg);

    void warning(String msg, Throwable thrown);

    void info(String msg);

    void addHandler(Handler handler) throws SecurityException;

    Handler[] getHandlers();

}
