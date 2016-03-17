package net.isger.brick.stub.model;

import java.util.List;

import net.isger.util.Sqls;

public class BaseModel implements Model {

    private String name;

    private Fields fields;

    public BaseModel() {
        fields = new Fields();
    }

    public BaseModel(Object... fields) {
        this();
        for (Object field : fields) {
            if (field instanceof Object[]) {
                addField((Object[]) field);
            } else if (field instanceof Field) {
                addField((Field) field);
            }
        }
    }

    public String getModelName() {
        Class<?> clazz = this.getClass();
        if (name == null && clazz != BaseModel.class) {
            name = Sqls.getTableName(clazz, "Model$");
        }
        return name;
    }

    protected void addField(Field field) {
        fields.add(field);
    }

    protected Field addField(Object... args) {
        Field field = new Field(args);
        addField(field);
        return field;
    }

    public List<String> getFieldNames() {
        return fields.getNames();
    }

    public Field getField(String name) {
        return fields.get(name);
    }

    protected Object getFieldValue(String name) {
        return getField(name).getValue();
    }

    protected void setFieldValue(String name, Object value) {
        getField(name).setValue(value);
    }

}
