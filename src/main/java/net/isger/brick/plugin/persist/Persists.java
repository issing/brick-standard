package net.isger.brick.plugin.persist;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.util.Helpers;
import net.isger.util.Strings;

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
        put("", persist);
    }

    public void put(String name, Persist persist) {
        int index = name.lastIndexOf(".");
        String key;
        if (index++ > 0) {
            key = name.substring(0, index);
            name = name.substring(index);
        } else {
            key = "";
        }
        key += getName(persist.getClass(), name);
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] persist [{}]", key, persist);
        }
        persist = persists.put(key, persist);
        if (persist != null) {
            LOG.warn("(!) Discard [{}] service [{}]", key, persist);
        }
    }

    public Persist get(String name) {
        return persists.get(name);
    }

    public Map<String, Persist> gets() {
        return Collections.unmodifiableMap(persists);
    }

    public static final String getName(Class<? extends Persist> clazz) {
        return getName(clazz, "");
    }

    public static final String getName(Class<? extends Persist> clazz,
            String name) {
        return Helpers.getAliasName(clazz, "Persist$", Strings.toLower(name));
    }

}
