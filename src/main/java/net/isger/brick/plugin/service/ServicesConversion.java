package net.isger.brick.plugin.service;

import java.io.File;

import net.isger.brick.util.ScanLoader;
import net.isger.util.Strings;
import net.isger.util.reflect.conversion.Conversion;
import net.isger.util.scan.ScanFilter;

public class ServicesConversion extends ScanLoader implements Conversion {

    private static final ScanFilter FILTER;

    private static ServicesConversion INSTANCE;

    static {
        FILTER = new ScanFilter() {
            public boolean isDeep(File root, File path) {
                return true;
            }

            public boolean accept(String name) {
                return Strings.endWithIgnoreCase(name, "Service[.]class");
            }
        };
    }

    private ServicesConversion() {
        super(Service.class, FILTER);
    }

    public static ServicesConversion getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServicesConversion();
        }
        return INSTANCE;
    }

    public boolean isSupport(Class<?> type) {
        return Services.class.isAssignableFrom(type);
    }

    public Object convert(Class<?> type, Object res) {
        return new Services(toList(load(res)));
    }

    public String toString() {
        return Services.class.getName();
    }

}
