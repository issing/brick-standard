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

    public void initial(PluginCommand cmd) {
        super.initial();
        Service service;
        for (Persist persist : persists.values()) {
            container.inject(persist);
        }
        for (Entry<String, Service> entry : services.entrySet()) {
            cmd = PluginCommand.mockAction();
            cmd.setName(entry.getKey());
            service = container.inject(entry.getValue());
            try {
                service.service(cmd);
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

    public void destroy(PluginCommand cmd) {
        for (Entry<String, Service> entry : services.entrySet()) {
            cmd.setName(entry.getKey());
            try {
                entry.getValue().service(cmd);
            } catch (Exception e) {
            }
        }
        super.destroy();
    }

}
