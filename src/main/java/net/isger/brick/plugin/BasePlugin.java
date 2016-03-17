package net.isger.brick.plugin;

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

    protected final Service getService(String name) {
        return services.get(name);
    }

    protected final Persist getPersist(String name) {
        return persists.get(name);
    }

}
