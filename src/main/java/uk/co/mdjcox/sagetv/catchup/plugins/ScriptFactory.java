package uk.co.mdjcox.sagetv.catchup.plugins;

import com.google.inject.assistedinject.Assisted;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 04/04/13
 * Time: 07:49
 * To change this template use File | Settings | File Templates.
 */
public interface ScriptFactory {
    SourceScript createSourceScript(@Assisted("id") String id, @Assisted("base") String base);

    ProgrammesScript createProgrammesScript(String base);

    EpisodesScript createEpisodesScript(String base);

    EpisodeScript createEpisodeScript(String base);

    PlayScript createPlayScript(String base);

    StopScript createStopScript(String base);
}
