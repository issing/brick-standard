package net.isger.brick.stub.model;

import net.isger.brick.core.BaseCommand;

public class Field {

    private String label;

    private String name;

    private String type;

    private int length;

    private Object value;

    public Field() {
        this((Object[]) null);
    }

    public Field(Object... args) {
        if (args != null) {
            switch (args.length) {
            case 4:
                this.length = (Integer) args[3];
            case 3:
                this.type = (String) args[2];
            case 2:
                this.name = (String) args[1];
            case 1:
                this.label = (String) args[0];
            }
        }
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public Object getValue() {
        if (value == null) {
            BaseCommand cmd = BaseCommand.getAction();
            if (cmd != null) {
                value = cmd.getParameter(name);
            }
        }
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
