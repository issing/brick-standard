package net.isger.brick.plugin.persist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.core.CoreHelper;
import net.isger.brick.plugin.PluginConstants;
import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.stub.StubCommand;
import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Metas;
import net.isger.brick.stub.model.Model;
import net.isger.util.Callable;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.reflect.AssemblerAdapter;
import net.isger.util.reflect.BoundField;
import net.isger.util.reflect.TypeToken;
import net.isger.util.sql.Pager;

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

    private static final Logger LOG;

    @Ignore(mode = Mode.INCLUDE)
    private boolean reset;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    private boolean create;

    private Object[] tables;

    static {
        LOG = LoggerFactory.getLogger(CommonPersist.class);
    }

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

    /**
     * 创建表（含元模型）
     * 
     * @param cmd
     * @param table
     * @return
     */
    private boolean create(StubCommand cmd, Object table) {
        /* 创建基础表 */
        cmd.setTable(table);
        if (this.reset) {
            try {
                cmd.setOperate(REMOVE);
                CoreHelper.toConsole(cmd);
            } catch (Exception e) {
            }
        } else {
            try {
                cmd.setOperate(EXISTS);
                CoreHelper.toConsole(cmd);
                return false;
            } catch (Exception e) {
            }
        }
        cmd.setOperate(CREATE);
        CoreHelper.toConsole(cmd);
        /* 创建元模型 */
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
     * 存在
     * 
     * @param cmd
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public boolean exists(StubCommand cmd, @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode, @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values) {
        boolean result = true;
        try {
            CoreHelper.toConsole(cmd);
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * 新增
     *
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public void insert(StubCommand cmd, @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode, @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values) {
        CoreHelper.toConsole(cmd);
    }

    /**
     * 删除
     *
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public void delete(StubCommand cmd, @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode, @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values) {
        CoreHelper.toConsole(cmd);
    }

    /**
     * 修改
     *
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public void update(StubCommand cmd, @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode, @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values) {
        CoreHelper.toConsole(cmd);
    }

    /**
     * 查询
     *
     * @param opcode
     * @param values
     */
    @Ignore(mode = Mode.INCLUDE)
    public Object select(StubCommand cmd, @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode, @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values, @Alias(PluginConstants.PARAM_STATEMENT_ARGS) Object[] args, @Alias(PluginConstants.PARAM_BEAN) Object bean, @Alias(PluginConstants.PARAM_PAGE) Pager page) {
        boolean isMultiple = Helpers.isMultiple(cmd.getTable());
        Object result = CoreHelper.toConsole(cmd);
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
    public Object single(StubCommand cmd, @Alias(PluginConstants.PARAM_STATEMENT_ID) Object opcode, @Alias(PluginConstants.PARAM_STATEMENT_VALUE) Object[] values, @Alias(PluginConstants.PARAM_STATEMENT_ARGS) Object[] args, @Alias(PluginConstants.PARAM_BEAN) Object bean) {
        PluginHelper.enablePage(cmd, false); // 关闭分页功能
        boolean isMultiple = Helpers.isMultiple(cmd.getTable());
        Object result = CoreHelper.toConsole(cmd);
        if (isMultiple) {
            List<Object> pendings = new ArrayList<Object>();
            for (Object pending : (Object[]) result) {
                pendings.add(Helpers.getElement(toResult(cmd, bean, null, (Object[]) pending), 0));
            }
            result = pendings;
        } else {
            result = Helpers.getElement(toResult(cmd, bean, null, (Object[]) result), 0);
        }
        return result;
    }

    /**
     * 结果
     * 
     * @param cmd
     * @param bean
     * @param page
     * @param grid
     * @return
     */
    @SuppressWarnings("unchecked")
    private Object toResult(StubCommand cmd, Object bean, Pager page, Object[] grid) {
        Object result;
        Object value = grid[grid.length - 1];
        if (value instanceof Number) {
            page.setTotal(((Number) value).intValue());
        }
        if (bean == null) {
            bean = this.tables[0];
        }
        Class<?> rawClass = Reflects.getClass(bean);
        if (rawClass == null || Map.class.isAssignableFrom(rawClass)) {
            result = Reflects.toList(grid);
        } else if (String.class.isAssignableFrom(rawClass) || Reflects.getPrimitiveClass(rawClass) != null) {
            result = Helpers.compact(grid[1]);
        } else if (Model.class.isAssignableFrom(rawClass)) {
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
            result = toResult(cmd, rawClass, grid);
        }
        result = toExtend(cmd, result);
        if (page != null) {
            result = new Object[] { result, page };
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object toResult(final StubCommand cmd, Class<?> clazz, Object[] grid) {
        final Map<BoundField, ResultMeta> metas = new HashMap<BoundField, ResultMeta>();
        /* 提取所有待映射的结果元 */
        Object result = Reflects.toList(clazz, grid, new AssemblerAdapter() {
            public Class<?> assemble(Class<?> rawClass) {
                if (rawClass.isInterface()) {
                    rawClass = console.getContainer().getInstance(Class.class, (Strings.toColumnName(rawClass.getSimpleName()).replaceAll("[_]", ".") + ".class"));
                }
                return rawClass;
            }

            public Object assemble(BoundField field, Object instance, Object value, Object... args) {
                Map<String, Object> data = (Map<String, Object>) args[0]; // 行值（原始数据）
                /* 字段结果元 */
                ResultMeta resultMeta = metas.get(field);
                if (resultMeta == null) {
                    metas.put(field, resultMeta = createResultMeta(cmd, field)); // 添加字段结果元
                }
                /* 引用数据类型 */
                if (resultMeta.meta.isReference()) {
                    switch (resultMeta.meta.getMode()) {
                    // 桥接模式处理（外部表）
                    case Meta.MODE_REFERENCE_BRIDGE:
                        Helpers.toAppend(resultMeta.mapping, data.get(resultMeta.sourceField), instance); // 向结果元添加字段值与实例映射
                        break;
                    }
                }
                /* 内联数据类型 */
                else {
                    String fieldName = Strings.toFieldName(resultMeta.sourceColumn); // 获取字段名
                    Object fieldValue = Helpers.getInstance(data, fieldName); // 获取字段值
                    if (value == Reflects.UNKNOWN) {
                        value = fieldValue; // 使用字段值替换
                    }
                    // 集合对象
                    else if (value instanceof Map) {
                        if (fieldValue == null) {
                            fieldValue = ((Map<String, Object>) value).get(resultMeta.sourceField);
                        } else if (fieldValue != value) {
                            ((Map<String, Object>) value).put(resultMeta.sourceField, fieldValue);
                        }
                    }
                    Helpers.toAppend(resultMeta.mapping, fieldValue, instance); // 向结果元添加字段值与实例映射
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

    /**
     * 创建结果元
     * 
     * @param cmd
     * @param field
     * @return
     */
    @SuppressWarnings("unchecked")
    private ResultMeta createResultMeta(StubCommand cmd, BoundField field) {
        ResultMeta resultMeta = new ResultMeta();
        resultMeta.meta = Meta.createMeta(field); // 字段元
        resultMeta.mapping = new HashMap<Object, List<Object>>(); // 映射集合
        if ((resultMeta.model = resultMeta.meta.toModel()) == null) {
            Class<?> rawClass = field.getToken().getRawClass();
            if (rawClass.isInterface()) {
                rawClass = console.getContainer().getInstance(Class.class, (Strings.toColumnName(rawClass.getSimpleName()).replaceAll("[_]", ".") + ".class"));
            }
            resultMeta.model = rawClass == null ? null : Model.create(rawClass);
            resultMeta.sourceColumn = resultMeta.meta.getName();
            resultMeta.targetColumn = (String) resultMeta.meta.getValue();
            resultMeta.sourceField = Strings.toFieldName(resultMeta.targetColumn);
        } else {
            Map<String, Object> config = (Map<String, Object>) resultMeta.meta.getValue();
            Map<String, Object> source = (Map<String, Object>) config.get("source");
            resultMeta.sourceColumn = (String) source.get("name");
            resultMeta.sourceField = Strings.toFieldName((String) source.get("value"));
            Map<String, Object> target = (Map<String, Object>) config.get("target");
            resultMeta.targetColumn = (String) target.get("value");
            resultMeta.targetField = Strings.toFieldName((String) target.get("name"));
        }
        resultMeta.model.metaEmpty();
        return resultMeta;
    }

    /**
     * 结果元映射
     * 
     * @param cmd
     * @param metas
     */
    @SuppressWarnings("unchecked")
    private void toMapping(StubCommand cmd, Map<BoundField, ResultMeta> metas) {
        StubCommand scmd;
        Model model;
        List<Model> models;
        BoundField boundField;
        ResultMeta resultMeta;
        for (Entry<BoundField, ResultMeta> entry : metas.entrySet()) {
            resultMeta = entry.getValue();
            if (resultMeta.model == null) {
                continue;
            }
            scmd = cmd.clone();
            models = new ArrayList<Model>();
            boundField = entry.getKey();
            /* 获取映射目标 */
            final Map<Object, List<Object>> targets = new HashMap<Object, List<Object>>();
            if (Strings.isEmpty(resultMeta.targetField)) {
                targets.putAll(resultMeta.mapping);
            } else {
                /* 查询映射数据（关系表） */
                scmd.setTable(models);
                for (Object sourceValue : resultMeta.mapping.keySet()) {
                    models.add(model = resultMeta.model.clone());
                    model.metaValue(resultMeta.sourceColumn, sourceValue); // 列值（源字段）
                }
                Helpers.each(select(scmd, null, null, null, Map.class, null), new Callable.Runnable() {
                    public void run(Object... args) {
                        Integer index = (Integer) args[0];
                        List<Map<String, Object>> values = (List<Map<String, Object>>) args[1]; // 映射数据结果集
                        Object[] outerArgs = (Object[]) args[2];
                        Model mappingModel = ((List<Model>) outerArgs[0]).get(index); // 获取映射数据结果集对应模型
                        ResultMeta outerMeta = (ResultMeta) outerArgs[1];
                        List<Object> instances = outerMeta.mapping.get(mappingModel.metaValue(outerMeta.sourceColumn)); // 待映射实例
                        for (Map<String, Object> value : values) {
                            Helpers.toAppend(targets, value.get(outerMeta.targetField), instances, false); // 添加映射目标
                        }
                    }
                }, models, resultMeta);
            }
            /* 获取元素类型 */
            TypeToken<?> typeToken = boundField.getToken();
            Class<?> rawClass = typeToken.getRawClass();
            if (Collection.class.isAssignableFrom(rawClass)) {
                rawClass = (Class<?>) Reflects.getActualType(typeToken.getType());
            } else if (rawClass.isArray()) {
                rawClass = (Class<?>) Reflects.getComponentType(typeToken.getType());
            }
            if (rawClass.isInterface()) {
                rawClass = console.getContainer().getInstance(Class.class, (Strings.toColumnName(rawClass.getSimpleName()).replaceAll("[_]", ".") + ".class"));
            }
            final Map<Object, List<Object>> pending = new HashMap<Object, List<Object>>(); // 待注入值
            /* 目标直接赋值（未配置目标列） */
            if (Strings.isEmpty(resultMeta.targetColumn)) {
                for (Entry<Object, List<Object>> targetEntry : targets.entrySet()) {
                    for (Object instance : targetEntry.getValue()) {
                        Helpers.toAppend(pending, instance, targetEntry.getKey(), false);
                    }
                }
            }
            /* 查询目标数据（已配置目标列） */
            else {
                // 构建目标查询模型（根据唯一键）
                List<Object> sources = new ArrayList<Object>();
                Model targetModel = Model.create(rawClass);
                targetModel.metaEmpty();
                scmd.setTable(models = new ArrayList<Model>());
                for (Object targetValue : targets.keySet()) {
                    if (Strings.isEmpty(targetValue)) {
                        continue; // 映射值为空（跳过检索）
                    }
                    // 为模型设定检索值
                    models.add(model = targetModel.clone());
                    model.metaValue(resultMeta.targetColumn, targetValue); // 列值（目标字段）
                    // 添加源对象（用于外部表容错）
                    Map<String, Object> source = new HashMap<String, Object>();
                    source.put(resultMeta.targetColumn, targetValue);
                    sources.add(Reflects.newInstance(rawClass, source));
                }
                if (models.size() > 0) {
                    Object instances;
                    try {
                        instances = single(scmd, null, null, null, rawClass);
                    } catch (Exception e) {
                        instances = sources;
                    }
                    Helpers.each(instances, new Callable<Void>() {
                        public Void call(Object... args) {
                            Object instance = args[1];
                            Object[] outerArgs = (Object[]) args[2];
                            Object key = ((Meta) outerArgs[0]).getValue(instance);
                            if (key != null) {
                                for (Object o : targets.get(key)) {
                                    Helpers.toAppend(pending, o, instance, false);
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
                        ((BoundField) outerArgs[0]).setValue(instance, outerArgs[1]);
                        return instance;
                    }

                }, boundField, p.getValue());
            }
        }
    }

    /**
     * 扩展
     * 
     * @param cmd
     * @param result
     * @return
     */
    private Object toExtend(final StubCommand cmd, Object result) {
        LOG.debug("(!) The extension method for data result processing has not been implemented.");
        return result;
    }

    /**
     * 结果元数据
     * 
     * @author issing
     */
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
