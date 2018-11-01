package net.isger.brick.plugin.persist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.plugin.PluginConstants;
import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.stub.StubCommand;
import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Metas;
import net.isger.brick.stub.model.Model;
import net.isger.util.Callable;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Sqls;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.reflect.BoundField;
import net.isger.util.reflect.TypeToken;
import net.isger.util.sql.Page;

/**
 * 通用持久
 * 
 * @author issing
 */
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

    public static final String STATEMENT_BATCH = "batch";

    @Ignore(mode = Mode.INCLUDE)
    private boolean reset;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    private boolean create;

    private Object[] tables;

    public CommonPersist(Object... tables) {
        this(true, tables);
    }

    public CommonPersist(boolean create, Object... tables) {
        this.create = create;
        this.tables = tables;
    }

    @Ignore(mode = Mode.INCLUDE)
    public final void initial(StubCommand cmd) {
        if (create) {
            boolean hasBoostrap = create(cmd, tables[0]);
            int size = tables.length;
            for (int i = 1; i < size; i++) {
                create((StubCommand) cmd.clone(), tables[i]);
            }
            if (hasBoostrap) {
                boostrap(cmd);
            }
        }
    }

    private boolean create(StubCommand cmd, Object table) {
        cmd.setTable(table);
        if (this.reset) {
            try {
                cmd.setOperate(REMOVE);
                PluginHelper.toConsole(cmd);
            } catch (Exception e) {
            }
        } else {
            try {
                cmd.setOperate(EXISTS);
                PluginHelper.toConsole(cmd);
                return false;
            } catch (Exception e) {
            }
        }
        cmd.setOperate(CREATE);
        PluginHelper.toConsole(cmd);
        Model model;
        for (Meta meta : Metas.getMetas(table).values()) {
            if ((model = meta.toModel()) != null) {
                create((StubCommand) cmd.clone(), model);
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
            @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode,
            @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values) {
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
            @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode,
            @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values) {
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
            @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode,
            @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values) {
        PluginHelper.toConsole(cmd);
    }

    /**
     * 查询
     *
     * @param opcode
     * @param values
     */
    @Ignore(mode = Mode.INCLUDE)
    public Object select(StubCommand cmd,
            @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode,
            @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values,
            @Alias(PluginConstants.PARAM_STATEMENT_ARGS) Object[] args,
            @Alias(PluginConstants.PARAM_BEAN) Object bean,
            @Alias(PluginConstants.PARAM_PAGE) Page page) {
        boolean isMultiple = Helpers.isMultiple(cmd.getTable());
        Object result = PluginHelper.toConsole(cmd);
        if (isMultiple) {
            List<Object> pendings = new ArrayList<Object>();
            for (Object pending : (Object[]) result) {
                pendings.add(toResult(cmd, bean, page, (Object[]) pending));
            }
            result = pendings;
        } else {
            result = toResult(cmd, bean, page, (Object[]) result);
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
    @Ignore(mode = Mode.INCLUDE)
    public Object single(StubCommand cmd,
            @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode,
            @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values,
            @Alias(PluginConstants.PARAM_STATEMENT_ARGS) Object[] args,
            @Alias(PluginConstants.PARAM_BEAN) Object bean) {
        boolean isMultiple = Helpers.isMultiple(cmd.getTable());
        cmd.setParameter(PluginConstants.PARAM_PAGE, null);
        Object result = PluginHelper.toConsole(cmd);
        if (isMultiple) {
            List<Object> pendings = new ArrayList<Object>();
            for (Object pending : (Object[]) result) {
                pendings.add(Helpers.getInstance(
                        toResult(cmd, bean, null, (Object[]) pending), 0));
            }
            result = pendings;
        } else {
            result = Helpers.getInstance(
                    toResult(cmd, bean, null, (Object[]) result), 0);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object toResult(StubCommand cmd, Object bean, Page page,
            Object[] grid) {
        Object result;
        Object value = grid[grid.length - 1];
        if (value instanceof Number) {
            page.setTotal(((Number) value).intValue());
        }
        if (bean == null) {
            bean = this.tables[0];
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
            result = toResult(cmd, clazz, grid);
        }
        if (page != null) {
            result = new Object[] { result, page };
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object toResult(StubCommand cmd, Class<?> clazz, Object[] grid) {
        final Map<BoundField, ResultMeta> metas = new HashMap<>();
        Object result = Reflects.toList(clazz, grid, new Callable<Object>() {
            public Object call(Object... args) {
                BoundField field = (BoundField) args[0];
                ResultMeta resultMeta = metas.get(field);
                if (resultMeta == null) {
                    metas.put(field, resultMeta = createResultMeta(field)); // 字段结果元数据
                }
                Map<String, Object> row = (Map<String, Object>) args[3]; // 行值
                /* 引用数据 */
                if (resultMeta.meta.isReference()) {
                    switch (resultMeta.meta.getMode()) {
                    // 桥接模式处理
                    case Meta.MODE_BRIDGE:
                        Helpers.toAppend(resultMeta.mapping,
                                row.get(resultMeta.sourceField), args[1]);
                        break;
                    }
                }
                /* 内联数据 */
                else {
                    String fieldName = Sqls
                            .toFieldName(resultMeta.sourceColumn);
                    Object fieldValue = Helpers.getInstance(row, fieldName);
                    if (args[2] == Reflects.UNKNOWN) {
                        args[2] = fieldValue;
                    }
                    // 集合对象
                    else if (args[2] instanceof Map && fieldValue != args[2]) {
                        ((Map<String, Object>) args[2])
                                .put(resultMeta.sourceField, fieldValue);
                    }
                    Helpers.toAppend(resultMeta.mapping, fieldValue, args[1]);
                }
                /* 初始置空 */
                return null;
            }
        });
        /* 映射处理 */
        if (metas.size() > 0) {
            toMapping(cmd, metas);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private ResultMeta createResultMeta(BoundField field) {
        ResultMeta resultMeta = new ResultMeta();
        resultMeta.meta = Meta.createMeta(field); // 元字段
        resultMeta.mapping = new HashMap<Object, List<Object>>();
        if ((resultMeta.model = resultMeta.meta.toModel()) == null) {
            resultMeta.model = Model.create(field.getToken().getRawClass());
            resultMeta.sourceColumn = resultMeta.meta.getName();
            resultMeta.targetColumn = (String) resultMeta.meta.getValue();
            resultMeta.sourceField = Sqls.toFieldName(resultMeta.targetColumn);
        } else {
            Map<String, Object> params = (Map<String, Object>) resultMeta.meta
                    .getValue();
            Map<String, Object> source = (Map<String, Object>) params
                    .get("source");
            resultMeta.sourceColumn = (String) source.get("name");
            resultMeta.sourceField = Sqls
                    .toFieldName((String) source.get("value"));
            Map<String, Object> target = (Map<String, Object>) params
                    .get("target");
            resultMeta.targetColumn = (String) target.get("value");
            resultMeta.targetField = Sqls
                    .toFieldName((String) target.get("name"));
        }
        resultMeta.model.metaEmpty();
        return resultMeta;
    }

    @SuppressWarnings("unchecked")
    private void toMapping(StubCommand cmd, Map<BoundField, ResultMeta> metas) {
        StubCommand scmd;
        Model model;
        List<Model> models;
        BoundField field;
        ResultMeta resultMeta;
        for (Entry<BoundField, ResultMeta> entry : metas.entrySet()) {
            scmd = cmd.clone();
            resultMeta = entry.getValue();
            if (resultMeta.model != null) {
                field = entry.getKey();
                models = new ArrayList<Model>();
                final Map<Object, List<Object>> targets = new HashMap<Object, List<Object>>();
                if (Strings.isEmpty(resultMeta.targetField)) {
                    targets.putAll(resultMeta.mapping);
                } else {
                    /* 查询映射数据（关系表） */
                    scmd.setTable(models);
                    for (Object sourceKey : resultMeta.mapping.keySet()) {
                        models.add(model = resultMeta.model.clone());
                        model.metaValue(resultMeta.sourceColumn, sourceKey); // 列值（源字段）
                    }
                    Helpers.each(
                            select(scmd, null, null, null, Map.class, null),
                            new Callable<Void>() {
                                public Void call(Object... args) {
                                    Integer index = (Integer) args[0];
                                    List<Map<String, Object>> values = (List<Map<String, Object>>) args[1];
                                    Object[] outerArgs = (Object[]) args[2];
                                    Model mapping = ((List<Model>) outerArgs[0])
                                            .get(index);
                                    ResultMeta outerMeta = (ResultMeta) outerArgs[1];
                                    List<Object> instances = outerMeta.mapping
                                            .get(mapping.metaValue(
                                                    outerMeta.sourceColumn));
                                    Object target;
                                    for (Map<String, Object> value : values) {
                                        target = value
                                                .get(outerMeta.targetField);
                                        Helpers.toAppend(targets, target,
                                                instances, false);
                                    }
                                    return null;
                                }
                            }, models, resultMeta);
                }
                /* 获取元素类型 */
                TypeToken<?> typeToken = field.getToken();
                Class<?> rawClass = typeToken.getRawClass();
                if (Collection.class.isAssignableFrom(rawClass)) {
                    rawClass = (Class<?>) Reflects
                            .getActualType(typeToken.getType());
                } else if (rawClass.isArray()) {
                    rawClass = (Class<?>) Reflects
                            .getComponentType(typeToken.getType());
                }
                final Map<Object, List<Object>> pending = new HashMap<Object, List<Object>>();
                /* 目标直接赋值（未配置目标列） */
                if (Strings.isEmpty(resultMeta.targetColumn)) {
                    for (Entry<Object, List<Object>> targetEntry : targets
                            .entrySet()) {
                        for (Object o : targetEntry.getValue()) {
                            Helpers.toAppend(pending, o, targetEntry.getKey(),
                                    false);
                        }
                    }
                }
                /* 查询目标数据（已配置目标列） */
                else {
                    // 构建目标查询模型（根据唯一键）
                    final Model targetModel = Model.create(rawClass);
                    targetModel.metaEmpty();
                    scmd.setTable(models = new ArrayList<Model>());
                    for (Object targetKey : targets.keySet()) {
                        if (Strings.isEmpty(targetKey)) {
                            continue; // 映射值为空（跳过检索）
                        }
                        // 为模型设定检索值
                        models.add(model = targetModel.clone());
                        model.metaValue(resultMeta.targetColumn, targetKey); // 列值（目标字段）
                    }
                    if (models.size() > 0) {
                        Helpers.each(single(scmd, null, null, null, rawClass),
                                new Callable<Void>() {
                                    public Void call(Object... args) {
                                        Object instance = args[1];
                                        Object[] outerArgs = (Object[]) args[2];
                                        Object key = ((Meta) outerArgs[0])
                                                .getValue(instance);
                                        if (key != null) {
                                            for (Object o : targets.get(key)) {
                                                Helpers.toAppend(pending, o,
                                                        instance, false);
                                            }
                                        }
                                        return null;
                                    }
                                }, targetModel.meta(resultMeta.targetColumn));
                    }
                }
                /* 完成数据映射 */
                for (Entry<Object, List<Object>> p : pending.entrySet()) {
                    Helpers.each(p.getKey(), new Callable<Object>() {
                        public Object call(Object... args) {
                            Object instance = args[1];
                            Object[] outerArgs = (Object[]) args[2];
                            ((BoundField) outerArgs[0]).setValue(instance,
                                    outerArgs[1]);
                            return instance;
                        }

                    }, field, p.getValue());
                }
            }
        }
    }

    /**
     * 存在
     * 
     * @param cmd
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public boolean exists(StubCommand cmd,
            @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode,
            @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values) {
        boolean result = true;
        try {
            PluginHelper.toConsole(cmd);
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    private class ResultMeta {
        Meta meta;
        Model model;
        String sourceColumn;
        String sourceField;
        String targetColumn;
        String targetField;
        Map<Object, List<Object>> mapping;
    }

}
