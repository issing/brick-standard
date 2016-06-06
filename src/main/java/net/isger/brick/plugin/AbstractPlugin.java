package net.isger.brick.plugin;

import net.isger.brick.core.BaseGate;
import net.isger.brick.plugin.persist.Persist;
import net.isger.brick.plugin.service.Service;
import net.isger.util.Strings;

public abstract class AbstractPlugin extends BaseGate implements Plugin {

    protected abstract Service getService(String name);

    protected abstract Persist getPersist(String name);

    public void operate() {
        if (Strings.isEmpty(getName())) {
            super.operate();
        } else {
            service();
        }
    }

    public void service() {
        service(getName());
    }

    protected void service(String name) {
        getService(name).operate();
    }

    public void persist() {
        persist(getName());
    }

    protected void persist(String name) {
        getPersist(name).operate();
    }

    protected String getName() {
        return PluginCommand.getAction().getName();
    }

}
