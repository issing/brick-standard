package net.isger.brick.stub.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.isger.util.Strings;
import net.isger.util.anno.Alias;

public class Fields {

    private Map<String, Field> fields;

    public Fields() {
        this(null);
    }

    public Fields(List<Object> fields) {
        this.fields = new HashMap<String, Field>();
        if (fields != null) {
            for (Object field : fields) {
                if (field instanceof Field) {
                    add((Field) field);
                }
            }
        }
    }

    public void add(Field field) {
        Class<?> clazz = field.getClass();
        Alias alias = clazz.getAnnotation(Alias.class);
        String name = alias == null ? field.getName() : alias.value();
        if (name == null || (name = name.trim()).length() == 0) {
            name = Strings.replaceIgnoreCase(clazz.getSimpleName(), "Field$",
                    "").toLowerCase();
        } else if (name.indexOf((int) '.') != -1) {
            throw new IllegalArgumentException("Invalid field alias " + name);
        }
        fields.put(name, field);
    }

    public Field get(String name) {
        return fields.get(name);
    }

    public List<String> getNames() {
        return Arrays
                .asList(fields.keySet().toArray(new String[fields.size()]));
    }

}
