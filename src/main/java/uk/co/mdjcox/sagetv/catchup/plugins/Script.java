package uk.co.mdjcox.sagetv.catchup.plugins;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import uk.co.mdjcox.sagetv.catchup.CatchupContextInterface;
import uk.co.mdjcox.utils.DownloadUtilsInterface;
import uk.co.mdjcox.utils.HtmlUtilsInterface;
import uk.co.mdjcox.utils.LoggerInterface;
import uk.co.mdjcox.utils.OsUtilsInterface;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 28/03/13
 * Time: 17:41
 * To change this template use File | Settings | File Templates.
 */
public abstract class Script {

    private LoggerInterface logger;
    private String script;
    private HtmlUtilsInterface htmlUtils;
    private DownloadUtilsInterface downloadUtils;
    private OsUtilsInterface osUtils;
    private CatchupContextInterface context;

    protected Script(LoggerInterface logger, String script, HtmlUtilsInterface htmlUtils,
                     DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils,
                     CatchupContextInterface catchupContext) {
        this.logger = logger;
        this.script = script;
        this.htmlUtils = htmlUtils;
        this.downloadUtils = downloadUtils;
        this.osUtils = osUtils;
        this.context = catchupContext;
    }

    protected LoggerInterface getLogger() {
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

        GroovyShell shell = new GroovyShell(comp);
        GroovyScript script = (GroovyScript) shell.parse(new File(this.script));
        script.setBinding(binding);
        script.setDownloadUtils(downloadUtils);
        script.setHtmlUtils(htmlUtils);
        script.setOsUtils(osUtils);
        script.setContext(context);
        script.setLogger(logger);

        script.run();

    }
}
