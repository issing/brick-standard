package net.isger.brick.plugin.persist;

import net.isger.brick.plugin.PluginCommand;
import net.isger.util.Manageable;

/**
 * 持久接口
 * 
 * @author issing
 *
 */
public interface Persist extends Manageable {

    /**
     * 持久入口
     * 
     * @param cmd
     */
    public void persist(PluginCommand cmd);

}
