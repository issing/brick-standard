package net.isger.brick.stub.dialect;

/**
 * H2
 * 
 * @author issing
 *
 */
public class H2Dialect extends SqlDialect {

    private static final String DRIVER_NAME = "org.h2.Driver";

    public boolean isSupport(String name) {
        return super.isSupport(name) || DRIVER_NAME.equals(name);
    }

    public PageSql getSearchEntry(Page page, String sql, Object[] values) {
        return new PageSql(page, sql, values) {

            public Object[] getValues() {
                Page page = super.getPage();
                int valCount = 2;
                Object[] wrapValues = null;
                Object[] values = super.getValues();
                if (values != null) {
                    valCount += values.length;
                    wrapValues = new Object[valCount];
                    System.arraycopy(values, 0, wrapValues, 0, values.length);
                } else {
                    wrapValues = new Object[valCount];
                }
                wrapValues[valCount - 1] = page.getStart() + page.getLimit();
                wrapValues[valCount - 2] = page.getStart();
                return wrapValues;
            }

        };
    }

}
