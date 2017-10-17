package net.isger.brick.test.bean;

import java.util.Date;

import net.isger.brick.stub.model.Model;
import net.isger.util.Helpers;

public class EmployModel extends Model {

    private static final String FIELD_ID = "id";

    private static final String FIELD_NAME = "name";

    private static final String FIELD_INPUT_TIME = "input_time";

    public EmployModel() {
        super(Helpers.wraps(Helpers.wrap("标识", FIELD_ID, "string", 20),
                Helpers.wrap("名称", FIELD_NAME, "string", 20),
                Helpers.wrap("维护时间", FIELD_INPUT_TIME, "date")));
    }

    public EmployModel(Object... args) {
        this();
        if (args != null) {
            switch (args.length) {
            case 3:
                metaValue(FIELD_INPUT_TIME, args[2]);
            case 2:
                metaValue(FIELD_NAME, args[1]);
            case 1:
                metaValue(FIELD_ID, args[0]);
            }
        }
    }

    public String getId() {
        return (String) metaValue(FIELD_ID);
    }

    public void setId(String id) {
        metaValue(FIELD_ID, id);
    }

    public String getName() {
        return (String) metaValue(FIELD_NAME);
    }

    public void setName(String name) {
        metaValue(FIELD_NAME, name);
    }

    public Date getInputTime() {
        return (Date) metaValue(FIELD_INPUT_TIME);
    }

    public void setInputTime(Date inputTime) {
        metaValue(FIELD_INPUT_TIME, inputTime);
    }

}
