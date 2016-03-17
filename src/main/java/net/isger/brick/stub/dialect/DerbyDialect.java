package net.isger.brick.stub.dialect;

public class DerbyDialect extends SqlDialect {

    private static final String DRIVER_NAME = "org.apache.derby.jdbc.EmbeddedDriver";

    public boolean isSupport(String name) {
        return super.isSupport(name) || DRIVER_NAME.equals(name);
    }

}
