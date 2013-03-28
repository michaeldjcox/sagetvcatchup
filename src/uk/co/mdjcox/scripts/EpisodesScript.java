package uk.co.mdjcox.scripts;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import uk.co.mdjcox.logger.LoggerInterface;
import uk.co.mdjcox.model.Programme;
import uk.co.mdjcox.model.Source;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:48
 * To change this template use File | Settings | File Templates.
 */
public class EpisodesScript extends Script {
    public EpisodesScript(LoggerInterface logger, String script) {
        super(logger, script);
    }

    public void getEpisodes(Programme programme) {
        try {
            getLogger().info("Getting episodes for " + programme);
            call("url", programme.getServiceUrl(), "programme", programme);
        } catch (Throwable e) {
            getLogger().severe("Unable to get episodes for: " + programme, e);
        } finally {
            getLogger().info(programme + " has " + programme.getEpisodes().size() + " episodes");
        }
    }

}
