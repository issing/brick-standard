package net.isger.brick.plugin.service;

import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.stub.StubCommand;
import net.isger.util.Helpers;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class CommonService extends BaseService {

    public static final String OPERATE_INSERT = "insert";

    public static final String OPERATE_DELETE = "delete";

    public static final String OPERATE_UPDATE = "update";

    public static final String OPERATE_SELECT = "select";

    public static final String PARAM_OPCODE = "service.opcode";

    public static final String OPCODE_ID = "id";

    public static final String OPCODE_NORMAL = "normal";

    public static final String OPCODE_BATCH = "batch";

    public static final String PARAM_VALUE = "service.value";

    @Ignore(mode = Mode.INCLUDE)
    public void initial(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_INITIAL);
    }

    @Ignore(mode = Mode.INCLUDE)
    public void insert(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_INSERT);
    }

    public Object insert(PluginCommand cmd, Object table) {
        StubCommand.setTable(cmd, table);
        PluginHelper.toPersist(cmd, OPERATE_INSERT);
        return table;
    }

    @Ignore(mode = Mode.INCLUDE)
    public void delete(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_DELETE);
    }

    public void delete(PluginCommand cmd, Object table) {
        StubCommand.setTable(cmd, table);
        PluginHelper.toPersist(cmd, OPERATE_DELETE);
    }

    @Ignore(mode = Mode.INCLUDE)
    public void update(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_UPDATE);
    }

    public void update(PluginCommand cmd, Object... table) {
        update(cmd, Helpers.groups(table));
    }

    public void update(PluginCommand cmd, Object[]... table) {
        StubCommand.setTable(cmd, table);
        PluginHelper.toPersist(cmd, OPERATE_UPDATE);
    }

    @Ignore(mode = Mode.INCLUDE)
    public void select(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_SELECT);
    }

    public void select(PluginCommand cmd, Object table) {
        StubCommand.setTable(cmd, table);
        PluginHelper.toPersist(cmd, OPERATE_SELECT);
    }

    @Ignore(mode = Mode.INCLUDE)
    public void destroy(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_DESTROY);
    }

}
