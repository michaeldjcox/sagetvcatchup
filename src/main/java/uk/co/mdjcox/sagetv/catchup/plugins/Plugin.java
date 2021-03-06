package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import uk.co.mdjcox.sagetv.catchup.ProgressTracker;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Programme;
import uk.co.mdjcox.sagetv.model.Recording;
import uk.co.mdjcox.sagetv.model.Source;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 05/04/13
 * Time: 18:42
 * To change this template use File | Settings | File Templates.
 */
public class Plugin implements PluginInterface {

  private String pluginId;
  private String base;
  private SourceScript sourceScript;
  private ProgrammesScript programmesScript;
  private EpisodesScript episodesScript;
  private EpisodeScript episodeScript;
  private CategoriesScript categoriesScript;
  private PlayScript playScript;
  private StopScript stopScript;
    private ProgressTracker progressTracker;


    @Inject
  private ScriptFactory scriptFactory;

  @Inject
  public Plugin(@Assisted("id") String id, @Assisted("base") String base) {
    this.pluginId = id;
    this.base = base;
  }

  @Override
  public String getPluginId() {
    return pluginId;
  }

  @Override
  public boolean beginCatalog() {
    return true;
  }

    @Override
    public void setProgressTracker(ProgressTracker progressTracker) {
       this.progressTracker = progressTracker;
    }

    @Override
  public void init() {
    sourceScript = scriptFactory.createSourceScript(pluginId, base);
    programmesScript = scriptFactory.createProgrammesScript(base);
    episodesScript = scriptFactory.createEpisodesScript(base);
    episodeScript = scriptFactory.createEpisodeScript(base);
    categoriesScript = scriptFactory.createCategoriesScript(base);
    playScript = scriptFactory.createPlayScript(base);
    stopScript = scriptFactory.createStopScript(base);
  }

  @Override
  public Collection<Source> getSources() {
    return sourceScript.getSources();
  }

  @Override
  public Collection<Programme> getProgrammes(Source source, AtomicBoolean stopFlag) {
    return programmesScript.getProgrammes(source, stopFlag);
  }

  @Override
  public Collection<Episode> getEpisodes(Source source, Programme programme, AtomicBoolean stopFlag) {
    return episodesScript.getEpisodes(source, programme, stopFlag);
  }

  @Override
  public void getEpisode(Source source, Programme programme, Episode episode, AtomicBoolean stopFlag) {
    episodeScript.getEpisode(source, programme, episode, stopFlag);
  }

  @Override
  public void getCategories(Source source, Map<String, List<String>> categories, AtomicBoolean stopFlag) {
    categoriesScript.getCategories(source, categories, stopFlag);
  }

  @Override
  public void playEpisode(Recording recording) {
    playScript.play(recording);
  }

  @Override
  public void stopEpisode(Recording recording) {
    stopScript.stop(recording);
  }

  public String getIconUrl(String channel) {
    return null;
  }

  @Override
  public String toString() {
    return "Plugin:" + pluginId;
  }
}
