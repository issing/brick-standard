package net.isger.brick.plugin.service;

import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginHelper;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class CommonService extends BaseService {

    public static final String PARAM_OPCODE = "service.opcode";

    public static final String OPCODE_ID = "id";

    public static final String OPCODE_NORMAL = "normal";

    public static final String OPCODE_BATCH = "batch";

    public static final String PARAM_VALUE = "service.value";

    @Ignore(mode = Mode.INCLUDE)
    public void initial(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, "initial");
    }

    @Ignore(mode = Mode.INCLUDE)
    public void insert(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, "insert");
    }

    @Ignore(mode = Mode.INCLUDE)
    public void delete(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, "delete");
    }

    @Ignore(mode = Mode.INCLUDE)
    public void update(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, "update");
    }

    @Ignore(mode = Mode.INCLUDE)
    public void select(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, "select");
    }

}
