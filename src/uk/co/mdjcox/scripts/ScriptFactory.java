package uk.co.mdjcox.scripts;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 04/04/13
 * Time: 07:49
 * To change this template use File | Settings | File Templates.
 */
public interface ScriptFactory {
    ProgrammesScript createProgrammesScript(String script);

    EpisodesScript createEpisodesScript(String script);

    EpisodeScript createEpisodeScript(String script);
}
