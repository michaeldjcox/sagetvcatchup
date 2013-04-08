package uk.co.mdjcox.plugins;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import uk.co.mdjcox.model.Episode;
import uk.co.mdjcox.model.Programme;
import uk.co.mdjcox.model.Source;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 05/04/13
 * Time: 18:42
 * To change this template use File | Settings | File Templates.
 */
public class Plugin {

    private String sourceId;
    private String base;
    private SourceScript sourceScript;
    private ProgrammesScript programmesScript;
    private EpisodesScript episodesScript;
    private EpisodeScript episodeScript;
    private Source source;
    @Inject
    private ScriptFactory scriptFactory;

    @Inject
    public Plugin(@Assisted("id") String id, @Assisted("base") String base) {
        this.sourceId = id;
        this.base = base;
    }

    public void init() {
        sourceScript = scriptFactory.createSourceScript(sourceId, base);
        programmesScript = scriptFactory.createProgrammesScript(base);
        episodesScript = scriptFactory.createEpisodesScript(base);
        episodeScript = scriptFactory.createEpisodeScript(base);
        source = sourceScript.getSource();
    }

    public Source getSource() {
        return source;
    }

    public Collection<Programme> getProgrammes() {
        return programmesScript.getProgrammes(source);  //To change body of created methods use File | Settings | File Templates.
    }

    public void getEpisodes(Programme programme) {
        episodesScript.getEpisodes(programme);
    }

    public void getEpisode(Programme programme, Episode episode) {
        episodeScript.getEpisode(programme, episode);
    }

    @Override
    public String toString() {
        return "Plugin:" + source;
    }
}
