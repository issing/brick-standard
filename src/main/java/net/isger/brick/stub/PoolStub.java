package net.isger.brick.stub;

import org.logicalcobwebs.proxool.ProxoolFacade;

import net.isger.brick.stub.dialect.Dialect;
import net.isger.util.Helpers;
import net.isger.util.Strings;

public class PoolStub extends SqlStub {

    private static final String DRIVER_NAME = "org.logicalcobwebs.proxool.ProxoolDriver";

    private String name;

    protected Dialect getDialect(String driverName) {
        return Dialects.getDialect(super.getDriverName());
    }

    protected String getDialectName() {
        String dialectName = super.getDialectName();
        if (dialectName == null) {
            Dialect dialect = Dialects.getDialect(super.getDriverName());
            if (dialect != null) {
                dialectName = dialect.getName();
            }
        }
        return dialectName;
    }

    protected String getDriverName() {
        return DRIVER_NAME;
    }

    protected String getUrl() {
        if (Strings.isEmpty(name)) {
            name = Helpers.getAliasName(this.getClass(), "Stub$");
        }
        return "proxool." + name + ":" + super.getDriverName() + ":"
                + super.getUrl();
    }

    public void destroy() {
        if (DRIVER_NAME.equals(getDriverName())) {
            ProxoolFacade.shutdown(0);
        }
    }

}
