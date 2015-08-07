package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import uk.co.mdjcox.sagetv.UpnpUtils;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.catchup.ProgressTracker;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.sagetv.utils.PropertiesFile;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by michael on 20/03/15.
 */
public class PluginUpnpLite implements PluginInterface {

    private String pluginId;
    private String base;
    private UpnpUtils upnpUtils;

    private Set<String> excludes = new HashSet<String>();
    private Map<String, String> categoryMaps = new LinkedHashMap<String, String>();

    private RemoteDevice device;
    private RemoteService[] services;

    private Map<String, ArrayList<UpnpUtils.UpnpItem>> sourceItems = new HashMap<String, ArrayList<UpnpUtils.UpnpItem>>();
    private Map<String, ArrayList<UpnpUtils.UpnpItem>> programmeItems = new HashMap<String, ArrayList<UpnpUtils.UpnpItem>>();
    private Map<String, ArrayList<ArrayList<Container>>> programmePaths = new HashMap<String, ArrayList<ArrayList<Container>>>();
    private Map<String, Map<String, List<String>>> specialSubcategories = new HashMap<String, Map<String, List<String>>>();
    private Map<String, String> channelImages = new HashMap<String, String>();

    private PlayScript playScript;
    private StopScript stopScript;

    private ProgressTracker progressTracker;

    @Inject
    CatchupContextInterface context;

    @Inject
    private ScriptFactory scriptFactory;
    private Map<String, Container> containers;

    @Inject
    public PluginUpnpLite(@Assisted("id") String id, @Assisted("base") String base) {
        this.pluginId = id;
        this.base = base;
    }

    @Override
    public void init() {
        try {
            playScript = scriptFactory.createPlayScript(base);
            stopScript = scriptFactory.createStopScript(base);

            PropertiesFile propertiesFile = new PropertiesFile(base + File.separator + pluginId + ".properties", true);
            Collection<String> excludeList = propertiesFile.getPropertySequence("excludes");
            if (excludeList != null) {
                excludes = new HashSet<String>(excludeList);
            }
            Collection<String> categoryMappings = propertiesFile.getPropertySequence("mapping");
            if (categoryMappings != null) {
                for (String map : categoryMappings) {
                    String[] split = map.split("=");
                    if (split.length > 1) {
                        categoryMaps.put(split[0], split[1]);
                    } else if (split.length == 1) {
                        categoryMaps.put(split[0], "");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise plugin " + pluginId, e);
        }
    }

    @Override
    public boolean beginCatalog() {
        services = new RemoteService[0];
        device = null;
        programmeItems.clear();
        programmePaths.clear();
        specialSubcategories.clear();

        RemoteDevice[] devices = new RemoteDevice[0];
        try {
            upnpUtils = UpnpUtils.instance();
            devices = upnpUtils.findDevices(progressTracker, "MediaServer", pluginId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        device = devices.length == 0 ? null : devices[0];

        try {
            if (device != null) {
                services = upnpUtils.findServices(progressTracker, "ContentDirectory", device);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        containers = upnpUtils.findContainers(progressTracker, UpnpUtils.ContentType.VIDEO, categoryMaps, excludes, services);

        return device != null && services.length>0;
    }

    @Override
    public void setProgressTracker(ProgressTracker progressTracker) {
        this.progressTracker = progressTracker;
    }



    @Override
    public Collection<Source> getSources() {

//                findContainers(
//                progressTracker,
//                new ArrayList<Container>(),
//                upnpService,
//                UpnpUtils.ContentType.VIDEO,
//                "0",
//                service,
//                excludes);

        Map<String, Source> sources = new HashMap<String, Source>();

        out: for (Map.Entry<String, Container> path : containers.entrySet()) {
            String sourceId = path.getKey();
            Container container = path.getValue();

            for (String map : categoryMaps.keySet()) {
                if (map.endsWith("/")) {
                    map = map.substring(0, map.lastIndexOf("/"));
                }
                if (map.endsWith(sourceId)) {
                    continue out;
                }
            }

            System.err.println("Doing source " + sourceId);

            sourceId = applyCategoryMaps(sourceId);

            if (sourceId.isEmpty()) {
                continue;
            }

            if (sourceId.indexOf("/") == -1 && !sourceId.isEmpty()) {
                if (sources.get(sourceId) == null) {
                    String sourceIdShort = sourceId;
                    sourceId = "Catchup/Sources/" + sourceIdShort;
                    String sourceName = container.getTitle();
                    Source source = new Source();
                    source.setSourceId(sourceIdShort);
                    source.setId(sourceId);
                    source.setShortName(sourceName);
                    source.setLongName(sourceName);
                    source.setServiceUrl("/category?sourceId=" + sourceId + ";type=html");
                    URI iconUri = container.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
                    URL iconUrl = normaliseURI(iconUri);
                    final String iconUrlString = iconUrl == null ? null : iconUrl.toString();
                    source.setIconUrl(iconUrlString);
                    sources.put(sourceId, source);
                }
            }




        }


        return sources.values();
    }

    private String applyCategoryMaps(String subcat) {
        System.err.println("Before " + subcat);
        for (Map.Entry<String,String> map : categoryMaps.entrySet()) {
            String from = map.getKey();
            String to = map.getValue();
            subcat = subcat.replaceFirst(from, to);
            if (subcat.startsWith("/")) {
                subcat = subcat.substring(1);
            }
        }

        if (subcat.endsWith("/")) {
            subcat = subcat.substring(0, subcat.length()-1);
        }

        System.err.println("After " + subcat.trim());
        return subcat.trim();
    }

    private URL normaliseURI(URI iconUri) {
        if (iconUri == null) {
            return null;
        }
        return device.normalizeURI(iconUri);
    }

    @Override
    public Collection<Programme> getProgrammes(Source source, AtomicBoolean stopFlag) {
        return new ArrayList<Programme>();
    }

    @Override
    public Collection<Episode> getEpisodes(Source source, Programme programme, AtomicBoolean stopFlag) {
        return new ArrayList<Episode>();
    }

    @Override
    public void getEpisode(Source source, Programme programme, Episode episode, AtomicBoolean stopFlag) {
        throw new UnsupportedOperationException("PluginUpnpLite.getCategories() not supported");
    }

    @Override
    public void getCategories(Source source, Map<String, List<String>> categories, AtomicBoolean stopFlag) {
        throw new UnsupportedOperationException("PluginUpnpLite.getCategories() not supported");
    }

    @Override
    public void playEpisode(Recording recording) {
        playScript.play(recording);
    }

    @Override
    public void stopEpisode(Recording recording) {
        stopScript.stop(recording);
    }

    public String getIconUrl(String channel) {
        return channelImages.get(channel);
    }

    @Override
    public String getPluginId() {
        return pluginId;
    }

    @Override
    public String toString() {
        return pluginId;
    }

}
