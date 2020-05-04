package net.isger.brick.stub;

import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.isger.util.Helpers;
import net.isger.util.Strings;

public class PoolStub extends SqlStub {

    private String name;

    public DataSource getDataSource() {
        DataSource dataSource = super.getDataSource();
        if (dataSource == null) {
            Properties props = new Properties();
            Object value;
            for (Entry<String, Object> param : getParameters().entrySet()) {
                props.setProperty(param.getKey(), Strings.empty((value = param.getValue()) instanceof Number ? ((Number) value).intValue() : value));
            }
            HikariConfig config;
            if (Strings.isEmpty(name)) {
                config = new HikariConfig();
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            } else {
                config = new HikariConfig(name.endsWith(".properties") ? name : name + ".properties");
            }
            config.setDataSourceProperties(props);
            config.setDriverClassName(getDriverName());
            config.setJdbcUrl(getUrl());
            config.setUsername(getUser());
            config.setPassword(getPassword());
            dataSource = new HikariDataSource(config);
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
