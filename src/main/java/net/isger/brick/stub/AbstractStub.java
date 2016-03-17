package net.isger.brick.stub;

import net.isger.brick.core.BaseGate;

public abstract class AbstractStub extends BaseGate implements Stub {

    public void create() {
        create(StubCommand.getAction());
    }

    public void insert() {
        insert(StubCommand.getAction());
    }

    public void delete() {
        delete(StubCommand.getAction());
    }

    public void update() {
        update(StubCommand.getAction());
    }

    public void search() {
        search(StubCommand.getAction());
    }

    public void remove() {
        remove(StubCommand.getAction());
    }

}
