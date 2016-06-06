package net.isger.brick.stub;

import net.isger.brick.stub.dialect.Dialect;
import net.isger.brick.stub.dialect.Dialects;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;

import org.logicalcobwebs.proxool.ProxoolFacade;

public class PoolStub extends SqlStub {

    private static final String DRIVER_NAME = "org.logicalcobwebs.proxool.ProxoolDriver";

    private String name;

    protected Dialect getDialect() {
        Dialect dialect = super.getDialect();
        if (dialect == null) {
            dialect = Dialects.getDialect(super.getDriverName());
        }
        return dialect;
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

    @Ignore
    public void destroy() {
        if (DRIVER_NAME.equals(getDriverName())) {
            ProxoolFacade.shutdown(0);
        }
    }

}
