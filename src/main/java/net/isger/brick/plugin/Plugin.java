package net.isger.brick.plugin;

import net.isger.brick.core.Gate;

/**
 * 插件接口
 * 
 * @author issing
 */
public interface Plugin extends Gate {

    /**
     * 服务
     * 
     */
    public void service(PluginCommand cmd);

    /**
     * 持久
     * 
     */
    public void persist(PluginCommand cmd);

}
