package uk.co.mdjcox.scripts;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import uk.co.mdjcox.logger.LoggerInterface;

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

    protected Script(LoggerInterface logger, String script) {
        this.logger = logger;
        this.script = script;
    }

    protected LoggerInterface getLogger() {
        return logger;
    }

    public void call(Object... params) throws Exception {
        String[] roots = new String[]{"./scripts"};
        GroovyScriptEngine gse = new GroovyScriptEngine(roots);
        Binding binding = new Binding();
        for (int i=0; i<params.length; i=i+2) {
            binding.setVariable(params[i].toString(), params[i+1]);
        }
        gse.run(script, binding);

    }


}
