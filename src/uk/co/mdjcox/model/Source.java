package uk.co.mdjcox.model;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 12/02/13
 * Time: 07:37
 * To change this template use File | Settings | File Templates.
 */
public class Source extends SubCategory {

    public Source(String id, String shortName, String longName,
                  String serviceUrl, String iconUrl) {
        super(id, shortName, longName, serviceUrl, iconUrl, "");
    }
}
