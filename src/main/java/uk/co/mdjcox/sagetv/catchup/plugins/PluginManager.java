package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;

import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.LinkedHashMap;

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
    private PropertiesInterface props;
    private LinkedHashMap<String, Plugin> plugins = new  LinkedHashMap<String, Plugin>();
    @Inject
    private PluginFactory pluginFactory;

    @Inject
    private PluginManager(Logger logger, PropertiesInterface props) {
        this.logger = logger;
        this.props = props;
    }

    public Collection<Plugin> getPlugins() {
        return plugins.values();
    }

    public Plugin getPlugin(String sourceId) {
        return plugins.get(sourceId);
    }

    public void load() {

        String base = props.getProperty("pluginDir", "/opt/sagetv/server/sagetvcatchup/plugins");

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
          if (props.getBoolean(sourceId + ".skip")) {
            logger.info("Skipping plugin " + sourceId);
            continue;
          }
            Plugin plugin = pluginFactory.createPlugin(sourceId, pluginDir.getAbsolutePath());
            plugin.init();
            plugins.put(sourceId, plugin);
        }
    }
}
