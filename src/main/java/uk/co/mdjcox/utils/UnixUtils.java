/**
 * UnixUtils.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.utils;


import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;


class UnixUtils extends OsUtils {

    UnixUtils(Logger logger) {
        super(logger);
    }

    public HashMap<String, String> getProcesses() {
        return getProcesses("ps -eo pid,command");
    }

    public void killProcess(String pid, String com) {
        kill("kill ", pid);
    }

    protected HashMap<String, String> getProcesses(String command) {
        HashMap<String,String> results = new HashMap<String,String>();
        try {
            ArrayList<String> output = new ArrayList<String>();
            ArrayList<String> errors = new ArrayList<String>();
            spawnProcess(command, "ps", true, output, errors);
            for (String out : output) {
                String[] split = split(out);
                String commandString = "";
                for (int i=1; i<split.length-1; i++) {
                    commandString += split[i] + " ";
                }
                if (split.length > 1) {
                    logger.info(split[0].trim() + "=" + commandString.trim());
                    results.put(commandString.trim(), split[0].trim());
                }
            }
        } catch (Exception e) {
            logger.warn("Cannot check OS processes", e);
        }
        return results;
    }


  @Override
  public boolean isWindows() {
    return false;
  }

  @Override
  public boolean isUnix() {
    return true;
  }
}
