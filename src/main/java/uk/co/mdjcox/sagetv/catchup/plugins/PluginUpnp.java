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
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by michael on 26/02/15.
 */
public class PluginUpnp implements PluginInterface {

  private String sourceId;
  private String base;
  private UpnpUtils upnpUtils;

  private RemoteDevice device;
  private RemoteService[] services;
  private Map<String, UpnpItem> items;

  private Set<String> excludes = new HashSet<String>();
  private int channelPosition = 3;
  private int categoryPosition = 4;
  private Map<String, String> categoryMaps = new HashMap<String, String>();

  private Map<String, ArrayList<UpnpItem>> programmeItems = new HashMap<String,ArrayList<UpnpItem>>();
  private Map<String,ArrayList<ArrayList<Container>>> programmePaths = new HashMap<String,ArrayList<ArrayList<Container>>>();
  private Map<String, List<String>> specialSubcategories = new HashMap<String, List<String>>();
  private Map<String, String> channelImages = new HashMap<String, String>();

  @Inject
  CatchupContextInterface context;

  @Inject
  public PluginUpnp(@Assisted("id") String id, @Assisted("base") String base) {
    this.sourceId = id;
    this.base = base;
  }

  @Override
  public void init() {
    upnpUtils = UpnpUtils.instance();

    try {
      PropertiesFile propertiesFile = new PropertiesFile(base + File.separator + sourceId + ".properties", true);
      String template = propertiesFile.getString("template", "");
      String[] templateParts = template.split("/");
      ArrayList<String> templatePartList = new ArrayList<String>();
      for (String bit : templateParts) {
        templatePartList.add(bit);
      }
      if (template.isEmpty()) {
        throw new Exception("No template specified for plugin " + sourceId);
      }
      channelPosition = templatePartList.indexOf("CHANNEL");
      categoryPosition = templatePartList.indexOf("CATEGORY");
      Collection<String> excludeList = propertiesFile.getPropertySequence("excludes");
      if (excludeList != null) {
        excludes = new HashSet<String>(excludeList);
      }
      Collection<String> categoryMappings = propertiesFile.getPropertySequence("mapping");
      for (String map : categoryMappings) {
        String[] split = map.split("=");
        if (split.length > 1) {
          categoryMaps.put(split[0], split[1]);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialise plugin " + sourceId, e);
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
      devices = upnpUtils.findDevices("MediaServer", sourceId);
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
  public Source getSource() {
    Source source = new Source();
    String id = HtmlUtils.instance().makeIdSafe(sourceId);
    source.setSourceId(id);
    source.setId("Catchup/Sources/" + id);
    source.setShortName(device.getDetails().getModelDetails().getModelName());
    source.setLongName(device.getDetails().getModelDetails().getModelDescription());
    source.setServiceUrl("/category?id=" + id + ";type=html");
    URI iconUri = device.getIcons()[0].getUri();
    URL iconUrl = device.normalizeURI(iconUri);
    source.setIconUrl(iconUrl.toString());
    return source;
  }

  @Override
  public Collection<Programme> getProgrammes(Source source, AtomicBoolean stopFlag) {
    items = upnpUtils.findItems(UpnpUtils.ContentType.VIDEO, excludes, services);

    Map<String, Programme> programmes = new TreeMap<String, Programme>();
    for (UpnpItem item : items.values()) {
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
      URL iconUrl = device.normalizeURI(iconUri);

      String parentId = "";

      Programme prog = new Programme(sourceId, id, shortName, longName, serviceUrl, iconUrl.toString(), parentId);

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
        programmes.put(prog.getId(), prog);
      }

      if (programmes.size() == context.getMaxProgrammes(source.getSourceId())) {
        break;
      }
    }
    return programmes.values();
  }

  @Override
  public Collection<Episode> getEpisodes(Source source, Programme programme, AtomicBoolean stopFlag) {
    List<Episode> episodes = new ArrayList<Episode>();
    for (UpnpItem item : programmeItems.get(programme.getId())) {
      String episodeTitle = item.getItem().getTitle();
      String id = HtmlUtils.instance().makeIdSafe(episodeTitle);
      String programmeTitle = programme.getShortName();
      String seriesTitle = "";
      String seriesNo="";
      String episodeNo="";
      String description = item.getItem().getFirstPropertyValue(DIDLObject.Property.UPNP.LONG_DESCRIPTION.class);
      URI iconUri = item.getItem().getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
      URL iconUrl = device.normalizeURI(iconUri);
      final Res resource = item.getItem().getFirstResource();
      String serviceUrl = resource.getValue();
      String airDate = "";
      String airTime = "";
      String origAirDate = "";
      String origAirTime = "";
      ArrayList<Container> path = new ArrayList<Container>(programmePaths.get(programme.getId()).get(0));
      for (int i=0 ; i< channelPosition; i++) {
        path.remove(0);
      }
      String channel = path.get(0).getTitle();
      URI channelUri = path.get(0).getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
      if (channelUri != null) {
        String channelId = "Catchup/Channel/" + HtmlUtils.instance().makeIdSafe(channel);
        URL channelUrl = device.normalizeURI(channelUri);
        channelImages.put(channelId, channelUrl.toString());
      }
      Set<String> subcats = new HashSet<String>();
      for (ArrayList<Container> pathItems : programmePaths.get(programme.getId())) {
        ArrayList<Container> categoryPathitems = new ArrayList<Container>(pathItems);
        for (int i=0 ; i< categoryPosition; i++) {
          categoryPathitems.remove(0);
        }
        StringBuilder builder = new StringBuilder();
        for (Container pathItem : categoryPathitems) {
          if (builder.length()>0) {
            builder.append("/");
          }
          if (pathItem.getTitle().equals(programmeTitle)) {
            break;
          }
          builder.append(pathItem.getTitle());
        }
        subcats.add(builder.toString());
      };
      Set<String> genres = new HashSet<String>();
      Iterator<String> itr = subcats.iterator();
      while (itr.hasNext()) {
        String subcat = itr.next();

        for (Map.Entry<String,String> map : categoryMaps.entrySet()) {
          String from = map.getKey();
          String to = map.getValue();
          subcat = subcat.replaceFirst(from+"/", to + "/");
        }

        if (subcat.endsWith("/")) {
          subcat = subcat.substring(0, subcat.length()-1);
        }

        if (subcat.contains("Genre/")) {
          subcat = subcat.replaceAll(".*" + "Genre/", "");
          String[] genreNames = subcat.split("/");
          for (String genreName : genreNames) {
            genres.add(genreName);
          }
        } else {
          List<String> links = specialSubcategories.get(subcat);
          if (links == null) {
            links = new ArrayList<String>();
            specialSubcategories.put(subcat, links);
          }
          links.add(serviceUrl);
        }
      }


      Episode episode = new Episode(source.getSourceId(), id, programmeTitle, seriesTitle, episodeTitle, seriesNo, episodeNo, description,
              iconUrl.toString(), serviceUrl, airDate, airTime, origAirDate, origAirTime, channel, genres);

      episodes.add(episode);

    }
    return episodes;
  }

  @Override
  public void getEpisode(Source source, Programme programme, Episode episode, AtomicBoolean stopFlag) {
    // Do nothing
  }

  @Override
  public void getCategories(Source source, Map<String, List<String>> categories, AtomicBoolean stopFlag) {
    categories.putAll(specialSubcategories);
  }

  @Override
  public void playEpisode(Recording recording) {
    // Do nothing
  }

  @Override
  public void stopEpisode(Recording recording) {
    // Do nothing
  }

  public String getChannelImage(String channel) {
    return channelImages.get(channel);
  }

  @Override
  public String toString() {
    return sourceId;
  }
}
