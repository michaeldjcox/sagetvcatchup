package uk.co.mdjcox.sagetv.catchup.plugins;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;

import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.OsUtilsInterface;
import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:41
 * To change this template use File | Settings | File Templates.
 */
public abstract class Script {

    private Logger logger;
    private String script;
    private HtmlUtilsInterface htmlUtils;
    private DownloadUtilsInterface downloadUtils;
    private OsUtilsInterface osUtils;
    private PropertiesInterface properties;

    protected Script(Logger logger, String script, HtmlUtilsInterface htmlUtils,
                     DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils, PropertiesInterface properties) {
        this.logger = logger;
        this.script = script;
        this.htmlUtils = htmlUtils;
        this.downloadUtils = downloadUtils;
        this.osUtils = osUtils;
        this.properties = properties;
    }

    protected Logger getLogger() {
        return logger;
    }

    public void call(Object... params) throws Exception {
        String[] roots = new String[]{"./plugins"};
        GroovyScriptEngine gse = new GroovyScriptEngine(roots);
        CompilerConfiguration comp = gse.getConfig();
        comp.setScriptBaseClass(GroovyScript.class.getName());
        gse.setConfig(comp);

        Binding binding = new Binding();
        for (int i = 0; i < params.length; i = i + 2) {
            binding.setVariable(params[i].toString(), params[i + 1]);
        }

        GroovyShell shell = new GroovyShell(binding, comp);
        GroovyScript script = (GroovyScript) shell.parse(new File(this.script));
        script.setDownloadUtils(downloadUtils);
        script.setHtmlUtils(htmlUtils);
        script.setOsUtils(osUtils);
        script.setProperties(properties);
        script.setLogger(logger);

        script.run();

    }
}