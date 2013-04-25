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
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;

import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.model.Category;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.SubCategory;
import uk.co.mdjcox.sagetv.model.Source;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.PropertiesFile;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * This class can take a {@link Catalog} of media {@link Source}, categories {@link SubCategory},
 * programmes {@link Programme} metadata and publish as a SageTV online video source.
 *
 * A SageTV custom online video source is two properties files contains in the
 * <code>OnlineVideos</code> subdirectory of the <code>STV</code> directory.
 */
@Singleton
public class Publisher {

  /** The logger to be used for debug output */
  private Logger logger;
  /** The utilities used to perform some string manipulation */
  private HtmlUtilsInterface htmlUtils;
  /** The qualifier added to the custom SageTV online video link and UIText files */
  private String qualifier;
  /** The SageTV STV directory */
  private String STV;

  /**
   * Creates a new instance which can be used to create new online video property files.
   *
   * @param logger The logger to be used for debug output
   * @param htmlUtils The utilities used to perform some string manipulation
   * @param qualifier The qualifier added to the custom SageTV online video link and UIText files
   * @param stvDirectory The SageTV STV directory
   */
  @Inject
  private Publisher(Logger logger, HtmlUtilsInterface htmlUtils, @Assisted("qualifier") String qualifier, @Assisted("STV") String stvDirectory)
  {
    this.logger = logger;
    this.qualifier = qualifier;
    this.STV = stvDirectory;
    this.htmlUtils = htmlUtils;
  }

  /**
   * This method removes the any created online video property files.
   *
   * @throws Exception if there was a problem deleting the files
   */
  public void unpublish() throws Exception {
    boolean success = true;
    String linkFileName = getLinkFile(qualifier);
    String labelFileName = getLabelFile(qualifier);

    File linkFile = new File(linkFileName);
    if (linkFile.exists()) {
      success = success && linkFile.delete();
    }

    File labelFile = new File(labelFileName);
    if (labelFile.exists()) {
      success = success && labelFile.delete();
    }

    if (!success) {
      throw new Exception("Unable to delete online services files");
    }

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
    return getRoot() + File.separator + "CustomOnlineVideoLinks" + qualifier + ".properties";
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
    return getRoot() + File.separator + "CustomOnlineVideoUIText" + qualifier + ".properties";
  }

  /**
   * Returns the full path for the STV online videos directory
   *
   * @return full path of the online videos directory
   */
  private String getRoot() {
    STV = STV.replace(File.separatorChar, '/');
    STV = STV.replaceAll("/[^/]*.xml", "");
    STV = STV.trim();
    String root = STV + File.separator + "OnlineVideos";
    return root;
  }

  /**
   * Takes a catalog of online video meta data and exports its as SageTV custom online video
   * property files.
   *
   * @param catalog the catalog of online video meta data
   *
   * @throws Exception if the property files cannot be created.
   */
  public void publish(Catalog catalog) throws Exception {
    Collection<Category> categories = catalog.getCategories();

    String linkFile = getLinkFile(qualifier);
    String labelFile = getLabelFile(qualifier);

    logger.info("Publishing to " + linkFile);
    logger.info("Publishing to " + labelFile);

    PropertiesFile links = new PropertiesFile(linkFile, false);
    PropertiesFile labels = new PropertiesFile(labelFile, false);

    links.clear();
    labels.clear();

    for (Category category : categories) {
      if (category.isRoot()) {
        continue;
      } else if (category.isSource()) {
        logger.info("Online adding source " + category.getId());
        addSource(category.getId(), category.getShortName(), category.getLongName(), links, labels);
      } else if (category.isProgrammeCategory()) {
        logger.info("Online adding final category " + category.getId());
        addCategory(category.getId(), category.getShortName(), category.getLongName(),
                    category.getIconUrl(), labels);
        addPodcast(category.getId(), category.getParentId(), ((Programme) category).getPodcastUrl(),
                   links, ((SubCategory) category).getOtherParentIds());
      } else if (category.isSubCategory()) {
        logger.info("Online adding non-final category " + category.getId());
        addSubCategory(category.getParentId(), category.getId(), category.getShortName(),
                       category.getLongName(),
                       category.getIconUrl(), links, labels);
      } else {
        logger.error(category.getId() + " was not handled");
      }
    }

    links.commit(linkFile, new LinksPropertyLayout());
    labels.commit(labelFile, new LabelsPropertyLayout());

  }

  /**
   * Adds a podcast to the custom online video property files.
   *
   * @param category a unique id for the podcast
   * @param subCat the primary category this podcast belongs to
   * @param url the podcast URL
   * @param links the <code>CustomOnlineVideoLinks</code> file to add the podcast to
   * @param otherSubCats other categories this podcast belongs to
   */
  private void addPodcast(String category, String subCat, String url, PropertiesFile links,
                          List<String> otherSubCats) {
    category = htmlUtils.makeIdSafe(category);
    subCat = htmlUtils.makeIdSafe(subCat);

    String result = "";
    if (!subCat.isEmpty()) {
      result += "xPodcast" + subCat;
    }

    for (String other : otherSubCats) {
      if (!result.isEmpty()) {
        result += ",";
      }
      result += "xPodcast" + htmlUtils.makeIdSafe(other);
    }
    result += ";" + url;

    links.setProperty("xFeedPodcastCustom/" + category, result);
  }

  /**
   *
   * @param callSign
   * @param name
   * @param description
   * @param categoryIconUrl
   * @param labels
   */
  private void addCategory(String callSign, String name, String description, String categoryIconUrl,
                           PropertiesFile labels) {
    callSign = htmlUtils.makeIdSafe(callSign);
    if ((categoryIconUrl != null) && (!categoryIconUrl.isEmpty())) {
      labels.setProperty("Category/" + callSign + "/ThumbURL", categoryIconUrl);
      labels.setProperty("Source/" + "xPodcast" + callSign + "/ThumbURL", categoryIconUrl);
    }
    if ((description != null) && (!description.isEmpty())) {
      labels.setProperty("Category/" + callSign + "/LongName", description);
      labels.setProperty("Source/" + "xPodcast" + callSign + "/LongName", description);
    }
    if ((name != null) && (!name.isEmpty())) {
      labels.setProperty("Category/" + callSign + "/ShortName", name);
      labels.setProperty("Source/" + "xPodcast" + callSign + "/ShortName", name);
    }
  }

  private void addSource(String category, String categoryShortName, String categoryLongName,
                         PropertiesFile links, PropertiesFile labels) {
    category = htmlUtils.makeIdSafe(category);
    String sourceId = "xPodcast" + category;
    logger.info("Online add source : " + sourceId);
    labels.setProperty("Source/" + sourceId + "/ShortName", categoryShortName);
    labels.setProperty("Source/" + sourceId + "/LongName", categoryLongName);

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

  private void addSubCategory(
      String parentId,
      String subCatId,
      String subCatTitle,
      String subCatDescription,
      String iconUrl,
      PropertiesFile links, PropertiesFile labels) {
    parentId = htmlUtils.makeIdSafe(parentId);
    subCatId = htmlUtils.makeIdSafe(subCatId);

    logger.info("Online adding subcategory: " + subCatId);

    links.setProperty("xFeedPodcastCustom/" + subCatId, "xPodcast" + parentId + ";xURLNone");
    links.setProperty(subCatId + "/IsCategory", "true");
    links.setProperty(subCatId + "/CategoryName", "xPodcast" + subCatId);

    labels.setProperty("Category/" + subCatId + "/ShortName", subCatTitle);
    labels.setProperty("Category/" + subCatId + "/LongName", subCatDescription);
    if (iconUrl != null && !iconUrl.isEmpty()) {
      labels.setProperty("Category/" + subCatId + "/ThumbURL", iconUrl);
      labels.setProperty("Source/" + "xPodcast" + subCatId + "/ThumbURL", iconUrl);
    }
    labels.setProperty("Source/" + "xPodcast" + subCatId + "/ShortName", subCatTitle);
    labels.setProperty("Source/" + "xPodcast" + subCatId + "/LongName", subCatDescription);
  }

}

