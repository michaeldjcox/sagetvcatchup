package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.RssBuilder;

import java.util.*;

/**
 * Created by michael on 07/10/14.
 */
public class ProgrammePodcast extends AbstractPodcast {

    private final HtmlUtilsInterface htmlUtils;
    private final Programme service;
    private final Catalog catalog;
    private String page;

    public ProgrammePodcast(String baseUrl, Catalog catalog, Programme category, HtmlUtilsInterface htmlUtils) {
        super(baseUrl);
        this.htmlUtils = htmlUtils;
        this.catalog = catalog;
        this.service = category;
        page = buildPage();
    }

    public String buildPage() {

        final String shortName = htmlUtils.makeContentSafe(service.getShortName());
        final String longName = htmlUtils.makeContentSafe(service.getLongName());
        String url = service.getServiceUrl();
        String iconUrl = service.getIconUrl();
      if (iconUrl != null && iconUrl.startsWith("/")) {
        iconUrl = getPodcastBaseUrl() + iconUrl;
      }

      if (url != null && url.startsWith("/")) {
        url = getPodcastBaseUrl() + url;
      }

        RssBuilder builder = new RssBuilder();
        builder.startDocument(shortName, longName, url);
        builder.addImage(iconUrl, shortName, url);
        Programme programme = (Programme) service;
        Set<String> episodes = programme.getEpisodes();
        EpisodeComparator comparator = new EpisodeComparator();
        TreeSet<Episode> sortedEpisodes = new TreeSet<Episode>(comparator);
        for (String episodeId : episodes) {
          Episode episode = catalog.getEpisode(episodeId);
          sortedEpisodes.add(episode);
        }
        for (Episode episode : sortedEpisodes) {
            final String title = htmlUtils.makeContentSafe(episode.getPodcastTitle());
            String desc = htmlUtils.makeContentSafe(episode.getDescription());
          desc += "<br/><br/>";
          desc += episode.getOrigAirDate() + " " + episode.getOrigAirTime();
          desc += "<br/>";
          if (!episode.getSeries().isEmpty() && !episode.getSeries().equals("0")) {
            desc += "Series:  " + episode.getSeries() + " ";
          }
          if (!episode.getEpisode().isEmpty() && !episode.getEpisode().equals("0")) {
            desc += "Episode: " + episode.getEpisode();
          }
            String episodeIconUrl = episode.getIconUrl();
            if (episodeIconUrl != null && episodeIconUrl.startsWith("/")) {
              episodeIconUrl = getPodcastBaseUrl() + episodeIconUrl;
            }
            final String controlUrl=getPodcastBaseUrl() +  "/control?id=" + episode.getId() + ";type=xml";
            builder.addCategoryItem(title, desc, controlUrl, episodeIconUrl);
        }
        builder.stopDocument();
        return builder.toString();
    }

    @Override
    public String getUri() {
        return "programme?id=" + service.getId() + ";type=xml";
    }

    @Override
    public String getPage() {
        return page;
    }


}
