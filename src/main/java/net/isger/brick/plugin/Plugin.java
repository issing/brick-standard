package net.isger.brick.plugin;

import java.util.Map;

import net.isger.brick.core.Gate;
import net.isger.brick.plugin.service.Service;

/**
 * 插件接口
 * 
 * @author issing
 */
public interface Plugin extends Gate {

    /**
     * 服务集合
     *
     * @return
     */
    public Map<String, Service> getServices();

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
