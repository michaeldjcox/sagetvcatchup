package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import uk.co.mdjcox.sagetv.catchup.plugins.Plugin;
import uk.co.mdjcox.sagetv.catchup.plugins.PluginManager;
import uk.co.mdjcox.sagetv.model.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA. User: michael Date: 28/03/13 Time: 17:34 To change this template use
 * File | Settings | File Templates.
 */
@Singleton
public class Cataloger {

    private Logger logger;
    private PluginManager pluginManager;
    private String podcastUrlBase;
    private String progressString = "Waiting";
    private AtomicBoolean stop = new AtomicBoolean(false);
    private AtomicBoolean catalogRunning = new AtomicBoolean(false);
    private ScheduledExecutorService service;
    private ScheduledFuture<?> future;
    private List<CatalogPublisher> publishers;
    private String errorSummary = "";
    private long refreshRate;
    private CatchupContextInterface context;

  @Inject
    private Cataloger(Logger logger, CatchupContextInterface context, PluginManager pluginManager) {
        this.logger = logger;
        this.pluginManager = pluginManager;
        this.podcastUrlBase = context.getPodcastBase();
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
                        errorSummary = buildErrorSummary(catalog.getErrors());
                    }
                    catalogRunning.set(false);
                }
              } catch (Throwable e) {
                logger.error("Caught Exception during cataloging", e);
              }
            }
        };
    }

    private void publish(Catalog catalog, List<CatalogPublisher> publishers) {
        for (CatalogPublisher publisher : publishers) {
            publisher.publish(catalog);
        }
    }

    private String buildErrorSummary(Collection<ParseError> errorList) {
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
            errorSummary += entry.getKey()+ " " + entry.getValue()+ " ";
        }
        errorSummary+=")";

        if (errorSummary.equals("()")) {
            errorSummary = "";
        }
        return errorSummary;
    }

    private Catalog catalog() {

        progressString = "Started";

        Catalog catalog = new Catalog();

        try {
            Map<String, Category> newCategories = new LinkedHashMap<String, Category>();
            Map<String, Episode> newEpisodes = new LinkedHashMap<String, Episode>();

            Root root = new Root("Catchup", "Catchup TV", "Catchup TV", podcastUrlBase, podcastUrlBase + "logo.png");
            newCategories.put(root.getId(), root);

            Source statusSource = new Source(root.getId(), "status", "Catchup Status", "Catchup Status", "", podcastUrlBase + "logo.png");
            statusSource.setPodcastUrl(podcastUrlBase + "category?id=status;type=xml");
            newCategories.put(statusSource.getId(), statusSource);
            root.addSubCategory(statusSource);

            for (Plugin plugin : pluginManager.getPlugins()) {
                checkForStop();

                Source sourceCat = plugin.getSource();
                String pluginName = sourceCat.getId();

                progressString = "Doing " + pluginName;

                ArrayList<String> testProgrammes = context.getTestProgrammes(pluginName);
                int testMaxProgrammes = context.getMaxProgrammes(pluginName);

                newCategories.put(sourceCat.getId(), sourceCat);

                logger.info("Found source: " + sourceCat);

                Map<String, Programme> newProgCategories = new LinkedHashMap<String, Programme>();

                root.addSubCategory(sourceCat);
                sourceCat.setParentId(root.getId());

                sourceCat.setPodcastUrl(podcastUrlBase + "category?id=" + sourceCat.getId() + ";type=xml");


                logger.info("Getting programmes found on: " + sourceCat);
                int programmeCount = 0;
                Collection<Programme> programmes = plugin.getProgrammes();
                for (Programme programme : programmes) {

                    checkForStop();

                    programmeCount++;

                    if (programmeCount > testMaxProgrammes) {
                        break;
                    }

                    progressString = "Doing " + pluginName + " programme " + programmeCount + "/" + programmes.size();


                    String programmeId = programme.getId();
                    if (testProgrammes != null && !testProgrammes.isEmpty()) {
                        if (!testProgrammes.contains(programmeId)) {
                            logger.info("Skipping programme " + programmeId);
                            continue;
                        }
                    }

                    programme.setPodcastUrl(podcastUrlBase + "programme?id=" + programmeId + ";type=xml");

                    Collection<Episode> episodes = plugin.getEpisodes(sourceCat, programme);

                    for (Episode episode : episodes) {
                        checkForStop();

                        plugin.getEpisode(sourceCat, programme, episode);

                        episode.setPodcastUrl(podcastUrlBase + "control?id=" + episode.getId() + ";type=xml");

                        newEpisodes.put(episode.getId(), episode);

                        programme.addEpisode(episode);
                    }

                    if (programme.getEpisodes().size() == 0) {
                        continue;
                    }

                    newProgCategories.put(programmeId, programme);
                }

                checkForStop();

                sourceCat.clearSubCategories();

                logger.info("Found " + newProgCategories.size() + " Programmes");
                logger.info("Found " + newEpisodes.size() + " Episodes");

                Map<String, SubCategory> newSubCategories = new LinkedHashMap<String, SubCategory>();


                progressString = "Doing " + pluginName + " additional categorisation";

                for (Programme programmeCat : newProgCategories.values()) {

                    checkForStop();
                    logger.info("Categorising " + programmeCat);
                    doAtoZcategorisation(sourceCat, programmeCat, newSubCategories);

                    boolean doneGenre = false;
                    for (String episodeId : programmeCat.getEpisodes()) {
                        Episode episode = newEpisodes.get(episodeId);
                        if ((episode == null) || episodeId.isEmpty()) {
                            continue;
                        }
                        if (!doneGenre) {
                            // Genre
                            doGenreCategorisation(sourceCat, programmeCat, episode, newSubCategories);

                            // Channel
                            doChannelCategorisation(sourceCat, programmeCat, episode, newSubCategories);
                        }

                        // Air Date
                        doAirDateCategorisation(sourceCat, programmeCat, episode, newSubCategories);
                    }
                }

                newCategories.putAll(newProgCategories);
                newCategories.putAll(newSubCategories);

            }

            catalog.setCategories(root, newCategories, newEpisodes);

            progressString = "Finished cataloging";

            return catalog;

        } catch (Throwable e) {
            logger.error("Failed to refresh properties file", e);
            if (!e.getMessage().equals("Stopped on request")) {
                progressString = "Failed to catalog";
            }
            return null;
        }
    }

    private void checkForStop() {
        if (stop.getAndSet(false)) {
            progressString = "Stopped";
            throw new RuntimeException("Stopped on request");
        }
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
            airdateCat.setPodcastUrl(podcastUrlBase + "category?id=" + airdateId + ";type=xml");
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
            airDateInstanceCat.setPodcastUrl(podcastUrlBase + "category?id=" + airDateInstanceId + ";type=xml");
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
                channelCat.setPodcastUrl(podcastUrlBase + "/category?id=" + channelCat.getId() + ";type=xml");
                newSubCategories.put(channelId, channelCat);
                sourceCat.addSubCategory(channelCat);
            }
            String channelInstanceId = sourceId + "/Channel/" + channelName.replace(" ", "");
            SubCategory channelInstanceCat = newSubCategories.get(channelInstanceId);
            if (channelInstanceCat == null) {
                channelInstanceCat =
                        new SubCategory(sourceId, channelInstanceId, channelName, channelName, sourceCat.getServiceUrl(),
                                sourceCat.getIconUrl(), channelCat.getId());
                channelInstanceCat.setPodcastUrl(podcastUrlBase + "/category?id=" + channelInstanceCat.getId() + ";type=xml");
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
                    genreCat.setPodcastUrl(podcastUrlBase + "/category?id=" + genreCat.getId() + ";type=xml");

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
                    genreInstanceCat.setPodcastUrl(podcastUrlBase + "/category?id=" + genreInstanceCat.getId() + ";type=xml");
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

        int i=0;
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
            atozCat.setPodcastUrl(podcastUrlBase + "/category?id=" + atozCat.getId() + ";type=xml");
            newSubCategories.put(atozId, atozCat);
            sourceCat.addSubCategory(atozCat);
        }
        String azId = sourceId + "/AtoZ/" + azName;
        SubCategory azCat = newSubCategories.get(azId);
        if (azCat == null) {
            azCat =
                    new SubCategory(sourceId, azId, azName, azName, sourceCat.getServiceUrl(), sourceCat.getIconUrl(),
                            atozCat.getId());
            azCat.setPodcastUrl(podcastUrlBase + "/category?id=" + azCat.getId() + ";type=xml");
            newSubCategories.put(azId, azCat);
            atozCat.addSubCategory(azCat);
        }

        programmeCat.addOtherParentId(azId);
        azCat.addSubCategory(programmeCat);
    }

    public String getProgress() {
        if ("Finished".equals(progressString) || "Failed".equals(progressString) || "Waiting".equals(progressString)) {
            if (future == null) {
              progressString += " - no next attempt scheduled";
            } else {
              long delay = future.getDelay(TimeUnit.MINUTES);
              progressString += " - next attempt " + (delay / 60) + "hrs " + (delay % 60) + "mins";
            }
        }
        return progressString;
    }

    public String getErrorSummary() {
        return errorSummary;
    }

    public void setProgress(String progress) {
        progressString = progress;
    }

    public void start(final List<CatalogPublisher> publishers, final CatalogPersister persister) {
      try {
        logger.info("Starting the catalog service");
        service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "catchup-scheduler");
            }
        });

        this.publishers = publishers;


        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            try {
              logger.info("Restoring catalog from backup");
              Catalog initial = persister.load();
              publish(initial, publishers);
              logger.info("Restored catalog from backup");
            } catch (Exception e) {
              logger.error("Failed to restore catalog from backup", e);
            }
          }
        };

        service.schedule(runnable, 0, TimeUnit.MINUTES);

        runnable = getCatalogRunnable(publishers);

        future = service.scheduleAtFixedRate(runnable, 2, refreshRate*60, TimeUnit.MINUTES);
        logger.info("Started the catalog service");
      } catch (Exception e) {
        logger.error("Failed to start the catalog service", e);
      }
    }

    public void shutdown() {
        stop.set(true);
        service.shutdownNow();
    }

    public boolean isRunning() {
        return catalogRunning.get();
    }

    public String startCataloging() {
        try {
            if (isRunning()) {
                return "Already running";
            } else {
                service.schedule(getCatalogRunnable(publishers), 0, TimeUnit.SECONDS);
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
}
