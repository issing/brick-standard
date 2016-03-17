package net.isger.brick.plugin.persist;

import java.util.Map;

import net.isger.brick.util.ScanLoader;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.reflect.Conversion;
import net.isger.util.scanner.ScanFilter;

public class PersistsConversion extends ScanLoader implements Conversion {

    private static final ScanFilter FILTER;

    private static PersistsConversion INSTANCE;

    static {
        FILTER = new ScanFilter() {
            public boolean isDeep() {
                return true;
            }

            public boolean accept(String name) {
                return Strings.endWithIgnoreCase(name, "Persist[.]class");
            }
        };
    }

    private PersistsConversion() {
        super(Persist.class, FILTER);
    }

    public static PersistsConversion getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PersistsConversion();
        }
        return INSTANCE;
    }

    public boolean isSupport(Class<?> type) {
        return Persists.class.isAssignableFrom(type);
    }

    public Object convert(Class<?> type, Object res) {
        return new Persists(toList(load(res)));
    }

    protected Object make(Class<?> clazz, Map<String, Object> res) {
        if (Reflects.isAbstract(clazz)) {
            return new PersistProxy(clazz);
        }
        return super.make(clazz, res);
    }

    public String toString() {
        return Persists.class.getName();
    }

}
