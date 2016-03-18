package net.isger.brick.plugin;

import net.isger.brick.core.Gate;
import net.isger.brick.core.GateModule;

/**
 * 插件模块
 * 
 * @author issing
 * 
 */
public class PluginModule extends GateModule {

    public static final String PLUGIN = "plugin";

    public Class<? extends Gate> getTargetClass() {
        return Plugin.class;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Gate> getImplementClass() {
        Class<? extends Gate> implClass = (Class<? extends Gate>) getImplementClass(
                PLUGIN, null);
        if (implClass == null) {
            implClass = super.getImplementClass();
        }
        return implClass;
    }

    public Class<? extends Gate> getBaseClass() {
        return BasePlugin.class;
    }

}
