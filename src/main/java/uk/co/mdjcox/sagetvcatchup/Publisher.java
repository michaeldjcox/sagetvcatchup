package uk.co.mdjcox.sagetvcatchup;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;

import uk.co.mdjcox.model.Catalog;
import uk.co.mdjcox.model.Category;
import uk.co.mdjcox.model.SubCategory;
import uk.co.mdjcox.utils.CustomOnlineLabelsPropertyLayout;
import uk.co.mdjcox.utils.CustomOnlineLinksPropertyLayout;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.PropertiesFile;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;
import java.util.Collection;
import java.util.List;

@Singleton
public class Publisher {

    private Logger logger;
    private PropertiesInterface props;
    private HtmlUtilsInterface htmlUtils;

    @Inject
    private Publisher(Logger logger, PropertiesInterface props, HtmlUtilsInterface htmlUtils) throws Exception {
        this.logger = logger;
        this.props = props;
        this.htmlUtils = htmlUtils;
    }

    public void unpublish() throws Exception {
        String file = props.getProperty("fileName", "Catchup");

        boolean success = true;
        String linkFileName = getLinkFile(file);
        String labelFileName = getLabelFile(file);

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

    private String getLinkFile(String file) {
        if (!file.isEmpty()) {
            file = "_" + file;
        }
        return getRoot() + File.separator + "CustomOnlineVideoLinks" + file + ".properties";
    }

    private String getLabelFile(String file) {
        if (!file.isEmpty()) {
            file = "_" + file;
        }
        return getRoot() + File.separator + "CustomOnlineVideoUIText" + file + ".properties";
    }

    private String getRoot() {
        String STV = props.getProperty("STV", "C:\\Program Files\\SageTV\\SageTV\\STVs\\SageTV7\\SageTV7.xml");
        STV = STV.replace(File.separatorChar, '/');
//        STV = STV.replaceAll(".*STVs/", "");
        STV = STV.replaceAll("/[^/]*.xml", "");
        STV = STV.trim();
        String root = STV + File.separator + "OnlineVideos";
        return root;
    }


    public void publish(Catalog catalog) throws Exception {
        String file = props.getProperty("fileName", "Catchup");
        Collection<Category> categories = catalog.getCategories();

        String linkFile = getLinkFile(file);
        String labelFile = getLabelFile(file);

        logger.info("Publishing to " + linkFile);
        logger.info("Publishing to " + labelFile);


        PropertiesFile links = new PropertiesFile(linkFile, false);
        PropertiesFile labels = new PropertiesFile(labelFile, false);

        links.clear();
        labels.clear();

        int port = props.getInt("podcasterPort", 8081);

        for (Category category : categories) {
            if (category.isRoot()) {
                continue;
            } else if (category.isSource()) {
                logger.info("Online adding source " + category.getId());
                addSource(category.getId(), category.getShortName(), category.getLongName(), links, labels);
            } else if (category.isProgrammeCategory()) {
                logger.info("Online adding final category " + category.getId());
                addCategory(category.getId(), category.getShortName(), category.getLongName(), category.getIconUrl(), labels);
                addPodcast(category.getId(), category.getParentId(), "http://localhost:"+port+"/" + category.getId(), links, ((SubCategory) category).getOtherParentIds());
            } else if (category.isSubCategory()) {
                logger.info("Online adding non-final category " + category.getId());
                addSubCategory(category.getParentId(), category.getId(), category.getShortName(), category.getLongName(),
                        category.getIconUrl(), links, labels);
            } else {
                logger.error(category.getId() + " was not handled");
            }
        }

        links.commit(linkFile, new CustomOnlineLinksPropertyLayout());
        labels.commit(labelFile, new CustomOnlineLabelsPropertyLayout());

    }

    private void addPodcast(String category, String subCat, String url, PropertiesFile links, List<String> otherSubCats) {
        category = htmlUtils.makeIdSafe(category);
        subCat = htmlUtils.makeIdSafe(subCat);

        String result = "";
        if (!subCat.isEmpty()) {
            result += "xPodcast" + subCat;
        }

        for (String other : otherSubCats) {
            if (!result.isEmpty()) result += ",";
            result += "xPodcast" + htmlUtils.makeIdSafe(other);
        }
        result += ";" + url;

        links.setProperty("xFeedPodcastCustom/" + category, result);
    }

    private void addCategory(String callSign, String name, String description, String categoryIconUrl, PropertiesFile labels) {
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

    private void addSource(String category, String categoryShortName, String categoryLongName, PropertiesFile links, PropertiesFile labels) {
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

