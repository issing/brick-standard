package net.isger.brick.test.persist;

import java.util.Date;

import net.isger.brick.plugin.persist.CommonPersist;
import net.isger.brick.stub.StubCommand;
import net.isger.brick.test.bean.EmployModel;

public class EmployPersist extends CommonPersist {

    public EmployPersist() {
        super(new EmployModel(""));
    }

    public void test() {
        StubCommand cmd = getStubCommand();
        String test = (String) cmd.getParameter("test");
        System.out.println("EmployPersist.test(): " + test);
        if ("stub".equalsIgnoreCase(test)) {
            cmd.setTable(EmployModel.class);
            try {
                toStub("remove");
            } catch (Exception e) {
            }
            toStub("create");
            EmployModel t = new EmployModel();
            t.setId("1");
            t.setName("first");
            t.setInputTime(new Date());
            cmd.setTable(t);
            toStub("insert");
            cmd.setOperate("search");
            toStub();
        }
    }
}
