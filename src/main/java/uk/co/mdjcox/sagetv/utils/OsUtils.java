/**
 * OsUtils.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.sagetv.utils;




import java.io.*;
import java.util.*;


public abstract class OsUtils implements OsUtilsInterface {
    private static OsUtilsInterface instance;
    protected LoggerInterface logger;
    private String os;

    public static OsUtilsInterface instance(LoggerInterface logger) {
        if (instance == null) {
            if (File.separatorChar=='\\') {
                instance = new WindowsUtils(logger);
            } else {
                instance = new UnixUtils(logger);
            }
        }
        return instance;
    }

    protected OsUtils(LoggerInterface logger) {
        this.logger = logger;
        os =  System.getProperty("os.name");
        os = os.replace(' ', '_');
        os = os.replace('.','_');
    }

    @Override
    public Process spawnProcess(String osCommand, String loggerName, boolean waitForCompletion, File workingDir) throws Exception {
        return spawnProcess(osCommand, loggerName, waitForCompletion, null, null, workingDir);
    }

    @Override
    public Process spawnProcess(String osCommand, String loggerName, boolean waitForCompletion,
                                ArrayList<String> output, ArrayList<String> errors, File workingDir) throws Exception {
        if (loggerName != null) {
          logger.info("Spawn " + osCommand);
        }

        String command = "";
        String envVar = "";

        if (osCommand.startsWith("cmd /c start ")) {
            envVar = osCommand.replace("cmd /c start ", "");
            String[] split = split(envVar);
            String name = split[0];
            if (name.startsWith("\"") && name.endsWith("\"") && !name.contains(".exe") && !name.contains(".EXE")) {
                command = "cmd /c start " + name;
                envVar = envVar.replace(name + " ", "");
            } else {
                command = "cmd /c start ";
                envVar = command;
            }
            command = command + " %MYCMD%";
        } else {
            command = osCommand;
            envVar = "";
        }

//        logger.info("Command=" + command);
//        logger.info("MyCMD=" + envVar);

        String[] env = getEnvAsStrings("MYCMD=" + envVar);

//        for (String envVariable : env) {
//          logger.info("ENV: " + envVariable);
//        }

        Process process = null;

        if (workingDir != null) {
         process = Runtime.getRuntime().exec(split(command), env, workingDir);
        } else {
          process = Runtime.getRuntime().exec(split(command), env);
        }
        captureStreams(process, loggerName, output, errors, waitForCompletion);
        if (waitForCompletion) {
//            logger.info("Entering proc waitfor " + osCommand);
            try {
                process.waitFor();
            } catch (InterruptedException e) {
//                logger.info("Interrupted waiting for process");
            }
//            logger.info("Exiting proc waitfor " + osCommand);
            int result = process.exitValue();
//            logger.info("Result is " + result + " for " + osCommand);
            return process;
        } else {
            return process;
        }
    }

    public String[] split(String command) {
        ArrayList<String> params = new ArrayList<String>();
          boolean inQuotes = false;
          String current = "";
          for (int i=0; i<command.length(); i++) {
              char chr = command.charAt(i);
              if (chr == '\"') {
                  current += chr;
                  if (inQuotes) {
                      params.add(current);
                      current = "";
                  }
                  inQuotes = !inQuotes;
                  continue;
              }

              if (inQuotes) {
                  current += chr;
                  continue;
              } else {
                  if (chr == ' ') {
                      if (current.length()>0) {
                          params.add(current);
                      }
                     current = "";
                  } else {
                      current += chr;
                  }
              }
          }
          if (current.trim().length()>0) {
              params.add(current);
          }

          return params.toArray(new String[params.size()]);
    }

    private void captureStreams(final Process proc, String action, ArrayList<String> output, ArrayList<String> errors, boolean wait) {
        StreamConsumer stderr ;
        StreamConsumer stdout ;
        stderr = new StreamConsumer( proc.getErrorStream(), action == null ? null : action + ".err", logger, errors);
        stdout = new StreamConsumer( proc.getInputStream(), action == null ? null : action + ".out", logger, output);
        stderr.start();
        stdout.start();
        // Send a linefeed to get around any pause for error
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {

                }
                OutputStream os = proc.getOutputStream();
                try {
                    os.write(" \n".getBytes());
                    os.flush();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();

        if (wait) {
            try {
                stdout.join();
            } catch (InterruptedException e) {
            }
            try {
                stderr.join();
            } catch (InterruptedException e) {
            }
        }
    }

  public boolean deleteFileOrDir(File fileOrDir, boolean deleteRoot) {
    if (fileOrDir.isDirectory()) {
      String[] children = fileOrDir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteFileOrDir(new File(fileOrDir, children[i]), true);
        final String fileName = fileOrDir + File.separator + children[i];
        if (!success) {
          logger.warn("FAILED deleting " + fileName);
          return false;
        } else {
          logger.info("Deleted " + fileName);
        }
      }
    }

    if (deleteRoot) {
      logger.info("Deleted " + fileOrDir);
      return fileOrDir.delete();
    } else {
      return false;
    }
  }




  private String[] getEnvAsStrings(String envVar) {
        Map<String, String> envp = new TreeMap(System.getenv());
        if (isWindows()) {
            String userDir = System.getProperty("user.dir");
            String homepath = userDir.substring(2);
            String homedrive = userDir.substring(0, userDir.indexOf("\\"));

            if (!envp.containsKey("HOMEPATH")) {
                envp.put("HOMEPATH", homepath);
                envp.put("HOMEDRIVE", homedrive);
                logger.info("Setting homedrive to " + homedrive);
                logger.info("Setting homepath to " + homepath);
            }
        }
        Set set = envp.entrySet();
        Iterator itr = set.iterator();
        String[] env = new String[envp.size()+1];
        int i=0;
        for (i = 0; i < set.size(); i++) {
            env[i] = itr.next().toString();
        }
        env[i]=envVar;
        return env;
    }

  protected HashMap<String, String> getProcesses(String command) {
        HashMap<String,String> results = new HashMap<String,String>();
        try {
            ArrayList<String> output = new ArrayList<String>();
            ArrayList<String> errors = new ArrayList<String>();
            spawnProcess(command, "ps", true, output, errors, null);
            for (String out : output) {
                String[] split = split(out);
                String commandString = "";
                for (int i=0; i<(split.length-1); i++) {
                    commandString += split[i] + " ";
                }
                if (split.length > 1) {
//                    logger.info(split[split.length-1].trim() + "=" + commandString.trim());
                    results.put(split[split.length-1].trim(), commandString.trim());
                }
            }
        } catch (Exception e) {
            logger.warn("Cannot check OS processes", e);
        }
        return results;
    }

  @Override
  public void killProcessesMatching(String regex) {
    logger.info("Killing processes matching: " + regex);
    HashMap<String, String> processes = getProcesses();
    Set<String> kills = new HashSet<String>();
    for (String pid : processes.keySet()) {
      String process = processes.get(pid);
      if (process.matches(regex)) {
        killProcess(pid, process);
        kills.add(pid);
      }
    }

    if (kills.isEmpty()) {
      logger.warn("Failed to kill any process matching " + regex);
    } else {
      logger.info("Killed processes " + kills + " which matched " + regex);
    }

  }

    public Map<String, String> findProcessesContaining(String token) {
//      logger.info("Finding processes containing: " + token);
        HashMap<String, String> matching = new HashMap<String, String>();
        HashMap<String, String> processes = getProcesses();
        for (String pid : processes.keySet()) {
            String process = processes.get(pid);
            if (process.contains(token)) {
                matching.put(pid, process);
            }
        }
//      logger.info("Found: " + matching.keySet());
      return matching;
    }

    public Map<String, String> findProcessesMatching(String regex) {
//      logger.info("Finding processes matching: " + regex);
        HashMap<String, String> matching = new HashMap<String, String>();
        HashMap<String, String> processes = getProcesses();
        for (String pid : processes.keySet()) {
            String process = processes.get(pid);
            if (process.matches(regex)) {
                matching.put(pid, process);
            }
        }
//      logger.info("Found: " + matching.keySet());
      return matching;
    }

  public void killProcessesContaining(String token) {
    logger.info("Killing processes containing: " + token);
        HashMap<String, String> processes = getProcesses();
        Set<String> kills = new HashSet<String>();
        for (String pid : processes.keySet()) {
            String process = processes.get(pid);
            if (process.contains(token)) {
                killProcess(pid, process);
                kills.add(pid);
            }
        }

        if (kills.isEmpty()) {
          logger.warn("Failed to kill any process containing " + token);
        } else {
          logger.info("Killed processes " + kills + " which contained " + token);
        }

    }

    @Override
    public void waitFor(long millis) {
      long stopTime = System.currentTimeMillis() + millis;
      while (System.currentTimeMillis() < stopTime) {
        try {
          long left = stopTime -System.currentTimeMillis();
          if (left > 0) {
            Thread.sleep(left);
          }
        } catch (InterruptedException e) {

        }
      }
    }

  protected void kill(String command, String pid) {
        if ((pid == null) || pid.trim().isEmpty()) return;
        try {
            Process process = Runtime.getRuntime().exec(command  + " " + pid);
            captureStreams(process, "kill", null, null, true);
            process.waitFor();
        } catch (Exception e) {
            logger.warn("Failed to kill OS process " + pid, e);
        }
    }



}
