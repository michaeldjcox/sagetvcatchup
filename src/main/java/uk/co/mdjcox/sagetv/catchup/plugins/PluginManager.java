package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.sagetv.utils.LoggerInterface;

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

    private LoggerInterface logger;
    private LinkedHashMap<String, PluginInterface> plugins = new  LinkedHashMap<String, PluginInterface>();
    private LinkedHashMap<String, PluginInterface> pluginForSource = new  LinkedHashMap<String, PluginInterface>();

    private Set<String> pluginNames = new TreeSet<String>();
    @Inject
    private PluginFactory pluginFactory;
    @Inject
    private PluginUpnpFactory pluginUpnpFactory;
    private CatchupContextInterface context;

    @Inject
    private PluginManager(LoggerInterface logger, CatchupContextInterface context) {
        this.logger = logger;
        this.context = context;
    }

    public Collection<PluginInterface> getPlugins() {
        return plugins.values();
    }

    public PluginInterface getPlugin(String sourceId) {
        return plugins.get(sourceId);
    }

    public PluginInterface getPluginForSource(String sourceId) {
        return pluginForSource.get(sourceId);
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
            PluginInterface plugin = null;
          final File[] files = pluginDir.listFiles();
          if (files != null) {
            try {
              if (files.length > 3) {
                plugin = pluginFactory.createPlugin(sourceId, pluginDir.getAbsolutePath());
              } else
              if (files.length > 0) {
                plugin = pluginUpnpFactory.createPlugin(sourceId, pluginDir.getAbsolutePath());
              } else {
                continue;
              }
              plugin.init();
              plugins.put(sourceId, plugin);

            } catch (Exception e) {
              logger.error("Failed to initialise plugin", e);
            }
          }
        }

        logger.info("Started the plugin manager");
      } catch (Exception e) {
        logger.error("Failed to start the plugin manager", e);
      }
    }

    public void addPluginForSource(PluginInterface plugin, Source source) {
        pluginForSource.put(source.getId(), plugin);
    }

    public Set<String> getPluginNames() {
    return pluginNames;
  }
}
