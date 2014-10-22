package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.catchup.server.Server;
import uk.co.mdjcox.sagetv.onlinevideo.SageTvPublisher;
import uk.co.mdjcox.utils.LoggerInterface;
import uk.co.mdjcox.utils.PersistentRollingFileAppender;
import uk.co.mdjcox.utils.PropertiesInterface;

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
  private PropertiesInterface props;
  private Cataloger cataloger;
  private PluginManager pluginManager;
  private Recorder recorder;
  private CatchupContextInterface context;
  private AbstractModule module;

  private boolean started = false;

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
      }));


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

      props = injector.getInstance(PropertiesInterface.class);
      context = injector.getInstance(CatchupContextInterface.class);

      logger.info("Properties: " + props.toString());
      logger.info("Context:    " + context.toString());

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

  public static void main(String[] args) {
    CatchupServer server = new CatchupServer();
    server.start();
  }

}
