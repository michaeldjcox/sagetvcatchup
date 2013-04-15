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
    Process spawnProcess(String radioCommand, String action, boolean wait) throws Exception;

    Process spawnProcess(String radioCommand, String action, boolean wait, ArrayList<String> output, ArrayList<String> errors) throws Exception;

    void killOsProcess(String pid, String cmd);

    HashMap<String, String> processList();
}
