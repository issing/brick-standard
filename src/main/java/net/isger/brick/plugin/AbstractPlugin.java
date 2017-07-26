package net.isger.brick.plugin;

import net.isger.brick.core.BaseGate;
import net.isger.brick.core.GateCommand;
import net.isger.brick.plugin.persist.Persist;
import net.isger.brick.plugin.service.Service;
import net.isger.brick.stub.StubCommand;
import net.isger.util.Asserts;
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
        String name = cmd.getName();
        Service service = getService(name);
        Asserts.isNotNull(
                service,
                "Unfound the specified service [%s] in the Plugin [%s], Check whether it is configured in the brick configuration file",
                name, this.getClass().getName());
        service.service(cmd);
    }

    public void persist(PluginCommand cmd) {
        String name = cmd.getName();
        Persist persist = getPersist(name);
        Asserts.isNotNull(
                persist,
                "Unfound the specified persist [%s] in the Plugin [%s], Check whether it is configured in the brick configuration file",
                name, this.getClass().getName());
        persist.persist(StubCommand.cast(cmd));
    }

}
