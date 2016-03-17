package net.isger.brick.stub;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import net.isger.brick.stub.dialect.Dialect;
import net.isger.brick.stub.dialect.PageSql;
import net.isger.util.Callable;
import net.isger.util.Helpers;
import net.isger.util.Sqls;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.sql.SqlEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 结构化查询语言存根
 * 
 * @author issing
 *
 */
@Ignore
public class SqlStub extends AbstractStub {

    public static final String PARAM_JNDINAME = "jndiName";

    public static final String PARAM_DRIVERNAME = "driverName";

    public static final String PARAM_URL = "url";

    public static final String PARAM_USER = "user";

    public static final String PARAM_PASSWORD = "password";

    private static final Logger LOG;

    @Ignore(mode = Mode.INCLUDE)
    private String dialectName;

    @Ignore(mode = Mode.INCLUDE)
    private DataSource dataSource;

    @Ignore(mode = Mode.INCLUDE)
    private Dialect dialect;

    static {
        LOG = LoggerFactory.getLogger(SqlStub.class);
    }

    @Ignore
    public void initial() {
        /* 初始数据源 */
        if (dataSource == null) {
            String value;
            // JNDI
            if (Strings.isNotEmpty(value = getJndiName())) {
                try {
                    dataSource = (DataSource) new InitialContext().lookup(value
                            .trim());
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Couldn't lookup DataSource from " + value + " - "
                                    + e.getMessage(), e);
                }
            }
            // JDBC
            else if (Strings.isNotEmpty(value = getUrl())
                    && Strings.isNotEmpty(getDriverName())) {
                if (LOG.isDebugEnabled()) {
                    LOG.info("Initializing DataSource url {}", value);
                }
                dataSource = new BaseDataSource(getDriverName(), value,
                        getUser(), getPassword());
            } else {
                throw new IllegalStateException(
                        "Unexpected DataSource or JDBC configuration. Make sure you use is the SQL stub");
            }
        }
        /* 初始方言 */
        String name = getDialectName();
        if (dialect == null) {
            dialect = Dialects.getDialect(name);
            if (dialect == null) {
                dialect = Dialects.getDialect(getDriverName());
            }
            name = dialect.getName();
        }
        dialectName = name;
    }

    protected String getDialectName() {
        return dialectName;
    }

    protected String getJndiName() {
        return (String) getParameter(PARAM_JNDINAME);
    }

    protected String getDriverName() {
        return (String) getParameter(PARAM_DRIVERNAME);
    }

    protected String getUrl() {
        return (String) getParameter(PARAM_URL);
    }

    protected String getUser() {
        return (String) getParameter(PARAM_USER);
    }

    protected String getPassword() {
        return (String) getParameter(PARAM_PASSWORD);
    }

    protected DataSource getDataSource() {
        return dataSource;
    }

    protected Dialect getDialect() {
        return dialect;
    }

    /**
     * 获取库连接
     * 
     * @return
     */
    protected Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Failure to connect database",
                    e.getCause());
        }
    }

    /**
     * 创建数据表
     */
    @Ignore
    public void create(StubCommand cmd) {
        Object table = cmd.getTable();
        final Connection conn = getConnection();
        Object[] condition = getCondition(cmd, 3);
        Object result;
        try {
            if (table instanceof String) {
                result = Sqls.modify(dialect.getCreateEntry((String) table,
                        (String[][]) condition[0]), conn);
            } else if (table instanceof Class) {
                if (condition == null) {
                    result = Sqls.modify(
                            dialect.getCreateEntry((Class<?>) table), conn);
                } else {
                    result = modify(cmd.getOperate(), (Class<?>) table,
                            condition, conn);
                }
            } else {
                result = each(table, new Callable<Object>() {
                    public Object call(Object... args) {
                        return Sqls.modify(dialect.getCreateEntry(args[0]),
                                conn);
                    }
                });
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 插入表数据
     */
    @Ignore
    public void insert(StubCommand cmd) {
        Object table = cmd.getTable();
        Object[] condition = getCondition(cmd, 3);
        final Connection conn = getConnection();
        Object result;
        try {
            if (table instanceof String) {
                result = Sqls
                        .modify(dialect.getInsertEntry((String) table,
                                condition), conn);
            } else if (table instanceof Class) {
                result = modify(cmd.getOperate(), (Class<?>) table, condition,
                        conn);
            } else {
                result = each(table, new Callable<Object>() {
                    public Object call(Object... args) {
                        return Sqls.modify(dialect.getInsertEntry(args[0]),
                                conn);
                    }
                });
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 删除表数据
     */
    @Ignore
    public void delete(StubCommand cmd) {
        Object table = cmd.getTable();
        Object[] condition = getCondition(cmd, 3);
        final Connection conn = getConnection();
        Object result;
        try {
            if (table instanceof String) {
                result = Sqls.modify(dialect.getDeleteEntry((String) table,
                        (Object[]) condition[0]), conn);
            } else if (table instanceof Class) {
                result = modify(cmd.getOperate(), (Class<?>) table, condition,
                        conn);
            } else {
                result = each(table, new Callable<Object>() {
                    public Object call(Object... args) {
                        return Sqls.modify(dialect.getDeleteEntry(args[0]),
                                conn);
                    }
                });
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 修改表数据
     */
    @Ignore
    public void update(StubCommand cmd) {
        Object table = cmd.getTable();
        Object[] condition = getCondition(cmd, 3);
        final Connection conn = getConnection();
        Object result;
        try {
            if (table instanceof String) {
                Object[][] values = (Object[][]) condition[0];
                result = Sqls.modify(dialect.getUpdateEntry((String) table,
                        values[0], values[1]), conn);
            } else if (table instanceof Class) {
                result = modify(cmd.getOperate(), (Class<?>) table, condition,
                        conn);
            } else {
                result = each(new Object[] { (Object[]) table },
                        new Callable<Object>() {
                            public Object call(Object... args) {
                                return Sqls.modify(dialect.getUpdateEntry(
                                        args[0], args[1]), conn);
                            }
                        });
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 查询表数据
     */
    @Ignore
    public void search(StubCommand cmd) {
        Object table = cmd.getTable();
        Object[] condition = getCondition(cmd, 3);
        final Connection conn = getConnection();
        Object[] result;
        search: try {
            SqlEntry sqlEntry;
            if (table instanceof String) {
                sqlEntry = dialect.getSearchEntry((String) table,
                        (String[]) condition[0], (Object[]) condition[1]);
                result = Sqls.query(sqlEntry, conn);
            } else if (table instanceof Class) {
                canonicalize(cmd.getOperate(), condition);
                String sql = Sqls.getSQL((Class<?>) table, dialectName,
                        (String) condition[0], (Object[]) condition[2]);
                sqlEntry = dialect.getSearchEntry(sql, (Object[]) condition[1]);
                result = Sqls.query(sqlEntry, conn);
            } else {
                result = (Object[]) each(table, new Callable<Object>() {
                    public Object call(Object... args) {
                        return Sqls.query(dialect.getSearchEntry(args[0]), conn);
                    }
                });
                break search;
            }
            /* 获取数据总数（分页） */
            if (sqlEntry instanceof PageSql) {
                String countSql = ((PageSql) sqlEntry).getCountSql();
                if (countSql != null) {
                    Object[] target = new Object[result.length + 1];
                    System.arraycopy(result, 0, target, 0, result.length);
                    target[result.length] = ((Number) ((Object[][]) Sqls.query(
                            countSql, sqlEntry.getValues(), conn)[1])[0][0])
                            .longValue();
                    result = (Object[]) target;

                }
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 删除数据表
     */
    @Ignore
    public void remove(StubCommand cmd) {
        Object table = cmd.getTable();
        final Connection conn = getConnection();
        Object result;
        try {
            result = each(table, new Callable<Object>() {
                public Object call(Object... args) {
                    return Sqls.modify(dialect.getRemoveEntry(args[0]), conn);
                }
            });
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 遍历操作
     * 
     * @param table
     * @param callable
     * @return
     */
    protected Object each(Object table, Callable<Object> callable) {
        int size;
        if (table instanceof Collection) {
            Collection<?> instance = (Collection<?>) table;
            size = instance.size();
            table = instance.toArray(new Object[size]);
        } else if (table.getClass().isArray()) {
            size = Array.getLength(table);
        } else {
            size = 1;
            table = new Object[] { table };
        }
        Object[] result = new Object[size];
        Object instance;
        for (int i = 0; i < size; i++) {
            instance = Array.get(table, i);
            if (instance != null && instance.getClass().isArray()) {
                result[i] = callable.call((Object[]) instance);
            } else {
                result[i] = callable.call(instance);
            }
        }
        return (size == 1) ? result[0] : result;
    }

    /**
     * 获取条件
     * 
     * @param cmd
     * @param type
     * @return
     */
    protected Object[] getCondition(StubCommand cmd, int type) {
        Object config = cmd.getCondition();
        if (config instanceof Condition) {
            throw new IllegalStateException(
                    "Unsupported feature in the current version");
        }
        return (Object[]) Helpers.copyArray(config, type);
    }

    /**
     * 修改操作
     * 
     * @param operate
     * @param table
     * @param condition
     * @param conn
     * @return
     */
    protected Object modify(String operate, Class<?> table, Object[] condition,
            Connection conn) {
        canonicalize(operate, condition);
        Object values = condition[1];
        String sql = Sqls.getSQL(table, dialectName, (String) condition[0],
                (Object[]) condition[2]);
        return values instanceof Object[][] ? Sqls.modify(sql,
                (Object[][]) values, conn) : Sqls.modify(sql,
                (Object[]) values, conn);
    }

    /**
     * 规范操作条件
     * 
     * @param operate
     * @param condition
     */
    private void canonicalize(String operate, Object[] condition) {
        if (!(condition[0] == null || condition[0] instanceof String)) {
            int index = condition.length - 1;
            while (index > 0) {
                condition[index] = condition[--index];
            }
            condition[index] = operate;
        }
    }

    /**
     * 关闭库连接
     * 
     * @param conn
     */
    public void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    @Ignore
    public void destroy() {
    }

}
