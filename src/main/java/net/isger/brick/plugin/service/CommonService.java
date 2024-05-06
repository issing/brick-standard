package net.isger.brick.plugin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.stub.StubCommand;
import net.isger.util.Helpers;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class CommonService extends BaseService {

    private static final Logger LOG;

    public static final String OPERATE_INITIAL = "initial";

    public static final String OPERATE_INSERT = "insert";

    public static final String OPERATE_DELETE = "delete";

    public static final String OPERATE_UPDATE = "update";

    public static final String OPERATE_SELECT = "select";

    public static final String OPERATE_DESTROY = "destroy";

    static {
        LOG = LoggerFactory.getLogger(CommonService.class);
    }

    @Ignore(mode = Mode.INCLUDE)
    public void initial(PluginCommand cmd) {
        super.initial();
        try {
            PluginHelper.toPersist(cmd, OPERATE_INITIAL);
        } catch (RuntimeException e) {
            LOG.debug("Failure to initial service [{}]", this.getClass().getName());
            throw e;
        }
    }

    @Ignore(mode = Mode.INCLUDE)
    public void insert(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_INSERT);
    }

    protected Object insert(PluginCommand cmd, Object table) {
        StubCommand.setTable(cmd, table);
        PluginHelper.toPersist(cmd, OPERATE_INSERT);
        return table;
    }

    @Ignore(mode = Mode.INCLUDE)
    public void delete(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_DELETE);
    }

    protected void delete(PluginCommand cmd, Object table) {
        StubCommand.setTable(cmd, table);
        PluginHelper.toPersist(cmd, OPERATE_DELETE);
    }

    @Ignore(mode = Mode.INCLUDE)
    public void update(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_UPDATE);
    }

    protected void update(PluginCommand cmd, Object... table) {
        update(cmd, Helpers.groups(table));
    }

    protected void update(PluginCommand cmd, Object[]... table) {
        StubCommand.setTable(cmd, table);
        PluginHelper.toPersist(cmd, OPERATE_UPDATE);
    }

    @Ignore(mode = Mode.INCLUDE)
    public void select(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_SELECT);
    }

    protected void select(PluginCommand cmd, Object table) {
        StubCommand.setTable(cmd, table);
        PluginHelper.toPersist(cmd, OPERATE_SELECT);
    }

    @Ignore(mode = Mode.INCLUDE)
    public void destroy(PluginCommand cmd) {
        PluginHelper.toPersist(cmd, OPERATE_DESTROY);
        super.destroy();
    }

}
