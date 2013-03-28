package uk.co.mdjcox.model;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 14/03/13
 * Time: 07:25
 * To change this template use File | Settings | File Templates.
 */
public class Programme extends SubCategory implements Cloneable {

    private HashMap<String, Episode> episodes = new HashMap<String, Episode>();

    public Programme(String id, String shortName, String longName, String serviceUrl, String iconUrl, String parentId) {
        super(id, shortName, longName, serviceUrl, iconUrl, parentId);
    }

    public  void addEpisode(Episode episode) {
        episodes.put(episode.getServiceUrl(), episode);
    }

    public  void removeEpisode(Episode episode) {
        episodes.remove(episode.getServiceUrl());
    }

    public HashMap<String, Episode> getEpisodes() {
        return episodes;
    }
}
