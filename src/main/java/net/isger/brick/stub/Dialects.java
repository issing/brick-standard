package net.isger.brick.stub;

import java.util.Hashtable;
import java.util.Map;

import net.isger.brick.stub.dialect.DerbyDialect;
import net.isger.brick.stub.dialect.Dialect;
import net.isger.brick.stub.dialect.H2Dialect;
import net.isger.brick.stub.dialect.MySQLDialect;
import net.isger.brick.stub.dialect.OracleDialect;
import net.isger.util.hitch.Director;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dialects {

    private static final String KEY_DIALECTS = "brick.core.dialects";

    private static final String DIALECT_PATH = "net/isger/brick/stub/dialect";

    private static final Logger LOG;

    private static final Dialects DIALECTS;

    private Map<String, Dialect> dialects;

    static {
        LOG = LoggerFactory.getLogger(Dialects.class);
        DIALECTS = new Dialects();
        addDialect(new H2Dialect());
        addDialect(new MySQLDialect());
        addDialect(new OracleDialect());
        addDialect(new DerbyDialect());
        new Director() {
            protected String directPath() {
                return directPath(KEY_DIALECTS, DIALECT_PATH);
            }
        }.direct(DIALECTS);
    }

    private Dialects() {
        dialects = new Hashtable<String, Dialect>();
    }

    public static void addDialect(Dialect dialect) {
        String name = dialect.getClass().getName();
        if (LOG.isDebugEnabled()) {
            LOG.info("Achieve dialect [{}]", name);
        }
        dialect = DIALECTS.dialects.put(name, dialect);
        if (dialect != null && LOG.isDebugEnabled()) {
            LOG.warn("(!) Discard dialect [{}]", dialect);
        }
    }

    public static Dialect getDialect(String driverName) {
        Dialect result = null;
        for (Dialect dialect : DIALECTS.dialects.values()) {
            if (dialect.isSupport(driverName)) {
                result = dialect;
                break;
            }
        }
        return result;
    }

}
