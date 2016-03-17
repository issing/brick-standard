package net.isger.brick.plugin.persist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.util.Helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Persists {

    private static final Logger LOG;

    private Map<String, Persist> persists;

    static {
        LOG = LoggerFactory.getLogger(Persists.class);
    }

    public Persists() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public Persists(List<Object> persists) {
        this.persists = new HashMap<String, Persist>();
        if (persists != null) {
            for (Object instance : persists) {
                if (instance instanceof Persist) {
                    add((Persist) instance);
                } else if (instance instanceof Map) {
                    for (Entry<String, Object> entry : ((Map<String, Object>) instance)
                            .entrySet()) {
                        instance = entry.getValue();
                        if (instance instanceof Persist) {
                            put(entry.getKey(), (Persist) instance);
                        }
                    }
                }
            }
        }
    }

    public void add(Persist persist) {
        put(null, persist);
    }

    public void put(String name, Persist persist) {
        name = Helpers.getAliasName(persist.getClass(), "Persist$", name)
                .toLowerCase();
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] persist [{}]", name, persist);
        }
        persist = persists.put(name, persist);
        if (persist != null) {
            LOG.warn("(!) Discard [{}] service [{}]", name, persist);
        }
    }

    public Persist get(String name) {
        return persists.get(name);
    }
}
