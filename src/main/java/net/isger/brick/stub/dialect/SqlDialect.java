package net.isger.brick.stub.dialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.isger.brick.stub.model.Field;
import net.isger.brick.stub.model.Model;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Sqls;
import net.isger.util.Strings;
import net.isger.util.reflect.BoundField;
import net.isger.util.sql.SqlEntry;

/**
 * 抽象方言
 * 
 * @author issing
 *
 */
public abstract class SqlDialect implements Dialect {

    private static final Describer STRING_DESCRIBER;

    private static final Describer DATE_DESCRIBER;

    /** 方言名称 */
    private String name;

    private Map<String, Describer> describers;

    static {
        STRING_DESCRIBER = new Describer() {
            public String describe() {
                return "VARCHAR(50)";
            }

            public String describe(Field field) {
                return "VARCHAR(" + field.getLength() + ")";
            }
        };
        DATE_DESCRIBER = new Describer() {
            public String describe() {
                return "DATE";
            }

            public String describe(Field field) {
                return "DATE";
            }
        };
    }

    public SqlDialect() {
        describers = new HashMap<String, Describer>();
        addDescriber(STRING, STRING_DESCRIBER);
        addDescriber(DATE, DATE_DESCRIBER);
    }

    public String getName() {
        if (Strings.isEmpty(name)) {
            name = Helpers.getAliasName(this.getClass(), "Dialect$");
        }
        return name;
    }

    public boolean isSupport(String name) {
        return getName().equalsIgnoreCase(name);
    }

    /**
     * 获取创建实例
     * 
     * @param clazz
     * @return
     */
    public SqlEntry getCreateEntry(Object table) {
        return getCreateEntry(getTableName(table), getColumnDescribes(table));
    }

    public SqlEntry getCreateEntry(String table, String[][] describes) {
        StringBuffer sql = new StringBuffer(512);
        sql.append("CREATE TABLE ").append(table).append("(");
        int count;
        for (String[] describe : describes) {
            count = describe.length;
            for (int i = 0; i < count; i++) {
                sql.append(describe[i]).append(" ");
            }
            sql.setLength(sql.length() - 1);
            sql.append(", ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(")");
        return new SqlEntry(sql.toString());
    }

    /**
     * 获取插入实例
     * 
     * @param table
     * @return
     */
    public SqlEntry getInsertEntry(Object table) {
        return getInsertEntry(getTableName(table), getTableData(table));
    }

    public SqlEntry getInsertEntry(String tableName, Object[] gridData) {
        StringBuffer sql = new StringBuffer(512);
        StringBuffer params = new StringBuffer(128);
        sql.append("INSERT INTO ").append(tableName).append("(");
        Object[] columns = (Object[]) gridData[0];
        int count = columns.length;
        for (int i = 0; i < count; i++) {
            sql.append(columns[i]).append(", ");
            params.append("?, ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(") VALUES (");
        params.setLength(params.length() - 2);
        sql.append(params);
        sql.append(")");
        return new SqlEntry(sql.toString(), (Object[]) gridData[1]);
    }

    /**
     * 获取删除实例
     * 
     * @param table
     * @return
     */
    public SqlEntry getDeleteEntry(Object table) {
        return getDeleteEntry(getTableName(table), getTableData(table));
    }

    public SqlEntry getDeleteEntry(String tableName, Object[] gridData) {
        StringBuffer sql = new StringBuffer(512);
        sql.append("DELETE FROM ").append(tableName).append(" WHERE 1 = 1");
        Object[] columns = (Object[]) gridData[0];
        int count = columns.length;
        if (gridData.length == 3 && Strings.isNotEmpty((String) gridData[2])) {
            throw new IllegalStateException(
                    "Unsupported feature in the current version");
        } else {
            for (int i = 0; i < count; i++) {
                sql.append(" AND ").append(columns[i]).append(" = ?");
            }
        }
        return new SqlEntry(sql.toString(), (Object[]) gridData[1]);
    }

    /**
     * 获取修改实例
     * 
     * @param newTable
     * @param oldTable
     * @return
     */
    public SqlEntry getUpdateEntry(Object newTable, Object oldTable) {
        return getUpdateEntry(getTableName(newTable), getTableData(newTable),
                getTableData(oldTable));
    }

    public SqlEntry getUpdateEntry(String tableName, Object[] newGridData,
            Object[] oldGridData) {
        StringBuffer sql = new StringBuffer(512);
        sql.append("UPDATE ").append(tableName).append(" SET ");
        Object[] columns = (Object[]) newGridData[0];
        int count = columns.length;
        for (int i = 0; i < count; i++) {
            sql.append(columns[i]).append(" = ?, ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE 1 = 1");
        columns = (String[]) oldGridData[0];
        count = columns.length;
        if (oldGridData.length == 3
                && Strings.isNotEmpty((String) oldGridData[2])) {
            throw new IllegalStateException(
                    "Unsupported feature in the current version");
        } else {
            for (int i = 0; i < count; i++) {
                sql.append(" AND ").append(columns[i]).append(" = ?");
            }
        }
        return new SqlEntry(sql.toString(), fusion(newGridData[1],
                oldGridData[1]));
    }

    /**
     * 获取查询实例
     * 
     * @param table
     * @return
     */
    public SqlEntry getSearchEntry(Object table) {
        return getSearchEntry(getTableName(table), getColumnNames(table),
                getTableData(table));
    }

    public SqlEntry getSearchEntry(String tableName, Object[] columns,
            Object[] gridData) {
        StringBuffer sql = new StringBuffer(512);
        StringBuffer restrict = new StringBuffer(128);
        sql.append("SELECT ");
        int count = columns.length;
        for (int i = 0; i < count; i++) {
            sql.append(columns[i]).append(", ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" FROM ").append(tableName).append(" WHERE 1 = 1");
        columns = (String[]) gridData[0];
        count = columns.length;
        if (gridData.length == 3 && Strings.isNotEmpty((String) gridData[2])) {
            throw new IllegalStateException(
                    "Unsupported feature in the current version");
        } else {
            for (int i = 0; i < count; i++) {
                restrict.append(" AND ").append(columns[i]).append(" = ?");
            }
            sql.append(restrict);
        }
        return getSearchEntry(sql.toString(), (Object[]) gridData[1]);
    }

    public SqlEntry getSearchEntry(String sql, Object[] values) {
        Page page = getPage(values);
        if (page == null) {
            return new SqlEntry(sql, values);
        }
        Object[] target = new Object[values.length - 1];
        System.arraycopy(values, 0, target, 0, target.length);
        return getSearchEntry(page, sql, target);
    }

    protected SqlEntry getSearchEntry(Page page, String sql, Object[] values) {
        return new PageSql(page, sql, values);
    }

    protected Page getPage(Object[] values) {
        if (values != null && values.length > 0
                && values[values.length - 1] instanceof Page) {
            return (Page) values[values.length - 1];
        }
        return null;
    }

    public SqlEntry getRemoveEntry(Object table) {
        return new SqlEntry(new StringBuffer("DROP TABLE ").append(
                getTableName(table)).toString());
    }

    protected String getTableName(Object table) {
        if (table instanceof Model) {
            return ((Model) table).getModelName();
        } else if (table instanceof String) {
            return (String) table;
        }
        return Sqls.getTableName(table.getClass());
    }

    protected String[] getColumnNames(Object table) {
        if (table instanceof Model) {
            return getColumnNames((Model) table);
        }
        String column;
        List<String> columns = new ArrayList<String>();
        BoundField field;
        for (List<BoundField> fields : Reflects
                .getBoundFields(table.getClass()).values()) {
            field = fields.get(0);
            column = field.getAliasName();
            if (column == null) {
                column = Sqls.toColumnName(field.getName());
            }
            columns.add(column);
        }
        return columns.toArray(new String[columns.size()]);
    }

    protected String[] getColumnNames(Model table) {
        List<String> fieldNames = table.getFieldNames();
        return fieldNames.toArray(new String[fieldNames.size()]);
    }

    protected String[][] getColumnDescribes(Object table) {
        if (table instanceof Model) {
            return getColumnDescribes((Model) table);
        } else if (!(table instanceof Class)) {
            table = table.getClass();
        }
        BoundField[] fields = Reflects.getBoundFields((Class<?>) table)
                .values().toArray(new BoundField[0]);
        Describer describer;
        int size = fields.length;
        String[][] describes = new String[size][];
        for (int i = 0; i < size; i++) {
            describes[i] = new String[2];
            describes[i][0] = fields[i].getAliasName();
            if (describes[i][0] == null) {
                describes[i][0] = Sqls.toColumnName(fields[i].getName());
            }
            describes[i][1] = fields[i].getField().getType().getSimpleName()
                    .toLowerCase();
            describer = describers.get(describes[i][1]);
            if (describer == null) {
                describer = describers.get(STRING);
            }
            describes[i][1] = describer.describe();
        }
        return describes;
    }

    protected String[][] getColumnDescribes(Model table) {
        List<String> fieldNames = table.getFieldNames();
        int fieldCount = fieldNames.size();
        String[][] describes = new String[fieldCount][];
        for (int i = 0; i < fieldCount; i++) {
            describes[i] = getColumnDescribe(table.getField(fieldNames.get(i)));
        }
        return describes;
    }

    protected String[] getColumnDescribe(Field field) {
        String type = field.getType().toLowerCase();
        Describer describer = describers.get(type);
        if (describer != null) {
            type = describer.describe(field);
        }
        return new String[] { field.getName(), type };
    }

    protected Object[] getTableData(Object table) {
        if (table instanceof Model) {
            return getTableData((Model) table);
        }
        String column;
        Object value;
        List<String> columns = new ArrayList<String>();
        List<Object> row = new ArrayList<Object>();
        BoundField field;
        for (List<BoundField> fields : Reflects
                .getBoundFields(table.getClass()).values()) {
            field = fields.get(0);
            column = field.getAliasName();
            if (column == null) {
                column = Sqls.toColumnName(field.getName());
            }
            value = field.getValue(table);
            if (value != null) {
                columns.add(column);
                row.add(value);
            }
        }
        return new Object[] { columns.toArray(new String[columns.size()]),
                row.toArray() };
    }

    protected Object[] getTableData(Model table) {
        List<String> fieldNames = table.getFieldNames();
        int fieldCount = fieldNames.size();
        String column;
        Object value;
        List<String> columns = new ArrayList<String>();
        List<Object> row = new ArrayList<Object>();
        for (int i = 0; i < fieldCount; i++) {
            column = fieldNames.get(i);
            value = table.getField(column).getValue();
            if (value != null) {
                columns.add(column);
                row.add(value);
            }
        }
        return new Object[] { columns.toArray(new String[columns.size()]),
                row.toArray() };
    }

    protected void addDescriber(String type, Describer describer) {
        describers.put(type, describer);
    }

    private Object[] fusion(Object array1, Object array2) {
        /* 合并数组 */
        Object[] result;
        if (array1 instanceof Object[][]) {
            Object[][] newData = (Object[][]) array1;
            Object[][] oldData = (Object[][]) array2;
            int count = newData.length;
            List<Object> row;
            List<Object[]> rows = new ArrayList<Object[]>();
            for (int i = 0; i < count; i++) {
                row = new ArrayList<Object>();
                row.addAll(Arrays.asList(newData[i]));
                row.addAll(Arrays.asList(oldData[i]));
                rows.add(row.toArray());
            }
            result = rows.toArray(new Object[rows.size()][]);
        } else {
            List<Object> row = new ArrayList<Object>();
            row.addAll(Arrays.asList((Object[]) array1));
            row.addAll(Arrays.asList((Object[]) array2));
            result = row.toArray();
        }
        return result;
    }

    // public String makeSortSQL(String sql, Sort sort) {
    // // TODO 容易产生SQL注入问题，必须改进
    // // 排序名称及方式不信任用户输入
    // return "SELECT * FROM (" + sql + ") t ORDER BY "
    // + Sqls.toColumnName(sort.getName()) + " " + sort.getOrder();
    // }

}
