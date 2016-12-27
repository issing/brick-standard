package net.isger.brick.plugin.persist;

import net.isger.brick.plugin.PluginOperator;
import net.isger.brick.stub.StubCommand;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class BasePersist implements Persist {

    @Ignore(mode = Mode.INCLUDE)
    private String stub;

    private PluginOperator operator;

    public BasePersist() {
        this.operator = new PluginOperator(this);
    }

    public void persist(StubCommand cmd) {
        if (Strings.isNotEmpty(stub) && Strings.isEmpty(cmd.getDomain())) {
            cmd.setDomain(stub);
        }
        operator.operate(cmd);
    }

}
