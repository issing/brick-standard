package net.isger.brick.stub.dialect;

/**
 * MySQL
 * 
 * @author issing
 *
 */
public class MySQLDialect extends SqlDialect {

    private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";

    public boolean isSupport(String name) {
        return super.isSupport(name) || DRIVER_NAME.equals(name);
    }

}
