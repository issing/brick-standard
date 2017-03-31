package net.isger.brick.plugin.persist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.plugin.PluginConstants;
import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.stub.StubCommand;
import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Metas;
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
        if (create) {
            if (toInitial(cmd, table)) {
                boostrap(cmd);
            }
        }
    }

    private boolean toInitial(StubCommand cmd, Object table) {
        cmd.setTable(table);
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
                return false;
            } catch (Exception e) {
                // try {
                // cmd.setOperate(REMOVE);
                // PluginHelper.toConsole(cmd);
                // } catch (Exception ex) {
                // }
            }
        }
        cmd.setOperate(CREATE);
        PluginHelper.toConsole(cmd);
        Model model;
        for (Meta meta : Metas.getMetas(table).values()) {
            if ((model = meta.toModel()) != null) {
                toInitial((StubCommand) cmd.clone(), model);
            }
        }
        return true;
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
     */
    @SuppressWarnings("unchecked")
    @Ignore(mode = Mode.INCLUDE)
    public Object select(StubCommand cmd,
            @Alias(PluginConstants.PARAM_OPCODE) Object opcode,
            @Alias(PluginConstants.PARAM_VALUE) Object[] values,
            @Alias(PluginConstants.PARAM_BEAN) Object bean,
            @Alias(PluginConstants.PARAM_PAGE) Page page) {
        Object result = PluginHelper.toConsole(cmd);
        if (result instanceof Object[]) {
            Object[] grid = (Object[]) result;
            Object value = grid[grid.length - 1];
            if (value instanceof Number) {
                page.setTotal(((Number) value).intValue());
            }
            if (bean == null) {
                bean = this.table;
            }
            Class<?> clazz = Reflects.getClass(bean);
            if (clazz == null || Map.class.isAssignableFrom(clazz)
                    || bean instanceof String) {
                result = Reflects.toList(grid);
            } else if (Model.class.isAssignableFrom(clazz)) {
                result = Reflects.toList(grid);
                if (!(bean instanceof Class)) {
                    List<Model> container = new ArrayList<Model>();
                    Model model;
                    for (Map<String, Object> row : (List<Map<String, Object>>) result) {
                        model = ((Model) bean).clone();
                        model.metaValue(row);
                        container.add(model);
                    }
                    result = container;
                }
            } else {
                result = Reflects.toList(clazz, grid);
            }
        }
        if (page != null) {
            result = new Object[] { result, page };
        }
        return result;
    }

    /**
     * 查询
     *
     * @param opcode
     * @param values
     * @return
     */
    @SuppressWarnings("unchecked")
    @Ignore(mode = Mode.INCLUDE)
    public Object single(StubCommand cmd,
            @Alias(PluginConstants.PARAM_OPCODE) Object opcode,
            @Alias(PluginConstants.PARAM_VALUE) Object[] values,
            @Alias(PluginConstants.PARAM_BEAN) Object bean) {
        Object result = PluginHelper.toConsole(cmd);
        if (result instanceof Object[]) {
            Object[] grid = (Object[]) result;
            if (bean == null) {
                bean = this.table;
            }
            Class<?> clazz = Reflects.getClass(bean);
            if (clazz == null || Map.class.isAssignableFrom(clazz)
                    || bean instanceof Class || bean instanceof String) {
                result = Reflects.toList(grid);
            } else if (Model.class.isAssignableFrom(clazz)) {
                result = Reflects.toList(grid);
                if (!(bean instanceof Class)) {
                    List<Model> container = new ArrayList<Model>();
                    Model model;
                    for (Map<String, Object> row : (List<Map<String, Object>>) result) {
                        model = ((Model) bean).clone();
                        model.metaValue(row);
                        container.add(model);
                        break;
                    }
                    result = container;
                }
            } else {
                result = Reflects.toList(clazz, grid);
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
