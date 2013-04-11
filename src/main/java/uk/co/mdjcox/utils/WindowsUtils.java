/**
 * WindowsUtils.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.utils;


import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.logger.LoggingManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;


class WindowsUtils extends OsUtils {

    private static final String CRLF = "\r\n";
    private static final String SEPARATOR = "AAAA";

    public WindowsUtils(LoggerInterface logger) {
        super(logger);
    }

    /**
     * Generate a VBScript string capable of querying the desired WMI
     * information.
     *
     * @param wmiQueryStr                the query string to be passed to the
     *                                   WMI sub-system. <br>i.e. "Select * from
     *                                   Win32_ComputerSystem"
     * @param wmiCommaSeparatedFieldName a comma separated list of the WMI
     *                                   fields to be collected from the query
     *                                   results. <br>i.e. "Model"
     *
     * @return the vbscript string.
     */
    private static String getVBScript(String wmiQueryStr,
                                      String wmiCommaSeparatedFieldName) {
        String vbs = "Dim oWMI : Set oWMI = GetObject(\"winmgmts:\")" + CRLF;
        vbs += "Dim classComponent : Set classComponent = oWMI.ExecQuery(\""
               + wmiQueryStr + "\")" + CRLF;
        vbs += "Dim obj, strData" + CRLF;
        vbs += "For Each obj in classComponent" + CRLF;
        String[] wmiFieldNameArray = wmiCommaSeparatedFieldName.split(",");
        for (int i = 0; i < wmiFieldNameArray.length; i++) {
            vbs += "  strData = strData & obj." + wmiFieldNameArray[i]
                   + " & \"" + SEPARATOR + "\"" + CRLF;
        }
        vbs += "  strData = strData & VBCrLf" + CRLF;
        vbs += "Next" + CRLF;
        vbs += "wscript.echo strData" + CRLF;
        return vbs;
    }

    /**
     * Get an environment variable from the windows OS
     *
     * @param envVarName the name of the env var to get
     *
     * @return the value of the env var
     *
     * @throws Exception if the given envVarName does not exist
     */
    private static String getEnvVar(String envVarName) throws Exception {
        String varName = "%" + envVarName + "%";
        String envVarValue = execute(
                new String[] {"cmd.exe", "/C", "echo " + varName});
        if (envVarValue.equals(varName)) {
            throw new Exception("Environment variable '" + envVarName
                                + "' does not exist!");
        }
        return envVarValue;
    }

    /**
     * Write the given data string to the given file
     *
     * @param filename the file to write the data to
     * @param data     a String ofdata to be written into the file
     *
     * @throws Exception if the output file cannot be written
     */
    private static void writeStrToFile(String filename, String data)
            throws Exception {
        FileWriter output = new FileWriter(filename);
        output.write(data);
        output.flush();
        output.close();
        output = null;
    }

    /**
     * Get the given WMI value from the WMI subsystem on the local computer
     *
     * @param wmiQueryStr  the query string as syntactically defined by the WMI
     *                     reference
     * @param wmiCommaSeparatedFieldName the field object that you want to get out of the query
     *                     results
     *
     * @return the value
     *
     * @throws Exception if there is a problem obtaining the value
     */
    private static String getWMIValue(String wmiQueryStr,
                                     String wmiCommaSeparatedFieldName)
            throws Exception {
        String vbScript = getVBScript(wmiQueryStr, wmiCommaSeparatedFieldName);
        String tmpFileName = System.getProperty("java.io.tmpdir") + File.separator + "jwmi.vbs";
        writeStrToFile(tmpFileName, vbScript);
        String output = execute(
                new String[] {"cmd.exe", "/C", "cscript.exe", tmpFileName});
        new File(tmpFileName).delete();

        return output.trim();
    }

    /**
     * Execute the application with the given command line parameters.
     *
     * @param cmdArray an array of the command line params
     *
     * @return the output as gathered from stdout of the process
     *
     * @throws Exception upon encountering a problem
     */
    private static String execute(String[] cmdArray) throws Exception {
        Process process = Runtime.getRuntime().exec(cmdArray);
        BufferedReader input = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        String output = "";
        String line = "";
        while ((line = input.readLine()) != null) {
            //need to filter out lines that don't contain our desired output
            if (!line.contains("Microsoft") && !line.equals("")) {
                output += line + CRLF;
            }
        }
        process.destroy();
        process = null;
        return output.trim();
    }

    public HashMap<String, String> processList() {
        HashMap<String,String> results = new HashMap<String,String>();
        try {
            logger.info("Getting OS processes");
            String result = getWMIValue("select ProcessId,Name,CommandLine from win32_process", "ProcessId,Name,CommandLine");
            String[] resultsTokens = result.split("\r\n");
            for (int i=0; i<resultsTokens.length-1; i++) {
                String[] resultsTokens2 = resultsTokens[i].split(SEPARATOR);
                String pid = resultsTokens2[0].trim();
                String name = pid;
                if (resultsTokens2.length > 1) {
                    name = resultsTokens2[1].trim();
                }
                String cmd = name;
                if (resultsTokens2.length>2) {
                    cmd = resultsTokens2[2].trim();
                }
                cmd = cmd.replace("  ", " ");
                logger.info(i + " " + pid + " " + name + " " + cmd);
                results.put(cmd, pid);
            }
        } catch (Exception e) {
            logger.warning("Cannot check OS processes", e);
        } finally{
            logger.info("Got OS processes");
        }

        return results;
    }

    public void killOsProcess(String pid, String com) {
        logger.info("Killing process " + pid + " for " + com);
        kill("taskkill /F /PID", pid);
        logger.info("Killed process " + pid + " for " + com);
    }

    public static void main(String[] args) throws Exception {
        LoggerInterface logger = LoggingManager.getLogger(WindowsUtils.class, "test", "/home/michael/Documents/sagetvcatchup");
        LoggingManager.addConsole(logger);
        OsUtils utils = OsUtils.instance(logger);
        String command = "\"C:\\Program Files\\VideoLAN\\VLC\\vlc.exe\"";
//        utils.spawnProcess(command, "VLC", false);
        Thread.sleep(3000);
        HashMap<String, String> processes = utils.processList();
        for (String process : processes.keySet()) {
            String pid = processes.get(process);
            if (process.equals(command)) {
                utils.killOsProcess(pid, command);
            }
        }
       logger.info("Finished test");

    }
}

