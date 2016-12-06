package net.isger.brick.plugin.service;

import net.isger.brick.plugin.PluginCommand;
import net.isger.util.Manageable;

/**
 * 服务
 * 
 * @author issing
 *
 */
public interface Service extends Manageable {

    /**
     * 服务入口
     * 
     * @param cmd
     */
    public void service(PluginCommand cmd);

}
