/**
 * Created by IntelliJ IDEA.
 * User: Michael
 * Date: 06-Dec-2008
 * Time: 22:57:46
 * To change this template use File | Settings | File Templates.
 */
package uk.co.mdjcox.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;


/**
 * Formats the output of a log record.
 * <p/>
 * The default log record format is overridden to provide a log output format on
 * a single line rather than two lines.
 *
 * @author Michael Cox
 * @version 1.0
 */
public final class LogRecordFormatter
        extends SimpleFormatter {

    /** The format of the date and time portion of each log message. */
    private static final String FORMAT = "{0,date} {0,time}";
    /** Line separator string appropriate for the platform. */
    private static final String LINE_SEPARATOR = System
            .getProperty("line.separator");
    /** Initial capacity for the log record string buffer. */
    private static final int STRING_BUFFER_ALLOCATION = 320;
    /**
     * A date instance that will be reused for the date and time output on each
     * log message.
     */
    private final Date dat = new Date();
    /** The formatter that will be used to format a line of log output. */
    private final MessageFormat formatter;

    /**
     * Arguments to be passed to the format when formatting each log record for
     * output.
     */
    private final Object[] args = new Object[1];

    /** Constructs an instance. */
    public LogRecordFormatter() {
        formatter = new MessageFormat(FORMAT);
    }

    /**
     * Formats the given log record as a string for output.
     *
     * @param record the log record to be formatted
     *
     * @return a string representation of the log recrd
     */
    @Override
    public synchronized String format(final LogRecord record) {
        final long time = record.getMillis();
        dat.setTime(time);
        args[0] = dat;
        final StringBuffer sb = new StringBuffer(STRING_BUFFER_ALLOCATION);
        formatter.format(args, sb, null);
        sb.append(' ');
        final Thread currentThread = Thread.currentThread();
        final String threadName = currentThread.getName();
        sb.append(threadName);
        sb.append(' ');
        final String className = record.getSourceClassName();
        if (className != null) {
            sb.append(className);
        } else {
            final String loggerName = record.getLoggerName();
            sb.append(loggerName);
        }
        final String methodName = record.getSourceMethodName();
        if (methodName != null) {
            sb.append(' ');
            sb.append(methodName);
        }
        sb.append(' ');
        final String message = formatMessage(record);
        final Level level = record.getLevel();
        final String levelName = level.getLocalizedName();
        sb.append(levelName);
        sb.append(": ");
        sb.append(message);
        sb.append(LINE_SEPARATOR);
        appendThrowable(record, sb);
        return sb.toString();
    }

    /**
     * Appends details of an exception thrown found in the log record to a
     * string buffer.
     *
     * @param record the log record containing the exception details
     * @param sb     the string buffer to which the details are added
     */
    private static void appendThrowable(final LogRecord record,
                                        final StringBuffer sb) {
        if (record.getThrown() != null) {
            final StringWriter sw = new StringWriter();
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(sw);
                final Throwable throwable = record.getThrown();
                throwable.printStackTrace(pw);
            } finally {
                if (pw != null) {
                    pw.close();
                }
            }
            final String str = sw.toString();
            sb.append(str);
        }
    }
}
