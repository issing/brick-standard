package net.isger.brick.plugin.persist;

import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginOperator;
import net.isger.brick.stub.StubCommand;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class BasePersist extends PluginOperator implements Persist {

    @Ignore(mode = Mode.INCLUDE)
    private String stub;

    public void initial() {
    }

    protected final StubCommand getStubCommand() {
        StubCommand cmd = StubCommand.getAction();
        if (Strings.isNotEmpty(stub)) {
            cmd.setDomain(stub);
        }
        return cmd;
    }

    protected final StubCommand mockStubCommand() {
        StubCommand.mockAction();
        return getStubCommand();
    }

    public void persist(PluginCommand cmd) {
        super.operate(cmd);
    }

    public void destroy() {
    }

}
