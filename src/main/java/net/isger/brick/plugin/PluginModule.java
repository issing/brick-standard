package net.isger.brick.plugin;

import net.isger.brick.auth.AuthCommand;
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
        Class<? extends Gate> implClass = (Class<? extends Gate>) getImplementClass(PLUGIN, null);
        if (implClass == null) {
            implClass = super.getImplementClass();
        }
        return implClass;
    }

    public Class<? extends Gate> getBaseClass() {
        return BasePlugin.class;
    }

    protected void initial(String domain, Gate gate) {
        AuthCommand cmd = new AuthCommand();
        PluginCommand token = new PluginCommand();
        token.setDomain(domain);
        token.setOperate(PluginCommand.OPERATE_INITIAL);
        cmd.setToken(token);
        console.execute(cmd);
    }

    protected void destroy(String domain, Gate gate) {
        AuthCommand cmd = new AuthCommand();
        PluginCommand token = new PluginCommand();
        token.setDomain(domain);
        token.setOperate(PluginCommand.OPERATE_DESTROY);
        cmd.setToken(token);
        console.execute(cmd);
    }

}
