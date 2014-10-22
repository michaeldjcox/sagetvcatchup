/**
 * UnixUtils.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.utils;




import java.util.ArrayList;
import java.util.HashMap;


class UnixUtils extends OsUtils {

    UnixUtils(LoggerInterface logger) {
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
            spawnProcess(command, null, true, output, errors, null);
            for (String out : output) {
                out = out.trim();
                String[] split = split(out);
                String commandString = "";
                for (int i=1; i<split.length; i++) {
                    commandString += split[i] + " ";
                }
                if (split.length > 1) {
                    results.put(split[0].trim(), commandString.trim());
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
