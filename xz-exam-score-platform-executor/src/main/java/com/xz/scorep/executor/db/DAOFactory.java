package com.xz.scorep.executor.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.hyd.dao.DAO;
import com.hyd.dao.DataSources;
import com.xz.scorep.executor.config.DbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Component
public class DAOFactory {

    public static final String DS_ROOT = "root";

    public static final String DS_MANAGER = "manager";

    @Autowired
    private DbConfig dbConfig;

    private DataSources dataSources = new DataSources();

    @PostConstruct
    public void init() {
        dataSources.setDataSource(DS_ROOT, getRootDataSource());
        dataSources.setDataSource(DS_MANAGER, getManagerDataSource());
    }

    public DAO getRootDao() {
        return dataSources.getDAO(DS_ROOT);
    }

    public DAO getManagerDao() {
        return dataSources.getDAO(DS_MANAGER);
    }

    public synchronized DAO getProjectDao(String projectId) {
        if (!dataSources.contains(projectId)) {
            dataSources.setDataSource(projectId, getProjectDataSource(projectId));
        }
        return dataSources.getDAO(projectId);
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

}
