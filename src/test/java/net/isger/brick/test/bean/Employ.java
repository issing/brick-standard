package net.isger.brick.test.bean;

import java.util.Date;

public class Employ {

    private String id;

    private String name;

    private Date inputTime;

    public Employ() {
    }

    public Employ(String id) {
        this(id, null, null);
    }

    public Employ(String id, String name) {
        this(id, name, null);
    }

    public Employ(String id, String name, Date inputTime) {
        this.id = id;
        this.name = name;
        this.inputTime = inputTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getInputTime() {
        return inputTime;
    }

    public void setInputTime(Date inputTime) {
        this.inputTime = inputTime;
    }

}
