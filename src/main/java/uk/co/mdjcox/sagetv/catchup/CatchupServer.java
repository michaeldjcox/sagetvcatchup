package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.catchup.server.Server;
import uk.co.mdjcox.sagetv.onlinevideo.SageTvPublisher;
import uk.co.mdjcox.utils.LoggerInterface;
import uk.co.mdjcox.utils.PersistentRollingFileAppender;
import uk.co.mdjcox.utils.PropertiesInterface;
import uk.co.mdjcox.utils.RmiHelper;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michael on 20/10/14.
 */
public class CatchupServer {

  public LoggerInterface logger;
  public static Injector injector;

  private Server server;
  private SageTvPublisher sagetvPublisher;
  private Cataloger cataloger;
  private PluginManager pluginManager;
  private Recorder recorder;
  private CatchupContextInterface context;
  private AbstractModule module;

  private boolean started = false;
  private CatchupServerService rmiService;

  public void start() {
    try {
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            stop();
          } catch (Exception e) {
            System.err.println("Shutdown hook failed");
            e.printStackTrace();
          }
        }
      }, "catchup-server-shutdown-hook"));


      PersistentRollingFileAppender.stopped = false;

      if (CatchupContext.isRunningInDev()) {
        module = new CatchupDevModule();
      }
      else {
        module = new CatchupModule();
      }

      injector = Guice.createInjector(module);
      logger = injector.getInstance(LoggerInterface.class);

      logger.info("#######################");
      logger.info("Starting catchup server");
      logger.info("#######################");

      PropertiesInterface props = injector.getInstance(PropertiesInterface.class);
      context = injector.getInstance(CatchupContextInterface.class);

      logger.info("Java properties: " + System.getProperties());
      logger.info("Catchup Properties: " + props.toString());
      logger.info("Catchup Context:    " + context.toString());

      pluginManager = injector.getInstance(PluginManager.class);
      server = injector.getInstance(Server.class);
      cataloger = injector.getInstance(Cataloger.class);
      sagetvPublisher = injector.getInstance(SageTvPublisher.class);
      recorder = injector.getInstance(Recorder.class);
      CatalogPersister persister = injector.getInstance(CatalogPersister.class);

      pluginManager.start();

      List<CatalogPublisher> publishers = new ArrayList<CatalogPublisher>();
      publishers.add(sagetvPublisher);
      publishers.add(server);
      publishers.add(persister);

      recorder.start();
      server.start();
      cataloger.start(publishers, persister);

      startRmiServer();

      new CatchupSuicideThread(logger, context).run();

      Thread thread = new Thread(new Runnable() {
          @Override
          public void run() {
                while (true) {
                    try {
                        long totalMem = Runtime.getRuntime().totalMemory() / 1024000;
                        long freeMem = Runtime.getRuntime().freeMemory() / 1024000;
                        long maxMem = Runtime.getRuntime().maxMemory() / 1024000;

                        logger.info("Heap: " + (totalMem -freeMem) + "/" + totalMem + "/" + maxMem + "Mb used" );
                    } catch (Exception e) {

                    }
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {

                    }
                }
          }
      }, "catchup-system-monitor");
        thread.setDaemon(true);
        thread.start();

      started = true;
    } catch (Throwable e) {
      if (logger == null) {
        System.err.println("Failed to start catchup server");
        e.printStackTrace();
      } else {
        logger.error("Failed to start catchup server", e);
      }
    }
  }

  public void stop() {
    try {
      if (logger != null) {
        logger.info("Stopping catchup server");
      }

      if (recorder != null) {
        recorder.shutdown();
      }

      if (cataloger != null) {
        cataloger.shutdown();
      }


      try {
        if (server != null) {
          server.shutdown();
        }
      } catch (Exception e) {
        if (logger != null) {
          logger.error("Failed to stop server", e);
        }
      }

      try {
        if (sagetvPublisher != null) {
          sagetvPublisher.unpublish();
        }
      } catch (Exception e) {
        if (logger != null) {
          logger.error("Failed to remove online video properties", e);
        }
      }

      stopRmiServer();

      started = false;
    } catch (Throwable ex) {
      if (logger != null) {
        logger.error("Failed to stop catchup server");
      }
    }
    finally
    {
      logger.info("#######################");
      logger.info("Stopped catchup server");
      logger.info("#######################");

      logger.flush();

      PersistentRollingFileAppender.stopped = true;

    }
  }

  public boolean isStarted() {
    return started;
  }

  public boolean isStopped() {
    return !started;
  }

  private void startRmiServer() {
    try {
      rmiService = new CatchupServerService(cataloger, recorder, context);

      int rmiRegistryPort = context.getCatchupServerRmiPort();
      logger.info("Offer remote access to server");
      RmiHelper.startupLocalRmiRegistry(rmiRegistryPort);
      String name =RmiHelper.rebind("127.0.0.1", rmiRegistryPort, "CatchupServer", rmiService);
      logger.info("Bound name >" + name +"<");

      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

        public void run() {
          try {
            logger.info("Stopping catchup rmi server");
            stopRmiServer();
            logger.info("Stopped catchup rmi server");
          } catch (Exception e) {
            logger.warn("Failed to stop catchup rmi server", e);
          }
        }
      }, "catchup-rmi-server-shutdown-hook"));
    } catch (Exception e) {
      logger.error("Cannot start catchup rmi server ", e);
    }
  }

  private void stopRmiServer() {
    try {
      logger.info("Discontinue rmi access to catchup server");
      int rmiRegistryPort = context.getCatchupServerRmiPort();
      RmiHelper.unbind("127.0.0.1", rmiRegistryPort, "CatchupServer");
    } catch (NotBoundException nb) {
      // Ignore
    }
    catch (Exception e) {
      logger.error("Cannot stop catchup rmi server ", e);
    }
  }

  public static void main(String[] args) {
    CatchupServer server = new CatchupServer();
    server.start();
  }

}
