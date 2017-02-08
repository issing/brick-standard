package net.isger.brick.plugin.persist;

import java.util.List;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.plugin.PluginConstants;
import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.stub.StubCommand;
import net.isger.brick.stub.model.Model;
import net.isger.util.Reflects;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.sql.Page;

@Ignore
public class CommonPersist extends PersistProxy {

    public static final String EXISTS = "exists";

    public static final String CREATE = "create";

    public static final String INSERT = "insert";

    public static final String DELETE = "delete";

    public static final String UPDATE = "update";

    public static final String SINGLE = "single";

    public static final String SELECT = "select";

    public static final String REMOVE = "remove";

    public static final String OPCODE_NORMAL = "normal";

    public static final String OPCODE_BATCH = "batch";

    protected final Object table;

    @Ignore(mode = Mode.INCLUDE)
    private boolean reset;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    private boolean create;

    public CommonPersist(Object table) {
        this(table, true);
    }

    public CommonPersist(Object table, boolean create) {
        this.table = table;
        this.create = create;
    }

    @Ignore(mode = Mode.INCLUDE)
    public final void initial(StubCommand cmd) {
        cmd.setTable(table);
        if (create) {
            if (this.reset) {
                try {
                    cmd.setOperate(REMOVE);
                    PluginHelper.toConsole(cmd);
                } catch (Exception e) {
                }
            } else {
                try {
                    cmd.setOperate(SELECT);
                    cmd.setCondition(EXISTS);
                    PluginHelper.toConsole(cmd);
                    return;
                } catch (Exception e) {
                }
            }
            cmd.setOperate(CREATE);
            PluginHelper.toConsole(cmd);
        }
        boostrap(cmd);
    }

    /**
     * 引导
     */
    protected void boostrap(StubCommand cmd) {
    }

    /**
     * 新增
     *
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public void insert(StubCommand cmd,
            @Alias(PluginConstants.PARAM_OPCODE) Object opcode,
            @Alias(PluginConstants.PARAM_VALUE) Object[] values) {
        PluginHelper.toConsole(cmd);
    }

    /**
     * 删除
     *
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public void delete(StubCommand cmd,
            @Alias(PluginConstants.PARAM_OPCODE) Object opcode,
            @Alias(PluginConstants.PARAM_VALUE) Object[] values) {
        PluginHelper.toConsole(cmd);
    }

    /**
     * 修改
     *
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public void update(StubCommand cmd,
            @Alias(PluginConstants.PARAM_OPCODE) Object opcode,
            @Alias(PluginConstants.PARAM_VALUE) Object[] values) {
        PluginHelper.toConsole(cmd);
    }

    /**
     * 查询
     *
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public Object single(StubCommand cmd,
            @Alias(PluginConstants.PARAM_OPCODE) Object opcode,
            @Alias(PluginConstants.PARAM_VALUE) Object[] values) {
        Object result = PluginHelper.toConsole(cmd);
        if (result instanceof Object[]) {
            Class<?> clazz = Reflects.getClass(this.table);
            if (clazz == null) {
                result = Reflects.toList((Object[]) result);
            } else {
                result = Reflects.toList(clazz, (Object[]) result);
            }
        } else if (!(result instanceof List)) {
            return result;
        }
        List<?> list = (List<?>) result;
        if (list.size() > 0) {
            result = list.get(0);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * 查询
     *
     * @param opcode
     * @param values
     */
    @Ignore(mode = Mode.INCLUDE)
    public Object select(StubCommand cmd,
            @Alias(PluginConstants.PARAM_OPCODE) Object opcode,
            @Alias(PluginConstants.PARAM_VALUE) Object[] values,
            @Alias(PluginConstants.PARAM_PAGE) Page page) {
        Object result = PluginHelper.toConsole(cmd);
        if (result instanceof Object[]) {
            Object[] gridMode = (Object[]) result;
            Object value = gridMode[gridMode.length - 1];
            if (value instanceof Integer) {
                page.setTotal((Integer) value);
            }
            Class<?> clazz = Reflects.getClass(this.table);
            if (clazz == null || Model.class.isAssignableFrom(clazz)) {
                result = Reflects.toList(gridMode);
            } else {
                result = Reflects.toList(clazz, gridMode);
            }
        }
        if (page != null) {
            result = new Object[] { result, page };
        }
        return result;
    }
    // /**
    // * 统计
    // *
    // * @param opcode
    // * @param values
    // * @return
    // */
    // public int count(@Alias(PARAM_OPCODE) Object opcode,
    // @Alias(PARAM_VALUE) Object[] values);

}
