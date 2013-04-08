package uk.co.mdjcox.scripts;

import com.google.inject.assistedinject.Assisted;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 08/04/13
 * Time: 07:35
 * To change this template use File | Settings | File Templates.
 */
public interface PluginFactory {
    Plugin createPlugin(@Assisted("id") String id, @Assisted("base") String base);

}
