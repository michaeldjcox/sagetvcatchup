package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.LoggerInterface;
import uk.co.mdjcox.utils.NumberedThreadFactory;
import uk.co.mdjcox.utils.RmiHelper;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:34
 * To change this template use
 * File | Settings | File Templates.
 */
@Singleton
public class Cataloger {

  private static final String STOPPED_ON_REQUEST = "Stopped on request";
  private LoggerInterface logger;
  private PluginManager pluginManager;
  private String progressString = "Waiting";
  private AtomicBoolean stop = new AtomicBoolean(false);
  private AtomicBoolean catalogRunning = new AtomicBoolean(false);
  private ScheduledExecutorService catalogingScheduler;
  private ExecutorService programmeThreadPool;
  private ExecutorService episodeThreadPool;

  private ScheduledFuture<?> future;
  private List<CatalogPublisher> publishers;
  private String errorSummary = "";
  private int refreshRate;
  private CatchupContextInterface context;
  private int sourceStats = 0;
  private int programmeStats = 0;
  private int episodeStats = 0;
  private Set<String> favourites = new HashSet<String>();
  private Catalog lastCatalog = new Catalog();

  @Inject
  private Cataloger(LoggerInterface logger, CatchupContextInterface context, PluginManager pluginManager) {
    this.logger = logger;
    this.pluginManager = pluginManager;
    this.refreshRate = context.getRefreshRate();
    this.context = context;
  }

  private Runnable getCatalogRunnable(final List<CatalogPublisher> publishers) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          Catalog catalog = null;
          try {
            catalogRunning.set(true);
            logger.info("Refreshing catalog");
            catalog = catalog();
            if (catalog != null) {
              setProgress("Publishing catalog");
              publish(catalog, publishers);
              lastCatalog = catalog;
              setProgress("Finished");
            }
          } catch (Throwable e) {
            if (catalog != null) {
              catalog.addError("FATAL", "Failed to publish to SageTV " + e.getMessage());
            }
            logger.error("Failed to refresh catalog", e);
            setProgress("Failed");
          } finally {
            if (catalog != null) {
              errorSummary = buildErrorSummary(catalog);
            }
            catalogRunning.set(false);
          }
        } catch (Throwable e) {
          logger.error("Caught Exception during cataloging", e);
        }
      }
    };
  }

  private void buildStatsSummary(Catalog catalog) {
    sourceStats = 0;
    programmeStats = 0;
    episodeStats = 0;

    for (Category category : catalog.getCategories()) {
      if (category.isProgrammeCategory() && category.getParentId().isEmpty()) {
        programmeStats++;
      }

      if (category.isSource() && !category.getSourceId().equals("Status") && !category.getSourceId().equals("Search")) {
        sourceStats++;
      }
    }

    episodeStats = catalog.getEpisodes().size();

  }

  private void publish(Catalog catalog, List<CatalogPublisher> publishers) {
    for (CatalogPublisher publisher : publishers) {
      publisher.publish(catalog);
    }
  }

  private String buildErrorSummary(Catalog catalog) {
    Collection<ParseError> errorList = catalog.getErrors();
    HashMap<String, Integer> errorSum = new HashMap<String, Integer>();
    for (ParseError error : errorList) {
      Integer count = errorSum.get(error.getLevel());
      if (count == null) {
        errorSum.put(error.getLevel(), 1);
      } else {
        errorSum.put(error.getLevel(), count + 1);
      }
    }

    String errorSummary = "( ";
    for (Map.Entry<String, Integer> entry : errorSum.entrySet()) {
      errorSummary += entry.getKey() + " " + entry.getValue() + " ";
    }
    errorSummary += ")";

    if (errorSummary.equals("()")) {
      errorSummary = "";
    }
    return errorSummary;
  }

  private Catalog catalog() {

    long startTime = System.currentTimeMillis();

    progressString = "Started";

    Catalog catalog = new Catalog();

    final AtomicInteger sourceStats = new AtomicInteger(0);
    final AtomicInteger programmeStats = new AtomicInteger(0);
    final AtomicInteger episodeStats = new AtomicInteger(0);

    try {
      favourites = getCatchupPluginRemote().getFavouriteTitles();
    } catch (Exception e) {
      logger.error("Failed to collect favourites", e);
    }

    try {
      Map<String, Category> newCategories = new ConcurrentHashMap<String, Category>();
      final Map<String, Episode> newEpisodes = new ConcurrentHashMap<String, Episode>();

      Root root = new Root("Catchup", "Catchup TV", "Catchup TV", "/", "/logo.png");
      newCategories.put(root.getId(), root);

      Source statusSource = new Source(root.getId(), "Status", "Status", "Status", "", "");
      statusSource.setPodcastUrl("/category?id=status;type=xml");
      newCategories.put(statusSource.getId(), statusSource);
      root.addSubCategory(statusSource);

      Source searchSource = new Source(root.getId(), "Search", "Search", "Search", "", "");
      searchSource.setPodcastUrl("/search?type=xml");
      newCategories.put(searchSource.getId(), searchSource);
      root.addSubCategory(searchSource);

      for (final Plugin plugin : pluginManager.getPlugins()) {

        if (context.skipPlugin(plugin.getSource().getId())) {
          continue;
        }

        checkForStop();

        final Source sourceCat = plugin.getSource();
        final String pluginName = sourceCat.getId();

        progressString = "Doing " + pluginName;

        ArrayList<String> testProgrammes = context.getTestProgrammes(pluginName);
        int testMaxProgrammes = context.getMaxProgrammes(pluginName);

        newCategories.put(sourceCat.getId(), sourceCat);

        sourceStats.incrementAndGet();

        logger.info("Found source: " + sourceCat);

        final Map<String, Programme> newProgCategories = new ConcurrentHashMap<String, Programme>();

        root.addSubCategory(sourceCat);
        sourceCat.setParentId(root.getId());

        sourceCat.setPodcastUrl("/category?id=" + sourceCat.getId() + ";type=xml");


        logger.info("Getting programmes found on: " + sourceCat);
        int programmeCount = 0;
        Collection<Programme> programmes = plugin.getProgrammes();

        final int programmesToDo = Math.min(testMaxProgrammes, programmes.size());
        final CountDownLatch programmesLatch = new CountDownLatch(programmesToDo);

        for (final Programme programme : programmes) {
          checkForStop();

          programmeCount++;

          programmeStats.incrementAndGet();

          if (programmeCount > testMaxProgrammes) {
            break;
          }

          final String programmeId = programme.getId();
          if (testProgrammes != null && !testProgrammes.isEmpty()) {
            if (!testProgrammes.contains(programmeId)) {
              logger.info("Skipping programme " + programmeId);
              programmesLatch.countDown();
              progressString = "Done " + pluginName + " programme " + (programmesToDo-programmesLatch.getCount()) + "/" + programmesToDo;
              logger.info(progressString);
              continue;
            }
          }

          Runnable runnable = new Runnable() {
            public void run() {
              try {
                logger.info("Doing programme " + programmeId);

                programme.setPodcastUrl("/programme?id=" + programmeId + ";type=xml");

                Collection<Episode> episodes = plugin.getEpisodes(sourceCat, programme);

                final CountDownLatch episodesLatch = new CountDownLatch(episodes.size());

                for (final Episode episode : episodes) {
                  checkForStop();

                  Runnable runnable1 = new Runnable() {
                    public void run() {
                      try {
                        plugin.getEpisode(sourceCat, programme, episode);

                        episode.setPodcastUrl("/control?id=" + episode.getId() + ";type=xml");

                        newEpisodes.put(episode.getId(), episode);

                        episodeStats.incrementAndGet();

                        synchronized (programme) {
                          programme.addEpisode(episode);
                        }
                      } catch (Exception e) {
                        if (!e.getMessage().equals(STOPPED_ON_REQUEST)) {
                          episode.addError("ERROR", "Failed to catalog episode " + e.getMessage());
                        }
                      } finally {
                        episodesLatch.countDown();
                      }
                    }
                  };

                  episodeThreadPool.submit(runnable1);
                }

                boolean done = false;
                while (!done) {
                  try {
                    checkForStop();
                    logger.info("Programme " + programmeId + " Waiting for " + episodesLatch.getCount() + " episode cataloging threads to finish");
                    done = episodesLatch.await(1, TimeUnit.MINUTES);
                  } catch (InterruptedException e) {
                    // Ignore
                  }
                }

                if (programme.getEpisodes().size() > 0) {
                  logger.info("Programme " + programmeId + " has episodes");
                  if (newProgCategories.containsKey(programmeId)) {
                    logger.warn("Programme " + programmeId + " has a duplicate - merging programmes");
                    Programme existingProg = newProgCategories.get(programmeId);
                    existingProg.addAllEpisodes(programme.getEpisodes());
                  } else {
                    newProgCategories.put(programmeId, programme);
                  }
                } else {
                  logger.warn("Programme " + programmeId + " has no episodes");
                }
              } catch (Exception e) {
                if (!e.getMessage().equals(STOPPED_ON_REQUEST)) {
                  programme.addError("ERROR", "Failed to catalog programme " + e.getMessage());
                }
              } finally {
                programmesLatch.countDown();
                progressString = "Done " + pluginName + " programme " + (programmesToDo-programmesLatch.getCount()) + "/" + programmesToDo;
                logger.info(progressString);
              }

            }
          };

          programmeThreadPool.submit(runnable);
        }

        boolean done = false;
        while (!done) {
          try {
            checkForStop();
            logger.info("Source " + sourceCat.getId() + " Waiting for " + programmesLatch.getCount() + " programme cataloging threads to finish");
            done = programmesLatch.await(1, TimeUnit.MINUTES);
          } catch (InterruptedException e) {
            // Ignore
          }
        }

        checkForStop();

        sourceCat.clearSubCategories();

        logger.info("Found " + newProgCategories.size() + " Programmes");
        logger.info("Found " + newEpisodes.size() + " Episodes");

        Map<String, SubCategory> newSubCategories = new ConcurrentHashMap<String, SubCategory>();


        progressString = "Doing " + pluginName + " additional categorisation";

        for (Programme programmeCat : newProgCategories.values()) {

          checkForStop();
          logger.info("Categorising " + programmeCat);
          doAtoZcategorisation(sourceCat, programmeCat, newSubCategories);

          // Favourite
          doFavouriteCategorisation(sourceCat, programmeCat, newSubCategories);

          for (String episodeId : programmeCat.getEpisodes()) {
            Episode episode = newEpisodes.get(episodeId);
            if ((episode == null) || episodeId.isEmpty()) {
              continue;
            }
            // Genre
            doGenreCategorisation(sourceCat, programmeCat, episode, newSubCategories);

            // Channel
            doChannelCategorisation(sourceCat, programmeCat, episode, newSubCategories);

            // Air Date
            doAirDateCategorisation(sourceCat, programmeCat, episode, newSubCategories);

            // New
            doNewProgrammeCategorisation(sourceCat, programmeCat, episode, newSubCategories);
          }
        }

        newCategories.putAll(newProgCategories);
        newCategories.putAll(newSubCategories);

      }

      catalog.setCategories(root, newCategories, newEpisodes);

      int duration = (int)(System.currentTimeMillis() - startTime);

      final int millisInHour = 60 * 60 * 1000;
      final int millisInMin = 60 * 1000;
      int hours = (int)(duration / millisInHour) ;
      int minutes = (int)((duration % millisInHour) / millisInMin);
      int seconds = (int)((duration % millisInMin) / 1000);

      progressString = "Finished cataloging in " + hours + "hrs " + minutes + " mins " + seconds + " secs";
      logger.info(progressString);

      this.sourceStats = sourceStats.intValue();
      this.programmeStats = programmeStats.intValue();
      this.episodeStats = episodeStats.intValue();

      return catalog;

    } catch (Throwable e) {
      logger.error("Failed to refresh properties file", e);
      if (!e.getMessage().equals(STOPPED_ON_REQUEST)) {
        progressString = "Failed to catalog";
      }
      return null;
    }
  }

  private void checkForStop() {
    if (stop.getAndSet(false)) {
      progressString = "Stopped";
      throw new RuntimeException(STOPPED_ON_REQUEST);
    }
  }

  private void doFavouriteCategorisation(Source sourceCat, Programme programmeCat,
                                       Map<String, SubCategory> newSubCategories) {

    String sourceId = sourceCat.getId();
    String favouriteId = sourceId + "/Favourite";
    SubCategory favouriteCat = newSubCategories.get(favouriteId);
    if (favouriteCat == null) {
      favouriteCat =
              new SubCategory(sourceId, favouriteId, "Favourites", "Favourites", sourceCat.getServiceUrl(),
                      sourceCat.getIconUrl(), sourceId);
      favouriteCat.setPodcastUrl("/category?id=" + favouriteId + ";type=xml");
      newSubCategories.put(favouriteId, favouriteCat);
      sourceCat.addSubCategory(favouriteCat);
    }

    String shortName = programmeCat.getShortName();
    if (favourites.contains(shortName.toUpperCase())) {
      favouriteCat.addSubCategory(programmeCat);
    }
  }

  private void doNewProgrammeCategorisation(Source sourceCat, Programme programmeCat, Episode episode,
                                         Map<String, SubCategory> newSubCategories) {
    String sourceId = sourceCat.getId();
    String favouriteId = sourceId + "/New";
    SubCategory favouriteCat = newSubCategories.get(favouriteId);
    if (favouriteCat == null) {
      favouriteCat =
              new SubCategory(sourceId, favouriteId, "New", "New", sourceCat.getServiceUrl(),
                      sourceCat.getIconUrl(), sourceId);
      favouriteCat.setPodcastUrl("/category?id=" + favouriteId + ";type=xml");
      newSubCategories.put(favouriteId, favouriteCat);
      sourceCat.addSubCategory(favouriteCat);
    }

    if (lastCatalog != null) {
      String id = programmeCat.getId();
      String epId = episode.getId();
      Category cat = lastCatalog.getCategory(id);
      Episode ep = lastCatalog.getEpisode(epId);
      if (cat == null) {
        Programme newProgrammeCat = addNewProgrammeCat(programmeCat, newSubCategories, sourceId, favouriteId, favouriteCat, id);
        newProgrammeCat.addAllEpisodes(programmeCat.getEpisodes());
      } else
      if (ep == null)
      {
        Programme newProgrammeCat = addNewProgrammeCat(programmeCat, newSubCategories, sourceId, favouriteId, favouriteCat, id);
        newProgrammeCat.addEpisode(episode);
      }
    }
  }

  private Programme addNewProgrammeCat(Programme programmeCat, Map<String, SubCategory> newSubCategories, String sourceId, String favouriteId, SubCategory favouriteCat, String id) {
    String favouriteProgId = sourceId + "/New/" + id;
    Programme newProgCat = (Programme)newSubCategories.get(favouriteProgId);
    if (newProgCat == null) {
      newProgCat = new Programme(sourceId, favouriteProgId,
              programmeCat.getShortName(),
              programmeCat.getLongName(),
              programmeCat.getServiceUrl(),
              programmeCat.getIconUrl(),
              favouriteId);
      newSubCategories.put(favouriteProgId, newProgCat);
      favouriteCat.addSubCategory(newProgCat);
    }
    favouriteCat.addSubCategory(programmeCat);
    return programmeCat;
  }

  private void doAirDateCategorisation(Source sourceCat, Programme programmeCat, Episode episode,
                                       Map<String, SubCategory> newSubCategories) {
    String airDateName = episode.getAirDate();
    if (airDateName == null || airDateName.isEmpty()) {
      return;
    }

    String sourceId = sourceCat.getId();
    String airdateId = sourceId + "/AirDate";
    SubCategory airdateCat = newSubCategories.get(airdateId);
    if (airdateCat == null) {
      airdateCat =
              new SubCategory(sourceId, airdateId, "Air Date", "Air Date", sourceCat.getServiceUrl(),
                      sourceCat.getIconUrl(), sourceId);
      airdateCat.setPodcastUrl("/category?id=" + airdateId + ";type=xml");
      newSubCategories.put(airdateId, airdateCat);
      sourceCat.addSubCategory(airdateCat);
    }
    String
            airDateInstanceId =
            sourceId + "/AirDate/" + airDateName.replace(" ", "").replace(",", "");
    Programme airDateInstanceCat = (Programme) newSubCategories.get(airDateInstanceId);
    if (airDateInstanceCat == null) {
      airDateInstanceCat =
              new Programme(sourceId, airDateInstanceId, airDateName, airDateName, sourceCat.getServiceUrl(),
                      sourceCat.getIconUrl(), airdateCat.getId());
      airDateInstanceCat.setPodcastUrl("/category?id=" + airDateInstanceId + ";type=xml");
      newSubCategories.put(airDateInstanceId, airDateInstanceCat);
      airdateCat.addSubCategory(airDateInstanceCat);
    }

    airDateInstanceCat.addEpisode(episode);
  }

  private void doChannelCategorisation(Source sourceCat, Programme programmeCat, Episode prog,
                                       Map<String, SubCategory> newSubCategories) {
    String channelName = prog.getChannel();
    if (channelName != null && !channelName.isEmpty()) {

      String sourceId = sourceCat.getId();
      String channelId = sourceId + "/Channel";
      SubCategory channelCat = newSubCategories.get(channelId);
      if (channelCat == null) {
        channelCat =
                new SubCategory(sourceId, channelId, "Channel", "Channel", sourceCat.getServiceUrl(),
                        sourceCat.getIconUrl(), sourceId);
        channelCat.setPodcastUrl("/category?id=" + channelCat.getId() + ";type=xml");
        newSubCategories.put(channelId, channelCat);
        sourceCat.addSubCategory(channelCat);
      }
      String channelInstanceId = sourceId + "/Channel/" + channelName.replace(" ", "");
      SubCategory channelInstanceCat = newSubCategories.get(channelInstanceId);
      if (channelInstanceCat == null) {
        channelInstanceCat =
                new SubCategory(sourceId, channelInstanceId, channelName, channelName, sourceCat.getServiceUrl(),
                        sourceCat.getIconUrl(), channelCat.getId());
        channelInstanceCat.setPodcastUrl("/category?id=" + channelInstanceCat.getId() + ";type=xml");
        newSubCategories.put(channelInstanceId, channelInstanceCat);
        channelCat.addSubCategory(channelInstanceCat);
      }

      programmeCat.addOtherParentId(channelInstanceId);
      channelInstanceCat.addSubCategory(programmeCat);
    }
  }

  private void doGenreCategorisation(Source sourceCat, Programme programmeCat, Episode prog,
                                     Map<String, SubCategory> newSubCategories) {
    Set<String> genres = prog.getGenres();
    if (genres != null && !genres.isEmpty()) {
      for (String genreName : genres) {
        String sourceId = sourceCat.getId();
        String genreId = sourceId + "/Genre";
        SubCategory genreCat = newSubCategories.get(genreId);
        if (genreCat == null) {
          genreCat =
                  new SubCategory(sourceId, genreId, "Genre", "Genre", sourceCat.getServiceUrl(),
                          sourceCat.getIconUrl(), sourceId);
          genreCat.setPodcastUrl("/category?id=" + genreCat.getId() + ";type=xml");

          newSubCategories.put(genreId, genreCat);
          sourceCat.addSubCategory(genreCat);
        }
        String genreInstanceId = sourceId + "/Genre/" + genreName.replace(" ", "");
        SubCategory genreInstanceCat = newSubCategories.get(genreInstanceId);
        if (genreInstanceCat == null) {
          genreInstanceCat =
                  new SubCategory(sourceId, genreInstanceId, genreName, genreName, sourceCat.getServiceUrl(),
                          sourceCat.getIconUrl(), genreCat.getId());
          newSubCategories.put(genreInstanceId, genreInstanceCat);
          genreInstanceCat.setPodcastUrl("/category?id=" + genreInstanceCat.getId() + ";type=xml");
          genreCat.addSubCategory(genreInstanceCat);
        }
        programmeCat.addOtherParentId(genreInstanceId);
        genreInstanceCat.addSubCategory(programmeCat);
      }
    }
  }

  private void doAtoZcategorisation(Source sourceCat, Programme programmeCat,
                                    Map<String, SubCategory> newSubCategories) {
    // A to Z
    String azName = programmeCat.getShortName();
    if (azName.startsWith("The ") || azName.startsWith("the ")) {
      azName = azName.substring(4);
    }

    int i = 0;
    while (!azName.isEmpty() && !Character.isLetterOrDigit(azName.charAt(i++))) {
      azName = azName.substring(1);
    }

    azName = azName.substring(0, 1).toUpperCase();

    String sourceId = sourceCat.getId();
    String atozId = sourceId + "/AtoZ";
    SubCategory atozCat = newSubCategories.get(atozId);
    if (atozCat == null) {
      atozCat =
              new SubCategory(sourceId, atozId, "A to Z", "A to Z", sourceCat.getServiceUrl(),
                      sourceCat.getIconUrl(), sourceId);
      atozCat.setPodcastUrl("/category?id=" + atozCat.getId() + ";type=xml");
      newSubCategories.put(atozId, atozCat);
      sourceCat.addSubCategory(atozCat);
    }
    String azId = sourceId + "/AtoZ/" + azName;
    SubCategory azCat = newSubCategories.get(azId);
    if (azCat == null) {
      azCat =
              new SubCategory(sourceId, azId, azName, azName, sourceCat.getServiceUrl(), sourceCat.getIconUrl(),
                      atozCat.getId());
      azCat.setPodcastUrl("/category?id=" + azCat.getId() + ";type=xml");
      newSubCategories.put(azId, azCat);
      atozCat.addSubCategory(azCat);
    }

    programmeCat.addOtherParentId(azId);
    azCat.addSubCategory(programmeCat);
  }

  public String getProgress() {
    if ("Finished".equals(progressString) || "Failed".equals(progressString)
            || "Waiting".equals(progressString) || "Stopped".equals(progressString)) {
      if (future == null) {
        progressString += "";
      } else {
        long delay = future.getDelay(TimeUnit.MILLISECONDS);
        delay = System.currentTimeMillis() + delay;
        Date date = new Date(delay);
        SimpleDateFormat format = new SimpleDateFormat("h:mma");
        String dateStr = format.format(date);
        progressString += " until " + dateStr;
      }
    }
    return progressString;
  }

  public String getErrorSummary() {
    return errorSummary;
  }

  public String getStatsSummary() {
    return sourceStats + " sources " + programmeStats + " programmes " + episodeStats + " episodes";
  }

  public void setProgress(String progress) {
    progressString = progress;
  }

  public void start(final List<CatalogPublisher> publishers, final CatalogPersister persister) {
    try {
      logger.info("Starting the catalog service");

      catalogingScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
          return new Thread(r, "cataloging-scheduler");
        }
      });

      programmeThreadPool = Executors.newFixedThreadPool(10, new NumberedThreadFactory("cataloger-programme"));
      episodeThreadPool = Executors.newFixedThreadPool(10, new NumberedThreadFactory("cataloger-episode"));

      this.publishers = publishers;


      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          try {
            logger.info("Restoring catalog from backup");
            Catalog initial = persister.load();
            errorSummary = buildErrorSummary(initial);
            buildStatsSummary(initial);
            publish(initial, publishers);
            lastCatalog = initial;
            logger.info("Restored catalog from backup");
          } catch (Exception e) {
            logger.error("Failed to restore catalog from backup", e);
          }
        }
      };

      catalogingScheduler.schedule(runnable, 0, TimeUnit.MINUTES);

      runnable = getCatalogRunnable(publishers);

      GregorianCalendar cal = new GregorianCalendar();
      cal.set(Calendar.HOUR_OF_DAY, context.getRefreshStartHour());
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      while (cal.getTimeInMillis() < System.currentTimeMillis()) {
        cal.add(Calendar.HOUR_OF_DAY, refreshRate);
      }
      long refreshStartTime = cal.getTimeInMillis();

      long initialDelay = (refreshStartTime - System.currentTimeMillis());

      logger.info("First catalog will be at " + cal.getTime() + " thats in " + (initialDelay / (1000 * 60 * 60)) + " Hours");

      int threshold = 0;
      for (String name : pluginManager.getPluginNames()) {
        if (!context.skipPlugin(name)) {
          if (context.getMaxProgrammes(name) != Integer.MAX_VALUE) {
            threshold += context.getMaxProgrammes(name);
          }
        }
      }
      if (threshold == 0) {
        threshold = context.getRefreshStartNowProgrammeThreshold();
      }

      if (programmeStats < threshold) {
        logger.info("Doing early cataloging as catalog looks a bit sparse: " + programmeStats + " programmes when expected at least " + threshold);
        future = catalogingScheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS);
      }

      future = catalogingScheduler.scheduleAtFixedRate(runnable, initialDelay, refreshRate * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
      logger.info("Started the catalog service");
    } catch (Exception e) {
      logger.error("Failed to start the catalog service", e);
    }
  }

  public void shutdown() {
    stop.set(true);
    catalogingScheduler.shutdownNow();
    programmeThreadPool.shutdownNow();
    episodeThreadPool.shutdownNow();
  }

  public boolean isRunning() {
    return catalogRunning.get();
  }

  public String startCataloging() {
    try {
      if (isRunning()) {
        return "Already running";
      } else {
        catalogingScheduler.schedule(getCatalogRunnable(publishers), 0, TimeUnit.SECONDS);
        return "Started catalog";
      }
    } catch (Exception e) {
      logger.error("Failed to start cataloging", e);
      return "Failed to start catalog";
    }

  }

  public String stopCataloging() {
    try {
      if (!isRunning()) {
        return "Already stopped";
      } else {
        stop.set(true);
        return "Stopping catalog";
      }
    } catch (Exception e) {
      logger.error("Failed to stop cataloging", e);
      return "Failed to stop catalog";
    }
  }

  private CatchupPluginRemote getCatchupPluginRemote() throws Exception {
    int rmiRegistryPort = context.getCatchupPluginRmiPort();

    return (CatchupPluginRemote) RmiHelper.lookup("localhost", rmiRegistryPort, "CatchupPlugin");
  }
}
