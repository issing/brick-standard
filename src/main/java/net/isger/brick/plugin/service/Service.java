package net.isger.brick.plugin.service;

import net.isger.brick.plugin.PluginCommand;

/**
 * 服务
 * 
 * @author issing
 */
public interface Service {

    /**
     * 服务入口
     * 
     * @param cmd
     */
    public void service(PluginCommand cmd);

}
