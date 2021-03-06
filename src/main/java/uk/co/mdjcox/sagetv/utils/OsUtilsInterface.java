package uk.co.mdjcox.sagetv.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 15/04/13
 * Time: 07:24
 * To change this template use File | Settings | File Templates.
 */
public interface OsUtilsInterface {
    Process spawnProcess(String osCommand, String loggerName, boolean waitForCompletion, File workingDir) throws Exception;

    Process spawnProcess(String osCommand, String loggerName, boolean waitForCompletion,
                         ArrayList<String> output, ArrayList<String> errors, File workingDir) throws Exception;

    void killProcess(String pid, String cmd);

    HashMap<String, String> getProcesses();

    void killProcessesContaining(String expression);

    Map<String, String> findProcessesContaining(String token);

    Map<String, String> findProcessesMatching(String regex);

    void killProcessesMatching(String regex);

    boolean isWindows();

    boolean isUnix();

    void waitFor(long millis);

    boolean deleteFileOrDir(File fileOrDir, boolean deleteRoot);

  }
