package com.xz.scorep.executor.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.xz.ajiaedu.common.lang.Value;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.util.StringColumnMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * (description)
 * created at 2016/12/16
 *
 * @author yidin
 */
@Component
public class MysqlDbiHandleFactory implements DbiHandleFactory {

    private DbConfig dbConfig;

    private DBI rootDBI;

    private boolean enabled;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Autowired
    public MysqlDbiHandleFactory(DbConfig dbConfig, DbiHandleFactoryManager dbiHandleFactoryManager) {

        if (!"com.mysql.jdbc.Driver".equals(dbConfig.getDriver())) {
            return;
        }

        this.dbConfig = dbConfig;
        this.rootDBI = createRootDBI();
        this.enabled = true;

        dbiHandleFactoryManager.register(DbType.MySQL, this);
    }

    private DBI createRootDBI() {
        DataSource dataSource = createRootDataSource();
        return new DBI(dataSource);
    }

    private DataSource createRootDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbConfig.getDriver());
        dataSource.setUrl(dbConfig.getUrl("information_schema"));
        dataSource.setUsername(dbConfig.getUser());
        dataSource.setPassword(dbConfig.getPass());
        return dataSource;
    }

    private DataSource createProjectDataSource(String projectId) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbConfig.getDriver());
        dataSource.setUrl(dbConfig.getUrl(projectId));
        dataSource.setUsername(projectId);
        dataSource.setPassword(projectId);
        return dataSource;
    }

    public DBIHandle getRootDBIHandle() {
        return new DBIHandle(this.rootDBI);
    }

    // 创建项目数据库
    @Override
    public void createProjectDatabase(String projectId) {
        DBIHandle rootDBIHandle = getRootDBIHandle();
        rootDBIHandle.runHandle(handle -> {
            if (databaseExists(projectId)) {
                dropProjectDatabase(projectId);
            }
            handle.execute("create database " + projectId);
            handle.execute("create user '" + projectId + "'@'%' IDENTIFIED by '" + projectId + "'");
            handle.execute("grant all on " + projectId + ".* to '" + projectId + "'@'%'");
            handle.execute("flush privileges");
        });
    }

    // 删除项目数据库
    @Override
    public void dropProjectDatabase(String projectId) {
        DBIHandle rootDBIHandle = getRootDBIHandle();
        rootDBIHandle.runHandle(handle -> {
            handle.execute("drop database " + projectId);
            handle.execute("drop user " + projectId);
        });
    }

    // 获得项目数据库访问对象
    @Override
    public DBIHandle getProjectDBIHandle(String projectId) {
        return new DBIHandle(new DBI(createProjectDataSource(projectId)));
    }

    // 检查数据库是否存在
    public boolean databaseExists(String database) {

        DBIHandle rootDBIHandle = getRootDBIHandle();
        Value<Boolean> exists = Value.of(false);

        rootDBIHandle.runHandle(handle -> {
            String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=?";

            String schemaName = handle.createQuery(sql)
                    .bind(0, database)
                    .map(StringColumnMapper.INSTANCE).first();

            exists.set(schemaName != null);
        });

        return exists.get();
    }

    // 清空所有表的所有内容
    public void truncateAllTables(String database) {
        DBIHandle dbiHandle = getProjectDBIHandle(database);
        dbiHandle.runHandle(handle ->
                handle.createQuery("show tables").map(StringColumnMapper.INSTANCE).list()
                        .forEach(tableName -> handle.execute("truncate table " + tableName)));
    }
}
