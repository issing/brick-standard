package net.isger.brick.plugin;

import java.util.Map.Entry;

import net.isger.brick.plugin.persist.Persist;
import net.isger.brick.plugin.persist.Persists;
import net.isger.brick.plugin.service.Service;
import net.isger.brick.plugin.service.Services;

public class BasePlugin extends AbstractPlugin {

    private Services services;

    private Persists persists;

    public BasePlugin() {
        services = new Services();
        persists = new Persists();
    }

    public void initial() {
        super.initial();
        PluginCommand cmd;
        Service service;
        for (Persist persist : persists.gets().values()) {
            container.inject(persist);
        }
        for (Entry<String, Service> entry : services.gets().entrySet()) {
            cmd = PluginCommand.mockAction();
            cmd.setName(entry.getKey());
            service = container.inject(entry.getValue());
            try {
                service.initial();
            } finally {
                PluginCommand.realAction();
            }
        }
    }

    protected final Service getService(String name) {
        return services.get(name);
    }

    protected final Persist getPersist(String name) {
        return persists.get(name);
    }

    public void destroy() {
        PluginCommand cmd = PluginCommand.getAction();
        for (Entry<String, Service> entry : services.gets().entrySet()) {
            cmd.setName(entry.getKey());
            entry.getValue().destroy();
        }
        super.destroy();
    }

}
