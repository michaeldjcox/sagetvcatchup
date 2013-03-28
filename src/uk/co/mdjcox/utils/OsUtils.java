/**
 * OsUtils.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.utils;


import uk.co.mdjcox.logger.LoggerInterface;

import java.io.*;
import java.util.*;


public abstract class OsUtils {
    private static OsUtils instance;
    protected LoggerInterface logger;
    private String os;

    public static OsUtils instance(LoggerInterface logger) {
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

    public Process spawnProcess(String radioCommand, String action, boolean wait) throws Exception {
        return spawnProcess(radioCommand, action, wait, null, null);
    }

    public Process spawnProcess(String radioCommand, String action, boolean wait, ArrayList<String> output, ArrayList<String> errors) throws Exception {
        logger.info("Spawn " + radioCommand);

        String command = "";
        String envVar = "";

        if (radioCommand.startsWith("cmd /c start ")) {
            envVar = radioCommand.replace("cmd /c start ", "");
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
            command = radioCommand;
            envVar = "";
        }

        logger.info("Command=" + command);
        logger.info("Env=" + envVar);

        String[] env = getEnvAsStrings("MYCMD=" + envVar);

        Process process = Runtime.getRuntime().exec(split(command), env);

        captureStreams(process, action, output, errors, wait);
        if (wait) {
            logger.info("Entering proc waitfor " + radioCommand);
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                logger.info("Interrupted waiting for process");
            }
            logger.info("Exiting proc waitfor " + radioCommand);
            int result = process.exitValue();
            logger.info("Result is " + result + " for " + radioCommand);
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
        Map<String, String> envp = System.getenv();
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

    public abstract void killOsProcess(String pid, String cmd);

    public abstract HashMap<String, String> processList();

    private class StreamConsumer extends Thread {
        private InputStream is;
        private LoggerInterface logger;
        private String type;
        private ArrayList<String> output;

        /** No access to default constructor */
        private StreamConsumer() {
        }

        public StreamConsumer(InputStream is, String type, LoggerInterface logger, ArrayList<String> output) {
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
                logger.severe("Stream consumer terminatated with exception", ioe);
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
                    logger.info(split[split.length-1].trim() + "=" + commandString.trim());
                    results.put(commandString.trim(), split[split.length-1].trim());
                }
            }
        } catch (Exception e) {
            logger.warning("Cannot check OS processes", e);
        }
        return results;
    }

    protected void kill(String command, String pid) {
        if ((pid == null) || pid.trim().isEmpty()) return;
        try {
            Process process = Runtime.getRuntime().exec(command  + " " + pid);
            captureStreams(process, "kill", null, null, true);
            process.waitFor();
        } catch (Exception e) {
            logger.warning("Failed to kill OS process "+ pid, e);
        }
    }
}
