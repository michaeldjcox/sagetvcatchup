package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import uk.co.mdjcox.sagetv.UpnpUtils;
import uk.co.mdjcox.sagetv.UpnpUtils.UpnpItem;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.sagetv.utils.HtmlUtils;
import uk.co.mdjcox.sagetv.utils.PropertiesFile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by michael on 26/02/15.
 */
public class PluginUpnp implements PluginInterface {

  private String pluginId;
  private String base;
  private UpnpUtils upnpUtils;

  private RemoteDevice device;
  private RemoteService[] services;
  private Map<String, UpnpItem> items;

  private Set<String> excludes = new HashSet<String>();
  private Map<String, String> categoryMaps = new LinkedHashMap<String, String>();

  private Map<String, ArrayList<UpnpItem>> sourceItems = new HashMap<String,ArrayList<UpnpItem>>();
  private Map<String, ArrayList<UpnpItem>> programmeItems = new HashMap<String,ArrayList<UpnpItem>>();
  private Map<String,ArrayList<ArrayList<Container>>> programmePaths = new HashMap<String,ArrayList<ArrayList<Container>>>();
  private Map<String, Map<String, List<String>>> specialSubcategories = new HashMap<String, Map<String, List<String>>>();
  private Map<String, String> channelImages = new HashMap<String, String>();

  private PlayScript playScript;
  private StopScript stopScript;

  @Inject
  CatchupContextInterface context;

  @Inject
  private ScriptFactory scriptFactory;

  @Inject
  public PluginUpnp(@Assisted("id") String id, @Assisted("base") String base) {
    this.pluginId = id;
    this.base = base;
  }

  @Override
  public String getPluginId() {
    return pluginId;
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
          } else
          if (split.length == 1) {
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
      devices = upnpUtils.findDevices("MediaServer", pluginId);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    device = devices.length == 0 ? null : devices[0];

    try {
      if (device != null) {
        services = upnpUtils.findServices("ContentDirectory", device);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }


    return device != null && services.length>0;
  }

  @Override
  public Collection<Source> getSources() {
    Map<String, Source> sources = new HashMap<String, Source>();
    items = upnpUtils.findItems(UpnpUtils.ContentType.VIDEO, excludes, services);
    for (UpnpItem item : items.values()) {
      for (ArrayList<Container> path : item.getPaths()) {
        String sourcePath = toStringPath(path);
        sourcePath = applyCategoryMaps(sourcePath);
        String sourceName = sourcePath;
        if (sourceName.contains("/")) {
          sourceName = sourceName.substring(0, sourceName.indexOf("/"));
        }

        final String sourceIdShort = HtmlUtils.instance().makeIdSafe(sourceName);
        final String sourceId = "Catchup/Sources/" + sourceIdShort;

        Source source = sources.get(sourceId);
        if (source == null) {
          source = new Source();
          source.setSourceId(sourceIdShort);
          source.setId(sourceId);
          source.setShortName(sourceName);
          source.setLongName(sourceName);
          source.setServiceUrl("/category?sourceId=" + sourceId + ";type=html");
          URI iconUri = path.get(0).getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
          URL iconUrl = normaliseURI(iconUri);
          final String iconUrlString = iconUrl == null ? null : iconUrl.toString();
          source.setIconUrl(iconUrlString);
          sources.put(source.getId(), source);
        }

        ArrayList<UpnpItem> sourceItemList = sourceItems.get(source.getId());
        if (sourceItemList == null) {
          sourceItemList = new ArrayList<UpnpItem>();
          sourceItems.put(source.getId(), sourceItemList);
        }
        sourceItemList.add(item);

      }
    }


    return sources.values();
  }

  private URL normaliseURI(URI iconUri) {
    if (iconUri == null) {
      return null;
    }
    return device.normalizeURI(iconUri);
  }

  private String toStringPath(ArrayList<Container> path) {
    String stringPath = "";
    for (Container container : path) {
      if (!stringPath.isEmpty()) {
        stringPath += "/";
      }
      stringPath += container.getTitle().trim();
    }
    if (stringPath.endsWith("/")) {
      stringPath = stringPath.substring(0, stringPath.length()-1);
    }
    return stringPath.trim();
  }

  @Override
  public Collection<Programme> getProgrammes(Source source, AtomicBoolean stopFlag) {

    //TODO outside the world of TV there are no "programmes" -  how do I detect and address this?
    Map<String, Programme> programmes = new TreeMap<String, Programme>();
    ArrayList<UpnpItem> items = sourceItems.get(source.getId());
    for (UpnpItem item : items) {
      ArrayList<ArrayList<Container>> paths = item.getPaths();
      ArrayList<Container> firstPath = paths.get(0);
      Container parentContainer = firstPath.get(firstPath.size()-1);
      String title = parentContainer.getTitle();
      String sourceId = source.getId();
      String id = HtmlUtils.instance().makeIdSafe(title);
      String shortName = title;
      String longName = title;
      String serviceUrl = "/programme?id="+ id +";type=html";
      URI iconUri = parentContainer.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
      URL iconUrl = normaliseURI(iconUri);
      final String iconUrlString = iconUrl == null ? null : iconUrl.toString();

      String parentId = "";

      Programme prog = new Programme(sourceId, id, shortName, longName, serviceUrl, iconUrlString, parentId);

      ArrayList<UpnpItem> progItems = programmeItems.get(id);
      if (progItems == null) {
        progItems = new ArrayList<UpnpItem>();
        programmeItems.put(id, progItems);
      }
      progItems.add(item);

      ArrayList<ArrayList<Container>> progPaths = programmePaths.get(id);
      if (progPaths == null) {
        progPaths = new ArrayList<ArrayList<Container>>();
        programmePaths.put(id, progPaths);
      }
      progPaths.addAll(item.getPaths());

      if (!programmes.containsKey(id)) {
        programmes.put(id, prog);
      }

      if (programmes.size() == context.getMaxProgrammes(source.getSourceId())) {
        break;
      }
    }
    return programmes.values();
  }

  @Override
  public Collection<Episode> getEpisodes(Source source, Programme programme, AtomicBoolean stopFlag) {

    Map<String, List<String>> specialCats = specialSubcategories.get(source.getId());
    if (specialCats == null) {
      specialCats = new HashMap<String, List<String>>();
      specialSubcategories.put(source.getId(), specialCats);
    }

    Map<String, Episode> episodes = new HashMap<String, Episode>();

    for (UpnpItem item : programmeItems.get(programme.getId())) {
      String episodeTitle = item.getItem().getTitle();
      String id = HtmlUtils.instance().makeIdSafe(episodeTitle);
      String programmeTitle = programme.getShortName();
      String seriesTitle = "";
      String seriesNo="";
      String episodeNo="";
      String description = item.getItem().getFirstPropertyValue(DIDLObject.Property.UPNP.LONG_DESCRIPTION.class);
      if (description == null){
        description = episodeTitle;
      }
      URI iconUri = item.getItem().getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
      URL iconUrl = normaliseURI(iconUri);
      final Res resource = item.getItem().getFirstResource();
      String serviceUrl = resource.getValue();
        // This does not work because on windows playon is on a virtual host
//      try {
//        URL url = new URL(serviceUrl);
//        serviceUrl = "http://localhost:" + url.getPort() + url.getFile();
//      } catch (Exception e) {
//        e.printStackTrace();
//      }

      String airDate = "";
      String airTime = "";
      String origAirDate = "";
      String origAirTime = "";
      String channel = "";
      Set<String> subcats = new HashSet<String>();
      for (ArrayList<Container> pathItems : programmePaths.get(programme.getId())) {
        String pathString = toStringPath(pathItems);
        pathString = applyCategoryMaps(pathString);
        String[] pathBits = pathString.split("/");
        String subCatPath = "";
        for (int i=1; i<pathBits.length; i++) {
          if (!subCatPath.isEmpty()) {
            subCatPath +="/"
;          }
          if (pathBits[i].equals(programmeTitle)) {
            break;
          }
          subCatPath += pathBits[i];
        }

        if (subCatPath.endsWith("/")) {
          subCatPath = subCatPath.substring(0, subCatPath.length()-1);
        }
        subcats.add(subCatPath);
      };
      Set<String> genres = new HashSet<String>();
      Iterator<String> itr = subcats.iterator();
      while (itr.hasNext()) {
        String subcat = itr.next();

        subcat = applyCategoryMaps(subcat);

        if (subcat.isEmpty()) {
          continue;
        }

        if (subcat.contains("Channel/")) {
          subcat = subcat.replaceAll(".*" + "Channel/", "");
          if (subcat.contains("/")) {
            channel = subcat.substring(0, subcat.indexOf("/"));
          } else {
            channel = subcat;
          }
          // TODO get the image for the channel from the Container
//          String channelId = "Catchup/Channel/" + HtmlUtils.instance().makeIdSafe(channel);
          //      URI channelUri = path.get(0).getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
//      if (channelUri != null) {
//        URL channelUrl = device.normalizeURI(channelUri);
//        channelImages.put(channelId, channelUrl.toString());
        } else
        if (subcat.contains("Genre/")) {
          subcat = subcat.replaceAll(".*" + "Genre/", "");
          String[] genreNames = subcat.split("/");
          for (String genreName : genreNames) {
            genres.add(genreName);
          }
        } else {
          List<String> links = specialCats.get(subcat);
          if (links == null) {
            links = new ArrayList<String>();
            specialCats.put(subcat, links);
          }
          if (!links.contains(serviceUrl)) {
            links.add(serviceUrl);
          }
        }
      }


      final String iconUrlString = iconUrl == null ? null : iconUrl.toString();
      Episode episode = new Episode(pluginId, id, programmeTitle, seriesTitle, episodeTitle, seriesNo, episodeNo, description,
              iconUrlString, serviceUrl, airDate, airTime, origAirDate, origAirTime, channel, genres);

      episodes.put(episode.getId(), episode);

    }
    return episodes.values();
  }

  private String applyCategoryMaps(String subcat) {
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
    return subcat.trim();
  }

  @Override
  public void getEpisode(Source source, Programme programme, Episode episode, AtomicBoolean stopFlag) {
    // Do nothing
  }

  @Override
  public void getCategories(Source source, Map<String, List<String>> categories, AtomicBoolean stopFlag) {
    Map<String, List<String>> specialCats = specialSubcategories.get(source.getId());
    categories.putAll(specialCats);
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
  public String toString() {
    return pluginId;
  }
}
