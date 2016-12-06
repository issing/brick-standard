package net.isger.brick.plugin;

import net.isger.brick.core.BaseGate;
import net.isger.brick.core.GateCommand;
import net.isger.brick.plugin.persist.Persist;
import net.isger.brick.plugin.service.Service;
import net.isger.util.Strings;

public abstract class AbstractPlugin extends BaseGate implements Plugin {

    protected abstract Service getService(String name);

    protected abstract Persist getPersist(String name);

    public void operate(GateCommand cmd) {
        PluginCommand pcmd = (PluginCommand) cmd;
        if (Strings.isEmpty(pcmd.getName())) {
            super.operate(cmd);
        } else {
            service(pcmd);
        }
    }

    public void service(PluginCommand cmd) {
        getService(cmd.getName()).service(cmd);
    }

    public void persist(PluginCommand cmd) {
        getPersist(cmd.getName()).persist(cmd);
    }

}
