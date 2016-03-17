package net.isger.brick.stub.dialect;

import net.isger.brick.stub.model.Field;

public interface Describer {

    public String describe();

    public String describe(Field field);

}
