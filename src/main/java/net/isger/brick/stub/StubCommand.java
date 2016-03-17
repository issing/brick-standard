package net.isger.brick.stub;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.core.GateCommand;

public class StubCommand extends GateCommand {

    public static final String OPERATE_INSERT = "insert";

    public static final String OPERATE_DELETE = "delete";

    public static final String OPERATE_UPDATE = "update";

    public static final String OPERATE_SEARCH = "search";

    public static final String KEY_TRANSIENT = "stub-transient";

    public static final String KEY_TABLE = "stub-table";

    public static final String KEY_OPERATE = "stub-operate";

    public static final String KEY_CONDITION = "stub-condition";

    public StubCommand() {
    }

    public StubCommand(Command cmd) {
        super(cmd);
    }

    public StubCommand(boolean hasShell) {
        super(hasShell);
    }

    public static StubCommand getAction() {
        return cast(BaseCommand.getAction());
    }

    public static StubCommand cast(BaseCommand cmd) {
        return cmd == null || cmd.getClass() == StubCommand.class ? (StubCommand) cmd
                : cmd.infect(new StubCommand(false));
    }

    public Object getTable() {
        return getHeader(KEY_TABLE);
    }

    public void setTable(Object table) {
        setHeader(KEY_TABLE, table);
    }

    public void useModel(Class<?> clazz) {
        setTable(getParameter(clazz));
    }

    public String getStubOperate() {
        return getHeader(KEY_OPERATE);
    }

    public void setStubOperate(String operate) {
        setHeader(KEY_OPERATE, operate);
    }

    public Object getCondition() {
        return getHeader(KEY_CONDITION);
    }

    public void setCondition(Condition condition) {
        setHeader(KEY_CONDITION, condition);
    }

    public void setCondition(Object... condition) {
        setHeader(KEY_CONDITION, condition);
    }

}
