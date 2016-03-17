package net.isger.brick.plugin;

import net.isger.brick.core.Gate;
import net.isger.brick.core.GateModule;
import net.isger.util.Asserts;

/**
 * 插件模块
 * 
 * @author issing
 * 
 */
public class PluginModule extends GateModule {

    public Class<? extends Gate> getTargetClass() {
        Class<? extends Gate> targetClass = (Class<? extends Gate>) super
                .getTargetClass();
        if (targetClass == null) {
            targetClass = Plugin.class;
        } else {
            Asserts.isAssignable(Plugin.class, targetClass,
                    "The plugin %s must implement the %s", targetClass,
                    Plugin.class);
        }
        return targetClass;
    }

    public Class<? extends Gate> getImplementClass() {
        return BasePlugin.class;
    }

}
