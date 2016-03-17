package net.isger.brick.stub.dialect;

import net.isger.util.sql.SqlEntry;

/**
 * 方言接口
 * 
 * @author issing
 * 
 */
public interface Dialect {

    public static final String BOOLEAN = "boolean";

    public static final String INTEGER = "integer";

    public static final String NUMBER = "number";

    public static final String DATE = "date";

    public static final String STRING = "string";

    public String getName();

    public boolean isSupport(String name);

    public SqlEntry getCreateEntry(Object table);

    public SqlEntry getCreateEntry(String table, String[][] describes);

    public SqlEntry getInsertEntry(Object table);

    public SqlEntry getInsertEntry(String table, Object[] gridData);

    public SqlEntry getDeleteEntry(Object table);

    public SqlEntry getDeleteEntry(String table, Object[] gridData);

    public SqlEntry getUpdateEntry(Object newTable, Object oldTable);

    public SqlEntry getUpdateEntry(String table, Object[] newGridData,
            Object[] oldGridData);

    public SqlEntry getSearchEntry(Object table);

    public SqlEntry getSearchEntry(String tableName, Object[] columns,
            Object[] gridData);

    public SqlEntry getSearchEntry(String sql, Object[] values);

    public SqlEntry getRemoveEntry(Object table);
    //
    // public String makeSortSQL(String sql, Sort sort);
    //
    // public String getLimitBeforeStatement();
    //
    // public String getLimitAfterStatement();
    //
    // public String getLimitBetweenStatement();
    //
    // public String getLimitOuterJoinBetweenStatement();
    //
    // public String getLimitBeforeNativeQueryStatement();
    //
    // public String getOrderByStatement();

}
