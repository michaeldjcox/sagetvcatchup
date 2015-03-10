/*
 * Project: sagetvcatchup
 * Class:   Catalog
 * Author:  michael
 * Date:    22/04/13
 * Time:    07:34
 */
package uk.co.mdjcox.sagetv.onlinevideo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.co.mdjcox.sagetv.catchup.CatalogPublisher;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.sagetv.model.*;
import uk.co.mdjcox.sagetv.utils.HtmlUtilsInterface;
import uk.co.mdjcox.sagetv.utils.LoggerInterface;
import uk.co.mdjcox.sagetv.utils.PropertiesFile;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class can take a {@link Catalog} of media {@link Source}, categories {@link SubCategory},
 * programmes {@link Programme} metadata and publish as a SageTV online video source.
 *
 * A SageTV custom online video source is two properties files contains in the
 * <code>OnlineVideos</code> subdirectory of the <code>STV</code> directory.
 */
@Singleton
public class SageTvPublisher implements CatalogPublisher {

    // Catchup context containing settings
    private final CatchupContextInterface context;
    /** The logger to be used for debug output */
    private LoggerInterface logger;
    /** The utilities used to perform some string manipulation */
    private HtmlUtilsInterface htmlUtils;
    /** The qualifier added to the custom SageTV online video link and UIText files */
    private String qualifier;
    /** The SageTV STV directory */
    private String onlineVideoPropertiesDir;
    /** The base URL */
    private String podcastBaseUrl;

    /**
     * Creates a new instance which can be used to create new online video property files.
     *
     * @param logger The logger to be used for debug output
     * @param htmlUtils The utilities used to perform some string manipulation
     * @param context The properties and environment
     */
    @Inject
    private SageTvPublisher(LoggerInterface logger, HtmlUtilsInterface htmlUtils, CatchupContextInterface context)
    {
        this.qualifier = context.getOnlineVideoPropertiesSuffix();
        this.onlineVideoPropertiesDir = context.getOnlineVideoPropertiesDir();
      this.context = context;
        this.podcastBaseUrl = context.getPodcastBase();
        checkNotNull(qualifier);
        checkNotNull(onlineVideoPropertiesDir);
        this.logger = logger;
        this.htmlUtils = htmlUtils;
    }

    /**
     * This method removes the any created online video property files.
     *
     * @return true if the property files were deleted
     */
    public boolean unpublish() throws Exception {
        logger.info("Removing online video files");

        boolean deleteLink = true;
        boolean deleteLabel = true;


        String linkFileName = getLinkFile(qualifier);
        String labelFileName = getLabelFile(qualifier);

        File linkFile = new File(linkFileName);
        if (linkFile.exists()) {
            deleteLink = linkFile.delete();
        }

        File labelFile = new File(labelFileName);
        if (labelFile.exists()) {
            deleteLabel = labelFile.delete();
        }

        return deleteLink && deleteLabel;
    }

    /**
     * Returns a full path name for the <code>CustomOnlineVideoLinks</code> properties file.
     *
     * @param qualifier the qualifier used to distinguish different <code>CustomOnlineVideoLinks</code> files
     *
     * @return the full file path
     */
    private String getLinkFile(String qualifier) {
        if (!qualifier.isEmpty()) {
            qualifier = "_" + qualifier;
        }
        return onlineVideoPropertiesDir + File.separator + "CustomOnlineVideoLinks" + qualifier + ".properties";
    }

    /**
     * Returns a full path name for the <code>CustomOnlineVideoUIText</code> properties file.
     *
     * @param qualifier the qualifier used to distinguish different <code>CustomOnlineVideoUIText</code> files
     *
     * @return the full file path
     */
    private String getLabelFile(String qualifier) {
        if (!qualifier.isEmpty()) {
            qualifier = "_" + qualifier;
        }
        return onlineVideoPropertiesDir + File.separator + "CustomOnlineVideoUIText" + qualifier + ".properties";
    }


    /**
     * Takes a catalog of online video meta data and exports its as SageTV custom online video
     * property files.
     *
     * @param catalog the catalog of online video meta data
     *
     * @throws Exception if the property files cannot be created.
     */
    public void publish(Catalog catalog)  {
        try {

            logger.info("Publishing new catalog to SageTV");

            String linkFile = getLinkFile(qualifier);
            String labelFile = getLabelFile(qualifier);

            logger.info("Publishing to " + linkFile);
            logger.info("Publishing to " + labelFile);

            PropertiesFile links = new PropertiesFile(linkFile, false);
            PropertiesFile labels = new PropertiesFile(labelFile, false);

            links.clear();
            labels.clear();

          Root root = catalog.getRoot();

            logger.info("Online adding root " + root.getId());

            addSource(root, links, labels);

          for (String categoryId : root.getSubCategories()) {
            SubCategory category = catalog.getSubcategory(categoryId);
            if (category == null) {
                category = catalog.getSource(categoryId);
            }

            if (category != null) {
              logger.info("Online adding root cat " + category.getId());
              boolean isSearch = category.getId().endsWith("/Search");
              addDynamicSource(category, links, labels, isSearch);
            }
          }

          for (Source source : catalog.getSources()) {
              logger.info("Online adding source " + source.getId());

              if (source.getSourceId().equals("Test")) {
                  continue;
              }

            addSource(source, links, labels);

            for (String categoryId : source.getSubCategories()) {
              SubCategory category = catalog.getSubcategory(categoryId);
              if (category != null) {
                logger.info("Online adding source cat " + category.getId());
                boolean isSearch = category.getId().endsWith("/Search");
                addDynamicSource(category, links, labels, isSearch);
              }
            }
          }

            links.commit(linkFile, new LinksPropertyLayout());
            labels.commit(labelFile, new LabelsPropertyLayout());

            logger.info("Published new catalog to SageTV");

        } catch (Exception e) {
          String message = e.getMessage();
          if (message == null) {
            message = e.getClass().getSimpleName();
          }
          Category root = catalog.getRoot();
          if (root != null) {
            root.addError("FATAL", "Failed to publish to SageTV: " + message);
          }
            logger.error("Failed to publish to SageTV", e);
        }

    }

    private void addDynamicSource(SubCategory programme, PropertiesFile links, PropertiesFile labels, boolean isSearch) {
        String id = programme.getId();
        String sourceId = programme.getId();
        String parentId = programme.getParentId();
        String name = programme.getShortName();
        String description = programme.getLongName();
        String iconUrl = programme.getIconUrl();
        String url = programme.getPodcastUrl();

        if (iconUrl != null && iconUrl.startsWith("/")) {
          iconUrl = podcastBaseUrl + iconUrl;
        }

      if (url != null && url.startsWith("/")) {
        url = podcastBaseUrl + url;
      }

        id = htmlUtils.makeIdSafe(id);
        parentId = htmlUtils.makeIdSafe(parentId);

        if ((iconUrl != null) && (!iconUrl.isEmpty())) {
            labels.setProperty("Category/" + id + "/ThumbURL", iconUrl);
            labels.setProperty("Source/" + "xPodcast" + id + "/ThumbURL", iconUrl);
        }
        if ((description != null) && (!description.isEmpty())) {
            labels.setProperty("Category/" + id + "/LongName", description);
            labels.setProperty("Source/" + "xPodcast" + id + "/LongName", description);
        }
        if ((name != null) && (!name.isEmpty())) {
            labels.setProperty("Category/" + id + "/ShortName", name);
            labels.setProperty("Source/" + "xPodcast" + id + "/ShortName", name);
        }

        String result = "";
        if (!parentId.isEmpty()) {
            result += "xPodcast" + parentId;
        } else {
            result += "xPodcast" + id;
        }

        result += ";" + url;

        links.setProperty("xFeedPodcastCustom/" + id, result);

        if (isSearch) {
          links.setProperty(id + "/IsSearch", "true");
          links.setProperty(id + "/URLSearchPrefix", podcastBaseUrl + "/search?sourceId="+ programme.getParentId() + ";text=");
          links.setProperty(id + "/URLSearchPostfix", ";type=xml");
        }
    }

    private void addDynamicSubcategory(Source source, Category programme, PropertiesFile links, PropertiesFile labels) {
        String id = programme.getId();
        String parentId = source.getParentId();
        String name = programme.getShortName();
        String description = programme.getLongName();
        String iconUrl = programme.getIconUrl();
        String url = programme.getPodcastUrl();
//        Set<String> otherParentIds = programme.getOtherParentIds();

      if (iconUrl != null && iconUrl.startsWith("/")) {
        iconUrl = podcastBaseUrl + iconUrl;
      }

      if (url != null && url.startsWith("/")) {
        url = podcastBaseUrl + url;
      }

      id = htmlUtils.makeIdSafe(id);
        parentId = htmlUtils.makeIdSafe(parentId);

        if ((iconUrl != null) && (!iconUrl.isEmpty())) {
            labels.setProperty("Category/" + id + "/ThumbURL", iconUrl);
            labels.setProperty("Source/" + "xPodcast" + id + "/ThumbURL", iconUrl);
        }
        if ((description != null) && (!description.isEmpty())) {
            labels.setProperty("Category/" + id + "/LongName", description);
            labels.setProperty("Source/" + "xPodcast" + id + "/LongName", description);
        }
        if ((name != null) && (!name.isEmpty())) {
            labels.setProperty("Category/" + id + "/ShortName", name);
            labels.setProperty("Source/" + "xPodcast" + id + "/ShortName", name);
        }

        String result = "";
        if (!parentId.isEmpty()) {
            result += "xPodcast" + parentId;
        }

//        for (String other : otherParentIds) {
//            if (!result.isEmpty()) {
//                result += ",";
//            }
//            result += "xPodcast" + htmlUtils.makeIdSafe(other);
//        }
        result += ";" + url;

        links.setProperty("xFeedPodcastCustom/" + id, result);
    }

    /**
     * Adds a final subcategory aka programme category containing the podcast link which will sageTV
     * will expand to a list of videos.
     *
     * @param programme the TV programme which this podcast will provide episodes for
     * @param links the custom online video property file containing the podcast URL
     * @param labels the custom online video property file containing associated UI text
     */
    private void addProgramme(Programme programme, PropertiesFile links, PropertiesFile labels) {
        String id = programme.getId();
        String parentId = programme.getParentId();
        String name = programme.getShortName();
        String description = programme.getLongName();
        String iconUrl = programme.getIconUrl();
        String url = programme.getPodcastUrl();
        Set<String> otherParentIds = programme.getOtherParentIds();

      if (iconUrl != null && iconUrl.startsWith("/")) {
        iconUrl = podcastBaseUrl + iconUrl;
      }

      if (url != null && url.startsWith("/")) {
        url = podcastBaseUrl + url;
      }

      id = htmlUtils.makeIdSafe(id);
        parentId = htmlUtils.makeIdSafe(parentId);

        if ((iconUrl != null) && (!iconUrl.isEmpty())) {
            labels.setProperty("Category/" + id + "/ThumbURL", iconUrl);
            labels.setProperty("Source/" + "xPodcast" + id + "/ThumbURL", iconUrl);
        }
        if ((description != null) && (!description.isEmpty())) {
            labels.setProperty("Category/" + id + "/LongName", description);
            labels.setProperty("Source/" + "xPodcast" + id + "/LongName", description);
        }
        if ((name != null) && (!name.isEmpty())) {
            labels.setProperty("Category/" + id + "/ShortName", name);
            labels.setProperty("Source/" + "xPodcast" + id + "/ShortName", name);
        }

        String result = "";
        if (!parentId.isEmpty()) {
            result += "xPodcast" + parentId;
        }

        for (String other : otherParentIds) {
            if (!result.isEmpty()) {
                result += ",";
            }
            result += "xPodcast" + htmlUtils.makeIdSafe(other);
        }
        result += ";" + url;

        links.setProperty("xFeedPodcastCustom/" + id, result);
    }
    /**
     * Adds a final subcategory aka programme category containing the podcast link which will sageTV
     * will expand to a list of videos.
     *
     * @param programme the programme which this podcast will provide controls for
     * @param episode the episode which this podcast will provide controls for
     * @param links the custom online video property file containing the podcast URL
     * @param labels the custom online video property file containing associated UI text
     */
    private void addEpisode(Programme programme, Episode episode, PropertiesFile links, PropertiesFile labels) {
        String id = episode.getId();
        String parentId = programme.getId();

        logger.info("ADDING " + id + " podcast to " + parentId);

        String name = episode.getPodcastTitle();
        String description = episode.getPodcastTitle();
        String iconUrl = episode.getIconUrl();
        String url = episode.getPodcastUrl();
        Set<String> otherParentIds = new HashSet<String>();

      if (iconUrl != null && iconUrl.startsWith("/")) {
        iconUrl = podcastBaseUrl + iconUrl;
      }

      if (url != null && url.startsWith("/")) {
        url = podcastBaseUrl + url;
      }

      id = htmlUtils.makeIdSafe(id);
        parentId = htmlUtils.makeIdSafe(parentId);

        if ((iconUrl != null) && (!iconUrl.isEmpty())) {
            labels.setProperty("Category/" + id + "/ThumbURL", iconUrl);
            labels.setProperty("Source/" + "xPodcast" + id + "/ThumbURL", iconUrl);
        }
        if ((description != null) && (!description.isEmpty())) {
            labels.setProperty("Category/" + id + "/LongName", description);
            labels.setProperty("Source/" + "xPodcast" + id + "/LongName", description);
        }
        if ((name != null) && (!name.isEmpty())) {
            labels.setProperty("Category/" + id + "/ShortName", name);
            labels.setProperty("Source/" + "xPodcast" + id + "/ShortName", name);
        }

        String result = "";
        if (!parentId.isEmpty()) {
            result += "xPodcast" + parentId;
        }

        for (String other : otherParentIds) {
            if (!result.isEmpty()) {
                result += ",";
            }
            result += "xPodcast" + htmlUtils.makeIdSafe(other);
        }
        result += ";" + url;

        String existing = links.getProperty("xFeedPodcastCustom/" + id);

        if (existing != null) {
            System.err.println("Found " + links.getProperty("xFeedPodcastCustom/" + id));
            id = id + "," + existing;
        }

        links.setProperty("xFeedPodcastCustom/" + id, result);
    }



    /**
     * Adds a top level online video source the custom online video property files.
     *
     * @param source the online source of the online videos
     * @param links the custom online video property file containing the podcast URL
     * @param labels the custom online video property file containing associated UI text
     */
    private void addSource(SubCategory source, PropertiesFile links, PropertiesFile labels) {

        String id = source.getId();
        String name = source.getShortName();
        String description = source.getLongName();

        id = htmlUtils.makeIdSafe(id);
        String sourceId = "xPodcast" + id;
        logger.info("Online add source : " + sourceId);
        labels.setProperty("Source/" + sourceId + "/ShortName", name);
        labels.setProperty("Source/" + sourceId + "/LongName", description);

        String sources = links.getProperty("CustomSources", "");
        if (!sources.contains(sourceId)) {
            if (sources.isEmpty()) {
                sources = sourceId;
            } else {
                sources = sources + "," + sourceId;
            }
        }
        links.setProperty("CustomSources", sources);
    }

    /**
     * Adds a subcategory to the custom online video property files.
     *
     * @param category the subcategory of online videos
     * @param links the custom online video property file containing the podcast URL
     * @param labels the custom online video property file containing associated UI text
     */
    private void addSubCategory(Category category, PropertiesFile links, PropertiesFile labels) {
        String parentId = category.getParentId();
        String id = category.getId();
        String name = category.getShortName();
        String description = category.getLongName();
        String iconUrl = category.getIconUrl();

      if (iconUrl != null && iconUrl.startsWith("/")) {
        iconUrl = podcastBaseUrl + iconUrl;
      }

      parentId = htmlUtils.makeIdSafe(parentId);
        id = htmlUtils.makeIdSafe(id);

        links.setProperty("xFeedPodcastCustom/" + id, "xPodcast" + parentId + ";xURLNone");
        links.setProperty(id + "/IsCategory", "true");
        links.setProperty(id + "/CategoryName", "xPodcast" + id);

        labels.setProperty("Category/" + id + "/ShortName", name);
        labels.setProperty("Category/" + id + "/LongName", description);
        if (iconUrl != null && !iconUrl.isEmpty()) {
            labels.setProperty("Category/" + id + "/ThumbURL", iconUrl);
            labels.setProperty("Source/" + "xPodcast" + id + "/ThumbURL", iconUrl);
        }
        labels.setProperty("Source/" + "xPodcast" + id + "/ShortName", name);
        labels.setProperty("Source/" + "xPodcast" + id + "/LongName", description);
    }

    private void addProgrammeSubCategory(Programme category, PropertiesFile links, PropertiesFile labels) {
        String parentId = category.getParentId();
        String id = category.getId();
        String name = category.getShortName();
        String description = category.getLongName();
        String iconUrl = category.getIconUrl();

      if (iconUrl != null && iconUrl.startsWith("/")) {
        iconUrl = podcastBaseUrl + iconUrl;
      }

      parentId = htmlUtils.makeIdSafe(parentId);
        id = htmlUtils.makeIdSafe(id);

        String result = "";
        if (!parentId.isEmpty()) {
            result += "xPodcast" + parentId;
        }

        Set<String> otherParentIds = category.getOtherParentIds();

        for (String other : otherParentIds) {
            if (!result.isEmpty()) {
                result += ",";
            }
            result += "xPodcast" + htmlUtils.makeIdSafe(other);
        }
        result += ";xURLNone";


        links.setProperty("xFeedPodcastCustom/" + id, result);
        links.setProperty(id + "/IsCategory", "true");
        links.setProperty(id + "/CategoryName", "xPodcast" + id);

        labels.setProperty("Category/" + id + "/ShortName", name);
        labels.setProperty("Category/" + id + "/LongName", description);
        if (iconUrl != null && !iconUrl.isEmpty()) {
            labels.setProperty("Category/" + id + "/ThumbURL", iconUrl);
            labels.setProperty("Source/" + "xPodcast" + id + "/ThumbURL", iconUrl);
        }
        labels.setProperty("Source/" + "xPodcast" + id + "/ShortName", name);
        labels.setProperty("Source/" + "xPodcast" + id + "/LongName", description);
    }

}

