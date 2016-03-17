package net.isger.brick.plugin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.util.Helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Services {

    private static final Logger LOG;

    private Map<String, Service> services;

    static {
        LOG = LoggerFactory.getLogger(Services.class);
    }

    public Services() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public Services(List<Object> services) {
        this.services = new HashMap<String, Service>();
        if (services != null) {
            for (Object instance : services) {
                if (instance instanceof Service) {
                    add((Service) instance);
                } else if (instance instanceof Map) {
                    for (Entry<String, Object> entry : ((Map<String, Object>) instance)
                            .entrySet()) {
                        instance = entry.getValue();
                        if (instance instanceof Service) {
                            put(entry.getKey(), (Service) instance);
                        }
                    }
                }
            }
        }
    }

    public void add(Service service) {
        put(null, service);
    }

    public void put(String name, Service service) {
        name = Helpers.getAliasName(service.getClass(), "Service$", name)
                .toLowerCase();
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] service [{}]", name, service);
        }
        service = services.put(name, service);
        if (service != null) {
            LOG.warn("(!) Discard [{}] service [{}]", name, service);
        }
    }

    public Service get(String name) {
        return services.get(name);
    }
}
