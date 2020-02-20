package net.isger.brick.plugin.persist;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;

import net.isger.brick.util.ScanLoader;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.reflect.ClassAssembler;
import net.isger.util.reflect.conversion.Conversion;
import net.isger.util.scan.ScanFilter;

public class PersistsConversion extends ScanLoader implements Conversion {

    private static final ScanFilter FILTER;

    private static PersistsConversion INSTANCE;

    static {
        FILTER = new ScanFilter() {
            public boolean isDeep(File root, File path) {
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

    public boolean isSupport(Type type) {
        return Persists.class.isAssignableFrom(Reflects.getRawClass(type));
    }

    public Object convert(Type type, Object res, ClassAssembler assembler) {
        return new Persists(toList(load(res, assembler)));
    }

    protected Object make(Class<?> clazz, Map<String, Object> res, ClassAssembler assembler) {
        if (Reflects.isAbstract(clazz)) {
            return new PersistProxy(clazz);
        }
        return super.make(clazz, res, assembler);
    }

    public String toString() {
        return Persists.class.getName();
    }

}
