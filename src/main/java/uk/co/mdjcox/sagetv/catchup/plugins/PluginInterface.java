package uk.co.mdjcox.sagetv.catchup.plugins;

import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.sagetv.model.Source;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by michael on 26/02/15.
 */
public interface PluginInterface {
  void init();

  boolean beginCatalog();

  Collection<Source> getSources();

  Collection<Programme> getProgrammes(Source source, AtomicBoolean stopFlag);

  Collection<Episode> getEpisodes(Source source, Programme programme, AtomicBoolean stopFlag);

  void getEpisode(Source source, Programme programme, Episode episode, AtomicBoolean stopFlag);

  void getCategories(Source source, Map<String, List<String>> categories, AtomicBoolean stopFlag);

  void playEpisode(Recording recording);

  void stopEpisode(Recording recording);

  String getIconUrl(String channel);

  String getPluginId();
}
