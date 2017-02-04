package com.xz.scorep.executor.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.xz.ajiaedu.common.lang.Value;
import com.xz.scorep.executor.config.DbConfig;
import org.skife.jdbi.v2.util.StringColumnMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@Component
public class MysqlDbiHandleFactory implements DbiHandleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MysqlDbiHandleFactory.class);

    private static final RemovalListener<String, DBIHandle> REMOVAL_LISTENER =
            (RemovalListener<String, DBIHandle>) (key, dbiHandle, cause) -> {
                if (dbiHandle != null) {
                    dbiHandle.close();
                }
            };

    private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";

    private DbConfig dbConfig;

    private DBIHandle rootDbiHandle;

    private DBIHandle managerDbiHandle;

    private boolean enabled;

    private LoadingCache<String, DBIHandle> projectDbiHandleCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .removalListener(REMOVAL_LISTENER)
            .build(this::createProjectDBIHandle);

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Autowired
    public MysqlDbiHandleFactory(DbConfig dbConfig, DbiHandleFactoryManager dbiHandleFactoryManager) {
        if (!DRIVER_NAME.equals(dbConfig.getDriver())) {
            this.enabled = false;
        } else {
            this.dbConfig = dbConfig;
            this.enabled = true;

            dbiHandleFactoryManager.register(DbType.MySQL, this);
        }
    }

    @PostConstruct
    public void init() {
        this.rootDbiHandle = new DBIHandle(getRootDataSource());
        this.managerDbiHandle = new DBIHandle(getManagerDataSource());
    }

    private synchronized DataSource getRootDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbConfig.getDriver());
        dataSource.setUrl(dbConfig.getUrl("information_schema"));
        dataSource.setUsername(dbConfig.getUser());
        dataSource.setPassword(dbConfig.getPass());
        return dataSource;
    }

    private synchronized DataSource getManagerDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbConfig.getDriver());
        dataSource.setUrl(dbConfig.getManagerUrl());
        dataSource.setUsername(dbConfig.getManagerUser());
        dataSource.setPassword(dbConfig.getManagerPass());
        return dataSource;
    }

    private synchronized DataSource getProjectDataSource(String projectId) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbConfig.getDriver());
        dataSource.setUrl(dbConfig.getUrl(projectId));
        dataSource.setUsername(projectId);
        dataSource.setPassword(projectId);
        return dataSource;
    }

    // 创建项目数据库
    @Override
    public void createProjectDatabase(String projectId) {
        DBIHandle rootDBIHandle = getRootDBIHandle();
        rootDBIHandle.runHandle(handle -> {
            if (databaseExists(projectId)) {
                dropProjectDatabase(projectId);
            }
            handle.execute("create database " + projectId + " DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci");
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
            handle.execute("drop database if exists " + projectId);
            handle.execute("drop user if exists " + projectId);
            LOG.info("数据库 " + projectId + " 已删除。");
        });
    }

    // 获得项目数据库访问对象
    @Override
    public DBIHandle getProjectDBIHandle(String projectId) {
        return this.projectDbiHandleCache.get(projectId);
    }

    private DBIHandle createProjectDBIHandle(String projectId) {
        return new DBIHandle(getProjectDataSource(projectId));
    }

    public DBIHandle getRootDBIHandle() {
        return this.rootDbiHandle;
    }

    @Override
    public DBIHandle getManagerDBIHandle() {
        return this.managerDbiHandle;
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
