package net.isger.brick.stub.model;

import java.util.List;

public interface Model {

    public String getModelName();

    public List<String> getFieldNames();

    public Field getField(String name);

}
