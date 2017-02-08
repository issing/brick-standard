package net.isger.brick.dao;

import net.isger.brick.stub.StubCommand;
import net.isger.brick.test.TestStub;
import net.isger.brick.test.bean.EmployModel;
import net.isger.util.Reflects;

public class EmployDAO {

    private TestStub test;

    public void list() {
        StubCommand cmd = new StubCommand();
        cmd.setTable(new EmployModel());
        test.select(cmd);
        Object[] result = (Object[]) cmd.getResult();
        System.out.println(Reflects.toList(result));
    }

}
