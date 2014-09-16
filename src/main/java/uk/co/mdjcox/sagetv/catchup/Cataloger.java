package uk.co.mdjcox.sagetv.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;

import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.sagetv.catchup.plugins.*;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA. User: michael Date: 28/03/13 Time: 17:34 To change this template use
 * File | Settings | File Templates.
 */
@Singleton
public class Cataloger {

    private Logger logger;
    private PropertiesInterface props;
    private PluginManager pluginManager;
    private String podcastUrlBase;
    private String progressString = "Waiting";
    private AtomicBoolean stop = new AtomicBoolean(false);

    @Inject
    private Cataloger(Logger logger, PropertiesInterface props, PluginManager pluginManager) {
        this.logger = logger;
        this.props = props;
        this.pluginManager = pluginManager;
        this.podcastUrlBase = "http://localhost:" + props.getString("podcasterPort", "8081") + "/";

    }

    public Catalog catalog() {

        progressString = "Started";

        Catalog catalog = new Catalog();

        try {
            Map<String, Category> newCategories = new LinkedHashMap<String, Category>();

            Root root = new Root("Catchup", "UK Catchup TV for SageTV", "http://localhost:8081",
                            "http://localhost:" + props.getInt("podcasterPort", 8081) + "/logo.png");
            newCategories.put(root.getId(), root);

            for (Plugin plugin : pluginManager.getPlugins()) {
                checkForStop();

                Source sourceCat = plugin.getSource();
                String pluginName = sourceCat.getId();

                progressString = "Doing " + pluginName;

                ArrayList<String> testProgrammes = props.getPropertySequence(pluginName + ".programmes");
                int testMaxProgrammes = props.getInt(pluginName + ".maxprogrammes", Integer.MAX_VALUE);

                newCategories.put(sourceCat.getId(), sourceCat);

                logger.info("Found source: " + sourceCat);

                Map<String, Programme> newProgCategories = new LinkedHashMap<String, Programme>();

                Map<String, Episode> newEpisodes = new LinkedHashMap<String, Episode>();

                root.addSubCategory(sourceCat);

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

                    programme.setPodcastUrl(podcastUrlBase + programmeId);

                    plugin.getEpisodes(sourceCat, programme);

                    for (Episode episode : programme.getEpisodes().values()) {
                        checkForStop();

                        plugin.getEpisode(sourceCat, programme, episode);
                    }

                    if (programme.getEpisodes().size() == 0) {
                        continue;
                    }

                    newProgCategories.put(programmeId, programme);
                    newEpisodes.putAll(programme.getEpisodes());
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
                    for (Episode episode : programmeCat.getEpisodes().values()) {
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

            catalog.setCategories(newCategories);

            progressString = "Finished cataloging";

            return catalog;

        } catch (Exception e) {
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
            airDateInstanceCat.setPodcastUrl(podcastUrlBase + airDateInstanceId);
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
                newSubCategories.put(channelId, channelCat);
                sourceCat.addSubCategory(channelCat);
            }
            String channelInstanceId = sourceId + "/Channel/" + channelName.replace(" ", "");
            SubCategory channelInstanceCat = newSubCategories.get(channelInstanceId);
            if (channelInstanceCat == null) {
                channelInstanceCat =
                        new SubCategory(sourceId, channelInstanceId, channelName, channelName, sourceCat.getServiceUrl(),
                                sourceCat.getIconUrl(), channelCat.getId());
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
            newSubCategories.put(atozId, atozCat);
            sourceCat.addSubCategory(atozCat);
        }
        String azId = sourceId + "/AtoZ/" + azName;
        SubCategory azCat = newSubCategories.get(azId);
        if (azCat == null) {
            azCat =
                    new SubCategory(sourceId, azId, azName, azName, sourceCat.getServiceUrl(), sourceCat.getIconUrl(),
                            atozCat.getId());
            newSubCategories.put(azId, azCat);
            atozCat.addSubCategory(azCat);
        }

        programmeCat.addOtherParentId(azId);
        azCat.addSubCategory(programmeCat);
    }

    public String getProgress() {
        return progressString;
    }

    public void setProgress(String progress) {
        progressString = progress;
    }

    public void stop() {
        stop.set(true);
    }
}
