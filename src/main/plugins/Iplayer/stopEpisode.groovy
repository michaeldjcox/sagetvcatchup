package Iplayer

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 15/04/13
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */

HashMap<String, String> processes = getProcesses();
for (String process : processes.keySet()) {
    String pid = processes.get(process);
    LOG_INFO("Checking " + pid + " " + process);
    if (process.contains(recording.getUrl())) {
        killProcess(pid, process);
    }
}