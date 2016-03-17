package net.isger.brick.stub;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * 基础数据源
 * 
 * @author issing
 *
 */
public class BaseDataSource implements DataSource {

    private String driverName;

    private String url;

    private String user;

    private String password;

    public BaseDataSource(String driverName, String url, String user,
            String password) {
        super();
        this.driverName = driverName;
        this.url = url;
        this.user = user;
        this.password = password;
        try {
            Class.forName(this.driverName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Invalid driver " + driverName);
        }
    }

    public Connection getConnection() throws SQLException {
        return getConnection(this.user, this.password);
    }

    public Connection getConnection(String user, String password)
            throws SQLException {
        return DriverManager.getConnection(this.url, user, password);
    }

    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

}
