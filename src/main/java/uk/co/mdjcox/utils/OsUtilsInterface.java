package uk.co.mdjcox.utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 15/04/13
 * Time: 07:24
 * To change this template use File | Settings | File Templates.
 */
public interface OsUtilsInterface {
    Process spawnProcess(String osCommand, String loggerName, boolean waitForCompletion) throws Exception;

    Process spawnProcess(String osCommand, String loggerName, boolean waitForCompletion, ArrayList<String> output, ArrayList<String> errors) throws Exception;

    void killProcess(String pid, String cmd);

    HashMap<String, String> getProcesses();

    void killProcessesContaining(String expression);
}
