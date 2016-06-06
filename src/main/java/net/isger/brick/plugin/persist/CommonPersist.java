package net.isger.brick.plugin.persist;

import java.util.List;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.stub.StubCommand;
import net.isger.util.Reflects;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static final String PARAM_OPCODE = "persist.opcode";

    public static final String PARAM_VALUE = "persist.value";

    private static final Logger LOG;

    protected final Object table;

    @Ignore(mode = Mode.INCLUDE)
    private boolean reset;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    static {
        LOG = LoggerFactory.getLogger(CommonPersist.class);
    }

    public CommonPersist(Object table) {
        this.table = table;
    }

    @Ignore(mode = Mode.INCLUDE)
    public void initial() {
        super.initial();
        StubCommand cmd = getStubCommand();
        cmd.setTable(table);
        if (this.reset) {
            try {
                cmd.setOperate(REMOVE);
                toStub();
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e.getCause());
            }
        } else {
            try {
                cmd.setOperate(SELECT);
                cmd.setCondition(EXISTS);
                toStub();
                return;
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e.getCause());
            }
        }
        cmd.setOperate(CREATE);
        toStub();
        boostrap();
    }

    /**
     * 引导
     */
    protected void boostrap() {
    }

    // /**
    // * 新增
    // *
    // * @param value
    // * @return
    // */
    // public <T> int insert(@Alias(PARAM_VALUE) T value);
    //
    // /**
    // * 新增
    // *
    // * @param values
    // * @return
    // */
    // public <T> int insert(T[] values);

    /**
     * 新增
     *
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public void insert(@Alias(PARAM_OPCODE) Object opcode,
            @Alias(PARAM_VALUE) Object... values) {
        toStub();
    }

    //
    // /**
    // * 修改
    // *
    // * @param value
    // * @return
    // */
    // public <T> int update(@Alias(PARAM_VALUE) T value);
    //
    // /**
    // * 修改
    // *
    // * @param opcode
    // * @param values
    // * @return
    // */
    // public int update(@Alias(PARAM_OPCODE) Object opcode,
    // @Alias(PARAM_VALUE) Object... values);
    //
    /**
     * 查询
     *
     * @param opcode
     * @param values
     * @return
     */
    @Ignore(mode = Mode.INCLUDE)
    public Object single(@Alias(PARAM_OPCODE) Object opcode,
            @Alias(PARAM_VALUE) Object... values) {
        Object result = toStub().getResult();
        if (result instanceof Object[]) {
            Class<?> clazz = Reflects.getClass(this.table);
            if (clazz == null) {
                result = Reflects.toListMap((Object[]) result);
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
    public Object select(@Alias(PARAM_OPCODE) Object opcode,
            @Alias(PARAM_VALUE) Object... values) {
        Object result = toStub().getResult();
        if (result instanceof Object[]) {
            Class<?> clazz = Reflects.getClass(this.table);
            if (clazz == null) {
                result = Reflects.toListMap((Object[]) result);
            } else {
                result = Reflects.toList(clazz, (Object[]) result);
            }
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
    // @Alias(PARAM_VALUE) Object... values);

}
