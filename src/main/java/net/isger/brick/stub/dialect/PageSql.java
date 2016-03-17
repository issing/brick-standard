package net.isger.brick.stub.dialect;

import net.isger.util.sql.SqlEntry;

public class PageSql extends SqlEntry {

    private Page page;

    public PageSql(Page page, String sql, Object... values) {
        super(sql, values);
        this.page = page;
    }

    public Page getPage() {
        return page;
    }

    public String getSql() {
        return sql + " limit ?, ?";
    }

    public Object[] getValues() {
        int valCount = 2;
        Object[] wrapValues = null;
        Object[] values = this.values;
        if (values != null) {
            valCount += values.length;
            wrapValues = new Object[valCount];
            System.arraycopy(values, 0, wrapValues, 0, values.length);
        } else {
            wrapValues = new Object[valCount];
        }
        wrapValues[valCount - 2] = (page.getStart() - 1) * page.getLimit();
        wrapValues[valCount - 1] = page.getLimit();
        return wrapValues;
    }

    public String getCountSql() {
        if (page.getTotal() > 0) {
            return null;
        }
        return "select count(1) from (" + super.getSql() + ") t";
    }

}
