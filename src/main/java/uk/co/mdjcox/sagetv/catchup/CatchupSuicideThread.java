package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.utils.LoggerInterface;
import uk.co.mdjcox.utils.RmiHelper;
import uk.co.mdjcox.utils.SageUtils;

/**
 * Created by michael on 24/10/14.
 */
public class CatchupSuicideThread extends Thread {
  private final CatchupContextInterface context;
  private final LoggerInterface logger;

  public CatchupSuicideThread(LoggerInterface logger, CatchupContextInterface context) {
    super("catchup-suicide-thread");
    this.context = context;
    this.logger = logger;
  }

  private CatchupPluginRemote getCatchupPluginRemote() throws Exception {
    int rmiRegistryPort = context.getCatchupPluginRmiPort();

    return (CatchupPluginRemote) RmiHelper.lookup("127.0.0.1", rmiRegistryPort, "CatchupPlugin");
  }

  @Override
  public void run() {

    if (CatchupContext.isRunningInDev()) {
      return;
    }
    int tries = 0;
    while (tries < 3) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
      boolean sagetvOk = false;
      try {
        sagetvOk = getCatchupPluginRemote().available();
      } catch (Exception e) {
      }
      if (!sagetvOk) {
        tries++;
      } else {
        tries = 0;
      }
    }
    logger.info("*** SageTV Catchup plugin is no longer there so server must exit***");
    System.exit(1);
  }
}
