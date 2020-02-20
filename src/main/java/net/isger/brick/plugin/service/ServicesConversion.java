package net.isger.brick.plugin.service;

import java.io.File;
import java.lang.reflect.Type;

import net.isger.brick.util.ScanLoader;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.reflect.ClassAssembler;
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

    public boolean isSupport(Type type) {
        return Services.class.isAssignableFrom(Reflects.getRawClass(type));
    }

    public Object convert(Type type, Object res, ClassAssembler assembler) {
        return new Services(toList(load(res, assembler)));
    }

    public String toString() {
        return Services.class.getName();
    }

}
