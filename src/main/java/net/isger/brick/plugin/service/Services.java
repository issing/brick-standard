package net.isger.brick.plugin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.util.Helpers;
import net.isger.util.Strings;

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
        put("", service);
    }

    public void put(String name, Service service) {
        int index = name.lastIndexOf(".");
        String key;
        if (index++ > 0) {
            key = name.substring(0, index);
            name = name.substring(index);
        } else {
            key = "";
        }
        key += getName(service.getClass(), name);
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] service [{}]", key, service);
        }
        service = services.put(key, service);
        if (service != null) {
            LOG.warn("(!) Discard [{}] service [{}]", key, service);
        }
    }

    public Service get(String name) {
        return services.get(name);
    }

    public Set<Entry<String, Service>> entrySet() {
        return services.entrySet();
    }

    public static final String getName(Class<?> clazz) {
        return getName(clazz, "");
    }

    public static final String getName(Class<?> clazz, String name) {
        return Helpers.getAliasName(clazz, "Service$", Strings.toLower(name));
    }

}
