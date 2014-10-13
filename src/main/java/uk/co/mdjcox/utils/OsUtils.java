/**
 * OsUtils.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.utils;


import org.slf4j.Logger;

import java.io.*;
import java.util.*;


public abstract class OsUtils implements OsUtilsInterface {
    private static OsUtilsInterface instance;
    protected Logger logger;
    private String os;

    public static OsUtilsInterface instance(Logger logger) {
        if (instance == null) {
            if (File.separatorChar=='\\') {
                instance = new WindowsUtils(logger);
            } else {
                instance = new UnixUtils(logger);
            }
        }
        return instance;
    }

    protected OsUtils(Logger logger) {
        this.logger = logger;
        os =  System.getProperty("os.name");
        os = os.replace(' ', '_');
        os = os.replace('.','_');
    }

    @Override
    public Process spawnProcess(String osCommand, String loggerName, boolean waitForCompletion) throws Exception {
        return spawnProcess(osCommand, loggerName, waitForCompletion, null, null);
    }

    @Override
    public Process spawnProcess(String osCommand, String loggerName, boolean waitForCompletion, ArrayList<String> output, ArrayList<String> errors) throws Exception {
        logger.info("Spawn " + osCommand);

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

        logger.info("Command=" + command);
        logger.info("Env=" + envVar);

        String[] env = getEnvAsStrings("MYCMD=" + envVar);

        Process process = Runtime.getRuntime().exec(split(command), env);

        captureStreams(process, loggerName, output, errors, waitForCompletion);
        if (waitForCompletion) {
            logger.info("Entering proc waitfor " + osCommand);
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                logger.info("Interrupted waiting for process");
            }
            logger.info("Exiting proc waitfor " + osCommand);
            int result = process.exitValue();
            logger.info("Result is " + result + " for " + osCommand);
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
        logger.info("Setting up streams for " + action);
        StreamConsumer stderr ;
        StreamConsumer stdout ;
        stderr = new StreamConsumer( proc.getErrorStream(), action + ".err", logger, errors);
        stdout = new StreamConsumer( proc.getInputStream(), action + ".out", logger, output);
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

    private class StreamConsumer extends Thread {
        private InputStream is;
        private Logger logger;
        private String type;
        private ArrayList<String> output;

        /** No access to default constructor */
        private StreamConsumer() {
        }

        public StreamConsumer(InputStream is, String type, Logger logger, ArrayList<String> output) {
            this.is = is;
            this.logger = logger;
            this.type = type;
            this.output = output;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (logger != null) {
                        logger.info(type + ": " + line);
                    }
                    if (output != null) {
                        output.add(line);
                    }
                }
            } catch (IOException ioe) {
                logger.error("Stream consumer terminatated with exception", ioe);
            } finally {
                logger.info("Stream consumer " + type + " terminating ");
            }
        }

        public ArrayList<String> getOutput() {
            return output;
        }
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
                for (int i=0; i<(split.length-1); i++) {
                    commandString += split[i] + " ";
                }
                if (split.length > 1) {
//                    logger.info(split[split.length-1].trim() + "=" + commandString.trim());
                    results.put(commandString.trim(), split[split.length-1].trim());
                }
            }
        } catch (Exception e) {
            logger.warn("Cannot check OS processes", e);
        }
        return results;
    }

  @Override
  public void killProcessesMatching(String regex) {
    HashMap<String, String> processes = getProcesses();
    Set<String> kills = new HashSet<String>();
    for (String process : processes.keySet()) {
      String pid = processes.get(process);
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
        HashMap<String, String> matching = new HashMap<String, String>();
        HashMap<String, String> processes = getProcesses();
        for (String process : processes.keySet()) {
            String pid = processes.get(process);
            if (process.contains(token)) {
                matching.put(pid, process);
            }
        }
        return matching;
    }

    public Map<String, String> findProcessesMatching(String regex) {
        HashMap<String, String> matching = new HashMap<String, String>();
        HashMap<String, String> processes = getProcesses();
        for (String process : processes.keySet()) {
            String pid = processes.get(process);
            if (process.matches(regex)) {
                matching.put(pid, process);
            }
        }
        return matching;
    }

  public void killProcessesContaining(String token) {
        HashMap<String, String> processes = getProcesses();
        Set<String> kills = new HashSet<String>();
        for (String process : processes.keySet()) {
            String pid = processes.get(process);
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
      logger.info("Waiting for " + millis + "ms");
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
