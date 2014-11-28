package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.utils.LoggerInterface;
import uk.co.mdjcox.utils.NamedThreadFactory;
import uk.co.mdjcox.utils.NumberedThreadFactory;
import uk.co.mdjcox.utils.RmiHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
  private static final int PROGRAMME_THREADS = 20;
  private static final int EPISODE_THREADS = 20;
  private static final int MILLIS_IN_A_DAY = 24 * 60 * 60 * 1000;
  private static final int MILLIS_IN_A_WEEK = MILLIS_IN_A_DAY * 7;
  private LoggerInterface logger;
  private PluginManager pluginManager;
  private AtomicReference<String> progressString = new AtomicReference<String>("");
  private AtomicBoolean stop = new AtomicBoolean(false);
  private AtomicBoolean catalogRunning = new AtomicBoolean(false);
  private ScheduledExecutorService catalogingScheduler;
  private ExecutorService programmeThreadPool;
  private ExecutorService episodeThreadPool;

  private ScheduledFuture<?> future;
  private List<CatalogPublisher> publishers;
  private int refreshRate;
  private CatchupContextInterface context;
  private Set<String> favourites = new HashSet<String>();
  private Catalog lastCatalog = new Catalog();
  private Catalog newCatalog = null;
  private boolean multithreaded=true;

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
          newCatalog = null;
          programmeThreadPool = Executors.newFixedThreadPool(PROGRAMME_THREADS, new NumberedThreadFactory("cataloger-programme"));
          episodeThreadPool = Executors.newFixedThreadPool(EPISODE_THREADS, new NumberedThreadFactory("cataloger-episode"));

          catalogRunning.set(true);
          try {
            stop.set(false);
            long startTime = System.currentTimeMillis();

            logger.info("Refreshing catalog");
            Catalog completedCatalog = catalog();
            if (completedCatalog != null) {
              setProgress("Publishing catalog");
              publish(completedCatalog, publishers);
              lastCatalog = completedCatalog;
              int duration = (int)(System.currentTimeMillis() - startTime);

              final int millisInHour = 60 * 60 * 1000;
              final int millisInMin = 60 * 1000;
              int hours = (int)(duration / millisInHour) ;
              int minutes = (int)((duration % millisInHour) / millisInMin);
              int seconds = (int)((duration % millisInMin) / 1000);

              progressString.set(String.format("Finished in %02d:%02d:%02d", hours, minutes, seconds));
              logger.info(progressString.get());
            }
          } catch (Throwable e) {
            String message = e.getMessage();
            if (message == null) {
              message = e.getClass().getSimpleName();
            }
            if (newCatalog != null) {
              Category root = newCatalog.getRoot();
              if (root != null) {
                root.addError("FATAL", "Failed to catalog: " + message);
              }
            }
            logger.error("Failed to refresh catalog", e);
            setProgress("Failed");
          } finally {
            newCatalog = null;
          }
        } catch (Throwable e) {
          logger.error("Caught Exception during cataloging", e);
        } finally {
          catalogRunning.set(false);
          if (programmeThreadPool != null) {
            try {
              programmeThreadPool.shutdownNow();
            } catch (Exception e) {
              // ignore
            }
          }
          if (episodeThreadPool != null) {
            try {
              episodeThreadPool.shutdownNow();
            } catch (Exception e) {
              // ignore
            }
          }

        }
      }
    };
  }



  private void publish(Catalog catalog, List<CatalogPublisher> publishers) {
    for (CatalogPublisher publisher : publishers) {
      publisher.publish(catalog);
    }
  }

  private Catalog catalog() {

    progressString.set("Started");

    newCatalog = new Catalog();

    try {

      Root root = new Root("Catchup", "Catchup TV", "Catchup TV", "/", "/logo.png");
      newCatalog.addRoot(root);

      try {
        favourites = getCatchupPluginRemote().getFavouriteTitles();
      } catch (Exception e) {
        root.addError("ERROR", "Failed to list SageTV favourites");
        logger.error("Failed to collect favourites", e);
      }

      Source statusSource = new Source(root.getId(), "status", "Status", "Status", "", "");
      statusSource.setPodcastUrl("/category?id=status;type=xml");
      newCatalog.addSource(statusSource);
      root.addSubCategory(statusSource);

      Source searchSource = new Source(root.getId(), "search", "Search", "Search", "", "");
      searchSource.setPodcastUrl("/search?type=xml");
      newCatalog.addSource(searchSource);
      root.addSubCategory(searchSource);

      for (final Plugin plugin : pluginManager.getPlugins()) {

        if (context.skipPlugin(plugin.getSource().getId())) {
          continue;
        }

        checkForStop();

        final Source sourceCat = plugin.getSource();
        final String pluginName = sourceCat.getId();

        progressString.set("Doing " + pluginName + " listing programmes");

        ArrayList<String> testProgrammes = context.getTestProgrammes(pluginName);
        int testMaxProgrammes = context.getMaxProgrammes(pluginName);
        if (testMaxProgrammes <=0) {
          testMaxProgrammes = Integer.MAX_VALUE;
        }

        newCatalog.addSource(sourceCat);

        logger.info("Found source: " + sourceCat);

        root.addSubCategory(sourceCat);
        sourceCat.setParentId(root.getId());

        sourceCat.setPodcastUrl("/category?id=" + sourceCat.getId() + ";type=xml");


        logger.info("Getting programmes found on: " + sourceCat);
        int programmeCount = 0;
        Collection<Programme> programmes = plugin.getProgrammes(stop);

        progressString.set("Doing " + pluginName + " programme cataloging");

        final int programmesToDo = Math.min(testMaxProgrammes, programmes.size());
        final CountDownLatch programmesLatch = new CountDownLatch(programmesToDo);

        final ConcurrentHashMap<String, Programme> newProgrammes = new ConcurrentHashMap<String, Programme>();

        for (final Programme programme : programmes) {
          checkForStop();

          programmeCount++;

          if (programmeCount > testMaxProgrammes) {
            break;
          }

          final String programmeId = programme.getId();
          if (testProgrammes != null && !testProgrammes.isEmpty()) {
            if (!testProgrammes.contains(programmeId)) {
              logger.info("Skipping programme " + programmeId);
              programmesLatch.countDown();
              progressString.set("Doing " + pluginName + " programme " + (programmesToDo - programmesLatch.getCount()) + "/" + programmesToDo);
              logger.info(progressString.get());
              continue;
            }
          }

          Runnable runnable = new Runnable() {
            public void run() {
              Programme prog = catalogProgramme(programmeId, programme, plugin, sourceCat, programmesLatch, pluginName, programmesToDo);
              if (prog != null) {
                newProgrammes.put(prog.getId(), prog);
              }
            }
          };

          if (multithreaded) {
            programmeThreadPool.submit(runnable);
          } else {
            runnable.run();
          }
        }

        waitForProgrammeCatalog(sourceCat, programmesLatch);

        checkForStop();

        sourceCat.clearSubCategories();

        logger.info("Found " + newCatalog.getStatsSummary());

        additionalCategorisation(sourceCat, newProgrammes);
      }

      return newCatalog;
    } catch (Throwable e) {

      String message = e.getMessage();
      if (message == null) {
        message = e.getClass().getSimpleName();
      }
      if (!message.equals(STOPPED_ON_REQUEST)) {
        logger.error("Failed to catalog", e);
        progressString.set("Failed to catalog");
      } else {
        logger.info("Cataloging was stopped on request", e);
        progressString.set("Stopped");
      }
      return null;
    }
  }

  private void waitForProgrammeCatalog(Source sourceCat, CountDownLatch programmesLatch) {
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
  }

  private void additionalCategorisation(Source sourceCat, ConcurrentHashMap<String, Programme> newProgrammes) {

    progressString.set("Doing " + sourceCat.getId() + " additional categorisation");

    for (Programme programmeCat : newProgrammes.values()) {

      checkForStop();
      logger.info("Categorising " + programmeCat);
      doAtoZcategorisation(sourceCat, programmeCat, newCatalog);

      // Favourite
      doFavouriteCategorisation(sourceCat, programmeCat, newCatalog);

      for (String episodeId : programmeCat.getEpisodes()) {
        Episode episode = newCatalog.getEpisode(episodeId);
        if ((episode == null) || episodeId.isEmpty()) {
          continue;
        }
        // Genre
        doGenreCategorisation(sourceCat, programmeCat, episode, newCatalog);

        // Channel
        doChannelCategorisation(sourceCat, programmeCat, episode, newCatalog);

        // Air Date
        doAirDateCategorisation(sourceCat, programmeCat, episode, newCatalog);

        // New
        doNewProgrammeCategorisation(sourceCat, programmeCat, episode, newCatalog);
      }
    }
  }

  private Programme catalogProgramme(String programmeId, final Programme programme, final Plugin plugin, final Source sourceCat, CountDownLatch programmesLatch, String pluginName, int programmesToDo) {
    try {
      try {
        logger.info("Doing programme " + programmeId);

        checkForStop();

        programme.setPodcastUrl("/programme?id=" + programmeId + ";type=xml");

        Collection<Episode> episodes = plugin.getEpisodes(sourceCat, programme, stop);

        checkForStop();

        final CountDownLatch episodesLatch = new CountDownLatch(episodes.size());

        for (final Episode episode : episodes) {
          checkForStop();

          Runnable runnable1 = new Runnable() {
            public void run() {
              catalogEpisode(plugin, sourceCat, programme, episode, episodesLatch);
            }
          };

          if (multithreaded) {
            episodeThreadPool.submit(runnable1);
          } else {
            runnable1.run();
          }
        }

        waitForEpisodeCatalog(programmeId, episodesLatch);

        checkForStop();

        if (programme.getEpisodes().size() > 0) {
            logger.info("Programme " + programmeId + " has episodes");
            if (newCatalog.getProgramme(programmeId) != null) {
              logger.warn("Programme " + programmeId + " has a duplicate - merging episodes");
              Programme existingProg = newCatalog.getProgramme(programmeId);
              existingProg.addAllEpisodes(programme.getEpisodes());
              existingProg.addError("WARNING", "Programme has a duplicate - merging episodes");
            } else {
              newCatalog.addProgramme(programme);
              return programme;
            }
        } else {
          logger.warn("Programme " + programmeId + " has no episodes");
          sourceCat.addError("ERROR", "Programme has no episodes: " + programmeId);
          for (ParseError error : programme.getErrors()) {
            sourceCat.addError(error.getLevel(), error.getMessage());
          }

        }
      } catch (Throwable e) {
        String message = e.getMessage();
        if (message == null) {
          message = e.getClass().getSimpleName();
        }
        if (!message.equals(STOPPED_ON_REQUEST)) {
          sourceCat.addError("ERROR", "Failed to catalog programme: " + programmeId);
          logger.error("Failed to catalog programme: " + programmeId, e);
        }
      } finally {
        programmesLatch.countDown();
        if (!stop.get()) {
          progressString.set("Doing " + pluginName + " programme " + (programmesToDo - programmesLatch.getCount()) + "/" + programmesToDo);
          logger.info(progressString.get());
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return null;
  }

  private void waitForEpisodeCatalog(String programmeId, CountDownLatch episodesLatch) {
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
  }

  private void catalogEpisode(Plugin plugin, Source sourceCat, Programme programme, Episode episode, CountDownLatch episodesLatch) {
    try {
      try {
        checkForStop();

        plugin.getEpisode(sourceCat, programme, episode, stop);

        checkForStop();

        episode.setPodcastUrl("/control?id=" + episode.getId() + ";type=xml");

        newCatalog.addEpisode(episode);

        programme.addEpisode(episode);
      } catch (Throwable e) {
        String message = e.getMessage();
        if (message == null) {
          message = e.getClass().getSimpleName();
        }
        if (!message.equals(STOPPED_ON_REQUEST)) {
          programme.addError("ERROR", "Failed to catalog episode " + episode.getEpisodeTitle());
        }
      } finally {
        episodesLatch.countDown();
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private void checkForStop() {
    if (stop.get()) {
      if (!progressString.get().startsWith("Stopped")) {
        progressString.set("Stopping");
      }
      throw new RuntimeException(STOPPED_ON_REQUEST);
    }
  }

  private void doFavouriteCategorisation(Source sourceCat, Programme programmeCat, Catalog catalog) {

    String sourceId = sourceCat.getId();
    String favouriteId = sourceId + "/Favourite";
    SubCategory favouriteCat = catalog.getSubcategory(favouriteId);
    if (favouriteCat == null) {
      favouriteCat =
              new SubCategory(sourceId, favouriteId, "Favourites", "Favourites", sourceCat.getServiceUrl(),
                      sourceCat.getIconUrl(), sourceId);
      favouriteCat.setPodcastUrl("/category?id=" + favouriteId + ";type=xml");
      catalog.addSubCategory(favouriteCat);
      sourceCat.addSubCategory(favouriteCat);
    }

    String shortName = programmeCat.getShortName();
    if (favourites.contains(shortName.toUpperCase())) {
        logger.info("Found a favourite " + shortName);
      favouriteCat.addSubCategory(programmeCat);
    }
  }

  private void doNewProgrammeCategorisation(Source sourceCat, Programme programmeCat, Episode episode, Catalog catalog) {
    String sourceId = sourceCat.getId();
    String favouriteId = sourceId + "/New";
    SubCategory favouriteCat = catalog.getSubcategory(favouriteId);
    if (favouriteCat == null) {
      favouriteCat =
              new SubCategory(sourceId, favouriteId, "New", "New", sourceCat.getServiceUrl(),
                      sourceCat.getIconUrl(), sourceId);
      favouriteCat.setPodcastUrl("/category?id=" + favouriteId + ";type=xml");
      catalog.addSubCategory(favouriteCat);
      sourceCat.addSubCategory(favouriteCat);
    }

    if (lastCatalog != null) {
      String id = programmeCat.getId();
      String epId = episode.getId();
      Programme cat = lastCatalog.getProgramme(id);
      Episode ep = lastCatalog.getEpisode(epId);
      if (cat == null) {
        SubCategory newProgrammeCat = addNewProgrammeCat("/New/", programmeCat, catalog, sourceId, favouriteId, favouriteCat, id);
        newProgrammeCat.addAllEpisodes(programmeCat.getEpisodes());
      } else
      if (ep == null)
      {
        SubCategory newProgrammeCat = addNewProgrammeCat("/New/", programmeCat, catalog, sourceId, favouriteId, favouriteCat, id);
        newProgrammeCat.addEpisode(episode);
      }
    }
  }

  private SubCategory addNewProgrammeCat(String idInsert, Programme programmeCat, Catalog catalog, String sourceId, String favouriteId, SubCategory favouriteCat, String id) {
    String favouriteProgId = sourceId + idInsert + id;
    SubCategory newProgCat = catalog.getSubcategory(favouriteProgId);
    if (newProgCat == null) {
      newProgCat = new SubCategory(sourceId, favouriteProgId,
              programmeCat.getShortName(),
              programmeCat.getLongName(),
              "/programme?id="+ favouriteProgId +";type=html",
              programmeCat.getIconUrl(),
              favouriteId);
      catalog.addSubCategory(newProgCat);
      favouriteCat.addSubCategory(newProgCat);
    }
    return newProgCat;
  }

  private void doAirDateCategorisation(Source sourceCat, Programme programmeCat, Episode episode, Catalog catalog) {
    String airDateName = episode.getAirDate();
    if (airDateName == null || airDateName.isEmpty()) {
      return;
    }

    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    Date date = null;

    try {
      date = format.parse(airDateName);
    } catch (ParseException e) {
      logger.warn("Failed to categorise by airDate=" + airDateName + " id="+ episode.getId());
      return;
    }

    System.err.println("Turning " + airDateName);
    airDateName = getRelativeTime(date);
    System.err.println("Into    " + airDateName);

    String sourceId = sourceCat.getId();
    String airdateId = sourceId + "/AirDate";
    SubCategory airdateCat = catalog.getSubcategory(airdateId);
    if (airdateCat == null) {
      airdateCat =
              new SubCategory(sourceId, airdateId, "Air Date", "Air Date", sourceCat.getServiceUrl(),
                      sourceCat.getIconUrl(), sourceId);
      airdateCat.setPodcastUrl("/category?id=" + airdateId + ";type=xml");
      catalog.addSubCategory(airdateCat);
      sourceCat.addSubCategory(airdateCat);
    }
    String
            airDateInstanceId =
            sourceId + "/AirDate/" + airDateName.replace(" ", "").replace(",", "");
    SubCategory airDateInstanceCat =  catalog.getSubcategory(airDateInstanceId);
    if (airDateInstanceCat == null) {
      airDateInstanceCat =
              new SubCategory(sourceId, airDateInstanceId, airDateName, airDateName, sourceCat.getServiceUrl(),
                      sourceCat.getIconUrl(), airdateCat.getId());
      airDateInstanceCat.setPodcastUrl("/category?id=" + airDateInstanceId + ";type=xml");
      catalog.addSubCategory(airDateInstanceCat);
      airdateCat.addSubCategory(airDateInstanceCat);
    }

    SubCategory newProgrammeCat = addNewProgrammeCat("/AirDate/" + airDateName.replace(" ", "").replace(",", "") + "/", programmeCat, catalog, sourceId, airDateInstanceId, airDateInstanceCat, programmeCat.getId());
    newProgrammeCat.addEpisode(episode);

    airDateInstanceCat.addSubCategory(newProgrammeCat);
  }

  private String getRelativeTime(Date date) {
    SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEE dd MMM");
    SimpleDateFormat monthFormatThisYear = new SimpleDateFormat("MMMM");
    SimpleDateFormat monthFormatLastYear = new SimpleDateFormat("MMMM yyyy");
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    dayOfWeekFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    monthFormatThisYear.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    monthFormatLastYear.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    yearFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));

    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(new Date());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.MONTH, -1);

    Date aMonthAgo = cal.getTime();
    aMonthAgo.setDate(1);

    if (date.after(aMonthAgo) || date.equals(aMonthAgo)) {
      return dayOfWeekFormat.format(date);
    }


    cal.setTime(new Date());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.MONTH, -12);

    Date aYearAgo = cal.getTime();
    aYearAgo.setDate(1);
    aYearAgo.setMonth(0);

    if (date.after(aYearAgo) || date.equals(aYearAgo)) {
      Date now = new Date();
      if (date.getYear() == now.getYear()) {
        return monthFormatThisYear.format(date);
      } else {
        return monthFormatLastYear.format(date);
      }
    }

    cal.setTime(new Date());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.YEAR, -10);

    Date aDecadeAgo = cal.getTime();
    aDecadeAgo.setDate(1);
    aDecadeAgo.setMonth(0);

    if (date.after(aDecadeAgo) || date.equals(aDecadeAgo)) {
      return yearFormat.format(date);
    }

    String year = yearFormat.format(date);
    year = year.charAt(2) + "0s";

    return year;
  }

  private void doChannelCategorisation(Source sourceCat, Programme programmeCat, Episode prog, Catalog catalog) {
    String channelName = prog.getChannel();
    if (channelName != null && !channelName.isEmpty()) {

      String sourceId = sourceCat.getId();
      String channelId = sourceId + "/Channel";
      SubCategory channelCat = catalog.getSubcategory(channelId);
      if (channelCat == null) {
        channelCat =
                new SubCategory(sourceId, channelId, "Channel", "Channel", sourceCat.getServiceUrl(),
                        sourceCat.getIconUrl(), sourceId);
        channelCat.setPodcastUrl("/category?id=" + channelCat.getId() + ";type=xml");
        catalog.addSubCategory(channelCat);
        sourceCat.addSubCategory(channelCat);
      }
      String channelInstanceId = sourceId + "/Channel/" + channelName.replace(" ", "");
      SubCategory channelInstanceCat = catalog.getSubcategory(channelInstanceId);
      if (channelInstanceCat == null) {
        channelInstanceCat =
                new SubCategory(sourceId, channelInstanceId, channelName, channelName, sourceCat.getServiceUrl(),
                        sourceCat.getIconUrl(), channelCat.getId());
        channelInstanceCat.setPodcastUrl("/category?id=" + channelInstanceCat.getId() + ";type=xml");
        catalog.addSubCategory(channelInstanceCat);
        channelCat.addSubCategory(channelInstanceCat);
      }

      programmeCat.addOtherParentId(channelInstanceId);
      channelInstanceCat.addSubCategory(programmeCat);
    }
  }

  private void doGenreCategorisation(Source sourceCat, Programme programmeCat, Episode prog, Catalog catalog) {
    Set<String> genres = prog.getGenres();
    if (genres != null && !genres.isEmpty()) {
      for (String genreName : genres) {
        String sourceId = sourceCat.getId();
        String genreId = sourceId + "/Genre";
        SubCategory genreCat = catalog.getSubcategory(genreId);
        if (genreCat == null) {
          genreCat =
                  new SubCategory(sourceId, genreId, "Genre", "Genre", sourceCat.getServiceUrl(),
                          sourceCat.getIconUrl(), sourceId);
          genreCat.setPodcastUrl("/category?id=" + genreCat.getId() + ";type=xml");

          catalog.addSubCategory(genreCat);
          sourceCat.addSubCategory(genreCat);
        }
        String genreInstanceId = sourceId + "/Genre/" + genreName.replace(" ", "");
        SubCategory genreInstanceCat = catalog.getSubcategory(genreInstanceId);
        if (genreInstanceCat == null) {
          genreInstanceCat =
                  new SubCategory(sourceId, genreInstanceId, genreName, genreName, sourceCat.getServiceUrl(),
                          sourceCat.getIconUrl(), genreCat.getId());
          catalog.addSubCategory(genreInstanceCat);
          genreInstanceCat.setPodcastUrl("/category?id=" + genreInstanceCat.getId() + ";type=xml");
          genreCat.addSubCategory(genreInstanceCat);
        }
        programmeCat.addOtherParentId(genreInstanceId);
        genreInstanceCat.addSubCategory(programmeCat);
      }
    }
  }

  private void doAtoZcategorisation(Source sourceCat, Programme programmeCat, Catalog catalog) {
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
    SubCategory atozCat = catalog.getSubcategory(atozId);
    if (atozCat == null) {
      atozCat =
              new SubCategory(sourceId, atozId, "A to Z", "A to Z", sourceCat.getServiceUrl(),
                      sourceCat.getIconUrl(), sourceId);
      atozCat.setPodcastUrl("/category?id=" + atozCat.getId() + ";type=xml");
      catalog.addSubCategory(atozCat);
      sourceCat.addSubCategory(atozCat);
    }
    String azId = sourceId + "/AtoZ/" + azName;
    SubCategory azCat = catalog.getSubcategory(azId);
    if (azCat == null) {
      azCat =
              new SubCategory(sourceId, azId, azName, azName, sourceCat.getServiceUrl(), sourceCat.getIconUrl(),
                      atozCat.getId());
      azCat.setPodcastUrl("/category?id=" + azCat.getId() + ";type=xml");
      catalog.addSubCategory(azCat);
      atozCat.addSubCategory(azCat);
    }

    programmeCat.addOtherParentId(azId);
    azCat.addSubCategory(programmeCat);
  }

  public String getProgress() {
    if (progressString.get().startsWith("Finished") || progressString.get().startsWith("Failed") ||
            progressString.get().startsWith("Waiting") || progressString.get().startsWith("Stopped")) {
      if (future != null) {
        if (!progressString.get().contains("Next catalog")) {
          long delay = future.getDelay(TimeUnit.MILLISECONDS);
          delay = System.currentTimeMillis() + delay;
          Date date = new Date(delay);
          SimpleDateFormat format = new SimpleDateFormat("h:mma");
          String dateStr = format.format(date);
          progressString.set(progressString.get() + ". Next catalog: " + dateStr);
        }
      }
    }
    return progressString.get();
  }

  public String getErrorSummary() {
    if (lastCatalog != null) {
      return lastCatalog.getErrorSummary();
    } else {
      return "";
    }
  }

  public String getErrorSummaryNew() {
    if (newCatalog != null) {
      return newCatalog.getErrorSummary();
    } else {
      return "";
    }
  }

  public String getStatsSummary() {
    if (lastCatalog != null) {
      return lastCatalog.getStatsSummary();
    } else {
      return "0 sources 0 categories 0 programmes 0 episodes";
    }
  }

  public String getStatsSummaryNew() {
    if (newCatalog != null) {
      return newCatalog.getStatsSummary();
    } else {
      return "";
    }
  }

  public void setProgress(String progress) {
    progressString.set(progress);
  }

  public void start(final List<CatalogPublisher> publishers, final CatalogPersister persister) {
    try {
      String msg = "Starting the catalog service";
      logger.info(msg);
      progressString.set(msg);

      catalogingScheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("cataloging-scheduler"));

      this.publishers = publishers;

      try {
        msg = "Restoring catalog from backup";
        logger.info(msg);
        progressString.set(msg);

        Catalog initial = persister.load();
        msg = "Publishing restored catalog";
        progressString.set(msg);
        publish(initial, publishers);
        lastCatalog = initial;
        msg = "Restored catalog from backup";
        progressString.set(msg);
        logger.info(msg);
      } catch (Exception e) {
        msg = "Failed to restore catalog from backup";
        progressString.set(msg);
        logger.error(msg, e);

      }

      Runnable runnable = getCatalogRunnable(publishers);

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

      final int millisInHour = 60 * 60 * 1000;
      final int millisInMin = 60 * 1000;
      int hours = (int)(initialDelay / millisInHour) ;
      int minutes = (int)((initialDelay % millisInHour) / millisInMin);
      int seconds = (int)((initialDelay % millisInMin) / 1000);

      int threshold = getSparseThreshold();
      int currentCount = (lastCatalog == null) ? 0 : lastCatalog.getNumberProgrammes();

      if (currentCount < threshold) {
        logger.info("Initial catalog looks a bit sparse: " + currentCount + " programmes when expected at least " + threshold + " - cataloging now");
        catalogingScheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS);
      } else {
        logger.info("Initial catalog looks populated - next catalog in " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
      }

      future = catalogingScheduler.scheduleAtFixedRate(runnable, initialDelay, refreshRate * 60 * 60 * 1000, TimeUnit.MILLISECONDS);

      progressString.set("Waiting");
      getProgress();
      logger.info("Started the catalog service");
    } catch (Exception e) {
      logger.error("Failed to start the catalog service", e);
    }
  }

  private int getSparseThreshold() {
    int threshold = 0;
    for (String name : pluginManager.getPluginNames()) {
      if (!context.skipPlugin(name)) {
        if (context.getMaxProgrammes(name) > 0 && context.getMaxProgrammes(name) < Integer.MAX_VALUE) {
          threshold += context.getMaxProgrammes(name)-(context.getMaxProgrammes(name)/10);
        }
      }
    }
    if (threshold == 0) {
      threshold = context.getRefreshStartNowProgrammeThreshold();
    }
    return threshold;
  }

  public void shutdown() {
    stop.set(true);
    if (catalogingScheduler != null) {
      try {
        catalogingScheduler.shutdownNow();
      } catch (Exception e) {
        //Ignore
      }
    }
    if (programmeThreadPool != null) {
      try {
        programmeThreadPool.shutdownNow();
      } catch (Exception e) {
        // Ignore
      }
    }
    if (episodeThreadPool != null) {
      try {
        episodeThreadPool.shutdownNow();
      } catch (Exception e) {
        // Ignore
      }
    }
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
        if (!progressString.get().startsWith("Stopped")) {
          progressString.set("Stopping");
        }
        return "Stopping catalog";
      }
    } catch (Exception e) {
      logger.error("Failed to stop cataloging", e);
      return "Failed to stop catalog";
    }
  }

  private CatchupPluginRemote getCatchupPluginRemote() throws Exception {
    int rmiRegistryPort = context.getCatchupPluginRmiPort();

    return (CatchupPluginRemote) RmiHelper.lookup("127.0.0.1", rmiRegistryPort, "CatchupPlugin");
  }
}
