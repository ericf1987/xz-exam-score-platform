package com.xz.scorep.executor.db;

import org.h2.jdbcx.JdbcConnectionPool;
import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * (description)
 * created at 16/12/28
 *
 * @author yidin
 */
@Component
public class H2DbiHandlerFactory implements DbiHandleFactory {

    private DbConfig dbConfig;

    private boolean enabled;

    private JdbcConnectionPool jdbcConnectionPool;

    private DBI dbi;

    @Autowired
    public H2DbiHandlerFactory(DbConfig dbConfig, DbiHandleFactoryManager dbiHandleFactoryManager) {
        if (!"org.h2.Driver".equals(dbConfig.getDriver())) {
            return;
        }

        this.dbConfig = dbConfig;
        this.enabled = true;
        this.jdbcConnectionPool = JdbcConnectionPool.create(
                dbConfig.getUrl("exam"), dbConfig.getUser(), dbConfig.getPass()
        );
        this.dbi = new DBI(jdbcConnectionPool);

        dbiHandleFactoryManager.register(DbType.H2, this);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void createProjectDatabase(String projectId) {

    }

    @Override
    public void dropProjectDatabase(String projectId) {
        getProjectDBIHandle(projectId).runHandle(handle -> {
            handle.execute("drop all objects");
        });
    }

    @Override
    public DBIHandle getProjectDBIHandle(String projectId) {
        return new DBIHandle(this.dbi);
    }
}
