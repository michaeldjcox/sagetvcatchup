package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 05/04/13
 * Time: 18:41
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class PluginManager {

    private Logger logger;
    private LinkedHashMap<String, Plugin> plugins = new  LinkedHashMap<String, Plugin>();
    private Set<String> pluginNames = new TreeSet<String>();
    @Inject
    private PluginFactory pluginFactory;
    private CatchupContextInterface context;

    @Inject
    private PluginManager(Logger logger, CatchupContextInterface context) {
        this.logger = logger;
        this.context = context;
    }

    public Collection<Plugin> getPlugins() {
        return plugins.values();
    }

    public Plugin getPlugin(String sourceId) {
        return plugins.get(sourceId);
    }

    public void start() {

      logger.info("Starting the plugin manager");

      try {
        String base = context.getPluginDir();

        File dir = new File(base);
        if (!dir.isDirectory()) {
            throw new RuntimeException("Plugin directory " + base + " is not a directory");
        }

        if (!dir.exists()) {
            throw new RuntimeException("Plugin directory " + base + " does not exist");
        }

        File[] pluginDirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        for (File pluginDir : pluginDirs) {
            String sourceId = pluginDir.getName();
          pluginNames.add(sourceId);
          if (context.skipPlugin(sourceId)) {
            logger.info("Skipping plugin " + sourceId);
            continue;
          }
            Plugin plugin = pluginFactory.createPlugin(sourceId, pluginDir.getAbsolutePath());
            plugin.init();
            plugins.put(sourceId, plugin);
        }

        logger.info("Started the plugin manager");
      } catch (Exception e) {
        logger.error("Failed to start the plugin manager", e);
      }
    }

  public Set<String> getPluginNames() {
    return pluginNames;
  }
}
