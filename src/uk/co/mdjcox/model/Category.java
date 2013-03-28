package uk.co.mdjcox.model;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 14/03/13
 * Time: 07:09
 * To change this template use File | Settings | File Templates.
 */
public abstract class Category {
    private String id;
    private String shortName;
    private String longName;
    private String serviceUrl;
    private String iconUrl;
    private String parentId;

    protected Category(String id, String shortName, String longName, String serviceUrl, String iconUrl, String parentId) {
        this.id = id;
        this.shortName = shortName;
        this.longName = longName;
        this.serviceUrl = serviceUrl;
        this.iconUrl = iconUrl;
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPodcastUrl() {
        return "http://localhost:8081/" + getId();

    }

    public boolean isSource() {
        return (this instanceof Source);
    }

    public boolean isSubCategory() {
        return (this instanceof SubCategory);
    }

    public boolean isProgrammeCategory() {
        return (this instanceof Programme);
    }

    public boolean isRoot() {
        return (this instanceof Root);
    }

    @Override
    public String toString() {
        return id;
    }
}
