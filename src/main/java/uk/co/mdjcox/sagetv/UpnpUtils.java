package uk.co.mdjcox.sagetv;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.VideoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Runs a simple UPnP discovery procedure.
 */
public class UpnpUtils {

  public enum ContentType {VIDEO, AUDIO, IMAGE};

  public static class UpnpItem {
    private Item item;
    private ArrayList<ArrayList<Container>> paths = new ArrayList<ArrayList<Container>>();

    public UpnpItem(Item item) {
      this.item = item;
    }

    public void addPath(ArrayList<Container> path) {
      paths.add(path);
    }

    public Item getItem() {
      return item;
    }

    public ArrayList<ArrayList<Container>> getPaths() {
      return paths;
    }

    public String getItemType() {
      return item.getClazz().getFriendlyName();
    }
  }

  // UPnP discovery is asynchronous, we need a callback
  private class BaseRegistryListener implements RegistryListener {
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) { }
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) { }
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) { }
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) { }
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) { }
    public void localDeviceAdded(Registry registry, LocalDevice device) { }
    public void localDeviceRemoved(Registry registry, LocalDevice device) { }
    public void beforeShutdown(Registry registry) { }
    public void afterShutdown() { }
  }

  private static UpnpUtils instance;

  public static UpnpUtils instance() {
    if (instance == null) {
      instance = new UpnpUtils();
    }
    return instance;
  }

  public UpnpUtils() {
  }



  public RemoteDevice[] findDevices(final String reqType, final String reqName) {
    final ArrayList<RemoteDevice> devices = new ArrayList<RemoteDevice>();
    UpnpService upnpService = null;
    try {
      BaseRegistryListener listener = new BaseRegistryListener() {
        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
          String deviceType = device.getType().getType();
          if (reqType == null || reqType.equals(deviceType)) {
            if (device.getDetails().getFriendlyName().contains(reqName)) {
              devices.add(device);
            }
          }
        }
      };

      upnpService = new UpnpServiceImpl(listener);
      upnpService.getControlPoint().search(new STAllHeader());
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      if (upnpService != null) {
        upnpService.shutdown();
      }
    }
    return devices.toArray(new RemoteDevice[devices.size()]);
  }

  public RemoteService[] findServices(String reqType, RemoteDevice... devices) {
    final ArrayList<RemoteService> services = new ArrayList<RemoteService>();
    for (RemoteDevice device : devices) {
      for (RemoteService service : device.getServices()) {
        final String serviceType = service.getServiceType().getType();
        if (reqType == null || reqType.equals(serviceType)) {
          services.add(service);
        }
      }
    }
    return services.toArray(new RemoteService[services.size()]);
  }


  public Map<String, UpnpItem> findItems(ContentType itemType, Set<String> excludes, RemoteService... services) {
    ConcurrentHashMap<String, UpnpItem> items = new ConcurrentHashMap<String, UpnpItem>();

    UpnpService upnpService = new UpnpServiceImpl(new BaseRegistryListener());

    final ArrayList<Container> containers = new ArrayList<Container>();
    for (RemoteService service : services) {
      Action browseAction = service.getAction("Browse");
      if (browseAction == null) {
        continue;
      }

      List<Container> serviceContainers = findContainers(new ArrayList<Container>(), upnpService, items, itemType, "0", service, excludes);

      containers.addAll(serviceContainers);
    }

    upnpService.shutdown();
    return items;
  }

  private List<Container> findContainers(final ArrayList<Container> path, UpnpService upnpService, final ConcurrentHashMap<String, UpnpItem> items, final ContentType itemType, final String parentId, final RemoteService contentService, final Set<String> excludes) {

    final List<Container> containers = new ArrayList<Container>();

    final Browse browse = new Browse(contentService, parentId, BrowseFlag.DIRECT_CHILDREN) {
      @Override
      public void received(ActionInvocation actionInvocation, DIDLContent didl) {
        for (Item item : didl.getItems()) {
          if ((itemType == ContentType.VIDEO && item instanceof VideoItem) ||
                  (itemType == ContentType.AUDIO && item instanceof AudioItem) ||
                  (itemType == ContentType.IMAGE && item instanceof ImageItem))
          {

            UpnpItem uitem = new UpnpItem(item);
            uitem.addPath(path);

            uitem = items.putIfAbsent(item.getTitle(), uitem);
            if (uitem != null) {
              uitem.addPath(path);
            } else {
              System.err.println("Items=" + items.size());
            }
          }
        }
          List<Container> theseContainers = didl.getContainers();
          containers: for (Container container : theseContainers) {
            if (excludes.contains(container.getTitle())) {
              continue;
            }
            containers.add(container);
        }
      }

      @Override
      public void updateStatus(Status status) { }

      @Override
      public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        System.err.println(defaultMsg);
      }
    };

    Future future = upnpService.getControlPoint().execute(browse);
    try {
      future.get();
    } catch (Exception e) {
      e.printStackTrace();
    }

  List<Container> containerContainers = new ArrayList<Container>();
  for (Container container : containers) {
    if (container.getTitle().equals("Preferences")) {
      continue;
    }
    ArrayList<Container> newPath = new ArrayList<Container>(path);
    newPath.add(container);
    containerContainers.addAll(findContainers(newPath, upnpService, items, itemType, container.getId(), contentService, excludes));
  }

  containers.addAll(containerContainers);

    return containers;
  }
}

