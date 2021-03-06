package net.isger.brick.stub;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Properties;

import net.isger.raw.Depository;
import net.isger.raw.Raw;
import net.isger.util.Files;
import net.isger.util.Reflects;
import net.isger.util.Strings;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

public class MybatisStub extends PoolStub {

    private static final String MYBATIS_PATH = "mybatis.xml";

    private String mappingPath;

    private boolean transactionManaged;

    private TransactionFactory transactionFactory;

    private SqlSessionFactory sqlSessionFactory;

    public MybatisStub() {
        mappingPath = MYBATIS_PATH;
    }

    public void initial() {
        super.initial();
        this.transactionFactory = createTransactionFactory();
        this.sqlSessionFactory = createSqlSessionFactory();
    }

    protected TransactionFactory createTransactionFactory() {
        TransactionFactory transactionFactory;
        if (transactionManaged) {
            transactionFactory = new ManagedTransactionFactory();
        } else {
            transactionFactory = new JdbcTransactionFactory();
        }
        return transactionFactory;
    }

    private SqlSessionFactory createSqlSessionFactory() {
        InputStream mappingStream = null;
        try {
            Configuration configuration;
            mappingStream = getMappingStream(mappingPath);
            if (mappingStream == null) {
                configuration = new Configuration();
            } else {
                // 初始配置属性
                Properties props = new Properties();
                props.putAll(getParameters());
                XMLConfigBuilder parser = new XMLConfigBuilder(
                        new InputStreamReader(mappingStream), "", props);
                configuration = parser.parse();
            }
            configuration.setEnvironment(new Environment(getDialect().name(),
                    getTransactionFactory(), getDataSource()));
            return new DefaultSqlSessionFactory(configuration);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error while building ibatis SqlSessionFactory: "
                            + e.getMessage(), e);
        } finally {
            Files.close(mappingStream);
        }
    }

    protected InputStream getMappingStream(String mappingPath)
            throws IOException {
        Raw seed = Depository.getRaw(mappingPath);
        if (seed != null) {
            return seed.getInputStream();
        }
        return Reflects.getResourceAsStream(mappingPath);
    }

    protected TransactionFactory getTransactionFactory() {
        return transactionFactory;
    }

    protected Configuration getConfiguration() {
        return sqlSessionFactory.getConfiguration();
    }

    protected SqlSession getSqlSession() {
        return sqlSessionFactory.openSession();
    }

    protected void addMapper(String name, String key) {
        Configuration configuration = getConfiguration();
        if (!configuration.hasStatement(key)) {
            name = name.replaceAll("[.]", "/") + "." + MYBATIS_PATH;
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(
                    Reflects.getResourceAsStream(name), configuration, name,
                    configuration.getSqlFragments());
            mapperParser.parse();
        }
    }

    public void insert(StubCommand cmd) {
        Object table = cmd.getTable();
        if (table instanceof Class) {
            Object[] condition = getCondition(cmd, 2);
            final Connection conn = getConnection(cmd);
            Object result;
            SqlSession session = null;
            try {
                session = getSqlSession();
                String name = ((Class<?>) table).getName();
                String key = name
                        + "."
                        + Strings
                                .empty((String) condition[0], cmd.getOperate());
                addMapper(name, key);
                result = session.insert(key, (Object[]) condition[1]);
            } finally {
                close(conn);
            }
            cmd.setResult(result);
        } else {
            super.insert(cmd);
        }
    }

    public void select(StubCommand cmd) {
        Object table = cmd.getTable();
        if (table instanceof Class) {
            Object[] condition = getCondition(cmd, 2);
            final Connection conn = getConnection(cmd);
            Object result;
            SqlSession session = null;
            try {
                session = getSqlSession();
                String name = ((Class<?>) table).getName();
                String key = name
                        + "."
                        + Strings
                                .empty((String) condition[0], cmd.getOperate());
                addMapper(name, key);
                result = session.selectList(key, (Object[]) condition[1]);
            } finally {
                close(conn);
            }
            cmd.setResult(result);
        } else {
            super.select(cmd);
        }
    }

}
