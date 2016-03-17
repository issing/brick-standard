package net.isger.brick.plugin.persist;

import net.isger.brick.plugin.PluginTarget;
import net.isger.brick.stub.StubCommand;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class BasePersist extends PluginTarget implements Persist {

    @Ignore(mode = Mode.INCLUDE)
    private String stub;

    protected final StubCommand getStubCommand() {
        StubCommand cmd = StubCommand.cast(super.getCommand());
        if (stub != null) {
            cmd.setDomain(stub);
        }
        return cmd;
    }

    protected final StubCommand mockStubCommand() {
        return StubCommand.cast(super.mockCommand());
    }

    protected final StubCommand realStubCommand() {
        return StubCommand.cast(super.realCommand());
    }

    protected StubCommand toStub() {
        StubCommand cmd = getStubCommand();
        getConsole().execute(cmd);
        return cmd;
    }

    protected StubCommand toStub(String operate) {
        StubCommand cmd = mockStubCommand();
        cmd.setOperate(operate);
        try {
            getConsole().execute(cmd);
        } finally {
            realCommand();
        }
        return cmd;
    }

}
