package net.isger.brick.stub;

import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import net.isger.util.Helpers;
import net.isger.util.Strings;

public class PoolStub extends SqlStub {

    private String name;

    public DataSource getDataSource() {
        DataSource dataSource = super.getDataSource();
        if (dataSource == null) {
            if (Strings.isEmpty(name)) {
                Object value;
                Properties props = new Properties();
                for (Entry<String, Object> param : getParameters().entrySet()) {
                    props.setProperty(param.getKey(), Strings.empty((value = param.getValue()) instanceof Number ? ((Number) value).intValue() : value));
                }
                ComboPooledDataSource source = new ComboPooledDataSource();
                String driver = getDriverName();
                if (!props.containsKey("driver")) {
                    props.setProperty("driver", driver);
                }
                if (!props.containsKey("driverClass")) {
                    props.setProperty("driverClass", driver);
                }
                String url = getUrl();
                if (!props.containsKey("jdbc")) {
                    props.setProperty("jdbc", url);
                }
                if (!props.containsKey("jdbcUrl")) {
                    props.setProperty("jdbcUrl", url);
                }
                String user = getUser();
                if (!props.containsKey("username")) {
                    props.setProperty("username", user);
                }
                source.setProperties(props);
                dataSource = source;
            } else {
                dataSource = new ComboPooledDataSource(name);
            }
        }
        return dataSource;
    }

    protected String getDriverName() {
        return Helpers.coalesce(super.getDriverName(), (String) getParameter("driver"), (String) getParameter("driverClass"));
    }

    protected String getUrl() {
        return Helpers.coalesce(super.getUrl(), (String) getParameter("jdbc"), (String) getParameter("jdbcUrl"));
    }

    protected String getUser() {
        return Helpers.coalesce(super.getUser(), (String) getParameter("username"));
    }
}
