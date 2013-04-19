package uk.co.mdjcox.model;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA. User: michael Date: 14/03/13 Time: 07:09 To change this template use
 * File | Settings | File Templates.
 */
public abstract class Category {

  private String id="";
  private String shortName="";
  private String longName="";
  private String serviceUrl="";
  private String iconUrl="";
  private String parentId="";

  protected Category(String id, String shortName, String longName, String serviceUrl,
                     String iconUrl, String parentId) {
    this.id = checkNotNull(id);
    this.shortName = checkNotNull(shortName);
    this.longName = checkNotNull(longName);
    this.serviceUrl = checkNotNull(serviceUrl);
    this.iconUrl = checkNotNull(iconUrl);
    this.parentId = checkNotNull(parentId);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = checkNotNull(id);
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = checkNotNull(shortName);
  }

  public String getLongName() {
    return longName;
  }

  public void setLongName(String longName) {
    this.longName = checkNotNull(longName);
  }

  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = checkNotNull(serviceUrl);
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = checkNotNull(iconUrl);
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = checkNotNull(parentId);
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
