package net.isger.brick.test.persist;

import java.util.Date;

import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.plugin.persist.CommonPersist;
import net.isger.brick.stub.StubCommand;
import net.isger.brick.test.bean.EmployModel;

public class EmployPersist extends CommonPersist {

    public EmployPersist() {
        super(new EmployModel());
    }

    public void test() {
        StubCommand cmd = getStubCommand();
        String test = (String) cmd.getParameter("test");
        System.out.println("EmployPersist.test(): " + test);
        if ("stub".equalsIgnoreCase(test)) {
            cmd.setTable(EmployModel.class);
            try {
                cmd.setOperate("remove");
                PluginHelper.toConsole(cmd);
            } catch (Exception e) {
            }
            cmd.setOperate("create");
            PluginHelper.toConsole(cmd);
            EmployModel t = new EmployModel();
            t.setId("1");
            t.setName("first");
            t.setInputTime(new Date());
            cmd.setTable(t);
            cmd.setOperate("insert");
            PluginHelper.toConsole(cmd);
            cmd.setOperate("search");
            PluginHelper.toConsole(cmd);
        }
    }
}
