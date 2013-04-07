package uk.co.mdjcox.scripts;

import uk.co.mdjcox.model.Source;

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

    public Plugin(String sourceId, String base) {
        this.sourceId = sourceId;
        this.base = base;
        findScripts();
    }

    private void findScripts() {



    }

    public Source getSource() {
        return sourceScript.getSource();
    }

    public ProgrammesScript getProgrammesScript() {
        return programmesScript.getProgrammes();
    }

    public EpisodesScript getEpisodesScript() {
        return episodesScript;
    }

    public EpisodeScript getEpisodeScript() {
        return episodeScript;
    }
}
