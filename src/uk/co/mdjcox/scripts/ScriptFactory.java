package uk.co.mdjcox.scripts;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 04/04/13
 * Time: 07:49
 * To change this template use File | Settings | File Templates.
 */
public interface ScriptFactory {
    SourceScript createSourceScript(String id, String base);
    ProgrammesScript createProgrammesScript(String base);
    EpisodesScript createEpisodesScript(String base);
    EpisodeScript createEpisodeScript(String base);
}
