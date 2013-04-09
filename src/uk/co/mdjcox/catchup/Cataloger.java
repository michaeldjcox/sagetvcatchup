package uk.co.mdjcox.catchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.*;
import uk.co.mdjcox.catchup.plugins.*;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class Cataloger {

    private LoggerInterface logger;
    private PropertiesInterface props;
    private PluginManager pluginManager;

    @Inject
    private Cataloger(LoggerInterface logger, PropertiesInterface props, PluginManager pluginManager) {
        this.logger = logger;
        this.props = props;
        this.pluginManager = pluginManager;
    }

    public Catalog catalog() {
        Catalog catalog = new Catalog();

        try {
            Map<String, Category> newCategories = new LinkedHashMap<String, Category>();

            Root root = new Root("Catchup", "UK Catchup TV for SageTV", "http://localhost:8081", "http://localhost:8081/logo.png");
            newCategories.put(root.getId(), root);

            for (Plugin plugin : pluginManager.getPlugins()) {

                Source sourceCat = plugin.getSource();
                newCategories.put(sourceCat.getId(), sourceCat);

                logger.info("Found source: " + sourceCat);

                Map<String, Programme> newProgCategories = new LinkedHashMap<String, Programme>();

                Map<String, Episode> newEpisodes = new LinkedHashMap<String, Episode>();

                root.addSubCategory(sourceCat);

                logger.info("Getting programmes found on: " + sourceCat);

                Collection<Programme> programmes = plugin.getProgrammes();
                for (Programme programme : programmes) {

                    plugin.getEpisodes(programme);

                    for (Episode episode : programme.getEpisodes().values()) {

                        plugin.getEpisode(programme, episode);
                    }

                    if (programme.getEpisodes().size() == 0) continue;

                    newProgCategories.put(programme.getId(), programme);
                    newEpisodes.putAll(programme.getEpisodes());

//                    // TODO take this out
                    if (newProgCategories.size() > 5) {
                        break;
                    }
                }

                sourceCat.getSubCategories().clear();


                logger.info("Found " + newProgCategories.size() + " Programmes");
                logger.info("Found " + newEpisodes.size() + " Episodes");


                Map<String, SubCategory> newSubCategories = new LinkedHashMap<String, SubCategory>();

                for (Programme programmeCat : newProgCategories.values()) {
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

        } catch (Exception e) {
            logger.severe("Failed to refresh properties file", e);
        }

        return catalog;
    }

    private void doAirDateCategorisation(Source sourceCat, Programme programmeCat, Episode episode, Map<String, SubCategory> newSubCategories) {
        String airDateName = episode.getAirDate();
        if (airDateName == null || airDateName.isEmpty()) return;

        String airdateId = sourceCat.getId() + "/AirDate";
        SubCategory airdateCat = newSubCategories.get(airdateId);
        if (airdateCat == null) {
            airdateCat = new SubCategory(airdateId, "Air Date", "Air Date", sourceCat.getServiceUrl(), sourceCat.getIconUrl(), sourceCat.getId());
            newSubCategories.put(airdateId, airdateCat);
            sourceCat.addSubCategory(airdateCat);
        }
        String airDateInstanceId = sourceCat.getId() + "/AirDate/" + airDateName.replace(" ", "").replace(",", "");
        Programme airDateInstanceCat = (Programme) newSubCategories.get(airDateInstanceId);
        if (airDateInstanceCat == null) {
            airDateInstanceCat = new Programme(airDateInstanceId, airDateName, airDateName, sourceCat.getServiceUrl(), sourceCat.getIconUrl(), airdateCat.getId());
            newSubCategories.put(airDateInstanceId, airDateInstanceCat);
            airdateCat.addSubCategory(airDateInstanceCat);
        }

        airDateInstanceCat.addEpisode(episode);
    }

    private void doChannelCategorisation(Source sourceCat, Programme programmeCat, Episode prog, Map<String, SubCategory> newSubCategories) {
        String channelName = prog.getChannel();
        if (channelName != null && !channelName.isEmpty()) {

            String channelId = sourceCat.getId() + "/Channel";
            SubCategory channelCat = newSubCategories.get(channelId);
            if (channelCat == null) {
                channelCat = new SubCategory(channelId, "Channel", "Channel", sourceCat.getServiceUrl(), sourceCat.getIconUrl(), sourceCat.getId());
                newSubCategories.put(channelId, channelCat);
                sourceCat.addSubCategory(channelCat);
            }
            String channelInstanceId = sourceCat.getId() + "/Channel/" + channelName.replace(" ", "");
            SubCategory channelInstanceCat = newSubCategories.get(channelInstanceId);
            if (channelInstanceCat == null) {
                channelInstanceCat = new SubCategory(channelInstanceId, channelName, channelName, sourceCat.getServiceUrl(), sourceCat.getIconUrl(), channelCat.getId());
                newSubCategories.put(channelInstanceId, channelInstanceCat);
                channelCat.addSubCategory(channelInstanceCat);
            }

            programmeCat.addOtherParentId(channelInstanceId);
            channelInstanceCat.addSubCategory(programmeCat);
        }
    }

    private void doGenreCategorisation(Source sourceCat, Programme programmeCat, Episode prog, Map<String, SubCategory> newSubCategories) {
        String genreName = prog.getCategory();
        if (genreName != null && !genreName.isEmpty()) {
            String genreId = sourceCat.getId() + "/Genre";
            SubCategory genreCat = newSubCategories.get(genreId);
            if (genreCat == null) {
                genreCat = new SubCategory(genreId, "Genre", "Genre", sourceCat.getServiceUrl(), sourceCat.getIconUrl(), sourceCat.getId());
                newSubCategories.put(genreId, genreCat);
                sourceCat.addSubCategory(genreCat);
            }
            String genreInstanceId = sourceCat.getId() + "/Genre/" + genreName.replace(" ", "");
            SubCategory genreInstanceCat = newSubCategories.get(genreInstanceId);
            if (genreInstanceCat == null) {
                genreInstanceCat = new SubCategory(genreInstanceId, genreName, genreName, sourceCat.getServiceUrl(), sourceCat.getIconUrl(), genreCat.getId());
                newSubCategories.put(genreInstanceId, genreInstanceCat);
                genreCat.addSubCategory(genreInstanceCat);
            }
            programmeCat.addOtherParentId(genreInstanceId);
            genreInstanceCat.addSubCategory(programmeCat);
        }
    }

    private void doAtoZcategorisation(Source sourceCat, Programme programmeCat, Map<String, SubCategory> newSubCategories) {
        // A to Z
        String programmeTitle = programmeCat.getShortName();
        String azName;
        if (!programmeTitle.startsWith("The ") && !programmeTitle.startsWith("the ")) {
            azName = programmeTitle.substring(0, 1).toUpperCase();
        } else {
            azName = programmeTitle.substring(4, 5).toUpperCase();
        }

        String atozId = sourceCat.getId() + "/AtoZ";
        SubCategory atozCat = newSubCategories.get(atozId);
        if (atozCat == null) {
            atozCat = new SubCategory(atozId, "A to Z", "A to Z", sourceCat.getServiceUrl(), sourceCat.getIconUrl(), sourceCat.getId());
            newSubCategories.put(atozId, atozCat);
            sourceCat.addSubCategory(atozCat);
        }
        String azId = sourceCat.getId() + "/AtoZ/" + azName;
        SubCategory azCat = newSubCategories.get(azId);
        if (azCat == null) {
            azCat = new SubCategory(azId, azName, azName, sourceCat.getServiceUrl(), sourceCat.getIconUrl(), atozCat.getId());
            newSubCategories.put(azId, azCat);
            atozCat.addSubCategory(azCat);
        }

        programmeCat.addOtherParentId(azId);
        azCat.addSubCategory(programmeCat);
    }

}
