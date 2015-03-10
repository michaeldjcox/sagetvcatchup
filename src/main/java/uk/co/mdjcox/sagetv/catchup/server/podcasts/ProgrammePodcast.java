package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.Catalog;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.SubCategory;
import uk.co.mdjcox.sagetv.utils.HtmlUtilsInterface;
import uk.co.mdjcox.sagetv.utils.RssBuilder;

import java.util.*;

/**
 * Created by michael on 07/10/14.
 */
public class ProgrammePodcast extends OnDemandPodcast {

    private final HtmlUtilsInterface htmlUtils;
    private final SubCategory programme;
    private final Catalog catalog;

    public ProgrammePodcast(String baseUrl, Catalog catalog, SubCategory category, HtmlUtilsInterface htmlUtils) {
        super(baseUrl);
        this.htmlUtils = htmlUtils;
        this.catalog = catalog;
        this.programme = category;
    }

    public String buildPage() {

        final String shortName = htmlUtils.makeContentSafe(programme.getShortName());
        final String longName = htmlUtils.makeContentSafe(programme.getLongName());
        String url = programme.getServiceUrl();
        String iconUrl = programme.getIconUrl();
      if (iconUrl != null && iconUrl.startsWith("/")) {
        iconUrl = getPodcastBaseUrl() + iconUrl;
      }

      if (url != null && url.startsWith("/")) {
        url = getPodcastBaseUrl() + url;
      }

        RssBuilder builder = new RssBuilder();
        builder.startDocument(shortName, longName, url);
        if (iconUrl != null) {
          builder.addImage(iconUrl, shortName, url);
        }
        Set<String> episodes = programme.getEpisodes();
        EpisodeComparator comparator = new EpisodeComparator();
        TreeSet<Episode> sortedEpisodes = new TreeSet<Episode>(comparator);
        for (String episodeId : episodes) {
          Episode episode = catalog.getEpisode(episodeId);
          sortedEpisodes.add(episode);
        }
        for (Episode episode : sortedEpisodes) {
            StringBuilder titleBuilder = new StringBuilder("");

          if (!episode.getSeries().isEmpty() && !episode.getSeries().equals("0")) {
            titleBuilder.append(episode.getSeries());
          }
          if (!episode.getEpisode().isEmpty() && !episode.getEpisode().equals("0")) {
            if (titleBuilder.length() != 0) {
              titleBuilder.append(".");
            }
            titleBuilder.append(episode.getEpisode());
            titleBuilder.append(": ");
          } else {
            if (titleBuilder.length() != 0) {
              titleBuilder.append(": ");
            }
          }
          titleBuilder.append(episode.getEpisodeTitle());

          StringBuilder descBuilder = new StringBuilder("");
          descBuilder.append("<i>");
          descBuilder.append(episode.getAirDate());
          descBuilder.append(' ');
          descBuilder.append(episode.getAirTime());
          descBuilder.append("</i>");
          final String availDate = episode.getRemovalDate();
          if (availDate != null && !availDate.isEmpty()) {
            descBuilder.append(" Until: ");
            descBuilder.append(availDate);
          }
          descBuilder.append("<br/>");
          descBuilder.append(episode.getDescription());
          descBuilder.append("<br/>");


          String desc = htmlUtils.makeContentSafe(descBuilder.toString());
          String title = htmlUtils.makeContentSafe(titleBuilder.toString());
    //      Episode icon - takes too much space
          String episodeIconUrl = episode.getIconUrl();
          if (episodeIconUrl != null && episodeIconUrl.startsWith("/")) {
            episodeIconUrl = getPodcastBaseUrl() + episodeIconUrl;
          }

          if (!episode.getPodcastUrl().startsWith("/control")) {
            String controlUrl = episode.getPodcastUrl();
            builder.addVideoItem(title, desc, controlUrl, episodeIconUrl);
          } else {
            String controlUrl=getPodcastBaseUrl() +  "/control?id=" + episode.getId() + ";type=xml";
            builder.addCategoryItem(title, desc, controlUrl, "");
          }



        }
        builder.stopDocument();
        return builder.toString();
    }

    @Override
    public String getUri() {
        return "programme?id=" + programme.getId() + ";type=xml";
    }

}
